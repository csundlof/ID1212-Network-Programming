package server.game;

import common.GameState;
import java.util.HashMap;
import java.util.LinkedList;

public class Game {

	private String secretWord;
	private String publicWord;
	private int score = 0;
	private int guessesRemaining;
	private HashMap<Character, LinkedList<Integer>> characters;
	private boolean active = false;
	
	public Game()
	{
	}
	
	public Object getState() {
		return new GameState(publicWord, guessesRemaining, score, active);
	}

	public void newGame()
	{
		characters = new HashMap<Character, LinkedList<Integer>>();
		secretWord = WordListManager.getRandomWord();
		System.out.println("Chose word: " + secretWord);
		for(int i = 0; i < secretWord.length(); ++i)
		{
			LinkedList<Integer> indexes = characters.get(secretWord.charAt(i));
			if(indexes == null)
			{
				indexes = new LinkedList<Integer>();
				characters.put(secretWord.charAt(i), indexes);
			}
			indexes.push(i);
		}
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < secretWord.length(); ++i)
		{
			sb.append("_");
		}
		publicWord = sb.toString();
		guessesRemaining = secretWord.length();
		active = true;
	}
	
	public boolean guess(String s)
	{
		if(!active || s.length() > 1 && s.length() != secretWord.length())
			return false;
		if(s.length() > 1 && s.equals(secretWord))
		{
			victory();
			return true;
		}
		else if(s.length() == 1 && characters.containsKey(s.charAt(0)))
		{
			StringBuilder sb = new StringBuilder(publicWord);
			for(int i : characters.get(s.charAt(0)))
			{
				sb.replace(i, i+1, s);
			}
			publicWord = sb.toString();
		}
		else 
			--guessesRemaining;
		if(publicWord.equals(secretWord))
			victory();
		else if(guessesRemaining == 0)
			loss();
		return true;
	}
	
	private void loss() {
		active = false;
		--score;
	}

	private void victory() {
		active = false;
		publicWord = secretWord;
		++score;
	}

	public boolean isActive()
	{
		return active;
	}
	
}
