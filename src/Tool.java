import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tool {

	private int group_size = 1;
	
	public int getGroupSize() {
		return group_size;
	}

	public void setGroupSize(int sequence_size) {
		this.group_size = sequence_size;
	}
	
	private int source_channel = -1;
	
	
	public int getSourceChannel() {
		return source_channel;
	}

	public void setSourceChannel(int source_channel) {
		this.source_channel = source_channel;
	}

	private int destination_channel = -1;
	
	public int getDestinationChannel() {
		return destination_channel;
	}

	public void setDestinationChannel(int destination_channel) {
		this.destination_channel = destination_channel;
		if(destination_channel != -1 && getSourceChannel() == -1) {
			setSourceChannel(0);
		}
	}

	private boolean compareArraysInList(List<byte[]> list1, List<byte[]> list2) {
		if(list1.size() != list2.size())
			return false;
		for(int i = 0; i < list1.size(); i++) {
			if(!Arrays.equals(list1.get(i), list2.get(i)))
				return false;
		}
		return true;
	}
	
	private void update(USM j, USM c) {
		j.reinit();
		c.reinit();
		List<byte[]> cn_audio = new ArrayList<byte[]>();
		List<byte[]> jp_audio = new ArrayList<byte[]>();
		while(j.next("@SFA")) {
			do {
				if(this.getDestinationChannel() == -1 || j.getChannelNumber() == this.getDestinationChannel()) {
					jp_audio.add(j.getChunk());
					if(j.remove())
						break;
				}else {
					break;
				}
			} while (j.getSgnt().equals("@SFA"));
		}
		while(c.next("@SFA")) {
			if(this.getSourceChannel() == -1) {
				cn_audio.add(c.getChunk());
			}else if(c.getChannelNumber() == this.getSourceChannel()) {
				if(this.getDestinationChannel() == -1) {
					c.setChannelNumber(0);
				}else {
					c.setChannelNumber(this.getDestinationChannel());
				}
				cn_audio.add(c.getChunk());
			}
		}
		
		if(cn_audio.size() == 0) {
			System.out.println("ERROR! FILE CONTAINS NO AUDIO: " + c.getUrl());
			return;
		}
		if(compareArraysInList(cn_audio, jp_audio)) {
			System.out.println("ERROR! AUDIO IN FILES IS THE SAME: " + c.getUrl());
			return;
		}
			
		int ctt = c.getTotalTime();
		int jtt = j.getTotalTime();
		if (Math.abs(ctt - jtt) > 300) {
			System.out.println("WARNING! TIME DIFERENCE (c-j=" + (ctt - jtt) + "ms): " + j.getUrl());
		}
		
		double k = 1. * jtt / ctt;
		if(k <= 1 && Math.abs(jp_audio.size() - k * cn_audio.size()) <= 5 && this.getDestinationChannel() == -1 && this.getSourceChannel() == -1) {
			updateOld(j, c);
		}else {
			j.addAudio(cn_audio, k, this.getGroupSize());
		}
		j.save();
	}
	
	private void updateOld(USM j, USM c) {
		j.reinit();
		c.reinit();
		boolean jb = true;
		boolean cb = true;
		while(true) {
			
			if(!j.getSgnt().equals("@SFA"))
				jb = j.next("@SFA");
			if(!c.getSgnt().equals("@SFA"))
				cb = c.next("@SFA");
			
			if (jb == cb) {
				if (jb) {
					j.rewrite(c.getChunk());
				} else {
					break;
				}
			} else {
				if(jb) {
					//System.out.println("WARNING! DIFFERENT NUMBER OF AUDIO CHUNKS (j > c): " + j.getUrl());
					do {
						do {
							if(j.remove())
								break;
						} while (j.getSgnt().equals("@SFA"));
					} while (j.next("@SFA"));
				} else {
					//System.out.println("WARNING! DIFFERENT NUMBER OF AUDIO CHUNKS (j < c): " + j.getUrl());
					do {
						j.add(c.getChunk());
					} while(c.next("@SFA"));
				}
				break;
			}
		
			jb = j.next("@SFA");
			cb = c.next("@SFA");
		}
	}
	
	private boolean compareFiles(String filePath1, String filePath2) {
		try {
			
			byte[] data1 = Files.readAllBytes(Paths.get(filePath1));
			byte[] data2 = Files.readAllBytes(Paths.get(filePath2));	
			return Arrays.equals(data1, data2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;	
	}
	
	public void run(String url_org, String url_cn) {
		File folder_cn = new File(url_cn);
		File[] files_cn = folder_cn.listFiles();
		System.out.println("Files to rewrite: " + files_cn.length);
		for (File file_cn : files_cn) {
			if (!file_cn.isDirectory()) {
				String name = file_cn.getName();
				File file_jp = new File(url_org + "\\" + name);
				String[] format = file_cn.getName().split("\\.");
				if (format.length > 1 && format[format.length - 1].equalsIgnoreCase("usm")) {
					if (file_jp.exists()) {
						if(!compareFiles(file_jp.getPath(), file_cn.getPath())) {
							USM j = new USM(file_jp.getPath());
							USM c = new USM(file_cn.getPath());
							update(j, c);
						} else {
							System.out.println("ERROR! FILES SAME: " + name);
						}
					} else {
						System.out.println("ERROR! FILE NO EXIST: " + name);
					}
				} else {
					System.out.println("ERROR! FILE FORMAT NO \"USM\": " + name);
				}
			}
		}
	}
	
}
