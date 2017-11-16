package server.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import common.Message;
import common.MessageType;
import server.game.Game;

public class GameServer implements Runnable {
	private Socket client;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private boolean connected = true;
	private Game game;
	
	public GameServer(Socket client) throws IOException
	{
		this.client = client;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
		game = new Game();
		new Thread(this).start();
	}

	@Override
	public void run() {
		try{
			Message m;
			while(connected)
			{
				m = new Message(MessageType.UPDATE, game.getState());
				sendMessage(m);
				m = (Message)in.readObject();
				switch(m.type)
				{
					case DISCONNECT:
						connected = false;
						break;
					case START:
						game.newGame();
						break;
					case GUESS_CHARACTER:
					case GUESS_WORD:
						if(game.isActive()){
							if(!game.guess((String)m.msg))
							{
								m = new Message(MessageType.MESSAGE, "Invalid guess(did your guess contain 1 character or as many as the word?).\n");
								sendMessage(m);
							}
						}
						else
						{
							m = new Message(MessageType.MESSAGE, "No game active.\n");
							sendMessage(m);
						}
						break;
					default:
						break;
				}
			}
			client.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void sendMessage(Object m) throws IOException
	{
		out.writeObject(m);
	}
}

