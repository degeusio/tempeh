package org.tempeh.xbrl.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.Pair;
import org.tempeh.TempehException;
import org.tempeh.Util;
import org.tempeh.data.AbstractCellPosition;
import org.tempeh.data.TaxonomyPosition;
import org.tempeh.xbrl.DefinitionLink;
import org.tempeh.xbrl.IXbrlFact;
import org.tempeh.xbrl.NamespaceResolver;
import org.tempeh.xbrl.PresentationLink;
import org.tempeh.xbrl.XbrlContext;
import org.tempeh.xbrl.XbrlInstance;
import org.tempeh.xbrl.XbrlLabel;
import org.tempeh.xbrl.XbrlPeriod;
import org.tempeh.xbrl.XbrlPeriod.PeriodType;
import org.tempeh.xbrl.XbrlSegment;
import org.tempeh.xbrl.XbrlTaxonomy;
import org.tempeh.xbrl.metadata.DomainMemberCollection;
import org.tempeh.xbrl.metadata.PresentationMemberLink;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

abstract class XbrlReport {

	protected final XbrlInstance instance;
	private Util util = new Util();

	public XbrlReport(XbrlInstance instance){
		this.instance = instance;
	}
	
	public void buildReport() throws Exception{
		//do nothing, child class can override
	}
	
	public void buildPresentationLink(PresentationLink presentationLink) throws Exception{
		//do nothing, child class can override
	}
	
	public boolean willHandlePresentation(PresentationLink presentationLink) throws Exception{
		return false;  //default, do not handle
	}
	
	protected String getPreferredLabelForTaxonomy(String taxonomyIdentifier, XbrlLabel defaultLabel){
		
		XbrlLabel label = instance.getWhateverLabel(taxonomyIdentifier);
		
		if(label == null)
			label = defaultLabel;
		
		
		if(label != null)
			return label.getCleanUpLabelValue();
		
		return null;
	}
	
	protected String getQualifiedTypeName(String securityIdentifier, String localNamespace, String namespace, String name) throws TempehException{

		if(namespace.equals(localNamespace))
			return Util.getQualifiedName(securityIdentifier, name);
		else if(NamespaceResolver.isUsGaapNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.US_GAAP_PREFIX, name);
		else if(NamespaceResolver.isDocumentAndEntityInfoNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.DOC_ENTITY_INFO_PREFIX, name);
		else if(NamespaceResolver.isCountryNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.COUNTRY_PREFIX, name);
		else if(NamespaceResolver.isInvestNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.INVEST_PREFIX, name);
		else if(NamespaceResolver.isStateOrProvinceNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.STATE_OR_PROVINCE_PREFIX, name);
		else if(NamespaceResolver.isCurrencyNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.CURRENCY_PREFIX, name);
		else if(NamespaceResolver.isNaicsNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.NAICS_PREFIX, name);
		else if(NamespaceResolver.isSicNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.SIC_PREFIX, name);
		else if(NamespaceResolver.isExchNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.EXCH_PREFIX, name);
		else if(NamespaceResolver.isUSTypeNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.US_TYPES_PREFIX, name);
		else if(NamespaceResolver.isXBRLNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.XBRL_PREFIX, name);
		else if(NamespaceResolver.isNonNumericNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.NON_NUMERIC_PREFIX, name);
		else if(NamespaceResolver.isNumericNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.NUMERIC_PREFIX, name);
		else if(NamespaceResolver.isRRNamespace(namespace))
			return Util.getQualifiedName(NamespaceResolver.RISK_RETURN_PREFIX, name);
		
		throw new TempehException("Unrecognized namespace:" + namespace + " for name:" + name);
	}
	
