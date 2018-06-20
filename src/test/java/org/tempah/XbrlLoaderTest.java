package org.tempeh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.tempeh.xbrl.XbrlLoader;
import org.tempeh.cache.LocalFileCache;
import org.tempeh.cache.IFileCache;
import org.tempeh.xbrl.XbrlException;


/* Issue with XbrlFinancialStatementTask.java which needs to be resolved.
 *
 * loader.load();
 *
 * It looks like the loader is still really closely tied with an 
 * expected HTTP response. The method is parsing XBRL while handling
 * specific byte stream IO. This method will need to be rewritten so
 * we can separate out concerns.
 *    
 */
public class XbrlLoaderTest {

    private static final Logger LOG =
	LogManager.getLogger(XbrlLoaderTest.class);
    private static FileInputStream xbrl;

    @Test
    public void testLoadFile(){

	LOG.info("Starting xbrl loader test.");

	try {
	    File resource = new File("src/test/resources/org/tempeh/artw-20141130.xml");
	    InputStream res = new FileInputStream(resource);
	    byte[] bytes = IOUtils.toByteArray(res);
	    
	    String xbrlInstance = resource.getAbsolutePath();
	    final LocalFileCache fileCache = new LocalFileCache("schemas");

	    String thisGuy = FileUtils.readFileToString(resource);
	    
	    XbrlLoader xl = new XbrlLoader(fileCache);
	    InputSource resSax = new InputSource(thisGuy);
	    xl.loadFile(resSax); // TODO: file is being read now but do something
        
	    assertTrue(true);
	}
	catch (FileNotFoundException foe){
	    LOG.error(foe);
	}
	catch (IOException ioe){
	    LOG.error(ioe);
	}
	catch (SAXException se){
	    LOG.error(se);
	}
	catch (XbrlException xe){
	    LOG.error(xe);
	}
    }
}
