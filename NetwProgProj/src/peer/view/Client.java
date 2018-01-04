package peer.view;

import java.util.Scanner;

import peer.controller.*;

public class Client implements Runnable {

	private static final ScreenWriter screen = new ScreenWriter();
	private Controller controller;
	private String host;
	private int port;

	public void start(String host, String port) {
		this.host = host;
		this.port = Integer.parseInt(port);
		new Thread(screen).start();
		help();
		controller = new Controller();
		new Thread(this).start();
	}

	@Override
	public void run() {
		String[] params;
		Scanner sc = new Scanner(System.in);
		try {
			controller.connect(host, port, screen);
		} catch (Exception e) {
			screen.print("An error occurred when connecting to the swarm");
			screen.print(e.getMessage());
		}
		loose: while (true) {
			params = sc.nextLine().toLowerCase().split(" ");
			switch (params[0]) {
			case "disconnect":
				if (!controller.isActive()) {
					screen.print("You are not connected to a server.");
					break;
				}
				controller.disconnect();
				break loose;
			case "help":
				help();
				break;
			case "start":
				if (!controller.isActive()) {
					screen.print("You are not connected to anyone.");
					break;
				}
				controller.Join();
				break;
			case "play":
				if (!controller.isActive()) {
					screen.print("You are not connected to a server.");
					break;
				}
				if (params.length > 1) {
					controller.Play(params[1]);
					break;
				}
			default:
				screen.print("Unknown command.");
			}
		}
		screen.print("Exiting application.");
		System.exit(0);
	}

	private void help() {
		screen.print("Commands: start, play ROCK|PAPER|SCISSORS, disconnect.\nThe game can be started by typing \"start\" after at least one other player has connected.");
	}

}
