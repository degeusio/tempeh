package org.tempeh.xbrl;

import org.tempeh.TempehException;


public class NamespaceResolver {
	
	public static final String US_GAAP_PREFIX = "gaap";
	public static final String DOC_ENTITY_INFO_PREFIX = "dei";
	public static final String COUNTRY_PREFIX = "country";
	public static final String INVEST_PREFIX = "invest";
	public static final String STATE_OR_PROVINCE_PREFIX = "stpr";
	public static final String CURRENCY_PREFIX = "cur";
	public static final String NAICS_PREFIX = "naics";
	public static final String EXCH_PREFIX = "exch";
	public static final String SIC_PREFIX = "sic";
	public static final String US_TYPES_PREFIX = "types";
	public static final String XBRL_PREFIX = "xbrli";
	public static final String NON_NUMERIC_PREFIX = "nonnum";
	public static final String NUMERIC_PREFIX = "num";
	public static final String RISK_RETURN_PREFIX = "rr";
	
	private static final String US_GAAP_NS_PREFIX = "http://fasb.org/us-gaap/";
	private static final String US_GAAP_NS_PREFIX2 = "http://xbrl.us/us-gaap/";
	private static final String DOC_ENTITY_INFO_NS_PREFIX = "http://xbrl.sec.gov/dei/";
	private static final String DOC_ENTITY_INFO_NS_PREFIX2 = "http://xbrl.us/dei/";
	private static final String COUNTRY_NS_PREFIX = "http://xbrl.sec.gov/country/";
	private static final String COUNTRY_NS_PREFIX2 = "http://xbrl.us/country/";
	private static final String US_TYPE_NS_PREFIX = "http://fasb.org/us-types/";
	private static final String US_TYPE_NS_PREFIX2 = "http://xbrl.us/us-types/";
	private static final String INVEST_NS_PREFIX = "http://xbrl.sec.gov/invest/";
	private static final String INVEST_NS_PREFIX2 = "http://xbrl.us/invest/";
	private static final String STATE_OR_PROVINCE_NS_PREFIX = "http://xbrl.sec.gov/stpr/";
	private static final String STATE_OR_PROVINCE_NS_PREFIX2 = "http://xbrl.us/stpr/";
	private static final String CURRENCY_NS_PREFIX = "http://xbrl.us/currency/";
	private static final String CURRENCY_NS_PREFIX2 = "http://xbrl.sec.gov/currency/";
	private static final String NAICS_NS_PREFIX = "http://xbrl.sec.gov/naics/";
	private static final String EXCH_NS_PREFIX = "http://xbrl.sec.gov/exch/";
	private static final String SIC_NS_PREFIX = "http://xbrl.sec.gov/sic/"; //Standard Industrial Classification
	private static final String RR_NS_PREFIX = "http://xbrl.sec.gov/rr/"; //Risk & Return
	
	private static final String[] recognizedNamespacePrefix = {
		DOC_ENTITY_INFO_NS_PREFIX,
		DOC_ENTITY_INFO_NS_PREFIX2,
		INVEST_NS_PREFIX,
		COUNTRY_NS_PREFIX,
		CURRENCY_NS_PREFIX,
		EXCH_NS_PREFIX,
		NAICS_NS_PREFIX,
		SIC_NS_PREFIX,
		STATE_OR_PROVINCE_NS_PREFIX,
		"http://fasb.org/us-roles/",
		US_TYPE_NS_PREFIX,
		US_TYPE_NS_PREFIX2,
		US_GAAP_NS_PREFIX,
		US_GAAP_NS_PREFIX2
	};
	
	private static int recognizedNamespaceIndex(String namespace){
		for(int i = 0; i < recognizedNamespacePrefix.length; i++){
			if(namespace.startsWith(recognizedNamespacePrefix[i]))
				return i;
		}
		
		return -1;
	}
	
