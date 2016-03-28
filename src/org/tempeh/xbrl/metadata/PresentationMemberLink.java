package org.tempeh.xbrl.metadata;

public class PresentationMemberLink extends DomainMemberLink{

	private String preferredLabel;
	
	public PresentationMemberLink(String taxonomyIdentifier, Double order, String use, Integer priority, String preferredLabel){
		super(taxonomyIdentifier, order, use, priority);
		this.preferredLabel = preferredLabel;
	}
	
	//for serialization
	protected PresentationMemberLink(){}
	
	public String getPreferredLabel() {
		return preferredLabel;
	}
}
