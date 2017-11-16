package common;

import java.io.Serializable;

public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final MessageType type;
	
	public final Object msg;
	
	public Message(MessageType type, Object msg)
	{
		this.type = type;
		this.msg = msg;
	}
}
