package org.tempeh.xbrl.metadata;

import org.tempeh.xbrl.XbrlTaxonomy;

public class DomainMemberLink {
	
	protected double order;
	protected String use;
	protected int priority;
	protected XbrlTaxonomy xbrlTaxonomy;
	protected String taxonomyLabel; //no need to serialize.  Temporarily stored in order to populate xbrlTaxonomy during parsing
	
	public static final String OPTIONAL_USE = "optional";
	public static final String PROHIBITED_USE = "prohibited";
	
	public DomainMemberLink(String taxonomyLabel, Double order, String use, Integer priority){
		this.taxonomyLabel = taxonomyLabel;
		if(order == null)
			this.order = 1;
		else
			this.order = order;
		
		if(use == null)
			this.use = OPTIONAL_USE;
		else
			this.use = use;
		
		//if(!this.use.equals(OPTIONAL_USE))
		//	logger.warn(taxonomyLabel + " has unrecognized use attribute:" + this.use);
		
		if(priority == null)
			this.priority = 0;
		else
			this.priority = priority;
	}

	//for serialization
	protected DomainMemberLink(){}
	

	public double getOrder() {
		return order;
	}

	public String getUse() {
		return use;
	}

	public int getPriority() {
		return priority;
	}

	public String getTaxonomyLabel() {
		return taxonomyLabel;
	}

	public XbrlTaxonomy getXbrlTaxonomy() {
		return xbrlTaxonomy;
	}

	public void setXbrlTaxonomy(XbrlTaxonomy xbrlTaxonomy) {
		this.xbrlTaxonomy = xbrlTaxonomy;
	}
}
