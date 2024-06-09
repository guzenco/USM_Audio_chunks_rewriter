import java.util.Scanner;

public class main {

	public static int parceIntFromArgs(String arg, String number, int min_value){
		int i = -1;
		try {
			i = Integer.parseInt(number); 
			if(i < min_value)
				throw new Exception("Incorrect size");
		} catch (Exception e) {
			System.out.println("WARNING! Incorrect value: " + arg);
		}
		return i;
	}
	
	public static void main(String[] args) {
		Tool t = new Tool();
		
		for (String arg : args) {
			String data[] = arg.split("=");
			if(data.length == 2) {
				switch (data[0]) {
				case "-group-size":
					int group_size = parceIntFromArgs(arg, data[1], 1);
					if(group_size != -1) {
						t.setGroupSize(group_size);
					}
					break;
				case "-source-channel":
					int source_channel = parceIntFromArgs(arg, data[1], 0);
					if(source_channel != -1) {
						t.setSourceChannel(source_channel);
					}
					break;
				case "-destination-channel":
					int destination_channel = parceIntFromArgs(arg, data[1], 0);
					if(destination_channel != -1) {
						t.setDestinationChannel(destination_channel);
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
