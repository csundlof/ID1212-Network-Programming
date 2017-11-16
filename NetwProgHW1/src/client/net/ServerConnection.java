package client.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

import common.GameState;
import common.Message;

public class ServerConnection implements Runnable {
	
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private volatile boolean connected;
    private OutputHandler screen;
    
	public void connect(String host, int port, OutputHandler screen) throws IOException
	{
		CompletableFuture.runAsync(() -> {
			socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(host, port));
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
				connected = true;
				this.screen = screen;
				new Thread(this).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void run() {
		try{
			Message m;
			while(connected)
			{
				m = (Message)in.readObject();
				switch(m.type)
				{
					case UPDATE:
						screen.print(((GameState)m.msg).toString());
						break;
					case MESSAGE:
						screen.print((String)m.msg);
						break;
					default:
						screen.print("Received message of unsupported type from server: " + m.type.toString());
				}
			}
		}
		catch(Exception e)
		{
			screen.print(e.getMessage());
			connected = false;
		}
	}
	
	public void sendMsg(Message m) throws IOException
	{
		out.writeObject(m);
	}
	
	public boolean isActive()
	{
		return connected;
	}

	public void disconnect() throws IOException {
		connected = false;
		out.close();
		in.close();
		socket.close();
	}
}