	public static boolean isNamespaceEssentiallyEqual(String namespace1, String namespace2) throws TempehException{
		//if exactly the same
		if(namespace1.equals(namespace2))
			return true;
		
		//these namespaces have different host
		if(isUsGaapNamespace(namespace1) && isUsGaapNamespace(namespace2))
			return true;
		if(isUSTypeNamespace(namespace1) && isUSTypeNamespace(namespace2))
			return true;
		if(isDocumentAndEntityInfoNamespace(namespace1) && isDocumentAndEntityInfoNamespace(namespace2))
			return true;
		
		int index1 = recognizedNamespaceIndex(namespace1);
		int index2 = recognizedNamespaceIndex(namespace2);
		
		if(index1 == -1)
			throw new TempehException("Do not recognized namespace:" + namespace1);
		else if(index2 == -1)
			throw new TempehException("Do not recognized namespace:" + namespace2);
		else if(index1 == index2)
			return true;
		
		return false;
	}
	
	public static boolean isUsGaapNamespace(String namespace){

		if(namespace.startsWith(US_GAAP_NS_PREFIX) || namespace.startsWith(US_GAAP_NS_PREFIX2))
			return true;
		
		return false;
	}

	public static boolean isUSTypeNamespace(String namespace){
		if(namespace.startsWith(US_TYPE_NS_PREFIX) || namespace.startsWith(US_TYPE_NS_PREFIX2))
			return true;
		
		return false;
	}
	
	public static boolean isDocumentAndEntityInfoNamespace(String namespace) {

		if(namespace.startsWith(DOC_ENTITY_INFO_NS_PREFIX) || namespace.startsWith(DOC_ENTITY_INFO_NS_PREFIX2))
			return true;
		
		return false;
	}
	
	public static boolean isInvestNamespace(String namespace){
		if(namespace.startsWith(INVEST_NS_PREFIX) || namespace.startsWith(INVEST_NS_PREFIX2))
			return true;

		return false;
	}
	
	public static boolean isCountryNamespace(String namespace) {

		if(namespace.startsWith(COUNTRY_NS_PREFIX) || namespace.startsWith(COUNTRY_NS_PREFIX2))
			return true;
		
		return false;
	}
	
	public static boolean isStateOrProvinceNamespace(String namespace) {

		if(namespace.startsWith(STATE_OR_PROVINCE_NS_PREFIX) || namespace.startsWith(STATE_OR_PROVINCE_NS_PREFIX2))
			return true;
		
		return false;
	}
	
	public static boolean isCurrencyNamespace(String namespace) {

		if(namespace.startsWith(CURRENCY_NS_PREFIX) || namespace.startsWith(CURRENCY_NS_PREFIX2))
			return true;
		
		return false;
	}
	
	public static boolean isNaicsNamespace(String namespace) {

		if(namespace.startsWith(NAICS_NS_PREFIX))
			return true;
		
		return false;
	}
	
	public static boolean isExchNamespace(String namespace){
		
		if(namespace.startsWith(EXCH_NS_PREFIX))
			return true;
		
		return false;
	}
	
	public static boolean isSicNamespace(String namespace){
		
		if(namespace.startsWith(SIC_NS_PREFIX))
			return true;
		
		return false;
	}
	
	public static boolean isRRNamespace(String namespace){
		
		if(namespace.startsWith(RR_NS_PREFIX))
			return true;
		
		return false;
	}

	public static boolean isXBRLNamespace(String namespace){
		
		//notice this is equals not startsWith
		if(namespace.equals("http://www.xbrl.org/2003/instance"))
			return true;
		
		return false;
	}

	public static boolean isNonNumericNamespace(String namespace){
		
		//notice this is equals not startsWith
		if(namespace.equals("http://www.xbrl.org/dtr/type/non-numeric"))
			return true;
		
		return false;
	}

	public static boolean isNumericNamespace(String namespace){
		
		//notice this is equals not startsWith
		if(namespace.equals("http://www.xbrl.org/dtr/type/numeric"))
			return true;
		
		return false;
	}

	public static interface INamespaceComparer{
		boolean isNamespaceSame(String namespace1, String namespace2);
	}

}
