package bootstrap.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Node {

	private static ServerSocket server;
	private static int port = 9002;
	private static ArrayList<InetSocketAddress> peers;
	private static ObjectOutputStream out;
	private static ObjectInputStream in;

	public static void main(String[] args) throws ClassNotFoundException {
		peers = new ArrayList<InetSocketAddress>();
		try {
			port = Integer.parseInt(args[1]);
		} catch (Exception e) {
			System.err.println("Invalid or no port number specified, using default " + port);
		}

		try {
			server = new ServerSocket(port);
			while (true) {
				Socket client = server.accept();
				out = new ObjectOutputStream(client.getOutputStream());
				in = new ObjectInputStream(client.getInputStream());
				String input = (String) in.readObject();
				System.out.println(input);
				if (input.equals("download")) {
					for (InetSocketAddress peer : peers) {
						out.writeObject(peer);
					}
					peers.add((InetSocketAddress) in.readObject()); // old
																	// clients
																	// are never
																	// purged
				} else if (input.equals("exit")) {
					peers.remove(client.getInetAddress());
				}
				out.writeObject(null);
				client.close();

				System.out.println("Peers in swarm: ");
				for (InetSocketAddress peer : peers) {
					System.out.println(peer);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
