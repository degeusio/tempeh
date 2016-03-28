package org.tempeh.xbrl;

import java.util.Objects;

public class XbrlTextFact implements IXbrlFact {

	private final String factType = "XbrlTextFact";  //for serialization
	private XbrlContext context;
	private XbrlTaxonomy xbrlTaxonomy;
	private String id;
	private boolean isNil;
	private String text;
	
	public XbrlTextFact(XbrlTaxonomy xbrlTaxonomy, String id, boolean isNil){
		this.xbrlTaxonomy = xbrlTaxonomy;
		this.id = id;
		this.isNil = isNil;
	}
	
	//for serialization
	private XbrlTextFact(){}

	@Override
	public int hashCode(){
		return Objects.hash(context, xbrlTaxonomy);
	}
	
	@Override
	public boolean equals(Object object){
		
		if(!(object instanceof XbrlTextFact))
			return false;
		
		//DO NOT INCLUDE ID PROPERTY
		XbrlTextFact fact = (XbrlTextFact)object;
		
		return Objects.equals(context, fact.getContext()) &&
				Objects.equals(xbrlTaxonomy, fact.getXbrlTaxonomy());
	}
	
	@Override
	public XbrlContext getContext() {
		return context;
	}

	@Override
	public void setContext(XbrlContext context){
		this.context = context;
	}
	
	@Override
	public XbrlTaxonomy getXbrlTaxonomy() {
		return this.xbrlTaxonomy;
	}
	
	@Override
	public String getId() {
		return id;
	}

	public boolean isNil() {
		return isNil;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
