package client.view;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import client.net.OutputHandler;

public class ScreenWriter implements Runnable, OutputHandler {

	private final BlockingQueue<String> stringsToPrint = new LinkedBlockingQueue<>();
	
	public synchronized void print(String output)
	{
		try{
			stringsToPrint.put(output);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while(true)
		{
			try{
				System.out.print(stringsToPrint.take());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

}
