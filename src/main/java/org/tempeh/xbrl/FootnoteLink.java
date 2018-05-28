package org.tempeh.xbrl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class FootnoteLink {

	private final Map<String, String> factIdToLabelMapping = new HashMap<String, String>();
	private final ArrayListMultimap<String, FootnoteArc> factLabelToFootnoteLinkMapping = ArrayListMultimap.<String, FootnoteArc>create();
	private final Map<String, XbrlLabel> footnoteByLabel = new HashMap<String, XbrlLabel>();
	
	public FootnoteLink(){
		
	}
	
	public void addFactLabel(String factId, String factLabel){
		factIdToLabelMapping.put(factId, factLabel);
	}
	
	public void addFootnoteArc(String factLabel, String footnoteLabel, Double order){
		factLabelToFootnoteLinkMapping.put(factLabel, new FootnoteArc(footnoteLabel, order));
	}
	
	public void addFootnote(XbrlLabel xbrlLabel){
		footnoteByLabel.put(xbrlLabel.getLabelId(), xbrlLabel);
	}
	
	public List<XbrlLabel> getFootnoteByFactId(String factId){
		if(factIdToLabelMapping.containsKey(factId)){
			String factLabel = factIdToLabelMapping.get(factId);
			if(factLabelToFootnoteLinkMapping.containsKey(factLabel)){
				List<FootnoteArc> footnoteArcs = factLabelToFootnoteLinkMapping.get(factLabel);
				if(footnoteArcs != null && footnoteArcs.size() > 0){
					
					Collections.sort(footnoteArcs, new Comparator<FootnoteArc>(){
						public int compare(FootnoteArc p1, FootnoteArc p2){
							if(p1.getOrder() < p2.getOrder())
								return -1;
							else if(p1.getOrder() >  p2.getOrder())
								return 1;
							else
								return 0;
						}
					});
					
					List<XbrlLabel> footnotes = new ArrayList<XbrlLabel>();
					for(FootnoteArc arc : footnoteArcs){
						if(footnoteByLabel.containsKey(arc.getFootnoteLabel())){
							footnotes.add(footnoteByLabel.get(arc.getFootnoteLabel()));
						}
					}
					
					return footnotes;
				}
			}
		}
		
		return null;
	}
	
	private static class FootnoteArc{

		private double order;
		private String footnoteLabel;
		
		public FootnoteArc(String footnoteLabel, Double order){
			this.footnoteLabel = footnoteLabel;
			if(order == null)
				this.order = 1;
			else
				this.order = order;
		}

		//for serialization
		private FootnoteArc(){}
		
		public double getOrder() {
			return order;
		}

		public String getFootnoteLabel() {
			return footnoteLabel;
		}
		
		
	}
	
	
}

