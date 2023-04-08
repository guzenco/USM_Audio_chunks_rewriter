import java.util.Scanner;

public class main {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Path to files for rewriting: ");
		String URL_JP = scanner.nextLine();
		System.out.print("Path to files with audio: ");
		String URL_CN = scanner.nextLine();
		System.out.print("Start? (y): ");
		if(scanner.nextLine().equalsIgnoreCase("y")) {
			Tool t = new Tool();
			System.out.println("STARTED!");
			t.run(URL_JP, URL_CN);
			System.out.println("DONE!");
		}
		scanner.close();
	}

}
