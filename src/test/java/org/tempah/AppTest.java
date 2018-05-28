package org.tempeh;

import junit.framework.TestCase;

import org.tempeh.cache.LocalFileCache;

/** 
 * Trying to understand and verify the basic functionality of this library.
 */
public class AppTest extends TestCase {


    // Verifying functionality of the main method given in the original code.
    public void testBasicFunctionality() {
   
	String xbrlInstance =
	    "http://www.sec.gov/Archives/edgar/data/7623/000143774915001434/artw-20141130.xml";
	
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
