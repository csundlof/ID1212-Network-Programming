package peer.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class Networking implements Runnable {

	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private volatile boolean connected;
	private ServerSocket peer;
	private ArrayList<PeerConnection> connectedPeers, tempPeers;
	private OutputHandler screen;

	public void connect(String host, int port, OutputHandler screen) throws IOException {
		Random random = new Random();
		peer = new ServerSocket(random.nextInt(500) + 9003);
		CompletableFuture.runAsync(() -> {
			socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(host, port));
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());
				this.screen = screen;
				connected = true;
				new Thread(this).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void run() {
		connectedPeers = new ArrayList<PeerConnection>();
		try {
			out.writeObject("download");
			out.writeObject(peer.getLocalSocketAddress());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Object o = "";
		while (o != null) {
			o = null;
			try {
				o = in.readObject();
				if (o != null) {
					try {
						connectedPeers.add(new PeerConnection((InetSocketAddress) o));
						screen.print("Connected to: " + ((InetSocketAddress) o).toString());
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		tempPeers = new ArrayList<PeerConnection>();
		while (connected) {
			try {
				Socket sock = peer.accept();
				PeerConnection p = new PeerConnection(sock);
				screen.print("Incoming connection from: " + sock.getRemoteSocketAddress());
				synchronized (tempPeers) {
					tempPeers.add(p);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public boolean isActive() {
		return connected;
	}

	public void sendMsg(String s) {
		for (PeerConnection p : connectedPeers) {
			p.sendMsg(s);
		}
		connectedPeers.removeIf(p -> p.connected == false);
	}

	public void disconnect() throws IOException {
		for (PeerConnection p : connectedPeers) {
			p.disconnect();
		}
		connectedPeers = new ArrayList<PeerConnection>();
		connected = false;
		out.close();
		in.close();
		socket.close();
	}

	public ArrayList<String> readMessages(int num) {
		ArrayList<String> messages = new ArrayList<String>();
		for (PeerConnection p : connectedPeers) {
			try {
				String msg = p.readMsg();
				messages.add(msg);
			} catch (IOException e) {
				messages.add(null);
				p.disconnect();
				e.printStackTrace();
			}
		}
		connectedPeers.removeIf(p -> p.connected == false);
		return messages;

	}

	public void mergePeers() {
		synchronized (tempPeers) {
			connectedPeers.addAll(tempPeers);
			tempPeers = new ArrayList<PeerConnection>();
		}
	}

	public int numPeers() {
		return connectedPeers.size();
	}

	public String getPort() {
		return peer.getLocalPort() + "";
	}
}
