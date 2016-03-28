package org.tempeh.xbrl;

public interface IXbrlFact {
	XbrlContext getContext();
	void setContext(XbrlContext context);
	XbrlTaxonomy getXbrlTaxonomy();
	String getId();
	boolean isNil();
}
