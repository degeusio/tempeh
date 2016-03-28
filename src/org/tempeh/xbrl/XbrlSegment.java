package org.tempeh.xbrl;

import java.util.Objects;

public class XbrlSegment{

	private String dimensionNamespace;
	private String dimensionName;
	private String memberNamespace;
	private String memberName;
	private XbrlLabel label;

	public XbrlSegment(String dimNS, String dimName, String memberNS, String memberName){
		this.dimensionNamespace = dimNS;
		this.dimensionName = dimName;
		this.memberNamespace = memberNS;
		this.memberName = memberName;
	}
	
	//for serialization
	private XbrlSegment(){}
	
	@Override
	public int hashCode(){
		return Objects.hash(dimensionNamespace, dimensionName, memberNamespace, memberName);
	}
	
	@Override
	public boolean equals(Object object){
		
		if(!(object instanceof XbrlSegment))
			return false;
		
		XbrlSegment segment = (XbrlSegment)object;
		return Objects.equals(dimensionNamespace, segment.getDimensionNamespace()) &&
				Objects.equals(dimensionName, segment.getDimensionName()) &&
				Objects.equals(memberNamespace, segment.getMemberNamespace()) &&
				Objects.equals(memberName, segment.getMemberName());
	}
	
	@Override
	public String toString(){
		return "dim:" + XbrlTaxonomy.getIdentifier(dimensionNamespace, dimensionName) + " member:" + XbrlTaxonomy.getIdentifier(memberNamespace, memberName);
	}
	
	public String getDimensionNamespace() {
		return dimensionNamespace;
	}
	public String getDimensionName() {
		return dimensionName;
	}
	public String getMemberNamespace() {
		return memberNamespace;
	}
	public String getMemberName() {
		return memberName;
	}
	public XbrlLabel getLabel() {
		return label;
	}
	public void setLabel(XbrlLabel label) {
		this.label = label;
	}
	
	
}
