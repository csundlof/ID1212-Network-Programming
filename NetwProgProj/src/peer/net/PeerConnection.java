package peer.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PeerConnection {

	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	public volatile boolean connected;

	public PeerConnection(InetSocketAddress o) throws IOException {
		socket = new Socket();
		socket.connect(o);
		out = new PrintWriter(socket.getOutputStream());
		in = new BufferedReader(new InputStreamReader((socket.getInputStream())));
		connected = true;
	}

	public PeerConnection(Socket accept) {
		socket = accept;
		try {
			out = new PrintWriter(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader((socket.getInputStream())));
			connected = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendMsg(String s) {
		if (connected) {
			out.println(s);
			out.flush();
		}
	}

	public String readMsg() throws IOException {
		String s = in.readLine();
		if (s.equals("bye"))
			connected = false;
		return s;
	}

	public void disconnect() {
		try {
			out.println("bye");
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connected = false;
	}
}