	/*
	protected XbrlMeta getXbrlMeta(String securityIdentifier, String type, String dataJson) 
			throws UnsupportedEncodingException, GadoGadoException, IOException{
		String key = EntityKey.getXbrlMetaKey(securityIdentifier, type, this.builder.getAccessId());
		
		XbrlMeta meta = new XbrlMeta();
		meta.setKey(key);
		meta.setSecurityIdentifier(securityIdentifier);
		meta.setType(type);
		meta.setLastUpdated(System.currentTimeMillis());
		meta.setDataJson(new Text(dataJson));
		
		return meta;
	}*/
	
	private void recursivelyGetAllFactsByContext(
			PresentationLink presLink, 
			PresentationMemberLink presItem, 
			Map<String, Set<IXbrlFact>> factsByContextId,
			List<Pair<XbrlTaxonomy, Integer>> lineItemAndLevel,
			int level){
		
		lineItemAndLevel.add(Pair.of(presItem.getXbrlTaxonomy(), level));
		
		if(!presItem.getXbrlTaxonomy().isAbstract()){
			
			List<IXbrlFact> facts = instance.getFactsByNamespaceAndName(presItem.getXbrlTaxonomy().getNamespace(), presItem.getXbrlTaxonomy().getName());
			if(facts != null){
				for(IXbrlFact fact : facts){
					Set<IXbrlFact> factSet = null;
					if(factsByContextId.containsKey(fact.getContext().getId()))
						factSet = factsByContextId.get(fact.getContext().getId());
					else{
						factSet = new HashSet<IXbrlFact>();
						factsByContextId.put(fact.getContext().getId(), factSet);
					}
					factSet.add(fact);
					//factsByContextId.put(fact.getContext().getId(), fact);
				}
			}
		}
		
		DomainMemberCollection lineItems = presLink.getPresentationMemberCollectionIfExist(presItem.getXbrlTaxonomy());
		if(lineItems != null){
			for(int i = 0; i < lineItems.getDomainMembers().size(); i++){
				PresentationMemberLink childItem = (PresentationMemberLink)lineItems.getDomainMembers().get(i);
				recursivelyGetAllFactsByContext(presLink, childItem, factsByContextId, lineItemAndLevel, level + 1);
			}
		}
	}

	//get rid of duplicates + non-line item taxonomies
	//after removing non-line item taxonomies, need to rebase level to 0
	private List<AbstractCellPosition> getTaxonomyPositions(List<Pair<XbrlTaxonomy, Integer>> lineItemAndLevel){
		List<AbstractCellPosition> taxonomyPositions = new ArrayList<AbstractCellPosition>();
		List<XbrlTaxonomy> uniqueTaxonomies = new ArrayList<XbrlTaxonomy>();
		
		int minLevel = Integer.MAX_VALUE;
		for(Pair<XbrlTaxonomy, Integer> pair : lineItemAndLevel){
			
			if(pair.getLeft().isHyperCubeItem() || 
					pair.getLeft().isDimensionItem() || 
					pair.getLeft().isDomainItem() || 
					(pair.getLeft().getName().equals("StatementLineItems") && NamespaceResolver.isUsGaapNamespace(pair.getLeft().getNamespace())))
				continue;
			
			if(!uniqueTaxonomies.contains(pair.getLeft())){
				uniqueTaxonomies.add(pair.getLeft());
				taxonomyPositions.add(new TaxonomyPosition(pair.getLeft(), pair.getRight()));
				
				if(pair.getRight() < minLevel)
					minLevel = pair.getRight();
			}
		}
		
		//rebase level to 0
		for(AbstractCellPosition pos : taxonomyPositions){
			TaxonomyPosition taxPos = (TaxonomyPosition)pos;
			taxPos.setLevel(taxPos.getLevel() - minLevel);
		}
		return taxonomyPositions;
	}
	
