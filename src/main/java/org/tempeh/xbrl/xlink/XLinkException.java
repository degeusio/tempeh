package org.tempeh.xbrl.xlink;

public class XLinkException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3669554815809096959L;

	public XLinkException(String message){
		super(message);
	}
	
	public XLinkException(Exception e){
		super(e);
	}
	
	public XLinkException(String message, Exception e){
		super(message, e);
	}
}
