package org.tempeh.xbrl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSNamespaceItemList;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.tempeh.TempehException;
import org.tempeh.xbrl.XbrlPeriod.PeriodType;
import org.tempeh.xbrl.xlink.IXLinkHandler;
import org.tempeh.xbrl.xlink.XLinkException;
import org.tempeh.xbrl.xlink.XLinkProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableMap;

public class XbrlElementHandler implements IElementHandler, IXLinkHandler {

	private final XLinkProcessor xlinkProcessor;
	private final XbrlLoader loader;
	private final Map<String, String> schemaToNamespaceMap = new HashMap<String, String>();
	//private final Map<String, XbrlRoleType> roleTypeMap = new HashMap<String, XbrlRoleType>();
	//private final Map<String, String> roleRefMap = new HashMap<String, String>();
	//private final Map<String, Document> locatorDocCache = new HashMap<String, Document>();
	private final Map<String, XbrlTaxonomy> xbrlConceptCache = new HashMap<String, XbrlTaxonomy>();
	private Stack<Object> elementStack = new Stack<Object>();
	private Attributes currentElementAttributes;
	private StringBuilder elementTextStringBuilder = new StringBuilder();
	private XbrlInstance instance;
	private XSNamedMap schemaElementDeclarations;
	private final Map<String, XSNamedMap> missingSchemaElementDeclarations = new HashMap<String, XSNamedMap>();
	private boolean parsingXbrlInstance = false;
	private boolean parsingXbrlSchema = false;
	private boolean parsingXLinkElement = false;
	
	//ALWAYS IN LOWER CASE
	private final String XBRL_LOCALNAME = "xbrl";
	private final String PERIOD_LOCALNAME = "period";
	private final String IDENTIFIER_LOCALNAME = "identifier";
	private final String SEGMENT_LOCALNAME = "segment";
	private final String CONTEXT_LOCALNAME = "context";
	private final String UNIT_LOCALNAME = "unit";
	private final String DIVIDE_LOCALNAME = "divide";
	//private final String NUMERATOR_LOCALNAME = "unitnumerator";
	//private final String DENOMINATOR_LOCALNAME = "unitdenominator";
	private final String MEASURE_LOCALNAME = "measure";
	private final String STARTDATE_LOCALNAME = "startdate";
	private final String ENDDATE_LOCALNAME = "enddate";
	private final String INSTANT_LOCALNAME = "instant";
	private final String FOREVER_LOCALNAME = "forever";
	private final String EXPLICITMEMBER_LOCALNAME = "explicitmember";
	private final String ROLETYPE_LOCALNAME = "roletype";
	private final String DEFINITION_LOCALNAME = "definition";
	private final String USEDON_LOCALNAME = "usedon";
	private final String CALCULATIONLINK_LOCALNAME = "calculationlink";
	private final String DEFINITIONLINK_LOCALNAME = "definitionlink";
	private final String PRESENTATIONLINK_LOCALNAME = "presentationlink";
	private final String LABELLINK_LOCALNAME = "labellink";
	private final String FOOTNOTELINK_LOCALNAME = "footnotelink";
	private final String LABEL_LOCALNAME = "label";
	private final String FOOTNOTE_LOCALNAME = "footnote";
	
