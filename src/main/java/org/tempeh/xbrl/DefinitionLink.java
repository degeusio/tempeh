package org.tempeh.xbrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tempeh.xbrl.metadata.DimensionDomainLink;
import org.tempeh.xbrl.metadata.DomainMemberCollection;
import org.tempeh.xbrl.metadata.DomainMemberLink;

public class DefinitionLink extends ExtendedLink {
	
	private final Map<String, DomainMemberCollection> definitionCache = new HashMap<String, DomainMemberCollection>();
	private final Set<String> primaryItemIdentifiers = new LinkedHashSet<String>();
	private final Map<String, DomainMemberCollection> fromLabelToDomainMemberMap = new LinkedHashMap<String, DomainMemberCollection>(); //no need to serialize, used temporarily during parsing
	private final Set<String> primaryItemLabels = new LinkedHashSet<String>(); //no need to serialize, used temporarily during parsing

	public static final String ALL_ARCROLE = "http://xbrl.org/int/dim/arcrole/all";
	public static final String NOT_ALL_ARCROLE = "http://xbrl.org/int/dim/arcrole/notAll";
	public static final String DIMENSION_DOMAIN_ARCROLE = "http://xbrl.org/int/dim/arcrole/dimension-domain";
	public static final String DIMENSION_DEFAULT_ARCROLE = "http://xbrl.org/int/dim/arcrole/dimension-default";
	public static final String DOMAIN_MEMBER_ARCROLE = "http://xbrl.org/int/dim/arcrole/domain-member";
	public static final String HYPERCUBE_DIMENSION_ARCROLE = "http://xbrl.org/int/dim/arcrole/hypercube-dimension";
	public static final String GENERAL_SPECIAL_ARCROLE = "http://www.xbrl.org/2003/arcrole/general-special";
	
	public DefinitionLink(String role, String title){
		super(role, title);
	}
	
	//for serialization
	private DefinitionLink(){}
	
	public void link(String fromLabel, String toLabel, String arcRole, Double order, String use, Integer priority) throws XbrlException{
		/*
		XbrlTaxonomy fromResource = resourceByLabelCache.get(fromLabel);
		XbrlTaxonomy toResource = resourceByLabelCache.get(toLabel);
		
		assert fromResource != null && toResource != null;*/
		if(arcRole.equals(ALL_ARCROLE) || arcRole.equals(NOT_ALL_ARCROLE)){
			//definition link should not have multiple ALL_ARCROLE/NOT_ALL_ARCROLE (not sure)
			//assert definitionCache.containsKey(ALL_ARCROLE) == false;
			//assert definitionCache.containsKey(NOT_ALL_ARCROLE) == false;
			
			DomainMemberCollection primaryItem = null;
			if(fromLabelToDomainMemberMap.containsKey(fromLabel))
				primaryItem = fromLabelToDomainMemberMap.get(fromLabel);
			else{
				primaryItem = new DomainMemberCollection(fromLabel);
				fromLabelToDomainMemberMap.put(fromLabel, primaryItem);
			}
			
			primaryItem.addDomainMember(new DomainMemberLink(toLabel, order, use, priority));
			primaryItemLabels.add(fromLabel);
			//definitionCache.put(arcRole, primaryItem);
		}
		else if(arcRole.equals(HYPERCUBE_DIMENSION_ARCROLE)){
			DomainMemberCollection dimensions = null;
			if(fromLabelToDomainMemberMap.containsKey(fromLabel))
				dimensions = fromLabelToDomainMemberMap.get(fromLabel);
			else{
				dimensions = new DomainMemberCollection(fromLabel);
				fromLabelToDomainMemberMap.put(fromLabel, dimensions);
			}
			dimensions.addDomainMember(new DomainMemberLink(toLabel, order, use, priority));
		}
		else if(arcRole.equals(DIMENSION_DEFAULT_ARCROLE) || arcRole.equals(DIMENSION_DOMAIN_ARCROLE)){
			DomainMemberCollection members = null;
			if(fromLabelToDomainMemberMap.containsKey(fromLabel))
				members = fromLabelToDomainMemberMap.get(fromLabel);
			else{
				members = new DomainMemberCollection(fromLabel);
				fromLabelToDomainMemberMap.put(fromLabel, members);
			}
			
			if(arcRole.equals(DIMENSION_DEFAULT_ARCROLE))
				members.addDomainMember(new DimensionDomainLink(toLabel, order, use, priority, true));
			else
				members.addDomainMember(new DimensionDomainLink(toLabel, order, use, priority, false));
		}
		else if(arcRole.equals(DOMAIN_MEMBER_ARCROLE) || arcRole.equals(GENERAL_SPECIAL_ARCROLE)){
			DomainMemberCollection members = null;
			if(fromLabelToDomainMemberMap.containsKey(fromLabel))
				members = fromLabelToDomainMemberMap.get(fromLabel);
			else{
				members = new DomainMemberCollection(fromLabel);
				fromLabelToDomainMemberMap.put(fromLabel, members);
			}
			members.addDomainMember(new DomainMemberLink(toLabel, order, use, priority));
		}
		else
			throw new XbrlException("Do not recognize arcRole: " + arcRole + " for definition link");
	}
	
