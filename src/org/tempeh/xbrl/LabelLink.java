package org.tempeh.xbrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class LabelLink extends ExtendedLink{

	private final ListMultimap<String, String> resourceToLabelMapping = ArrayListMultimap.<String, String>create();
	private final ListMultimap<String, XbrlLabel> labelCache = ArrayListMultimap.<String, XbrlLabel>create();  //Multiple labels can have same labelId
	private final ListMultimap<String, String> fromLabelToLabelMapping = ArrayListMultimap.<String, String>create();  //no need to serialize, used temporarily during parsing
	public LabelLink(String role, String title){
		super(role, title);
	}
	
	//for serialization
	private LabelLink(){}
	
	public void addLabel(XbrlLabel label){
		labelCache.put(label.getLabelId(), label);
	}
	
	public void link(String fromLabel, String toLabel, String arcRole) throws XbrlException{
		fromLabelToLabelMapping.put(fromLabel, toLabel);
	}
	
	//called after extended link is complete
	public void verifyLabels() throws XbrlException{
		for(String fromLabel : fromLabelToLabelMapping.keySet()){
			List<XbrlTaxonomy> fromResources = getResourceByLabel(fromLabel);
			if(fromResources == null || fromResources.size() == 0){
				throw new XbrlException("Unable to find resource for:" + fromLabel);
			}
			
			List<String> toLabels = fromLabelToLabelMapping.get(fromLabel);
			for(String toLabel : toLabels){
				if(labelCache.containsKey(toLabel)){
					for(XbrlTaxonomy res : fromResources){
						resourceToLabelMapping.put(res.getIdentifier(), toLabel);
					}
				}
				else{
					throw new XbrlException("No labels found for: " + toLabel);
				}
			}
		}
		
		//clear these in case another extended link with same role is used again, the labels can duplicate
		fromLabelToLabelMapping.clear();
		clearResources();
	}
	
	public Set<String> getAllTaxonomyIdentifiers(){
		return resourceToLabelMapping.keySet();
	}
	
	public XbrlLabel getLabel(String identifier, String labelRole){
		if(resourceToLabelMapping.containsKey(identifier)){
			List<XbrlLabel> labels = getLabels(identifier);
			if(labels != null){
				for(XbrlLabel label : labels){
					if(label.getLabelRole().equals(labelRole)){
						return label;
					}
				}
			}
		}
		
		return null;
	}
	
	public XbrlLabel getDocumentationLabel(String identifier){
		if(resourceToLabelMapping.containsKey(identifier)){
			List<XbrlLabel> labels = getLabels(identifier);

			if(labels != null && labels.size() > 0){
				//see if any generic ones exist
				for(XbrlLabel label : labels){
					if(label.getLabelRole().endsWith("/role/documentation")){
						return label;
					}
				}
			}
		}
		
		return null;
	}
	
	public XbrlLabel getAnyLabel(String identifier){
		if(resourceToLabelMapping.containsKey(identifier)){
			List<XbrlLabel> labels = getLabels(identifier);

			if(labels != null && labels.size() > 0){
				//see if any generic ones exist
				for(XbrlLabel label : labels){
					if(label.getLabelRole().endsWith("/role/label")){
						return label;
					}
				}
				
				//see if any terse ones exist
				for(XbrlLabel label : labels){
					if(label.getLabelRole().endsWith("/role/terseLabel")){
						return label;
					}
				}
				
				//see if any terse ones exist
				for(XbrlLabel label : labels){
					if(label.getLabelRole().endsWith("/role/verboseLabel")){
						return label;
					}
				}
				
				//if not, just return the first one if any exist
				return labels.get(0);
			}
		}
		
		return null;
	}
	
	public List<XbrlLabel> getLabels(String identifier){
		List<XbrlLabel> allLabels = new ArrayList<XbrlLabel>();
		
		List<String> labelIds = resourceToLabelMapping.get(identifier);
		for(String labelId : labelIds){
			List<XbrlLabel> labels = labelCache.get(labelId);
			allLabels.addAll(labels);
		}
		
		return allLabels;
	}
	
}
