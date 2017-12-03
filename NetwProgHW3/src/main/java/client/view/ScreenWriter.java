package client.view;

import common.OutputHandler;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ScreenWriter extends UnicastRemoteObject implements Runnable, OutputHandler, Serializable {

	private static final BlockingQueue<String> stringsToPrint = new LinkedBlockingQueue<>();
	
        public ScreenWriter() throws RemoteException{
            
        }
        
        @Override
	public synchronized void print(String output) throws RemoteException
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
				System.out.println(stringsToPrint.take());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

}