	protected FactTable buildPresentationItem(PresentationLink presLink, List<DomainMemberCollection> presRoots){
		
		//ListMultimap<String, IXbrlFact> factsByContextId = ArrayListMultimap.<String, IXbrlFact>create();
		Map<String, Set<IXbrlFact>> factsByContextId = new HashMap<String, Set<IXbrlFact>>();
		List<Pair<XbrlTaxonomy, Integer>> lineItemAndLevel = new ArrayList<Pair<XbrlTaxonomy, Integer>>();
		
		for(DomainMemberCollection root : presRoots){
			for(int i = 0; i < root.getDomainMembers().size(); i++){
				PresentationMemberLink childItem = (PresentationMemberLink)root.getDomainMembers().get(i);
				recursivelyGetAllFactsByContext(presLink, childItem, factsByContextId, lineItemAndLevel, 0);
			}
		}

		
		//List<XbrlTaxonomy> membersInThisPres = null;
		DefinitionLink defLink = instance.getDefinitionLinks().get(presLink.getRole());
		
		List<AbstractCellPosition> taxonomyPositions = getTaxonomyPositions(lineItemAndLevel);
		FactTable factTable = new FactTable();
		factTable.setTaxonomyPositions(taxonomyPositions);
		
		for(String contextId : factsByContextId.keySet()){
			XbrlContext context = instance.getContext(contextId);
			
			//should not happen
			if(context == null){
				continue;
			}
			
			boolean validContext = false;
			if(context.getSegments().size() == 0){ //always include non-segmented facts
				validContext = true;
			}
			else if(context.getSegments().size() > 0 && defLink != null){
				boolean allSegmentsDefined = true;
				for(XbrlSegment segment : context.getSegments()){
					String dimensionIdentifier = XbrlTaxonomy.getIdentifier(segment.getDimensionNamespace(), segment.getDimensionName());
					String memberIdentifier = XbrlTaxonomy.getIdentifier(segment.getMemberNamespace(), segment.getMemberName());
					
					if(!defLink.isContainSegment(dimensionIdentifier, memberIdentifier)){
						allSegmentsDefined = false;
						break;
					}
				}
				validContext = allSegmentsDefined;
			}
			
			if(validContext){
				factTable.addFacts(context, factsByContextId.get(contextId));
			}
		}
		
		return factTable;
	}
	
	protected static class FactTable{
		private ListMultimap<XbrlContext, IXbrlFact> factsByContext = ArrayListMultimap.<XbrlContext, IXbrlFact>create();
		private List<AbstractCellPosition> taxonomyPositions;
		
		public FactTable(){
			
		}

		public List<AbstractCellPosition> getTaxonomyPositions() {
			return taxonomyPositions;
		}

		public void setTaxonomyPositions(List<AbstractCellPosition> taxonomyPositions) {
			this.taxonomyPositions = taxonomyPositions;
		}

		public void addFacts(XbrlContext context, Collection<IXbrlFact> facts){
			factsByContext.putAll(context, facts);
		}
		
		public ListMultimap<XbrlContext, IXbrlFact> getFactsByContext() {
			return factsByContext;
		}
		
		public void addFactTable(FactTable table){
			for(XbrlContext context : table.getFactsByContext().keySet()){
				if(!factsByContext.containsKey(context))
					factsByContext.putAll(context, table.getFactsByContext().get(context));
			}
			
			
			for(AbstractCellPosition pos : table.getTaxonomyPositions()){
				TaxonomyPosition taxPos = (TaxonomyPosition)pos;
				if(!alreadyContainTaxonomy(taxPos.getXbrlTaxonomy())){
					if(taxonomyPositions == null)
						taxonomyPositions = new ArrayList<AbstractCellPosition>();
					
					taxonomyPositions.add(pos);
				}
			}
		}
		
		private boolean alreadyContainTaxonomy(XbrlTaxonomy taxonomy){
			if(taxonomyPositions == null)
				return false;
			
			for(AbstractCellPosition pos : taxonomyPositions){
				TaxonomyPosition taxPos = (TaxonomyPosition)pos;
				if(taxPos.getXbrlTaxonomy().equals(taxonomy))
					return true;
			}
			
			return false;
		}
	}
	
}
