package org.tempeh.xbrl.xlink;

import org.xml.sax.Attributes;


public interface ICustomLinkRecogniser {

	public boolean isLink(String uri, String localName, String qName);
	public String getHref(String uri, String localName, String qName, Attributes attributes) throws XLinkException;
	public String getRole(String uri, String localName, String qName, Attributes attributes);
	public String getArcrole(String uri, String localName, String qName, Attributes attributes);
	public String getTitle(String uri, String localName, String qName, Attributes attributes);
	public String getShow(String uri, String localName, String qName, Attributes attributes);
	public String getActuate(String uri, String localName, String qName, Attributes attributes);
}
