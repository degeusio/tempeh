package org.tempeh.xbrl;

import java.util.ArrayList;
import java.util.List;

import org.tempeh.TempehException;

public class XbrlUnit {
	
	public enum UnitType{
		CURRENCY,
		PURE_NUMBER,
		SHARE,
		EPS,
		UNKNOWN
	}
	
	private String id;
	private boolean isDivide = false;
	private static final String CURRENCY_PREFIX = "iso4217";
	
	private List<String> measures = new ArrayList<String>();
	
	public XbrlUnit(String id){
		this.id = id;
	}

	//for serialization
	private XbrlUnit(){}
	
	public String getId() {
		return id;
	}

	public void addMeasure(String measure){
		measures.add(measure);
	}
	
	public void setDivide(boolean isDivide) {
		this.isDivide = isDivide;
	}

	public String getFormattedUnit() throws TempehException{
		if(measures.size() == 1){
			String prefix = (measures.get(0).indexOf(":") == -1)?"":measures.get(0).substring(0, measures.get(0).indexOf(":"));
			String value = (measures.get(0).indexOf(":") == -1)?measures.get(0):measures.get(0).substring(measures.get(0).indexOf(":") + 1);
			
			return value;
			/*
			if(prefix.equals(CURRENCY_PREFIX)){
				return value.toUpperCase();
			}
			else if(prefix.equals(XbrlConstants.XBRL21Prefix)){
				if(value.equals("shares"))
					return "shares";
				else if(value.equals("pure"))
					return "";  //A pure number
			}
			else
				return value;*/
		}
		else if(measures.size() == 2){
			if(measures.get(0).equals(measures.get(1))){
				String value = (measures.get(0).indexOf(":") == -1)?measures.get(0):measures.get(0).substring(measures.get(0).indexOf(":") + 1);
				return "sq." + value;
			}
			else{
				String numeratorPrefix = (measures.get(0).indexOf(":") == -1)?"":measures.get(0).substring(0, measures.get(0).indexOf(":"));
				String numeratorValue = (measures.get(0).indexOf(":") == -1)?measures.get(0):measures.get(0).substring(measures.get(0).indexOf(":") + 1);
				
				String denominatorPrefix = (measures.get(1).indexOf(":") == -1)?"":measures.get(1).substring(0, measures.get(1).indexOf(":"));
				String denominatorValue = (measures.get(1).indexOf(":") == -1)?measures.get(1):measures.get(1).substring(measures.get(1).indexOf(":") + 1);
				
				//if(numeratorPrefix.equals(CURRENCY_PREFIX) && denominatorPrefix.equals(XbrlConstants.XBRL21Prefix) && denominatorValue.equals("shares"))
				//	return numeratorValue.toUpperCase() + "/shares";
				return numeratorValue + "/" + denominatorValue;
			}
		}
		
		throw new TempehException("Cannot format unrecognized unit with measures " + measures.toString());
	}

	
}
