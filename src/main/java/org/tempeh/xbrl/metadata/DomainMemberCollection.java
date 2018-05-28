package org.tempeh.xbrl.metadata;

import java.util.ArrayList;
import java.util.List;

import org.tempeh.xbrl.XbrlTaxonomy;

public class DomainMemberCollection {

	protected XbrlTaxonomy parentXbrlTaxonomy;
	protected final List<DomainMemberLink> domainMembers = new ArrayList<DomainMemberLink>();
	protected String taxonomyLabel; //no need to serialize.  Temporarily stored in order to populate xbrlTaxonomy during parsing
	
	public DomainMemberCollection(String taxonomyLabel){
		this.taxonomyLabel = taxonomyLabel;
	}

	//for serialization
	private DomainMemberCollection(){}
		
	public void addDomainMember(DomainMemberLink link){
		if(domainMembers.size() == 0)
			domainMembers.add(link);
		else{
			for(int i = domainMembers.size() - 1; i >= 0; i--){
				if(domainMembers.get(i).getOrder() < link.getOrder()){
					if(i+1 == domainMembers.size())
						domainMembers.add(link);
					else
						domainMembers.add(i+1, link);
					return;
				}
			}
			
			domainMembers.add(0, link);
		}
	}
	
	public XbrlTaxonomy getParentXbrlTaxonomy() {
		return parentXbrlTaxonomy;
	}

	public void setParentXbrlTaxonomy(XbrlTaxonomy parentXbrlTaxonomy) {
		this.parentXbrlTaxonomy = parentXbrlTaxonomy;
	}

	public List<DomainMemberLink> getDomainMembers() {
		return domainMembers;
	}

	public String getTaxonomyLabel() {
		return taxonomyLabel;
	}
	
	
}

