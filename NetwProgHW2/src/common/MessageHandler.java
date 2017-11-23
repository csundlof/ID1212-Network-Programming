package common;

import java.util.ArrayDeque;
import java.util.Queue;

public class MessageHandler {
	
	private final Queue<String> messages = new ArrayDeque<>();
	private StringBuilder sb = new StringBuilder();
	
	public synchronized void handleMessage(String s)
	{
		sb.append(s);
		int newLine;
		while((newLine = sb.indexOf("\n")) != -1)
		{
			messages.add(sb.substring(0, ++newLine));
			sb.replace(0, newLine, "");
		}
	}

	public boolean containsMessages() {
		return !messages.isEmpty();
	}
	
	public String getMessage()
	{
		return messages.remove();
	}
	
	public MessageType getType(String s)
	{
		return MessageType.getTypeFromChar(s.charAt(0));
	}

	public String strip(String msg) {
		return msg.substring(1).trim(); // removes message type from message
	}
}
