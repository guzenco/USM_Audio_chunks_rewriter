import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class USM {
	
	/*
	num_of_chunks *
	{
	   4 bytes (char) - chunk signature  // "CRID" or "@SFV" or "@SFA"
	   4 bytes (uint32) - chunk size

	   chunk_data
	   {
	      1 byte - unknown  // always 0?
	      1 byte (uint8) - payload offset
	      2 bytes (uint16) - padding size
	      1 byte (uint8) - channel number
	      2 bytes - unknown
	      1 byte (uint8) - payload type  // 0 - stream (audio/video binary data)
	                                     // 1 - header (media metadata about a video or audio track)
	                                     // 2 - section end (info about end of the chunk data)
	                                     // 3 - seek (data about the seek positions of a video track)

	      4 bytes (uint32) - frame time // used for "stream" chunks, 0 for other chunks

	      4 bytes (uint32) - frame rate // for audio chunk always 2997
	                                    // for stream chunk 100*stream_rate
	                                    // for other chunks always 30
	      8 bytes - unknown
	      x bytes - payload  // e.g. "@UTF" chunk
	      x bytes - padding
	   } 
	}
	 */
	
	private String url;
	private List<byte[]> list;
	private int pos;
	
	public USM(String url) {
		this.url = url;
		reinit();
	}
	
	public void reinit() {
		Path path = Paths.get(url);
		list = new ArrayList<byte[]>();
		toStart();
		byte[] data = {};
		try {
			data = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int offset = 0;
		int size;
		do {
			size = ByteBuffer.wrap(get(data,offset + 4,4)).getInt();
			list.add(get(data, offset, size + 8));
			offset += size + 8;
		}while(offset < data.length);
	}
	

	public List<byte[]> getData() {
		return list;
	}


	public void setData(List<byte[]>  data) {
		this.list = data;
	}

	public void toStart() {
		pos = 0;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public int getChannelNumber() {
		return getChunkData()[4];
	}
	
	public void setChannelNumber(int channel) {
		byte[] data = list.get(pos);
		data[8 + 4] = (byte) channel;
	}
	
	public int getAudioChannels() {
		int _pos = pos;
		toStart();
		int c = 0;
		while(next("@SFA")) {
			if(this.getPayloadType() == 1) {
				c++;
			}else {
				break;
			}
		}
		pos = _pos;
		return c;
	}
	
	public int getPayloadType () {
		return getChunkData()[7];
	}
	
	public byte[] getPayload () {
		return get(list.get(pos), 32, getSize() +8);
	}
	
	public int getDelayForSbt() {
		byte[] b = list.get(pos);
		byte[] d = {b[32+11], b[32+10], b[32+9], b[32+8]};
		return ByteBuffer.wrap(d).getInt();
	}
	
	public void setDelayForSbt(int delay) {
		byte[] b = list.get(pos);
		byte[] d = ByteBuffer.allocate(4).putInt(delay).array();
		for(int i = 0; i < 4; i ++) {
			b[32 + 11 - i] = d[i];
		}
	}
	
	public int getTotalTime() {
		int _pos = pos;
		toStart();
		int c = 0;
		int framerate = -1;
		while(next("@SFV")) {
			if(getPayloadType() == 0) {
				c++;
				if(framerate == -1)
					framerate = getFrameRate();
			}
		}
		pos = _pos;
		return (int) Math.round(c * (1000.* 100. / framerate));
	}
	
	public int getFrameRate() {
		byte data[] = get(getChunkData(), 12, 4);
		return ByteBuffer.wrap(data).getInt();
	}
	
	public void setFrameRate(int fr) {
		byte[] bytes = ByteBuffer.allocate(4).putInt(fr).array();
		byte[] data = list.get(pos);
		for(int i = 0; i < 4; i++) {
			data[8 + 12 + i] = bytes[i];
		}
	}
	
	public byte[] getByteData() {
		int size = 0;
		for (byte[] bs : list) {
			size += bs.length;
		}
		byte data[] = new byte[size];
		size = 0;
		for(int i = 0; i < list.size(); i++) {
			byte[] c = list.get(i);
			for(int j = 0; j < c.length; j++) {
				data[size++] = c[j];
			}
		}
		return data;
	}

	public void save() {	
		try {
			Files.write(Paths.get(url), getByteData());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public byte[] getChunk() {
		return get(list.get(pos), 0, getSize() +8);
	}
	
	public byte[] getChunkData() {
		return get(list.get(pos),8, getSize());
	}

	public boolean remove() {
		list.remove(pos);
		if(pos >= list.size()) {
			pos = list.size() - 1;
			return true;
		}
		return false;
	}

	public boolean rewrite(byte[] d) {
		list.set(pos, d);
		return true;
	}
	
	public boolean rewriteb(byte[] d) {
		if(d.length != list.get(pos).length)
			return false;
		list.set(pos, d);
		return true;
	}
	
	
	public void add(byte[] d) {
		list.add(d);
	}
	
	public void addAudio(List<byte[]> da, double k, int group_size) {
		if(da.size() == 0)
			return;
		byte[][] result = new byte[da.size() + list.size()][];

		int start_pos = 0;
		toStart();
	
		int mcs = 0;
		int video_traks = 0;
		
		int[] payloadtypes = {1, 2, 3, 2};
		for (int payloadtype : payloadtypes) {
			while (this.getPayloadType() == payloadtype) {
				result[start_pos++] = list.get(0);
				String sgnt = this.getSgnt();
				list.remove(0);
				if (sgnt.equals("@SFV")) {
					video_traks++;
					if(!this.getSgnt().equals("@SFV") || this.getPayloadType() != payloadtype) {
						while (da.get(mcs)[15] == payloadtype) {
							result[start_pos++] = da.get(mcs++);
						}
					}
				}
			}
		}
		video_traks /= payloadtypes.length;
		
		start_pos += video_traks;
		int i = mcs;
		
		int audio_distributed;
		int area_for_distribution;
		
		if(k <= 1) {		
			audio_distributed = (int) Math.floor(da.size() * k);
			area_for_distribution = (result.length - (da.size() - audio_distributed));
		}else {
			audio_distributed = da.size();
			area_for_distribution = result.length;
		}
		audio_distributed -= mcs;
		area_for_distribution -= start_pos;
		while(i < audio_distributed) {
			int index = (int) Math.floor(1. * (i - mcs) / audio_distributed * area_for_distribution);
			index += start_pos;
			for(int j = 0; j < group_size && i < audio_distributed ;j++) {
				while(result[index] != null)
					index++;
				result[index] = da.get(i);
				i++;
			}	
		};
		
		int d = result.length - da.size();
		while(i < da.size()) {
			result[d + i] = da.get(i++);
		}
		
		int index = 0;
		for (byte[] bs : list) {
			while(result[index] != null)
				index++;
			result[index] = bs;
		}
		list = new ArrayList<byte[]>(Arrays.asList(result));
		toStart();
	}
		
	public boolean next() {
		if(pos + 1 >= list.size())
			return false;
		pos++;
		return true;
	}
	
	public boolean next(String s) {
		while(next()) 
			if(getSgnt().equals(s))
				return true;
		return false;
	}

	private byte[] get(byte[] data, int addr, int size) {
		return Arrays.copyOfRange(data, addr, addr+size);
	}
	
	public String getSgnt() {
		byte data[] = list.get(pos);
		char text[] = {(char)data[0], (char)data[1], (char)data[2], (char)data[3]};
		return new String(text);
	}
	
	public int getSize() {
		return ByteBuffer.wrap(get(list.get(pos),4,4)).getInt();
	}
	
}
