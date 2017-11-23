package client.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import common.Message;
import common.MessageHandler;

public class ServerConnection implements Runnable {

	private static final String GENERIC_ERROR_MESSAGE = "Connection error.";

	private final ByteBuffer msgFromServer = ByteBuffer.allocateDirect(8000);
	private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
	private final MessageHandler messageHandler = new MessageHandler();
	private SocketChannel socketChannel;
	private Selector selector;
	private InetSocketAddress serverAddress;
	private volatile boolean connected;
	private OutputHandler screen;

	public void connect(String host, int port, OutputHandler screen) throws IOException
	{
		serverAddress = new InetSocketAddress(host, port);
		this.screen = screen;
		new Thread(this).start();
	}

	@Override
	public void run() {
		try{
			initializeConnection();
			initializeSelector();
			while(connected)
			{
				selector.select();
				for(SelectionKey selectedKey : selector.selectedKeys())
				{
					selector.selectedKeys().remove(selectedKey);
					if(!selectedKey.isValid())
						continue;
					if(selectedKey.isReadable())
					{
						readFromServer(selectedKey);
					}
					else if(selectedKey.isWritable())
					{
						sendToServer(selectedKey);
					}
					else if(selectedKey.isConnectable())
					{
						finishConnection(selectedKey);
					}
				}
			}
		}
		catch(Exception e)
		{
			print(e.toString());
			e.printStackTrace();
			connected = false;
		}
	}

	private void sendToServer(SelectionKey selectedKey) throws IOException {
		ByteBuffer b;
        synchronized (messagesToSend) {
            while ((b = messagesToSend.peek()) != null) {
                socketChannel.write(b);
                if (b.hasRemaining()) {
                    return;
                }
                messagesToSend.remove();
            }
            selectedKey.interestOps(SelectionKey.OP_READ);
        }
	}

	private void readFromServer(SelectionKey selectedKey) throws IOException {
		msgFromServer.clear();
		if(socketChannel.read(msgFromServer) == -1)
		{
			throw new IOException(GENERIC_ERROR_MESSAGE);
		}
		String receivedMessage = getMessage();
		messageHandler.handleMessage(receivedMessage);
		while(messageHandler.containsMessages())
		{
			String msg = messageHandler.getMessage();

			switch(messageHandler.getType(msg))
			{
			case UPDATE:
			case MESSAGE:
				print(messageHandler.strip(msg) + "\n"); // remove type from message to be printed
				break;
			default: 
				print("Received message \"" + msg + "\" of unsupported type from server " + messageHandler.getType(msg).toString() + "\n");
			}
		}
		selectedKey.interestOps(SelectionKey.OP_WRITE);
	}

	private String getMessage()
	{
		msgFromServer.flip();
		byte[] dst = new byte[msgFromServer.remaining()];
		msgFromServer.get(dst);
		return new String(dst);
	}

	private void initializeSelector() throws IOException {
		selector = Selector.open();
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
	}

	private void initializeConnection() throws IOException {
		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.connect(serverAddress);
		connected = true;
	}

	private void finishConnection(SelectionKey key) throws IOException {
		socketChannel.finishConnect();
		key.interestOps(SelectionKey.OP_READ);
		StringBuffer sb = new StringBuffer("Connected to ");
		InetSocketAddress tempAddress;
		try{
			tempAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
		}
		catch(Exception e)
		{
			tempAddress = serverAddress;
		}
		sb.append(tempAddress.getHostName());
		sb.append(":");
		sb.append(tempAddress.getPort());
		sb.append("\n");
		print(sb.toString());
	}

	public void sendMsg(Message m) throws IOException
	{
		synchronized(messagesToSend)
		{
			messagesToSend.add(ByteBuffer.wrap(m.toString().getBytes()));
		}
		socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
		selector.wakeup();
	}

	public boolean isActive()
	{
		return connected;
	}

	public void disconnect() throws IOException {
		connected = false;
		socketChannel.close();
		selector.close();
	}

	private void print(String s)
	{
		CompletableFuture.runAsync(() -> {
			try {
				screen.print(s);
			} catch (Exception e) {
			}
		});
	}
}
