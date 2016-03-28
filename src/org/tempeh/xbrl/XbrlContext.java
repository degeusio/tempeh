package org.tempeh.xbrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XbrlContext {
	
	private static final String CIK_SCHEME = "http://www.sec.gov/CIK";

	private String id;
	private String identifierScheme;
	private String identifierValue;
	private XbrlPeriod period;
	private final List<XbrlSegment> segments = new ArrayList<XbrlSegment>();
		
	public XbrlContext(String id){
		this.id = id;
	}
	
	//for serialization
	private XbrlContext(){}

	@Override
	public int hashCode(){
		return Objects.hash(identifierScheme, identifierValue, period, segments);
	}
	
	@Override
	public boolean equals(Object object){
		
		if(!(object instanceof XbrlContext))
			return false;
		
		//DO NOT INCLUDE ID PROPERTY
		XbrlContext context = (XbrlContext)object;
		
		if(!isSegmentsEqual(context))
			return false;
		
		return Objects.equals(identifierScheme, context.getIdentifierScheme()) &&
				Objects.equals(identifierValue, context.getIdentifierValue()) &&
				Objects.equals(period, context.getPeriod());
	}
	
	public boolean isSegmentsEqual(XbrlContext context){
		//make sure that segments are equal
		if(context.segments.size() != this.segments.size())
			return false;

		for(XbrlSegment segment : context.segments){
			if(!this.segments.contains(segment))
				return false;
		}

		for(XbrlSegment segment : this.segments){
			if(!context.segments.contains(segment))
				return false;
		}
		
		return true;
	}
	
	@Override
	public String toString(){
		String segmentDesc = "";
		int count = 0;
		for(XbrlSegment segment : segments){
			if(count != 0)
				segmentDesc += ", ";
			
			segmentDesc += segment.toString();
			count++;
		}
		
		if(!segmentDesc.isEmpty())
			return segmentDesc + " " + period.toString();
		else
			return period.toString();
	}
	
	public String getIdentifierScheme() {
		return identifierScheme;
	}

	public void setIdentifierScheme(String identifierScheme) {
		this.identifierScheme = identifierScheme;
	}

	public String getIdentifierValue() {
		return identifierValue;
	}

	public void setIdentifierValue(String identifierValue) {
		this.identifierValue = identifierValue;
	}

	public XbrlPeriod getPeriod() {
		return period;
	}

	public void setPeriod(XbrlPeriod period) {
		this.period = period;
	}

	public String getId() {
		return id;
	}
	
	public List<XbrlSegment> getSegments() {
		return segments;
	}

	public void addExplicitMember(String dimNS, String dimName, String memberNS, String memberName){
		segments.add(new XbrlSegment(dimNS, dimName, memberNS, memberName));
	}
	
}
