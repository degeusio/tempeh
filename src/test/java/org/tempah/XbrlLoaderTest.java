package org.tempeh;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.tempeh.xbrl.XbrlLoader;


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


    
    @Test
    public void testLoad(){

       
    }

    

}
