package org.tempeh.xbrl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Stack;

public class BaseURIResolver {

	final private Stack<URI> baseURIs = new Stack<URI>();

	public BaseURIResolver() {
		
	}

	public URI getBaseURI() {
		if(baseURIs.isEmpty())
			return null;
		else
			return baseURIs.peek();
	}

	public void addBaseURI(URI xmlBase) throws XbrlException{
		addBaseURI(xmlBase.toString());
	}
	
	public void addBaseURI(String xmlBase) throws XbrlException{

		// If no xmlBase information then just use the current one
		if ((xmlBase == "") || (xmlBase == null)) {
			baseURIs.push(getBaseURI());
			return;
		}

		URI base = null;
		if (getBaseURI() == null) {
			try {
				base = new URI(xmlBase);
			} catch (URISyntaxException e) {
				throw new XbrlException("Base URI attribute contains a Malformed URI: " + xmlBase, e);
			}
		} else {
			try {
				base = getBaseURI().resolve(new URI(xmlBase));
			} catch (URISyntaxException e) {
				throw new XbrlException("Base URI resolution of attribute value " + xmlBase + " against " + getBaseURI() + "involved a Malformed URI.", e);
			}
		}
		baseURIs.push(base);
	}

	public void removeBaseURI() {
		baseURIs.pop();
	}

	public void reset(){
		baseURIs.clear();
	}
}

