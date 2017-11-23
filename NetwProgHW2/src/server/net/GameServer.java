package server.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ForkJoinPool;

import common.Message;
import common.MessageHandler;
import common.MessageType;
import server.game.Game;

public class GameServer implements Runnable {
	
	private static final String GENERIC_ERROR_MESSAGE = "Connection error.";
    private final SocketChannel gameChannel;
    private final MessageHandler messageHandler = new MessageHandler();
	private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(1024);
	private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
	private boolean readyToSend;
	private volatile boolean expectingMove = false;
	private Game game;
	
	public GameServer(SocketChannel gameChannel) throws IOException
	{
		this.gameChannel = gameChannel;
		game = new Game();
		sendGameState();
	}

	private void sendGameState() throws IOException {
		Message m = new Message(MessageType.UPDATE, game.getState());
		sendMessage(m);
		expectingMove = game.isActive();
	}

	@Override
	public void run() {
		try{
			Message m;
			synchronized(messagesToSend){
				loose:while(messageHandler.containsMessages())
				{
					String msg = messageHandler.getMessage();
					switch(messageHandler.getType(msg))
					{
					case DISCONNECT:
						disconnect();
						break loose;
					case START:
						game.newGame();
						expectingMove = true;
						break;
					case GUESS_CHARACTER:
					case GUESS_WORD:
						if(game.isActive() && expectingMove){
							if(!game.guess(messageHandler.strip(msg)))
							{
								m = new Message(MessageType.MESSAGE, "Invalid guess(did your guess contain 1 character or as many as the word?).");
								sendMessage(m);
							}
							else
								expectingMove = false;
						}
						else if(game.isActive() && !expectingMove)
						{
							m = new Message(MessageType.MESSAGE, "You're sending guesses too fast."); // this will happen if two guesses are sent before server can handle one
							sendMessage(m);
						}
						else
						{
							m = new Message(MessageType.MESSAGE, "No game active.");
							sendMessage(m);
						}
						break;
					default:
						break;
					}
				}
				sendGameState();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void sendMessage(Object m) throws IOException
	{
		synchronized(messagesToSend)
		{
			messagesToSend.add(ByteBuffer.wrap(m.toString().getBytes()));
		}
	}

	public void readMessage() throws IOException {
		msgFromClient.clear();
		if(gameChannel.read(msgFromClient) == -1)
		{
			throw new IOException(GENERIC_ERROR_MESSAGE);
		}
		String receivedMessage = getMessage();
		messageHandler.handleMessage(receivedMessage);
		ForkJoinPool.commonPool().execute(this);
	}

	private String getMessage()
	{
		msgFromClient.flip();
		byte[] dst = new byte[msgFromClient.remaining()];
		msgFromClient.get(dst);
		return new String(dst);
	}
	
	public void disconnect() throws IOException {
		gameChannel.close();
	}

	public boolean sendMessages() throws IOException {
		ByteBuffer b;
		boolean sent = false;
        synchronized (messagesToSend) {
            while ((b = messagesToSend.peek()) != null) {
                gameChannel.write(b);
                if (b.hasRemaining()) {
                    return false;
                }
                messagesToSend.remove();
                sent = true;
            }
        }
        return sent;
	}
	
	public boolean ready()
	{
		return readyToSend;
	}
}

