package org.tempeh.xbrl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.TimeZone;

import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XSGrammar;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.tempeh.Util;
import org.tempeh.cache.IFileCache;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.xerces.parsers.SAXParser;

public class XbrlLoader extends DefaultHandler implements ErrorHandler, XMLEntityResolver {

    private static final int BIG_PRIME = 2039;
    private final BaseURIResolver baseURIResolver;
    private final IElementHandler xbrlHandler;
    private final XMLGrammarPoolImpl grammarPool;
    private final SymbolTable symbolTable;
    private final HashMap<URI, URI> xbrlSchemaRemap; //some SEC XBRL docs contain wrong schema URLs
    private final Queue<URI> additionalDocsToParse = new LinkedList<URI>();
    private final IFileCache fileCache;
    //private final List<HttpURLConnection> openConnsToCleanUp = new ArrayList<HttpURLConnection>();
    
	private URI uri;
	
	public XbrlLoader(IFileCache fileCache){
		this.fileCache = fileCache;
		this.baseURIResolver = new BaseURIResolver();
		this.xbrlHandler = new XbrlElementHandler(this);
		this.symbolTable = new SymbolTable(BIG_PRIME);
		this.grammarPool = new XMLGrammarPoolImpl();
		
		this.xbrlSchemaRemap = new HashMap<URI, URI>();
		this.xbrlSchemaRemap.put(URI.create("http://www.xbrl.org/2003/linkbase/xbrl-instance-2003-12-31.xsd"),URI.create("http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd"));
		this.xbrlSchemaRemap.put(URI.create("http://www.xbrl.org/2003/instance/xbrl-instance-2003-12-31.xsd"),URI.create("http://www.xbrl.org/2003/xbrl-instance-2003-12-31.xsd"));
		this.xbrlSchemaRemap.put(URI.create("http://www.xbrl.org/2003/linkbase/xbrl-linkbase-2003-12-31.xsd"),URI.create("http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd"));
		this.xbrlSchemaRemap.put(URI.create("http://www.xbrl.org/2003/instance/xbrl-linkbase-2003-12-31.xsd"),URI.create("http://www.xbrl.org/2003/xbrl-linkbase-2003-12-31.xsd"));
		this.xbrlSchemaRemap.put(URI.create("http://www.xbrl.org/2003/instance/xl-2003-12-31.xsd"),URI.create("http://www.xbrl.org/2003/xl-2003-12-31.xsd"));
		this.xbrlSchemaRemap.put(URI.create("http://www.xbrl.org/2003/linkbase/xl-2003-12-31.xsd"),URI.create("http://www.xbrl.org/2003/xl-2003-12-31.xsd"));
		this.xbrlSchemaRemap.put(URI.create("http://www.xbrl.org/2003/instance/xlink-2003-12-31.xsd"),URI.create("http://www.xbrl.org/2003/xlink-2003-12-31.xsd"));
		this.xbrlSchemaRemap.put(URI.create("http://www.xbrl.org/2003/linkbase/xlink-2003-12-31.xsd"),URI.create("http://www.xbrl.org/2003/xlink-2003-12-31.xsd"));
        
		/*
		XMLGrammarPreparser preparser = new XMLGrammarPreparser(symbolTable);
        
        preparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);

        preparser.setProperty("http://apache.org/xml/properties/internal/grammar-pool", grammarPool);
        
        preparser.setFeature("http://xml.org/sax/features/namespaces", true);
        preparser.setFeature("http://xml.org/sax/features/validation", true);

        // note we can set schema features just in case ...
        preparser.setFeature("http://apache.org/xml/features/validation/schema", true);
        preparser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
        preparser.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", false);

        // Specify the entity resolver to use for the schemas.
        
        preparser.setEntityResolver(this);

        for(String schema : xbrlSchemas){
        	try{
        		XSGrammar grammar = (XSGrammar)preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA, new XMLInputSource(null, schema, schema));

        	}
        	catch(Exception e){
        		logger.error("Error preparsing xbrl schema: " + schema, e);
        	}
        }*/
        
	}

    public XbrlInstance loadFile(InputSource inputSource)
	throws IOException, SAXException, XbrlException {

	XbrlInstance instance = new XbrlInstance();
	Util util = new Util();
	
	xbrlHandler.newXbrlDoc(instance);
	SAXParser sp = new SAXParser(symbolTable, grammarPool);
	sp.setErrorHandler(this);
	sp.setEntityResolver(this);
	sp.setContentHandler(this);
	sp.parse(inputSource);
	
	instance.verifyInstance();

	return instance;
    }
	
