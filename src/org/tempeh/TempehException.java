package org.tempeh;

public class TempehException extends Exception {

	public TempehException(String message){
		super(message);
	}
	
	public TempehException(String message, Exception e){
		super(message, e);
	}
}
