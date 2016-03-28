package org.tempeh.xbrl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class ExtendedLink implements IExtendedLink {

	private final ListMultimap<String, XbrlTaxonomy> resourceByLabelCache = ArrayListMultimap.<String, XbrlTaxonomy>create();
	
	protected String role;
	protected String title;
	
	public ExtendedLink(String role, String title){
		this.role = role;
		this.title = title;
	}
	
	//for serialization
	protected ExtendedLink(){}
	
	@Override
	public void addResource(String label, XbrlTaxonomy xbrlTaxonomy) {
		resourceByLabelCache.put(label, xbrlTaxonomy);	
	}

	protected List<XbrlTaxonomy> getResourceByLabel(String label){
		return resourceByLabelCache.get(label);
	}
	
	protected void clearResources(){
		resourceByLabelCache.clear();
	}
	
	public String getRole() {
		return role;
	}
}