	//called after extended link is complete
	public void verifyDefinitions() throws XbrlException{
		for(String fromLabel : fromLabelToDomainMemberMap.keySet()){
			List<XbrlTaxonomy> fromResources = getResourceByLabel(fromLabel);
			
			if(fromResources == null || fromResources.size() == 0)
				throw new XbrlException("Cannot find resource: " + fromLabel + " from defition link:" + getRole());
			
			XbrlTaxonomy fromResource = fromResources.get(0);
			DomainMemberCollection collection = fromLabelToDomainMemberMap.get(fromLabel);
			collection.setParentXbrlTaxonomy(fromResource);
			
			if(primaryItemLabels.contains(fromLabel))
				primaryItemIdentifiers.add(fromResource.getIdentifier());
			
			Map<XbrlTaxonomy, DomainMemberLink> uniqueLinksByXbrlTaxonomy = new LinkedHashMap<XbrlTaxonomy, DomainMemberLink>();
			List<DomainMemberLink> linksToRemove = new ArrayList<DomainMemberLink>();
			for(DomainMemberLink link : collection.getDomainMembers()){
				List<XbrlTaxonomy> toResources = getResourceByLabel(link.getTaxonomyLabel());
				if(toResources == null || toResources.size() == 0)
					throw new XbrlException("Cannot find resource: " + link.getTaxonomyLabel() + " from definition link:" + getRole());
				
				XbrlTaxonomy toResource = toResources.get(0);
				if(uniqueLinksByXbrlTaxonomy.containsKey(toResource)){
					DomainMemberLink existingLink = uniqueLinksByXbrlTaxonomy.get(toResource);
					if(link instanceof DimensionDomainLink && ((DimensionDomainLink)link).isDefault()){
						uniqueLinksByXbrlTaxonomy.put(toResource, link); //when there is duplicates, we want the one marked default
						linksToRemove.add(existingLink);
					}
					else
						linksToRemove.add(link);
				}
				else
					uniqueLinksByXbrlTaxonomy.put(toResource, link);
				
				link.setXbrlTaxonomy(toResource);
			}
			
			if(linksToRemove.size() > 0)
				collection.getDomainMembers().removeAll(linksToRemove);
			
			//possible that collection for fromResource already exist.  If so, add to it
			if(definitionCache.containsKey(fromResource.getIdentifier())){
				DomainMemberCollection existingCollection = definitionCache.get(fromResource.getIdentifier());
				for(DomainMemberLink link : collection.getDomainMembers()){
					existingCollection.addDomainMember(link);
				}
			}
			else
				definitionCache.put(fromResource.getIdentifier(), collection);
		}
		
		//clear these in case another extended link with same role is used again, the labels can duplicate
		fromLabelToDomainMemberMap.clear();
		clearResources();
		primaryItemLabels.clear();
	}
	
	public String getPrimaryItemIdentifier(){;
		
		if(primaryItemIdentifiers.size() == 1)
			return primaryItemIdentifiers.iterator().next();
		
		return null;
	}
	
	private void recursivelyGetAllDomainMembersForCube(DomainItem domain, DomainMemberCollection domainCol){
		
		for(DomainMemberLink memberLink : domainCol.getDomainMembers()){
			
			//logger.info("MEMBER - " + memberLink.getXbrlTaxonomy().getIdentifier());
			DomainItem item = new DomainItem(memberLink.getXbrlTaxonomy());
			domain.getChildren().add(item);
		
			if(definitionCache.containsKey(memberLink.getXbrlTaxonomy().getIdentifier())){
				recursivelyGetAllDomainMembersForCube(item, definitionCache.get(memberLink.getXbrlTaxonomy().getIdentifier()));
			}
		}
	}
	
