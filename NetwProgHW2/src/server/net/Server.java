package server.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import server.game.WordListManager;

public class Server {

    private static final int LINGER_TIME = 5000;
	private static final String wordFile = "words.txt";
	private Selector selector;
	private ServerSocketChannel listeningSocketChannel;
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
		Server server = new Server();
		server.serve();
	}
	
	private void serve()
	{
		try
		{
			initListeningSocketChannel();
			initializeSelector();
			while(true)
			{
				selector.select();
				for(SelectionKey selectedKey : selector.selectedKeys())
				{
					selector.selectedKeys().remove(selectedKey);
					if(!selectedKey.isValid())
						continue;
					if(selectedKey.isReadable())
					{
						readFromClient(selectedKey);
					}
					else if(selectedKey.isWritable())
					{
						sendToClient(selectedKey);
					}
					else if(selectedKey.isAcceptable())
					{
						startServer(selectedKey);
					}
				}
			}
		}
		catch(Exception e)
		{
			System.err.println("Server startup failed.");
			e.printStackTrace();
		}
	}
	
	private void initializeSelector() throws IOException {
		selector = Selector.open();
		listeningSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	private void initListeningSocketChannel() throws IOException {
		listeningSocketChannel = ServerSocketChannel.open();
		listeningSocketChannel.configureBlocking(false);
		listeningSocketChannel.bind(new InetSocketAddress(port));
	}
	
	private void readFromClient(SelectionKey key) throws IOException
	{
		try
		{
			((GameServer)key.attachment()).readMessage();
			key.interestOps(SelectionKey.OP_WRITE);
		}
		catch(Exception e)
		{
			removeServer(key);
		}
		selector.wakeup();
	}
	
	private void sendToClient(SelectionKey key) throws IOException
	{
		try{
			if(((GameServer)key.attachment()).sendMessages())
				key.interestOps(SelectionKey.OP_READ);
		}
		catch(Exception e)
		{
			removeServer(key);
		}
	}
	
	private void removeServer(SelectionKey key) throws IOException
	{
		GameServer game = (GameServer) key.attachment();
		game.disconnect();
		key.cancel();
	}
	
	private void startServer(SelectionKey key) throws IOException
	{
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel gameChannel = serverSocketChannel.accept();
        gameChannel.configureBlocking(false);
        GameServer game = new GameServer(gameChannel);
        gameChannel.register(selector, SelectionKey.OP_WRITE, game);
        gameChannel.setOption(StandardSocketOptions.SO_LINGER, LINGER_TIME);
	}
	
}
