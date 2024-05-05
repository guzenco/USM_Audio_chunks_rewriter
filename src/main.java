import java.util.Scanner;

public class main {

	public static void main(String[] args) {
		Tool t = new Tool();
		
		for (String arg : args) {
			String data[] = arg.split("=");
			if(data.length == 2) {
				switch (data[0]) {
				case "-group-size":
					int group_size;
					try {
						group_size = Integer.parseInt(data[1]); 
						if(group_size < 1)
							throw new Exception("Incorrect size");
						t.setGroupSize(group_size);
						
					} catch (Exception e) {
						System.out.println("WARNING! Incorrect value: " + arg);
					}
					break;

				default:
					System.out.println("WARNING! Unknown argument: " + arg);
					break;
				}
			}
		}
		System.out.println("Audio chunks group size: " +  t.getGroupSize());
		
		Scanner scanner = new Scanner(System.in);
		System.out.print("Path to files for rewriting: ");
		String URL_JP = scanner.nextLine();
		System.out.print("Path to files with audio: ");
		String URL_CN = scanner.nextLine();
		System.out.print("Start? (y): ");
		if(scanner.nextLine().equalsIgnoreCase("y")) {
			System.out.println("STARTED!");
			t.run(URL_JP, URL_CN);
			System.out.println("DONE!");
		}
		scanner.close();
	}

}
