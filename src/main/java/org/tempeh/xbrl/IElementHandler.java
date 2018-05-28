package org.tempeh.xbrl;

import org.xml.sax.Attributes;

public interface IElementHandler {
	
	public void newXbrlDoc(XbrlInstance instance);
	//public void startParsingAdditionalDoc(URI docURI);
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws Exception;
    public void endElement(String uri, String localName, String qName) throws Exception;
    public void startPrefixMapping(String prefix, String uri) throws Exception;
    public void endPrefixMapping(String prefix) throws Exception;
    public void characters(char buf[], int offset, int len) throws Exception;
}
