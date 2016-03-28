package org.tempeh;

import org.tempeh.cache.LocalFileCache;

public class App {

	public static void main(String[] args){
		String xbrlInstance = "http://www.sec.gov/Archives/edgar/data/7623/000143774915001434/artw-20141130.xml";
		
		final LocalFileCache fileCache = new LocalFileCache("schemas");
		XbrlFinancialStatementTask task = new XbrlFinancialStatementTask(fileCache, xbrlInstance);
		
		try{
			task.runTask();
		}
		catch(Exception e){
			System.out.print(e);
		}
	}
}
