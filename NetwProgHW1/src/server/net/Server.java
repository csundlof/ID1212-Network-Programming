package server.net;

import java.io.IOException;
import java.net.ServerSocket;

import server.game.WordListManager;

public class Server {

	private static final String wordFile = "words.txt";
	//public static final WordListManager dictionary = new WordListManager(wordFile);
	private static ServerSocket server;
	private static int port = 9002;
	
	public static void main(String[] args) {
		WordListManager.init(wordFile);
		
		try{
			port = Integer.parseInt(args[1]);
		}
		catch(Exception e)
		{
			System.err.println("Invalid or no port number specified, using default " + port);
		}
		
		try {
			server = new ServerSocket(port);
			while(true)
			{
				new GameServer(server.accept());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