	public XbrlInstance load(URI xbrlInstanceURI, InputSource inputSource) throws IOException, SAXException, XbrlException{

		XbrlInstance instance = new XbrlInstance();
		this.uri = xbrlInstanceURI;
		baseURIResolver.reset();
		additionalDocsToParse.clear();
		Util util = new Util();

		baseURIResolver.addBaseURI(uri);
		
		xbrlHandler.newXbrlDoc(instance);
		SAXParser sp = new SAXParser(symbolTable, grammarPool);
		sp.setErrorHandler(this);
		sp.setEntityResolver(this);
		sp.setContentHandler(this);
		sp.parse(inputSource);

		while(!additionalDocsToParse.isEmpty()){
			URI docURI = additionalDocsToParse.remove();
			baseURIResolver.addBaseURI(docURI);
			//xbrlHandler.startParsingAdditionalDoc(docURI);
			HttpURLConnection conn = util.fetchUrl(docURI.toString());
			if(conn == null)
				throw new XbrlException("Unable to get URL: " + docURI.toString());

			try{
				sp.parse(new InputSource(conn.getInputStream()));
			}
			finally{
				conn.disconnect();
				baseURIResolver.removeBaseURI();
			}
			//sp.parse(new InputSource(docURI.toString()));
		}

		instance.verifyInstance();
		baseURIResolver.removeBaseURI();

		return instance;
	}
	
	@Override
	public void startDocument() throws SAXException{
	
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		try{
			/*
			String schemaLocations = attributes.getValue(XbrlConstants.XMLSchemaInstanceNamespace, "schemaLocation");
			if(schemaLocations != null){
				String[] fields = schemaLocations.trim().split("\\s+");
				for (int i = 1; i<fields.length; i = i+2) {
					URI schemaURI = baseURIResolver.getBaseURI().resolve(new URI(fields[i]));
					logger.debug("schemaLocation: " + schemaURI);
				}
			}*/
			
			//logger.info("qName: " + qName + " href: " + attributes.getValue("xlink:href") + " label:" + attributes.getValue("xlink:label"));
			xbrlHandler.startElement(uri, localName, qName, attributes);
		}
		catch(Exception e){
			throw new SAXException("Error at startElement: " + localName + " uri: " + uri + " qName: " + qName, e);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		try{
			xbrlHandler.endElement(uri, localName, qName);
		}
		catch(Exception e){
			throw new SAXException("Error at endElement: " + localName + " uri: " + uri + " qName: " + qName, e);
		}
	}
	
	@Override
	public void characters(char buf[], int offset, int len) throws SAXException 
    {
		try{
			xbrlHandler.characters(buf, offset, len);
		}
		catch(Exception e){
			throw new SAXException("Error processing element value: " + new String(buf, offset, len));
		}
    } 
	
	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException{
		try{
			xbrlHandler.startPrefixMapping(prefix, uri);
		}catch(Exception e){
			throw new SAXException("Error at startPrefixMapping: " + prefix, e);
		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException{
		try{
			xbrlHandler.endPrefixMapping(prefix);
		}catch(Exception e){
			throw new SAXException("Error at endPrefixMapping: " + prefix, e);
		}
	}
	
	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier) throws XNIException, IOException {
		//logger.info("Resolving entity for: " + resourceIdentifier.getExpandedSystemId() + " namespace " + resourceIdentifier.getNamespace());
		
		try{
			URI originalURI = new URI(resourceIdentifier.getExpandedSystemId());
			if(xbrlSchemaRemap.containsKey(originalURI)){
				URI newURI = xbrlSchemaRemap.get(originalURI);
				
				return new XMLInputSource(null, newURI.toString(), newURI.toString(), fileCache.getFileInputStream(newURI.toString()), null);
			}
		}
		catch(URISyntaxException e){
			
		}
		
		return new XMLInputSource(resourceIdentifier.getPublicId(), 
				resourceIdentifier.getExpandedSystemId(), 
				resourceIdentifier.getExpandedSystemId(), 
				fileCache.getFileInputStream(resourceIdentifier.getExpandedSystemId()), 
				null);

	}
	
	public void queueAdditionalDocsToLoad(URI docURI){
		if(!additionalDocsToParse.contains(docURI)){
			additionalDocsToParse.add(docURI);
		}
	}
	
	/*
	public InputSource resolveEntity(String publicId, String systemId){
		logger.info("publicId: " + publicId + " systemId " + systemId);
		return null;
	}*/
	
	public void error(SAXParseException exception){
		
	}
	
	public void fatalError(SAXParseException exception){
		
	}
	
	public void warning(SAXParseException exception){
		
	}
	
	public BaseURIResolver getBaseURIResolver() {
		return baseURIResolver;
	}

	public XMLGrammarPoolImpl getGrammarPool() {
		return grammarPool;
	}

	public SymbolTable getSymbolTable() {
		return symbolTable;
	}


	public IFileCache getFileCache() {
		return fileCache;
	}

}
