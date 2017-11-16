package common;

import java.io.Serializable;

public class GameState implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String word;
	private int guessesRemaining;
	private int score;
	private boolean active;
	
	public GameState(String word, int guess, int score, boolean active)
	{
		this.word = word;
		guessesRemaining = guess;
		this.score = score;
		this.active = active;
	}
	
	public int getWordLength()
	{
		return word.length();
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Word: ");
		sb.append(word == null ? "no value" : word);
		sb.append(" Remaining failed attempts: ");
		sb.append(active ? guessesRemaining : "no value");
		sb.append(" Score: ");
		sb.append(score);
		sb.append("\n");
		return sb.toString();
	}
}
