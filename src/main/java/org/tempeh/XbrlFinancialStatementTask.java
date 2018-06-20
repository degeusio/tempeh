package org.tempeh;

import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.tempeh.cache.IFileCache;
import org.tempeh.xbrl.PresentationLink;
import org.tempeh.xbrl.XbrlInstance;
import org.tempeh.xbrl.XbrlLoader;
import org.tempeh.xbrl.report.FinancialStatementsReport;
import org.xml.sax.InputSource;


public class XbrlFinancialStatementTask {

    private static final Logger logger =
	LogManager.getLogger(XbrlFinancialStatementTask.class);
    private final IFileCache fileCache;
    private String xbrlInstanceUri;
    
    public XbrlFinancialStatementTask(IFileCache fileCache, String xbrlInstanceUri){
	this.fileCache = fileCache;
	this.xbrlInstanceUri = xbrlInstanceUri;
    }

    public FileInputStream secFileReader(String filePath) {
	
	File file = new File(filePath);
	FileInputStream fis = null;
	
	try {
	    fis = new FileInputStream(file);
	     // https://stackoverflow.com/questions/11114665/
	    return fis;
	}
	catch (IOException ioe) {
	    System.out.println("Unable to open file.");
	    return fis;
	}
    }

    public ByteArrayInputStream secFileReader2 (String filePath){

	ByteArrayInputStream fbytes = null;

	try {
	    File file = new File(filePath);
	    fbytes = new ByteArrayInputStream(FileUtils.readFileToByteArray(file));
	    return fbytes;
	}
	catch (Exception e){
	    System.out.println(e);
	    return fbytes;
	}
    }

    public void runTask() throws Exception {
	
	Util util = new Util();
	XbrlLoader loader = new XbrlLoader(fileCache);
	XbrlInstance instance = null;
	HttpURLConnection conn = null;
	try{
	    conn = util.fetchUrl(xbrlInstanceUri);
	    if(conn == null){
		throw new TempehException("Unable to get URL: " + xbrlInstanceUri);
	    }
	    
	    instance = loader.load(new URI(xbrlInstanceUri),
				   new InputSource(conn.getInputStream()));
	}
	catch(Exception e){
	    System.out.println(e);
	    throw new TempehException("Error parsing xbrl instance: " + xbrlInstanceUri, e);
	}
	finally{
	    if(conn != null)
		conn.disconnect();
	}
	
	FinancialStatementsReport report = new FinancialStatementsReport(instance);
	buildFinancialStatements(report, instance);
    }
    
    private void buildFinancialStatements(FinancialStatementsReport report, XbrlInstance instance)
	throws Exception{
	
	for(PresentationLink presLink : instance.getPresentationLinks().values()){
	    if(report.willHandlePresentation(presLink))
		report.buildPresentationLink(presLink);
	}	
    }
}
