import java.io.File;

public class Tool {


	private void update(USM j, USM c) {
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
					System.out.println("WARNING! DIFFERENT NUMBER OF AUDIO CHUNKS (j > c): " + j.getUrl());
					do {
						do {
							if(j.remove())
								break;
						} while (j.getSgnt().equals("@SFA"));
					} while (j.next("@SFA"));
				} else {
					System.out.println("WARNING! DIFFERENT NUMBER OF AUDIO CHUNKS (j < c): " + j.getUrl());
					do {
						j.add(c.getChunk());
					} while(c.next("@SFA"));
				}
				break;
			}
		
			jb = j.next("@SFA");
			cb = c.next("@SFA");
		}
		
		j.save();
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
						USM j = new USM(file_jp.getPath());
						USM c = new USM(file_cn.getPath());
						update(j, c);
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