	public List<Cube> getHyperCubes(){
		//ListMultimap<String, XbrlDomain> tableDomains = ArrayListMultimap.<String, XbrlDomain>create();
		List<Cube> cubeList = new ArrayList<Cube>();
		
		for(String primaryItemIdentifier : primaryItemIdentifiers){
			DomainMemberCollection primaryItem = definitionCache.get(primaryItemIdentifier);
			
			//hypercube should be contain within primary item
			for(DomainMemberLink link : primaryItem.getDomainMembers()){
				if(link.getXbrlTaxonomy().isHyperCubeItem() && definitionCache.containsKey(link.getXbrlTaxonomy().getIdentifier())){
					//logger.info("CUBE - " + link.getXbrlTaxonomy().getIdentifier());
					DomainMemberCollection cubeCol = definitionCache.get(link.getXbrlTaxonomy().getIdentifier());
					
					Cube cube = new Cube(cubeCol.getParentXbrlTaxonomy().getNamespace(), cubeCol.getParentXbrlTaxonomy().getName());
					//dimensions should be contain within hypercube
					for(DomainMemberLink dimLink : cubeCol.getDomainMembers()){
						if(dimLink.getXbrlTaxonomy().isDimensionItem() && definitionCache.containsKey(dimLink.getXbrlTaxonomy().getIdentifier())){
							//logger.info("DIM - " + dimLink.getXbrlTaxonomy().getIdentifier());
							DomainMemberCollection dimCol = definitionCache.get(dimLink.getXbrlTaxonomy().getIdentifier());
							
							Dimension dim = new Dimension(dimCol.getParentXbrlTaxonomy());
							cube.getDimensions().add(dim);
							//domains should be contain within dimension
							for(DomainMemberLink domainLink : dimCol.getDomainMembers()){
								if(domainLink.getXbrlTaxonomy().isDomainItem()){
									//logger.info("DOMAIN - " + domainLink.getXbrlTaxonomy().getIdentifier());
									DomainItem item = new DomainItem(domainLink.getXbrlTaxonomy());
									dim.getDomains().add(item);
									if(definitionCache.containsKey(domainLink.getXbrlTaxonomy().getIdentifier()))
										recursivelyGetAllDomainMembersForCube(item, definitionCache.get(domainLink.getXbrlTaxonomy().getIdentifier()));
								}
							}
							
						}
					}
					
					cubeList.add(cube);
				}
			}
		}
		
		return cubeList;
	}
	
	private void recursivelyGetAllDomainMembers(DomainMemberCollection collection, List<XbrlTaxonomy> memberTaxonomies){
		
		if(collection == null)
			return;
		
		for(DomainMemberLink link : collection.getDomainMembers()){
			if(link.getXbrlTaxonomy().isDomainItem()){
				memberTaxonomies.add(link.getXbrlTaxonomy());
			}
			
			if(definitionCache.containsKey(link.getXbrlTaxonomy().getIdentifier()))
				recursivelyGetAllDomainMembers(definitionCache.get(link.getXbrlTaxonomy().getIdentifier()), memberTaxonomies);
		}
	}

	public List<XbrlTaxonomy> getAllMembersForDimension(String dimensionIdentifier){
		List<XbrlTaxonomy> members = new ArrayList<XbrlTaxonomy>();
		
		if(definitionCache.containsKey(dimensionIdentifier)){
			DomainMemberCollection dimensionCollection = definitionCache.get(dimensionIdentifier);
			recursivelyGetAllDomainMembers(dimensionCollection, members);
		}
		
		return members;
	}
	
	private boolean isDimensionContainMember(DomainMemberCollection dimensionCollection, String memberIdentifier){
		
		if(dimensionCollection == null)
			return false;
		
		for(DomainMemberLink link : dimensionCollection.getDomainMembers()){
			boolean found = false;
			if(definitionCache.containsKey(link.getXbrlTaxonomy().getIdentifier()))
				found = isDimensionContainMember(definitionCache.get(link.getXbrlTaxonomy().getIdentifier()), memberIdentifier);
			
			if(found || link.getXbrlTaxonomy().getIdentifier().equals(memberIdentifier))
				return true;
		}
		
		return false;
	}

	public boolean isContainSegment(String dimensionIdentifier, String memberIdentifier){
		if(definitionCache.containsKey(dimensionIdentifier)){
			return isDimensionContainMember(definitionCache.get(dimensionIdentifier), memberIdentifier);
		}
		
		return false;
	}
	
	public static class Cube{
		public String ns;
		public String name;
		private List<Dimension> dimensions = new ArrayList<Dimension>();
		
		public Cube(String ns, String name){
			this.ns = ns;
			this.name = name;
		}

		public List<Dimension> getDimensions() {
			return dimensions;
		}
		
		
	}
	
	public static class Dimension{
		public XbrlTaxonomy tax;
		private List<DomainItem> domains = new ArrayList<DomainItem>();
		
		public Dimension(XbrlTaxonomy tax){
			this.tax = tax;
		}

		public List<DomainItem> getDomains() {
			return domains;
		}
		
	}
	
	public static class DomainItem{
		public XbrlTaxonomy tax;
		private List<DomainItem> children = new ArrayList<DomainItem>();
		
		public DomainItem(XbrlTaxonomy tax){
			this.tax = tax;
		}

		public List<DomainItem> getChildren() {
			return children;
		}
		
	}

}

