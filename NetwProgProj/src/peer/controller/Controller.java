package peer.controller;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import peer.game.Game;
import peer.net.Networking;
import peer.net.OutputHandler;

public class Controller {

	private Networking connection = new Networking();
	private Game game;

	public void Play(String play) {
		CompletableFuture.runAsync(() -> {
			try {
				game.play(play);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void Join() {
		CompletableFuture.runAsync(() -> {
			try {
				game.joinRound();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public void connect(String host, int port, OutputHandler screen) {
		CompletableFuture.runAsync(() -> {
			try {
				game = new Game(screen, connection);
				connection.connect(host, port, screen);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).thenRun(() -> screen.print("Joined the swarm."));
	}

	public boolean isActive() {
		return connection.isActive();
	}

	public void disconnect() {
		CompletableFuture.runAsync(() -> {
			try {
				connection.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	public void mergePeers() {
		CompletableFuture.runAsync(() -> {
			connection.mergePeers();
		});
	}

}
