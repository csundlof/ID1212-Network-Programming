package peer.game;

import java.util.ArrayList;

import peer.net.Networking;
import peer.net.OutputHandler;

public class Game {

	private int players = 1;
	private String lastMove = "";
	private OutputHandler screen;
	private Networking networking;
	public static volatile boolean roundActive;
	private int playersBeat = 0;
	private int points = 0;

	public Game(OutputHandler screen, Networking n) {
		this.screen = screen;
		networking = n;
	}

	public void play(String s) {
		if (players == 1) {
			screen.print("No one else is connected.");
			return;
		}
		if (!roundActive) {
			screen.print("No round active.");
			return;
		}
		s = s.toLowerCase();
		lastMove = s;
		if (!(lastMove.equals("rock") || lastMove.equals("paper") || lastMove.equals("scissors"))) {
			screen.print("Invalid move, expecting rock, paper, or scissors.");
			return;
		}
		networking.sendMsg(s);
		updatePoints();
		sharePoints();
		finishRound();
	}

	private void finishRound() {
		screen.print("Round over. Type start to play again.");
		roundActive = false;
	}

	public void joinRound() {
		networking.mergePeers();
		networking.sendMsg("join");
		players = networking.numPeers();
		if (players == 0) {
			screen.print("You can't play alone.");
			return;
		}
		screen.print("Waiting for responses from " + players + " players.");
		ArrayList<String> messages = networking.readMessages(players);
		int newPlayers = 0;
		for (String s : messages) {
			if (s!=null && s.equals("join"))
				++newPlayers;
		}
		players = ++newPlayers;
		roundActive = true;
		screen.print("Round started. Play!");
	}

	private void updatePoints() {
		ArrayList<String> plays = networking.readMessages(players - 1);
		plays.add(lastMove);
		int rock = 0, paper = 0, scissors = 0;
		for (String play : plays) {
			if (play.equals("rock"))
				++rock;
			else if (play.equals("paper"))
				++paper;
			else if (play.equals("scissors"))
				++scissors;
		}
		if (lastMove.equals("rock")) {
			playersBeat = scissors;
		} else if (lastMove.equals("paper")) {
			playersBeat = rock;
		} else if (lastMove.equals("scissors")) {
			playersBeat = paper;
		}
	}

	private void sharePoints() {
		networking.sendMsg("" + playersBeat);
		ArrayList<String> leaderboards = networking.readMessages(players - 1);
		int wins = 0;
		for (String s : leaderboards) {
			if (playersBeat > Integer.parseInt(s))
				++wins;
			else if (playersBeat < Integer.parseInt(s)) {
				wins = 0;
				break;
			}
		}
		leaderboards.add("" + playersBeat);
		leaderboards.sort((a, b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)));
		screen.print("RAW SCORE (you have " + playersBeat + " points.)");
		for (String s : leaderboards) {
			screen.print(s);
		}
		if (wins > 0) {
			screen.print("You beat " + wins + " players.");
			points += wins;
		}
		networking.sendMsg(networking.getPort() + " " + points);
		ArrayList<String> roundScore = networking.readMessages(players - 1);
		roundScore.add("YOU " + points);
		screen.print("Rounds won (you have " + points + " wins.)");
		for (String s : roundScore) {
			screen.print(s);
		}
	}
}
