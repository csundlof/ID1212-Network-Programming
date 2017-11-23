package common;

public enum MessageType {
	CONNECT,
	START,
	GUESS_CHARACTER,
	GUESS_WORD,
	UPDATE,
	MESSAGE,
	DISCONNECT,
	UNKNOWN;
	
	public static MessageType getTypeFromChar(char c)
	{
		for(MessageType type : MessageType.values())
		{
			if((char)type.ordinal() == c){
				return type;
			}
		}
		return UNKNOWN;
	}
	
	public static char getCharFromType(MessageType type)
	{
		return (char)type.ordinal();
	}
}
