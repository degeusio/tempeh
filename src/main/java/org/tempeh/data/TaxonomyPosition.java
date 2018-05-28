package org.tempeh.data;

import org.tempeh.xbrl.XbrlLabel;
import org.tempeh.xbrl.XbrlTaxonomy;

public class TaxonomyPosition extends AbstractCellPosition {
	
	private XbrlTaxonomy xbrlTaxonomy;
	private XbrlLabel xbrlLabel;
	private int level;
	
	public TaxonomyPosition(XbrlTaxonomy xbrlTaxonomy, int level){
		super(0);
		this.xbrlTaxonomy = xbrlTaxonomy;
		this.level = level;
	}

	//for serialization
	private TaxonomyPosition(){}
	
	public XbrlTaxonomy getXbrlTaxonomy() {
		return xbrlTaxonomy;
	}

	public XbrlLabel getXbrlLabel() {
		return xbrlLabel;
	}

	public void setXbrlLabel(XbrlLabel xbrlLabel) {
		this.xbrlLabel = xbrlLabel;
	}

	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public String toString(){
		return xbrlTaxonomy.getIdentifier();
	}

}
