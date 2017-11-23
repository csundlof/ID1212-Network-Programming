package client.startup;

import java.util.Scanner;

import client.controller.Controller;
import client.net.ServerConnection;
import client.view.ScreenWriter;
import common.Message;
import common.MessageType;

public class Client implements Runnable {

	private static final ScreenWriter screen = new ScreenWriter();
	private Controller controller;
	private static String host = "127.0.0.1";
	private static int port = 9002;
	private void start(String host, String port)
	{
		new Thread(screen).start();
		help();
		Client.host = host;
		Client.port = Integer.parseInt(port);
		controller = new Controller();
		new ServerConnection();
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		String[] params;
		Message m;
		Scanner sc = new Scanner(System.in);
		loose:while(true)
		{
			params = sc.nextLine().toLowerCase().split(" ");
			switch(params[0])
			{
				case "connect":
					if(controller.isActive())
					{
						screen.print("You are already connected to a server.");
						break;
					}
					try {
						controller.connect(host, port, screen);
					} catch (Exception e) {
						screen.print("An error occurred when connecting to the server\n");
						screen.print(e.getMessage());
					}
					break;
				case "disconnect":
					if(!controller.isActive())
					{
						screen.print("You are not connected to a server.\n");
						break;
					}
					m = new Message(MessageType.DISCONNECT, null);
					controller.sendMessage(m);
					controller.disconnect();
					break loose;
				case "help":
					help();
					break;
				case "start":
					if(!controller.isActive())
					{
						screen.print("You are not connected to a server.\n");
						break;
					}
					m = new Message(MessageType.START, null);
					controller.sendMessage(m);
					break;
				case "guess":
					if(!controller.isActive())
					{
						screen.print("You are not connected to a server.\n");
						break;
					}
					if(params.length > 1)
					{
						m = new Message(params[1].length() > 1 ? MessageType.GUESS_WORD : MessageType.GUESS_CHARACTER, params[1]);
						controller.sendMessage(m);
						break;
					}
				default:
					screen.print("Unknown command.\n");
			}
		}
		screen.print("Exiting application.\n");
		System.exit(0);
	}
	
	private void help()
	{
		screen.print("Commands: connect, start, guess c, guess word, disconnect\n");
	}
	
	public static void main(String[] args) {
		try{
			new Client().start(args[1], args[2]);
		}
		catch(Exception e)
		{
			System.err.println("Invalid command line arguments, using default host and port.");
			new Client().start(host, String.valueOf(port));
		}
	}

}
