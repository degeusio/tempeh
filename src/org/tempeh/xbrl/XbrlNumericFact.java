package org.tempeh.xbrl;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XbrlNumericFact implements IXbrlFact{
	
	private final String factType = "XbrlNumericFact"; //for serialization
	private XbrlContext context;
	private XbrlTaxonomy xbrlTaxonomy;
	private String id;
	private XbrlUnit unit;
	private String decimals;
	private String precision;
	private String stringValue;
	private boolean isNil;
	//do not serialize
	private String unitId;
	private static final String INF = "INF";
	
	public XbrlNumericFact(XbrlTaxonomy xbrlTaxonomy, String id, String unitId, String decimals, String precision, boolean isNil){
		this.xbrlTaxonomy = xbrlTaxonomy;
		this.id = id;
		this.decimals = decimals;
		this.precision = precision;
		this.isNil = isNil;
		this.unitId = unitId;
	}
	
	//for serialization
	private XbrlNumericFact(){}
	
	@Override
	public int hashCode(){
		return Objects.hash(context, xbrlTaxonomy);
	}
	
	@Override
	public boolean equals(Object object){
		
		if(!(object instanceof XbrlNumericFact))
			return false;
		
		//DO NOT INCLUDE ID PROPERTY
		XbrlNumericFact fact = (XbrlNumericFact)object;
		
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

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	
	public boolean isNil() {
		return isNil;
	}

	public boolean isMonetaryType(){
		if(this.xbrlTaxonomy.getTypeNamespace().equals(XbrlConstants.XBRL21Namespace) && this.xbrlTaxonomy.getTypeName().equals("monetaryItemType"))
			return true;
		
		return false;
	}
	
	public boolean isPerShareType(){
		if(this.xbrlTaxonomy.getTypeNamespace().equals(XbrlConstants.XBRLNumNamespace) && this.xbrlTaxonomy.getTypeName().equals("perShareItemType"))
			return true;
		
		return false;
	}
	
	public boolean isSharesType(){
		if(this.xbrlTaxonomy.getTypeNamespace().equals(XbrlConstants.XBRL21Namespace) && this.xbrlTaxonomy.getTypeName().equals("sharesItemType"))
			return true;
		
		return false;
	}
	
	public XbrlUnit getUnit() {
		return unit;
	}

	public void setUnit(XbrlUnit unit) {
		this.unit = unit;
	}
	
	public String getUnitId() {
		return unitId;
	}

	public String getDecimals() {
		return decimals;
	}

	public String getPrecision() {
		return precision;
	}

	public String getPrecisionAdjustedValue() throws XbrlException {
		String precision = getInferredPrecision();
		if (precision.equals("INF")) 
			return stringValue;

		String rawValue = stringValue;
		Pattern pattern = Pattern.compile("^(-?)(\\d*)(\\.?)(\\d*)(e?-?\\d*)?$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(stringValue);
		String digitsBeforeDecimal = "";
		String digitsAfterDecimal = "";
		// String exponent = "";
		if (matcher.matches()) {
			digitsBeforeDecimal = matcher.group(2);
			digitsAfterDecimal= matcher.group(4);
			String digits =  digitsBeforeDecimal + digitsAfterDecimal;
			Pattern pattern2 = Pattern.compile("^(0*)(\\d{0," + precision + "})(\\d*)$");
			Matcher matcher2 = pattern2.matcher(digits);
			if (!matcher2.matches()) 
				throw new XbrlException("The raw value is not formatted in a way that allows precision adjustment.");     

			digits = matcher2.group(1) + matcher2.group(2);
			int zerosToInsert = matcher2.group(3).length();
			for (int i=1; i<=zerosToInsert; i++) digits += "0";
			digitsBeforeDecimal = digits.substring(0,digitsBeforeDecimal.length());
			digitsAfterDecimal= digits.substring(digitsBeforeDecimal.length(), digits.length());
			String value = matcher.group(1) + digitsBeforeDecimal + matcher.group(3) + digitsAfterDecimal + matcher.group(5);   	
			return value;
		}
		throw new XbrlException("Precision adjustment failed using precision " + precision + " and value " + rawValue);
	}
	
	private String getInferredPrecision() throws XbrlException {
		//xbrl only allow one or the other.  If only decimals, we need to infer precision
		if (decimals == null){
			//assert precision != null;
			return precision;
		}

		//INF means EXACT value
		if (decimals.equals(INF)) 
			return INF;


		// Parse the value into its components
		Pattern pattern = Pattern.compile("^-?(\\d*)\\.?(\\d*)(e?(-?\\d*))?$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(stringValue);
		String digitsBeforeDecimal = null;
		String digitsAfterDecimal = null;
		String exponent = null;
		if (matcher.matches()) {
			digitsBeforeDecimal = matcher.group(1);
			digitsAfterDecimal= matcher.group(2);
			exponent = matcher.group(4);
		}

		// Eliminate leading zeros before decimal place.
		if (digitsBeforeDecimal != null) {
			pattern = Pattern.compile("^(0+)");
			matcher = pattern.matcher(digitsBeforeDecimal);
			digitsBeforeDecimal = matcher.replaceAll("");
		}

		int part1 = 0;
		if (digitsBeforeDecimal != null) {
			part1 = digitsBeforeDecimal.length();
		} else {
			if (digitsAfterDecimal != null) {
				pattern = Pattern.compile("^(0+)");
				matcher = pattern.matcher(digitsBeforeDecimal);
				if (matcher.matches()) {
					String zerosAfterDecimal = matcher.group(1);
					if (zerosAfterDecimal != null) {
						part1 = -(zerosAfterDecimal.length()); 
					}
				}
			}
		}

		int part2 = 0;
		if (exponent != null && !exponent.equals("")) part2 = (new Integer(exponent)).intValue();

		int part3 = (new Integer(decimals)).intValue();

		int x = part1 + part2 + part3;

		int precision = (x > 0) ? x : 0;

		return (new Integer(precision)).toString();	
	}
}
