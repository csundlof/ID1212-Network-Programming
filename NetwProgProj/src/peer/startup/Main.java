package peer.startup;

import peer.view.Client;

public class Main {
	private static String host = "127.0.0.1";
	private static int port = 9002;

	public static void main(String[] args) {
		try {
			new Client().start(args[1], args[2]);
		} catch (Exception e) {
			System.err.println("Invalid command line arguments, using default host and port.");
			new Client().start(host, String.valueOf(port));
		}
	}
}
