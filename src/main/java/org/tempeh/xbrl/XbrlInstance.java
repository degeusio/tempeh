package org.tempeh.xbrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.tempeh.Util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class XbrlInstance {

	private final ListMultimap<String, IXbrlFact> factsByNamespaceAndLocalName = ArrayListMultimap.<String, IXbrlFact>create();
	private final Map<String, XbrlContext> contextMap = new HashMap<String, XbrlContext>();
	private final LinkedHashMap<String, CalculationLink> calculationLinks = new LinkedHashMap<String, CalculationLink>();
	private final LinkedHashMap<String, PresentationLink> presentationLinks = new LinkedHashMap<String, PresentationLink>();
	private final LinkedHashMap<String, DefinitionLink> definitionLinks = new LinkedHashMap<String, DefinitionLink>();
	private final Map<String, String> namespaceToPrefixMapping = new HashMap<String, String>();
	private final Map<String, XbrlRoleType> roleTypes = new HashMap<String, XbrlRoleType>();
	private final LinkedHashMap<String, LabelLink> labelLinks = new LinkedHashMap<String, LabelLink>();
	private final FootnoteLink footnoteLink = new FootnoteLink();;
	private String localNamespace;
	
	//do not serialize
	private final ListMultimap<String, IXbrlFact> factsByContextId = ArrayListMultimap.<String, IXbrlFact>create();
	private final Map<String, XbrlUnit> unitMap = new HashMap<String, XbrlUnit>();
	
	public XbrlInstance(){

	}

	public IXbrlFact addFact(String contextRef, XbrlTaxonomy xbrlTaxonomy, String id, String decimals, String precision,
				 String unitId, boolean isNil) throws XbrlException{

		if(id == null || id.isEmpty()){
			//if fact doesn't have id, give it a unique identifier
			id = xbrlTaxonomy.getIdentifier() + contextRef + System.currentTimeMillis();
		}
		
		IXbrlFact fact = null;
		if(unitId != null)
			fact = new XbrlNumericFact(xbrlTaxonomy, id, unitId, decimals, precision, isNil);
		else
			fact = new XbrlTextFact(xbrlTaxonomy, id, isNil);

		factsByNamespaceAndLocalName.put(xbrlTaxonomy.getIdentifier(), fact);
		factsByContextId.put(contextRef, fact);
		return fact;
	}
	
	public void addNamespaceToPrefixMapping(String prefix, String namespace){
		namespaceToPrefixMapping.put(namespace, prefix);
	}

	public void addContext(XbrlContext context){
		contextMap.put(context.getId(), context);
	}
	
	public void addUnit(XbrlUnit unit){
		unitMap.put(unit.getId(), unit);
	}

	public FootnoteLink getFootnoteLink() {
		return footnoteLink;
	}
	
	public Map<String, CalculationLink> getCalculationLinks() {
		return calculationLinks;
	}

	public Map<String, PresentationLink> getPresentationLinks() {
		return presentationLinks;
	}

	public Map<String, DefinitionLink> getDefinitionLinks() {
		return definitionLinks;
	}

	public Map<String, XbrlRoleType> getRoleTypes() {
		return roleTypes;
	}

	public Map<String, LabelLink> getLabelLinks() {
		return labelLinks;
	}

	public XbrlContext getContext(String contextId){
		if(contextMap.containsKey(contextId))
			return contextMap.get(contextId);
		
		return null;
	}
	
	public String getNamespaceFromPrefix(String prefix){
		for(String namespace : namespaceToPrefixMapping.keySet()){
			String p = namespaceToPrefixMapping.get(namespace);
			if(p.equals(prefix))
				return namespace;
		}
		return null;
	}
	
	public List<String> getAllNamespaces(){
		return new ArrayList<String>(namespaceToPrefixMapping.keySet());
	}
	
	public String getQualifiedName(String namespace, String localName){
		if(namespaceToPrefixMapping.containsKey(namespace))
			return Util.getQualifiedName(namespaceToPrefixMapping.get(namespace), localName);
		else
			return localName;
	}
	
	public List<IXbrlFact> getFactsByNamespaceAndName(String namespaceURI, String localName){
		String key = XbrlTaxonomy.getIdentifier(namespaceURI, localName);
		if(factsByNamespaceAndLocalName.containsKey(key))
			return factsByNamespaceAndLocalName.get(key);

		return null;
	}
	
	public List<IXbrlFact> getAllFacts(){
		return new ArrayList<IXbrlFact>(factsByNamespaceAndLocalName.values());
	}
	
	//call only when you cannot find the preferred label nor the default label.  Last effort
	public XbrlLabel getWhateverLabel(String identifier){
		for(LabelLink link : labelLinks.values()){
			XbrlLabel label = link.getAnyLabel(identifier);
			if(label != null)
				return label;
		}
		
		return null;
	}
	
	public XbrlLabel getDocumentationLabel(String identifier){
		for(LabelLink link : labelLinks.values()){
			XbrlLabel label = link.getDocumentationLabel(identifier);
			if(label != null)
				return label;
		}
		
		return null;
	}
	
	public String getLocalNamespace() {
		return localNamespace;
	}

	public void setLocalNamespace(String localNamespace) {
		this.localNamespace = localNamespace;
	}

	public XbrlLabel getLabel(String identifier, String labelRole){
		
		for(LabelLink link : labelLinks.values()){
			XbrlLabel label = link.getLabel(identifier, labelRole);
			if(label != null)
				return label;
		}

		//logger.error("No label links found");
		return null;
	}
	
	public void verifyInstance() throws XbrlException{
		
		if(this.localNamespace == null || this.localNamespace.isEmpty())
			throw new XbrlException("Local namespace cannot be null or empty");
		
		for(String contextId : factsByContextId.keySet()){
			XbrlContext context = contextMap.get(contextId);
			if(context == null)
				throw new XbrlException("Context id: " + contextId + " not found");
			
			List<IXbrlFact> facts = factsByContextId.get(contextId);
			for(IXbrlFact fact : facts){
				fact.setContext(context);
				if(fact instanceof XbrlNumericFact){
					XbrlNumericFact nFact = (XbrlNumericFact)fact;
					XbrlUnit unit = unitMap.get(nFact.getUnitId());
					if(unit == null)
						throw new XbrlException("unit id: " + nFact.getUnitId() + " not found for fact:" + fact.getId());
					
					nFact.setUnit(unit);
				}
			}
		}
		
	}
	
}
