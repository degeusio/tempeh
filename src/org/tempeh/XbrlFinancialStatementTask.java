package org.tempeh;

import java.net.HttpURLConnection;
import java.net.URI;

import org.tempeh.cache.IFileCache;
import org.tempeh.xbrl.PresentationLink;
import org.tempeh.xbrl.XbrlInstance;
import org.tempeh.xbrl.XbrlLoader;
import org.tempeh.xbrl.report.FinancialStatementsReport;
import org.xml.sax.InputSource;


public class XbrlFinancialStatementTask{

	private final IFileCache fileCache;
	private String xbrlInstanceUri;
	
	public XbrlFinancialStatementTask(IFileCache fileCache, String xbrlInstanceUri){
		this.fileCache = fileCache;
		this.xbrlInstanceUri = xbrlInstanceUri;
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
			
			instance = loader.load(new URI(xbrlInstanceUri), new InputSource(conn.getInputStream()));
		}
		catch(Exception e){
			throw new TempehException("Error parsing xbrl instance: " + xbrlInstanceUri, e);
		}
		finally{
			if(conn != null)
				conn.disconnect();
		}
		
		FinancialStatementsReport report = new FinancialStatementsReport(instance);
		buildFinancialStatements(report, instance);
	}
	
	private void buildFinancialStatements(FinancialStatementsReport report, XbrlInstance instance) throws Exception{
		
		for(PresentationLink presLink : instance.getPresentationLinks().values()){
			if(report.willHandlePresentation(presLink))
				report.buildPresentationLink(presLink);
		}
		
	}
	

	
}