	private static final Map<String, String> knownSchemaMapping = ImmutableMap.<String, String>builder()
			.put("http://fasb.org/us-gaap/2015-01-31", "http://xbrl.fasb.org/us-gaap/2015/elts/us-gaap-2015-01-31.xsd")
			.put("http://fasb.org/us-gaap/2014-01-31", "http://xbrl.fasb.org/us-gaap/2014/elts/us-gaap-2014-01-31.xsd")
			.put("http://fasb.org/us-gaap/2013-01-31", "http://xbrl.fasb.org/us-gaap/2013/elts/us-gaap-2013-01-31.xsd")
			.put("http://fasb.org/us-gaap/2012-01-31", "http://xbrl.fasb.org/us-gaap/2012/elts/us-gaap-2012-01-31.xsd")
			.put("http://fasb.org/us-gaap/2011-01-31", "http://xbrl.fasb.org/us-gaap/2011/elts/us-gaap-2011-01-31.xsd")
			.put("http://xbrl.us/us-gaap/2009-01-31", "http://taxonomies.xbrl.us/us-gaap/2009/elts/us-gaap-2009-01-31.xsd")
			.put("http://fasb.org/us-types/2013-01-31", "http://xbrl.fasb.org/us-gaap/2013/elts/us-types-2013-01-31.xsd")
			.put("http://fasb.org/us-types/2012-01-31", "http://xbrl.fasb.org/us-gaap/2012/elts/us-types-2012-01-31.xsd")
			.put("http://fasb.org/us-types/2011-01-31", "http://xbrl.fasb.org/us-gaap/2011/elts/us-types-2011-01-31.xsd")
			.put("http://xbrl.us/us-types/2009-01-31", "http://taxonomies.xbrl.us/us-gaap/2009/elts/us-types-2009-01-31.xsd")
			.put("http://fasb.org/us-roles/2013-01-31", "http://xbrl.fasb.org/us-gaap/2013/elts/us-roles-2013-01-31.xsd")
			.put("http://fasb.org/us-roles/2012-01-31", "http://xbrl.fasb.org/us-gaap/2012/elts/us-roles-2012-01-31.xsd")
			.put("http://fasb.org/us-roles/2011-01-31", "http://xbrl.fasb.org/us-gaap/2011/elts/us-roles-2011-01-31.xsd")
			.put("http://xbrl.us/us-roles/2009-01-31", "http://taxonomies.xbrl.us/us-gaap/2009/elts/us-roles-2009-01-31.xsd")
			.put("http://xbrl.sec.gov/country/2013-01-31", "http://xbrl.sec.gov/country/2013/country-2013-01-31.xsd")
			.put("http://xbrl.sec.gov/country/2012-01-31", "http://xbrl.sec.gov/country/2012/country-2012-01-31.xsd")
			.put("http://xbrl.sec.gov/country/2011-01-31", "http://xbrl.sec.gov/country/2011/country-2011-01-31.xsd")
			.put("http://xbrl.us/country/2009-01-31", "http://taxonomies.xbrl.us/us-gaap/2009/non-gaap/country-2009-01-31.xsd")
			.put("http://xbrl.sec.gov/currency/2014-01-31", "http://xbrl.sec.gov/currency/2014/currency-2014-01-31.xsd")
			.put("http://xbrl.sec.gov/currency/2012-01-31", "http://xbrl.sec.gov/currency/2012/currency-2012-01-31.xsd")
			.put("http://xbrl.sec.gov/currency/2011-01-31", "http://xbrl.sec.gov/currency/2011/currency-2011-01-31.xsd")
			.put("http://xbrl.us/currency/2009-01-31", "http://taxonomies.xbrl.us/us-gaap/2009/non-gaap/currency-2009-01-31.xsd")
			.put("http://xbrl.sec.gov/exch/2014-01-31", "http://xbrl.sec.gov/exch/2014/exch-2014-01-31.xsd")
			.put("http://xbrl.sec.gov/exch/2013-01-31", "http://xbrl.sec.gov/exch/2013/exch-2013-01-31.xsd")
			.put("http://xbrl.sec.gov/exch/2012-01-31", "http://xbrl.sec.gov/exch/2012/exch-2012-01-31.xsd")
			.put("http://xbrl.sec.gov/exch/2011-01-31", "http://xbrl.sec.gov/exch/2011/exch-2011-01-31.xsd")
			.put("http://xbrl.us/exch/2009-01-31", "http://taxonomies.xbrl.us/us-gaap/2009/non-gaap/exch-2009-01-31.xsd")
			.put("http://xbrl.sec.gov/invest/2013-01-31", "http://xbrl.sec.gov/invest/2013/invest-2013-01-31.xsd")
			.put("http://xbrl.sec.gov/invest/2012-01-31", "http://xbrl.sec.gov/invest/2012/invest-2012-01-31.xsd")
			.put("http://xbrl.sec.gov/invest/2011-01-31", "http://xbrl.sec.gov/invest/2011/invest-2011-01-31.xsd")
			.put("http://xbrl.us/invest/2009-01-31", "http://taxonomies.xbrl.us/us-gaap/2009/non-gaap/invest-2009-01-31.xsd")
			.put("http://xbrl.sec.gov/naics/2011-01-31", "http://xbrl.sec.gov/naics/2011/naics-2011-01-31.xsd")
			.put("http://xbrl.sec.gov/sic/2011-01-31", "http://xbrl.sec.gov/sic/2011/sic-2011-01-31.xsd")
			.put("http://xbrl.sec.gov/stpr/2011-01-31", "http://xbrl.sec.gov/stpr/2011/stpr-2011-01-31.xsd")
			.put("http://xbrl.sec.gov/dei/2014-01-31", "http://xbrl.sec.gov/dei/2014/dei-2014-01-31.xsd")
			.put("http://xbrl.sec.gov/dei/2013-01-31", "http://xbrl.sec.gov/dei/2013/dei-2013-01-31.xsd")
			.put("http://xbrl.sec.gov/dei/2012-01-31", "http://xbrl.sec.gov/dei/2012/dei-2012-01-31.xsd")
			.put("http://xbrl.sec.gov/dei/2011-01-31", "http://xbrl.sec.gov/dei/2011/dei-2011-01-31.xsd")
			.put("http://xbrl.us/dei/2009-01-31", "http://taxonomies.xbrl.us/us-gaap/2009/non-gaap/dei-2009-01-31.xsd")
			.put("http://xbrl.us/ar/2009-01-31", "http://taxonomies.xbrl.us/us-gaap/2009/non-gaap/ar-2009-01-31.xsd")
			.build();
	//http://fasb.org/us-gaap/2012-01-31
	public XbrlElementHandler(XbrlLoader loader){
		this.xlinkProcessor = new XLinkProcessor(this);
		this.loader = loader;
	}
	
