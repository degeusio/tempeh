package org.tempeh.xbrl.metadata;

public class SummationMemberLink extends DomainMemberLink{
	
	private double weight;
	
	public SummationMemberLink(String taxonomyIdentifier, Double order, String use, Integer priority, double weight){
		super(taxonomyIdentifier, order, use, priority);
		this.weight = weight;
	}

	//for serialization
	protected SummationMemberLink(){}
		
	public double getWeight() {
		return weight;
	}

}
