package org.tempeh.xbrl.metadata;

public class DimensionDomainLink extends DomainMemberLink {

	private boolean isDefault;
	
	public DimensionDomainLink(String taxonomyIdentifier, Double order, String use, Integer priority, boolean isDefault){
		super(taxonomyIdentifier, order, use, priority);
		this.isDefault = isDefault;
	}

	//for serialization
	private DimensionDomainLink(){}
		
	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
	
	
}
