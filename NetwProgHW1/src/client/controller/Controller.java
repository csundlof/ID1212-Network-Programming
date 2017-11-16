package client.controller;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import client.net.OutputHandler;
import client.net.ServerConnection;
import common.Message;

public class Controller {

	private ServerConnection connection = new ServerConnection();
	
	public void connect(String host, int port, OutputHandler screen) {
		CompletableFuture.runAsync(() -> {
		try{
			connection.connect(host, port, screen);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		}).thenRun(() -> screen.print("Connected to " + host + ":" + port + "\n"));
	}
	
	public void sendMessage(Message m)
	{
        CompletableFuture.runAsync(() -> {
            try {
                connection.sendMsg(m);
            } catch (Exception e) {
            }
        });
	}
	
	public boolean isActive()
	{
		return connection.isActive();
	}
	
	public void disconnect()
	{
        CompletableFuture.runAsync(() -> {
    		try {
    			connection.disconnect();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        });
	}

}
