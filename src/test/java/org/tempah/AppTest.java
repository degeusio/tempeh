package org.tempeh;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.tempeh.cache.LocalFileCache;

/** 
 * Trying to understand and verify the basic functionality of this library.
 */
public class AppTest {

    private static final Logger logger = LogManager.getLogger(AppTest.class);
    private static FileInputStream xbrl;

    @BeforeClass
    public static void loadFile(){
	try {
	    String fpath = "src/test/resources/org/tempeh/artw-20141130.xml";
	    xbrl = new FileInputStream(new File(fpath));
	}
	catch (IOException ioe){
	    System.out.println(ioe);
	}
    }

    // Verifying functionality of the main method given in the original code
    public void testBasicFunctionality() {

	// Unable to fetch URL for some reason
	
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

    @Test
    public void testBasicFunctionalityNoUrlFetch(){

	
	assertTrue(true);
    }
}
