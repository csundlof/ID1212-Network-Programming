package server.game;

import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class WordListManager {

	private static ArrayList<String> wordList;
	private static Random randomGenerator;
    private static final String root = ".";
    private static Path workingDir = Paths.get(root);
	
    public static void init(String path)
    {
		wordList = new ArrayList<String>();
		randomGenerator = new Random();
		readWords(path);
    }
    
	public WordListManager(String path)
	{
		wordList = new ArrayList<String>();
		randomGenerator = new Random();
		readWords(path);
	}
	
	public static String getRandomWord()
	{
		return wordList.get(randomGenerator.nextInt(wordList.size()));
	}
	
	private static void readWords(String path)
	{
		String file = workingDir.resolve(Paths.get(path)).toString();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) // No validation of input done. 
		{
			reader.lines().forEach(line -> wordList.add(line));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
