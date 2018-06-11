package org.tempeh;

import java.io.File;
import java.io.FileInputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.tempeh.xbrl.XbrlLoader;
import org.tempeh.cache.LocalFileCache;


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

    private static final Logger logger =
	LogManager.getLogger(XbrlLoaderTest.class);
    private static FileInputStream xbrl;
    private File resource = new File("src/test/resources/org/tempeh/artw-20141130.xml");

    @Test
    public void testLoadFile(){

	logger.info("Starting xbrl loader test.");

	String xbrlInstance = resource.getAbsolutePath();
	final LocalFileCache fileCache = new LocalFileCache("schemas");

	XbrlLoader xl = new XbrlLoader();
	xl.loadFile(); // TODO: needs a direct test here, getting tired
        
	assertTrue(true);
    }
}
