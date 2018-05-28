package org.tempeh.xbrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.tempeh.xbrl.metadata.DomainMemberCollection;
import org.tempeh.xbrl.metadata.DomainMemberLink;
import org.tempeh.xbrl.metadata.SummationMemberLink;

public class CalculationLink extends ExtendedLink{
	
	private final List<DomainMemberCollection> summationCache = new ArrayList<DomainMemberCollection>();
	private final Map<String, DomainMemberCollection> fromLabelToDomainMemberMap = new LinkedHashMap<String, DomainMemberCollection>(); //no need to serialize, used temporarily during parsing
	
	public CalculationLink(String role, String title){
		super(role, title);
	}
	
	//for serialization
	private CalculationLink(){}
	
	public void link(String fromLabel, String toLabel, String arcRole, Double order, String use, Integer priority, double weight) throws XbrlException{
		if(arcRole.equals(XbrlConstants.CalculationArcrole)){
			/*
			XbrlTaxonomy fromResource = resourceByLabelCache.get(fromLabel);
			XbrlTaxonomy toResource = resourceByLabelCache.get(toLabel);
			
			if(fromResource == null || toResource == null)
				throw new XbrlException("unable to complete calculation arc from: " + fromLabel  + " toL: " + toLabel + " because one of the link is not found");*/
			
			DomainMemberCollection collection = null;
			if(fromLabelToDomainMemberMap.containsKey(fromLabel))
				collection = fromLabelToDomainMemberMap.get(fromLabel);
			else{
				collection = new DomainMemberCollection(fromLabel);
				fromLabelToDomainMemberMap.put(fromLabel, collection);
			}
			
			collection.addDomainMember(new SummationMemberLink(toLabel, order, use, priority, weight));
		}
	}

	//called after extended link is complete
	public void verifyCalculations() throws XbrlException{
		for(String fromLabel : fromLabelToDomainMemberMap.keySet()){
			List<XbrlTaxonomy> fromResources = getResourceByLabel(fromLabel);
			
			if(fromResources == null || fromResources.size() == 0)
				throw new XbrlException("Cannot find resource: " + fromLabel + " from calculation link:" + getRole());

			
			XbrlTaxonomy fromResource = fromResources.get(0);
			DomainMemberCollection collection = fromLabelToDomainMemberMap.get(fromLabel);
			collection.setParentXbrlTaxonomy(fromResource);
			
			for(DomainMemberLink link : collection.getDomainMembers()){
				List<XbrlTaxonomy> toResources = getResourceByLabel(link.getTaxonomyLabel());
				if(toResources == null || toResources.size() == 0)
					throw new XbrlException("Cannot find resource: " + link.getTaxonomyLabel() + " from calculation link:" + getRole());
				
				link.setXbrlTaxonomy(toResources.get(0));
			}
			
			//possible that collection for fromResource already exist.  If so, add it to
			DomainMemberCollection existingCollection = null;
			for(DomainMemberCollection col : summationCache){
				if(col.getParentXbrlTaxonomy().equals(fromResource)){
					existingCollection = col;
					break;
				}
			}
			
			if(existingCollection != null){
				for(DomainMemberLink link : collection.getDomainMembers()){
					existingCollection.addDomainMember(link);
				}
			}
			else
				summationCache.add(collection);
		}
		
		//clear these in case another extended link with same role is used again, the labels can duplicate
		fromLabelToDomainMemberMap.clear();
		clearResources();
	}
	
	public List<DomainMemberCollection> getAllSummationLinks(){
		return summationCache;
	}
	
	public boolean isTaxonomyASum(XbrlTaxonomy taxonomy){
		for(DomainMemberCollection collection : summationCache){
			if(collection.getParentXbrlTaxonomy().equals(taxonomy))
				return true;
		}
		return false;
	}
	
	public double getWeightForTaxonomy(XbrlTaxonomy taxonomy){
		for(DomainMemberCollection collection : summationCache){
			for(int i = 0; i < collection.getDomainMembers().size(); i++){
				SummationMemberLink link = (SummationMemberLink)collection.getDomainMembers().get(i);
				if(link.getXbrlTaxonomy().equals(taxonomy))
					return link.getWeight();
			}
		}
		
		return 1;
	}

}
