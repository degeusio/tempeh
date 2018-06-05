package org.tempeh.xbrl.report;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.tempeh.data.AbstractCellPosition;
import org.tempeh.data.TaxonomyPosition;
import org.tempeh.xbrl.PresentationLink;
import org.tempeh.xbrl.XbrlContext;
import org.tempeh.xbrl.XbrlEnums.FinancialStatementType;
import org.tempeh.xbrl.XbrlInstance;
import org.tempeh.xbrl.XbrlRoleType;
import org.tempeh.xbrl.XbrlRoleType.RoleCategory;
import org.tempeh.xbrl.XbrlTaxonomy;
import org.tempeh.xbrl.metadata.DomainMemberCollection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

public class FinancialStatementsReport extends XbrlReport {

	ListMultimap<String, Map<String, Object>> stmtJsonByType = ArrayListMultimap.<String, Map<String, Object>>create();
	
	public FinancialStatementsReport(XbrlInstance instance){
		super(instance);
	}

	@Override
	public boolean willHandlePresentation(PresentationLink presentationLink) throws Exception {
		XbrlRoleType roleType = this.instance.getRoleTypes().get(presentationLink.getRole());
		if(roleType != null){
			if(roleType.getRoleCategory() == RoleCategory.STATEMENT && !roleType.isParenthetical() &&
			   !roleType.isDetails() && !roleType.isTables()){

				//logger.info("Statement - " + presentationLink.getRole());
				return true;
			}
		}
		
		return false;
		//return presentationLink.isFinancialStatementLink();
	}

	@Override
	public void buildPresentationLink(PresentationLink presentationLink) throws Exception {

		FinancialStatementType stmtType = presentationLink.getFinancialStatementType();
		if(stmtType == null){
			return;
		}
		
		List<DomainMemberCollection> topLevelCollections = presentationLink.getTopLevelPresentationLinks();
		
		if(topLevelCollections.size() == 0)
			return;
		
		FactTable factTable = buildPresentationItem(presentationLink, topLevelCollections);
		
		for(AbstractCellPosition pos : factTable.getTaxonomyPositions()){
			TaxonomyPosition taxPos = (TaxonomyPosition)pos;
			if(!taxPos.getXbrlTaxonomy().isAbstract()){
				System.out.println(taxPos.getXbrlTaxonomy().getName());
			}
		}
	}
	
	@Override
	public void buildReport() throws Exception{
		
	}
	
}
