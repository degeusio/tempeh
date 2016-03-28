package org.tempeh.xbrl;

public class XbrlException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 597963005987268818L;

	public XbrlException(String message){
		super(message);
	}
	
	public XbrlException(String message, Throwable cause){
		super(message, cause);
	}
}