	@Override
	public void newXbrlDoc(XbrlInstance instance){
		this.instance = instance;
		elementStack.clear();
		schemaToNamespaceMap.clear();
		xbrlConceptCache.clear();
		//locatorDocCache.clear();
		elementTextStringBuilder = new StringBuilder();
		parsingXbrlInstance = false;
		parsingXbrlSchema = false;
		parsingXLinkElement = false;
		currentElementAttributes = null;
		schemaElementDeclarations = null;
		missingSchemaElementDeclarations.clear();
		//xbrlSchemaModel = null;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws Exception {
		
		currentElementAttributes = attributes;
		//make sure localName is lower string before doing any comparison
		String lowerCaseLocalName = localName.toLowerCase();
				
		//this can happen while parsingXLinkElement = true
		if(uri.equals(XbrlConstants.XBRL21LinkNamespace) && lowerCaseLocalName.equals(ROLETYPE_LOCALNAME)){
			String id = attributes.getValue("id");
			String roleURI = attributes.getValue("roleURI");
			//logger.info("role type id - " + id + " roleURI - " + roleURI);
			XbrlRoleType roleType = new XbrlRoleType(id, roleURI);
			instance.getRoleTypes().put(roleURI, roleType);
			elementStack.push(roleType);
		}
		
		xlinkProcessor.startElement(uri, localName, qName, attributes);
		
		if(parsingXLinkElement)
			return;
				
		if(uri.equals(XbrlConstants.XBRL21Namespace)){

			//beginning tag for an XBRL instance
			if(lowerCaseLocalName.equals(XBRL_LOCALNAME)){
				parsingXbrlInstance = true;
			}
			else if(lowerCaseLocalName.equals(PERIOD_LOCALNAME)) {
				XbrlContext currentContext = (XbrlContext)elementStack.peek();
				XbrlPeriod period = new XbrlPeriod();
				currentContext.setPeriod(period);
				elementStack.push(period);
			} 
			else if(lowerCaseLocalName.equals(IDENTIFIER_LOCALNAME)) {
				XbrlContext currentContext = (XbrlContext)elementStack.peek();
				String scheme = attributes.getValue("scheme");
				currentContext.setIdentifierScheme(scheme);
				 
			} 
			else if(lowerCaseLocalName.equals(SEGMENT_LOCALNAME)) {
				/*
				XbrlContext currentContext = (XbrlContext)elementStack.peek();
				XbrlSegments segments = new XbrlSegments();
				currentContext.setSegments(segments);
				elementStack.push(segments);*/
			} 
			else if(lowerCaseLocalName.equals(CONTEXT_LOCALNAME)) {
				String id = attributes.getValue("id");
				XbrlContext context = new XbrlContext(id);
				//contextMap.put(id, context);
				instance.addContext(context);
				elementStack.push(context);
			} 
			else if(lowerCaseLocalName.equals(UNIT_LOCALNAME)){
				String id = attributes.getValue("id");
				XbrlUnit unit = new XbrlUnit(id);
				instance.addUnit(unit);
				elementStack.push(unit);
			}
			else if(lowerCaseLocalName.equals(DIVIDE_LOCALNAME)){
				XbrlUnit currentUnit = (XbrlUnit)elementStack.peek();
				currentUnit.setDivide(true);
			}
			
		}
		else if(parsingXbrlInstance){
			
			String contextRef = attributes.getValue("contextRef");
			if(contextRef != null){
				
				String id = attributes.getValue("id");
				String decimals = attributes.getValue("decimals");
				String precision = attributes.getValue("precision");
				String unitRef = attributes.getValue("unitRef");
				//look for nil attribute.  Because nil always show up as qualified name, can be different.  Have seen it as xsi:nil or xs:nil
				String nil = null;
				for(int i = 0; i < attributes.getLength(); i++){
					if(attributes.getLocalName(i).equals("nil")){
						nil = attributes.getValue(i);
						break;
					}
				}
				boolean isNil = false;
				if(nil != null)
					isNil = Boolean.parseBoolean(nil);

				XbrlTaxonomy xbrlTaxonomy = getXbrlTaxonomyByNamespaceAndName(uri, localName);
				//assert xbrlTaxonomy != null;
				if(xbrlTaxonomy == null)
					throw new TempehException("No taxonomy found for uri: " + uri + " name: " + localName);

				IXbrlFact fact = instance.addFact(contextRef, xbrlTaxonomy, id, decimals, precision, unitRef, isNil);
				elementStack.push(fact);
			}
			
		}
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws Exception {
		
		String lowerCaseLocalName = localName.toLowerCase();
		String elementText = elementTextStringBuilder.toString().trim();
		
		//this can happen while parsingXLinkElement = true, so has to go before
		if(uri.equals(XbrlConstants.XBRL21LinkNamespace) && 
				(lowerCaseLocalName.equals(ROLETYPE_LOCALNAME) ||
						lowerCaseLocalName.equals(DEFINITION_LOCALNAME) || 
						lowerCaseLocalName.equals(USEDON_LOCALNAME))){
			
			if(lowerCaseLocalName.equals(ROLETYPE_LOCALNAME)){
				elementStack.pop();
			}
			else if(lowerCaseLocalName.equals(DEFINITION_LOCALNAME)){
				XbrlRoleType currentRoleType = (XbrlRoleType)elementStack.peek();
				currentRoleType.setDefinition(elementText);
			}
			else if(lowerCaseLocalName.equals(USEDON_LOCALNAME)){
				XbrlRoleType currentRoleType = (XbrlRoleType)elementStack.peek();
				currentRoleType.getUsedOn().add(elementText);
			}
		}
		else if(parsingXLinkElement){
			xlinkProcessor.endElement(uri, localName, qName);
		}
		else if(parsingXbrlInstance){

			if(uri.equals(XbrlConstants.XBRL21Namespace)){

				if(lowerCaseLocalName.equals(XBRL_LOCALNAME)){
					parsingXbrlInstance = false;
				}
				else if(lowerCaseLocalName.equals(PERIOD_LOCALNAME)) {
					elementStack.pop();
				} 
				else if(lowerCaseLocalName.equals(SEGMENT_LOCALNAME)) {
					//elementStack.pop();
				} 
				else if(lowerCaseLocalName.equals(CONTEXT_LOCALNAME)) {
					elementStack.pop();
				} 
				else if(lowerCaseLocalName.equals(UNIT_LOCALNAME)){
					elementStack.pop();
				}
				else if(lowerCaseLocalName.equals(IDENTIFIER_LOCALNAME)){
					XbrlContext currentContext = (XbrlContext)elementStack.peek();
					currentContext.setIdentifierValue(elementText);
				}
				else if(lowerCaseLocalName.equals(STARTDATE_LOCALNAME)){
					XbrlPeriod currentPeriod = (XbrlPeriod)elementStack.peek();
					currentPeriod.setStart(elementText);
					currentPeriod.setPeriodType(PeriodType.duration);
				}
				else if(lowerCaseLocalName.equals(ENDDATE_LOCALNAME)){
					XbrlPeriod currentPeriod = (XbrlPeriod)elementStack.peek();
					currentPeriod.setEnd(elementText);
					currentPeriod.setPeriodType(PeriodType.duration);
				}
				else if(lowerCaseLocalName.equals(INSTANT_LOCALNAME)){
					XbrlPeriod currentPeriod = (XbrlPeriod)elementStack.peek();
					currentPeriod.setStart(elementText);
					currentPeriod.setEnd(elementText);
					currentPeriod.setPeriodType(PeriodType.instant);
				}
				else if(lowerCaseLocalName.equals(FOREVER_LOCALNAME)){
					XbrlPeriod currentPeriod = (XbrlPeriod)elementStack.peek();
					currentPeriod.setPeriodType(PeriodType.forever);
				}
				else if(lowerCaseLocalName.equals(MEASURE_LOCALNAME)){
					XbrlUnit currentUnit = (XbrlUnit)elementStack.peek();
					currentUnit.addMeasure(elementText);
				}

			}
			else if(uri.equals(XbrlConstants.XBRLDINamespace)){
				if(lowerCaseLocalName.equals(EXPLICITMEMBER_LOCALNAME)){
					XbrlContext currentContext = (XbrlContext)elementStack.peek();
					String dimension = currentElementAttributes.getValue("dimension");
					String[] dimensionSplitted = dimension.split(":");
					String[] memberSplitted = elementText.split(":");
					String dimensionNamespace = instance.getNamespaceFromPrefix(dimensionSplitted[0]);
					String memberNamespace = instance.getNamespaceFromPrefix(memberSplitted[0]);
					//logger.info("adding explicit member : " + elementText + " of dim: " + dimension);
					currentContext.addExplicitMember(dimensionNamespace, dimensionSplitted[1], memberNamespace, memberSplitted[1]);
				}
			}
			
			if(elementStack.size() > 0){
				Object topLevelItem = elementStack.peek();
				if(topLevelItem instanceof IXbrlFact){
					IXbrlFact fact = (IXbrlFact)topLevelItem;
					
					if(fact instanceof XbrlNumericFact){
						((XbrlNumericFact)fact).setStringValue(elementText);
					}
					else if(fact instanceof XbrlTextFact){
						((XbrlTextFact)fact).setText(elementText);
					}
					elementStack.pop();
					
					/*  SOME DOCUMENTS WILL NOT HAVE ID
					String id = currentElementAttributes.getValue("id");
					if(fact.getId().equals(id)){
						if(fact instanceof XbrlNumericFact){
							((XbrlNumericFact)fact).setStringValue(elementText);
						}
						else if(fact instanceof XbrlTextFact){
							((XbrlTextFact)fact).setText(elementText);
						}
						elementStack.pop();
					}*/
				}
			}

		}

		//reset
		currentElementAttributes = null;
		elementTextStringBuilder = new StringBuilder();
	}

	@Override
	public void characters(char buf[], int offset, int len) throws Exception
	{
		elementTextStringBuilder.append(buf, offset, len);
		//logger.info("characters: " + elementValue);
	}
	
	public void startPrefixMapping(String prefix, String uri) throws Exception{
		//logger.info("prefix mapping: " + prefix + " to uri: " + uri);
		
		//When parsing xbrl schema, prefix mapping also gets called.  We don't want mapping for schema file.  Can be different from instance file
		if(!parsingXbrlSchema)
			instance.addNamespaceToPrefixMapping(prefix, uri);
	}
	
    public void endPrefixMapping(String prefix) throws Exception{
    	
    }
    
	@Override
	public void startSimpleLink(String namespaceURI, String lName,
			String qName, Attributes attrs, String href, String role,
			String arcrole, String title, String show, String actuate)
					throws XLinkException {

		parsingXLinkElement = true;

		String lowerCaseLocalName = lName.toLowerCase();
		if(namespaceURI.equals(XbrlConstants.XBRL21LinkNamespace)){
			if(lowerCaseLocalName.equals("schemaref")){

				if(href.startsWith("http://")){
					return;
				}
				parsingXbrlSchema = true;
				XMLSchemaLoader schemaLoader = new XMLSchemaLoader();
				schemaLoader.setEntityResolver(loader);
				schemaLoader.setFeature("http://apache.org/xml/features/generate-synthetic-annotations", true);
				
				URI targetLink = loader.getBaseURIResolver().getBaseURI().resolve(href);
				XSModel xbrlSchemaModel = schemaLoader.loadURI(targetLink.toString());
				
				schemaElementDeclarations = xbrlSchemaModel.getComponents(XSConstants.ELEMENT_DECLARATION);
				/*
				XSNamedMap map = xbrlSchemaModel.getComponents(XSConstants.ELEMENT_DECLARATION);
				for(int i = 0; i < map.getLength(); i++){
					XSObject obj = map.item(i);
					XSElementDeclaration elementDec = (XSElementDeclaration)obj;
					logger.info("ns: " + elementDec.getNamespace() + " name: " + elementDec.getName() + " string: " + elementDec.toString());
					
					XSObjectList list = elementDec.getAnnotations();
					for(int j = 0; j < list.getLength(); j++){
						XSAnnotation attributeObj = (XSAnnotation)list.item(j);
						//logger.info(((XSAnnotation)attributeObj).getAnnotationString());
						attributeObj.writeAnnotation(loader, XSAnnotation.SAX_CONTENTHANDLER);
					}
				}*/
				
				try{
					parseXbrlSchemaAppInfo(xbrlSchemaModel.getAnnotations());
				}
				catch(Exception e){throw new XLinkException(e);}

				XSNamespaceItemList list = xbrlSchemaModel.getNamespaceItems();
				for(int i = 0; i < list.getLength(); i++){
					XSNamespaceItem nsItem = list.item(i);
					StringList locations = nsItem.getDocumentLocations();
					for (int j=0; j<locations.getLength(); j++) {
						String location = locations.item(j);
						if(location.equals(targetLink.toString())){
							//logger.info("setting local namespace: " + nsItem.getSchemaNamespace());
							this.instance.setLocalNamespace(nsItem.getSchemaNamespace());
						}
						
						schemaToNamespaceMap.put(location, nsItem.getSchemaNamespace());
					}
				}
			}
			else if(lowerCaseLocalName.equals("linkbaseref")){
				URI targetLink = loader.getBaseURIResolver().getBaseURI().resolve(href);
				loader.queueAdditionalDocsToLoad(targetLink);
			}
			else if(lowerCaseLocalName.equals("roleref")){
				//String roleURI = attrs.getValue("roleURI");
				//roleRefMap.put(roleURI, href);
			}
		}
	}

	@Override
	public void endSimpleLink(String namespaceURI, String sName, String qName)
			throws XLinkException {
		// TODO Auto-generated method stub
		parsingXLinkElement = false;
	}

	@Override
	public void startTitle(String namespaceURI, String lName, String qName,
			Attributes attrs) throws XLinkException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endTitle(String namespaceURI, String sName, String qName)
			throws XLinkException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startExtendedLink(String namespaceURI, String lName,
			String qName, Attributes attrs, String role, String title)
			throws XLinkException {

		parsingXLinkElement = true;
		
		String lowerCaseLocalName = lName.toLowerCase();
		if(namespaceURI.equals(XbrlConstants.XBRL21LinkNamespace)){
			if(lowerCaseLocalName.equals(CALCULATIONLINK_LOCALNAME)){
				CalculationLink calcLink = null;
				if(instance.getCalculationLinks().containsKey(role))
					calcLink = instance.getCalculationLinks().get(role);
				else
					calcLink = new CalculationLink(role, title);
				elementStack.push(calcLink);
			}
			else if(lowerCaseLocalName.equals(DEFINITIONLINK_LOCALNAME)){
				DefinitionLink defLink = null;
				if(instance.getDefinitionLinks().containsKey(role))
					defLink = instance.getDefinitionLinks().get(role);
				else
					defLink = new DefinitionLink(role, title);
				elementStack.push(defLink);
			}
			else if(lowerCaseLocalName.equals(PRESENTATIONLINK_LOCALNAME)){
				PresentationLink presLink = null;
				if(instance.getPresentationLinks().containsKey(role))
					presLink = instance.getPresentationLinks().get(role);
				else
					presLink = new PresentationLink(role, title);
				elementStack.push(presLink);
			}
			else if(lowerCaseLocalName.equals(LABELLINK_LOCALNAME)){
				LabelLink labelLink = null;
				if(instance.getLabelLinks().containsKey(role))
					labelLink = instance.getLabelLinks().get(role);
				else
					labelLink = new LabelLink(role, title);
				elementStack.push(labelLink);
			}
			else if(lowerCaseLocalName.equals(FOOTNOTELINK_LOCALNAME)){
				FootnoteLink footnoteLink = instance.getFootnoteLink();
				elementStack.push(footnoteLink);
			}
		}
	}

	@Override
	public void endExtendedLink(String namespaceURI, String sName, String qName)
			throws XLinkException {
		parsingXLinkElement = false;
		
		try{
			String lowerCaseLocalName = sName.toLowerCase();
			if(namespaceURI.equals(XbrlConstants.XBRL21LinkNamespace)){
				if(lowerCaseLocalName.equals(CALCULATIONLINK_LOCALNAME)){
					CalculationLink calcLink = (CalculationLink)elementStack.pop();
					if(!instance.getCalculationLinks().containsKey(calcLink.getRole()))
						instance.getCalculationLinks().put(calcLink.getRole(), calcLink);

					calcLink.verifyCalculations();
				}
				else if(lowerCaseLocalName.equals(DEFINITIONLINK_LOCALNAME)){
					DefinitionLink defLink = (DefinitionLink)elementStack.pop();
					if(!instance.getDefinitionLinks().containsKey(defLink.getRole()))
						instance.getDefinitionLinks().put(defLink.getRole(), defLink);
					
					defLink.verifyDefinitions();
					//defLink.printLinks();
				}
				else if(lowerCaseLocalName.equals(PRESENTATIONLINK_LOCALNAME)){
					PresentationLink presLink = (PresentationLink)elementStack.pop();
					if(!instance.getPresentationLinks().containsKey(presLink.getRole()))
						instance.getPresentationLinks().put(presLink.getRole(), presLink);
					
					presLink.verifyPresentations();
					//presLink.printLinks();
				}
				else if(lowerCaseLocalName.equals(LABELLINK_LOCALNAME)){
					LabelLink labelLink = (LabelLink)elementStack.pop();
					if(!instance.getLabelLinks().containsKey(labelLink.getRole()))
						instance.getLabelLinks().put(labelLink.getRole(), labelLink);
					
					labelLink.verifyLabels();
				}
				else if(lowerCaseLocalName.equals(FOOTNOTELINK_LOCALNAME)){
					elementStack.pop();
				}
			}
		}
		catch(Exception e){
			throw new XLinkException(e);
		}
	}

	@Override
	public void startResource(String namespaceURI, String lName, String qName,
			Attributes attrs, String role, String title, String label)
			throws XLinkException {
		
		String lowerCaseLocalName = lName.toLowerCase();
		if(namespaceURI.equals(XbrlConstants.XBRL21LinkNamespace)){
			if(lowerCaseLocalName.equals(LABEL_LOCALNAME)){
				String language = attrs.getValue("xml:lang");
				XbrlLabel xbrlLabel = new XbrlLabel(label, role, language);
				elementStack.push(xbrlLabel);
			}
			else if(lowerCaseLocalName.equals(FOOTNOTE_LOCALNAME)){
				String language = attrs.getValue("xml:lang");
				XbrlLabel xbrlLabel = new XbrlLabel(label, role, language);
				elementStack.push(xbrlLabel);
			}
		}
	}

	@Override
	public void endResource(String namespaceURI, String sName, String qName)
			throws XLinkException {
		
		String lowerCaseLocalName = sName.toLowerCase();
		if(namespaceURI.equals(XbrlConstants.XBRL21LinkNamespace)){
			if(lowerCaseLocalName.equals(LABEL_LOCALNAME)){
				String elementText = elementTextStringBuilder.toString().trim();
				XbrlLabel xbrlLabel = (XbrlLabel)elementStack.pop();
				xbrlLabel.setLabelValue(elementText);
				LabelLink labelLink = (LabelLink)elementStack.peek();
				labelLink.addLabel(xbrlLabel);
			}
			else if(lowerCaseLocalName.equals(FOOTNOTE_LOCALNAME)){
				String elementText = elementTextStringBuilder.toString().trim();
				XbrlLabel xbrlLabel = (XbrlLabel)elementStack.pop();
				xbrlLabel.setLabelValue(elementText);
				FootnoteLink footnoteLink = (FootnoteLink)elementStack.peek();
				footnoteLink.addFootnote(xbrlLabel);
			}
		}
	}

	@Override
	public void startLocator(String namespaceURI, String lName, String qName, Attributes attrs, String href, String role, String title, String label) throws XLinkException {
		
		try{
			//if href contains a fragment, such as '#abcd', means it's pointing to a fact in xbrl instance
			if(href.indexOf("#") == 0){
				String refId = href.substring(1);
				FootnoteLink footnoteLink = (FootnoteLink)elementStack.peek();
				if(footnoteLink != null){
					footnoteLink.addFactLabel(refId, label);
				}
			}
			else{
				URI targetHref = loader.getBaseURIResolver().getBaseURI().resolve(href);
				
				String resourceId = targetHref.getFragment();
				int endIndex = (targetHref.toString().indexOf('#') != -1)?targetHref.toString().indexOf('#'):targetHref.toString().length() - 1;
				String resourcePath = targetHref.toString().substring(0, endIndex);
				String namespace = schemaToNamespaceMap.get(resourcePath);

				if(namespace == null){
					//if none found, check for known ones
					for(String ns : knownSchemaMapping.keySet()){
						String schema = knownSchemaMapping.get(ns);
						if(schema.equals(resourcePath)){
							namespace = ns;
							break;
						}
					}
					if(namespace == null)
						throw new XLinkException("no namespace found for resourcePath: " + resourcePath);
				}
				XbrlTaxonomy targetConcept = getXbrlTaxonomyById(namespace, resourceId);
				
				if(targetConcept != null){
					IExtendedLink extendedLink = (IExtendedLink)elementStack.peek();
					extendedLink.addResource(label, targetConcept);
				}
				
				//logger.info("resourcePath: " + resourcePath + " ns: " + prefix);
				
				/*
				XbrlTaxonomy targetConcept = null;
				if(xbrlConceptCache.containsKey(targetHref.toString())){
					targetConcept = xbrlConceptCache.get(targetHref.toString());
				}
				else{

					XPathFactory factory = XPathFactory.newInstance();
					XPath xpath = factory.newXPath();
					XPathExpression expr = xpath.compile("//*[@id='" + resourceId + "']");

					Document doc = null;
					if(locatorDocCache.containsKey(resourcePath))
						doc = locatorDocCache.get(resourcePath);
					else{
						DOMParser p = new DOMParser(loader.getSymbolTable(), loader.getGrammarPool());
						p.parse(new InputSource(resourcePath));
						doc = p.getDocument();
						locatorDocCache.put(resourcePath, doc);
					}

					Node node = (Node)expr.evaluate(doc, XPathConstants.NODE);
					if(node != null){
						targetConcept = new XbrlTaxonomy(targetHref.toString(), namespace, node);
						xbrlConceptCache.put(targetHref.toString(), targetConcept);
					}
				}*/
				
			}
		}
		catch(Exception e){
			throw new XLinkException("<startLocator> Error locating resource for href: " + href, e);
		}
		
		
	}

	@Override
	public void endLocator(String namespaceURI, String sName, String qName)
			throws XLinkException {
		
	}

	@Override
	public void startArc(String namespaceURI, String lName, String qName,
			Attributes attrs, String from, String to, String arcrole,
			String title, String show, String actuate) throws XLinkException {
		
		try{
			String lowerCaseLocalName = lName.toLowerCase();
			if(namespaceURI.equals(XbrlConstants.XBRL21LinkNamespace)){
				
				Double order = null;
				if(attrs.getValue("order") != null)
					order = Double.parseDouble(attrs.getValue("order"));
				String use = attrs.getValue("use");
				Integer priority = null;
				if(attrs.getValue("priority") != null)
					priority = Integer.parseInt(attrs.getValue("priority"));
				//if(attrs.getValue("xbrldt:targetRole") != null)
				//	logger.info(attrs.getValue("xbrldt:targetRole"));
				
				if(lowerCaseLocalName.equals("calculationarc")){
					CalculationLink calcLink = (CalculationLink)elementStack.peek();
					calcLink.link(from, to, arcrole, order, use, priority, Double.parseDouble(attrs.getValue("weight")));
				}
				else if(lowerCaseLocalName.equals("definitionarc")){
					DefinitionLink defLink = (DefinitionLink)elementStack.peek();
					defLink.link(from, to, arcrole, order, use, priority);
				}
				else if(lowerCaseLocalName.equals("presentationarc")){
					PresentationLink presLink = (PresentationLink)elementStack.peek();
					presLink.link(from, to, attrs.getValue("preferredLabel"), order, use, priority);
				}
				else if(lowerCaseLocalName.equals("labelarc")){
					LabelLink labelLink = (LabelLink)elementStack.peek();
					labelLink.link(from, to, arcrole);
				}
				else if(lowerCaseLocalName.equals("footnotearc")){
					FootnoteLink footnotelink = (FootnoteLink)elementStack.peek();
					footnotelink.addFootnoteArc(from, to, order);
				}
			}
		}
		catch(Exception e){
			throw new XLinkException("<startArc> Error linking arc", e);
		}
	}

	@Override
	public void endArc(String namespaceURI, String sName, String qName)
			throws XLinkException {
		
	}

	@Override
	public void error(String namespaceURI, String lName, String qName, Attributes attrs, String message) throws XLinkException {
		
	}

	@Override
	public void warning(String namespaceURI, String lName, String qName, Attributes attrs, String message) throws XLinkException {
		
	}
	
	private XbrlTaxonomy getXbrlTaxonomyById(String namespace, String resourceId) throws XbrlException, IOException{
		//assume id will have format of <prefix>_<name>
		String name = resourceId.substring(resourceId.indexOf("_") + 1);
		XbrlTaxonomy taxonomy = getXbrlTaxonomyByNamespaceAndName(namespace, name);
		return taxonomy;
	}
	
	public XbrlTaxonomy getXbrlTaxonomyByNamespaceAndName(String namespace, String name) throws XbrlException, IOException{
		
		if(xbrlConceptCache.containsKey(XbrlTaxonomy.getIdentifier(namespace, name)))
			return xbrlConceptCache.get(XbrlTaxonomy.getIdentifier(namespace, name));
		
		XSElementDeclaration elementDec = (XSElementDeclaration)schemaElementDeclarations.itemByName(namespace, name);
		if(elementDec != null){
			XbrlTaxonomy xbrlTaxonomy = parseXbrlTaxonomy(elementDec);
			
			if(xbrlTaxonomy != null){
				xbrlConceptCache.put(xbrlTaxonomy.getIdentifier(), xbrlTaxonomy);
				return xbrlTaxonomy;
			}
		}
		else{
			//if taxonomy doesn't exist, probably means external schema is not imported.  Try loading from known ones
			return loadMissingTaxonomyFromKnownSchemas(namespace, name);
		}
		
		return null;
	}
	
	private XbrlTaxonomy loadMissingTaxonomyFromKnownSchemas(String namespace, String name) throws XbrlException, IOException{

		XSNamedMap elementDeclarations = null;
		if(missingSchemaElementDeclarations.containsKey(namespace))
			elementDeclarations = missingSchemaElementDeclarations.get(namespace);
		else if(knownSchemaMapping.containsKey(namespace)){
			XMLSchemaLoader schemaLoader = new XMLSchemaLoader();
			schemaLoader.setEntityResolver(loader);
			schemaLoader.setFeature("http://apache.org/xml/features/generate-synthetic-annotations", true);
			//URI targetLink = loader.getBaseURIResolver().getBaseURI().resolve(knownSchemaMapping.get(namespace));
			String cachedFilePath = this.loader.getFileCache().getCachedFilePath(knownSchemaMapping.get(namespace));
			String uri = new File(cachedFilePath).toURI().toString();
			XSModel xbrlSchemaModel = schemaLoader.loadURI(uri);
			schemaToNamespaceMap.put(knownSchemaMapping.get(namespace), namespace);
			
			elementDeclarations = xbrlSchemaModel.getComponents(XSConstants.ELEMENT_DECLARATION);
			if(elementDeclarations != null)
				missingSchemaElementDeclarations.put(namespace, elementDeclarations);
		}
		
		if(elementDeclarations != null){
			XSElementDeclaration elementDec = (XSElementDeclaration)elementDeclarations.itemByName(namespace, name);
			if(elementDec != null){
				XbrlTaxonomy xbrlTaxonomy = parseXbrlTaxonomy(elementDec);
				
				if(xbrlTaxonomy != null){
					xbrlConceptCache.put(xbrlTaxonomy.getIdentifier(), xbrlTaxonomy);
					return xbrlTaxonomy;
				}
			}
		}
		return null;
	}
	
	private XbrlTaxonomy parseXbrlTaxonomy(XSElementDeclaration elementDec) throws XbrlException{
		//should only contain the synthetic annotation for the attributes
		XSObjectList list = elementDec.getAnnotations();
		if(list.getLength() != 1)
			throw new XbrlException("element with ns: " + elementDec.getNamespace() + " name: " + elementDec.getName() + " has unexpected number of annotations:" + list.getLength());
		
		try{
			XSAnnotation attributeObj = (XSAnnotation)list.item(0);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			attributeObj.writeAnnotation(doc, XSAnnotation.W3C_DOM_DOCUMENT);
			NamedNodeMap attributes = doc.getFirstChild().getAttributes();
			XbrlTaxonomy xbrlTaxonomy = new XbrlTaxonomy(elementDec, attributes);
			return xbrlTaxonomy;
		}
		catch(Exception e){
			
		}
		return null;
	}
	
	private void parseXbrlSchemaAppInfo(XSObjectList annotationList) throws IOException, SAXException{
		for(int i = 0; i < annotationList.getLength(); i++){
			XSObject object = annotationList.item(i);
			//logger.info("annotation: " + ((XSAnnotation)object).getAnnotationString());
			
			((XSAnnotation)object).writeAnnotation(loader, XSAnnotation.SAX_CONTENTHANDLER);
			//((XSAnnotation)object).writeAnnotation(doc, XSAnnotation.W3C_DOM_DOCUMENT);
			
		}
	}

}
