package org.tempeh.xbrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tempeh.xbrl.XbrlEnums.FinancialStatementType;
import org.tempeh.xbrl.metadata.DomainMemberCollection;
import org.tempeh.xbrl.metadata.DomainMemberLink;
import org.tempeh.xbrl.metadata.PresentationMemberLink;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class PresentationLink extends ExtendedLink {

	private final List<DomainMemberCollection> presentationItemCache = new ArrayList<DomainMemberCollection>();
	private final Map<String, DomainMemberCollection> fromLabelToDomainMemberMap = new LinkedHashMap<String, DomainMemberCollection>(); //no need to serialize, used temporarily during parsing
	private static String balanceSheetRegex = ".*balance\\s*sheet.*|.*statement[\\w ]+(?:financial)?(?:condition|position|assets[\\w ]*liabilities).*";
	private static String incomeStmtRegex = ".*income\\s*statement.*|.*comprehensive\\s*income.*|.*(?:statement|result)[\\w ]+(?:income|earning|loss|operation).*";
	private static String shareholderEquityRegex = ".*shareholdersequity.*|.*stockholdersequity.*";
	public static Pattern balanceSheetPattern = Pattern.compile(balanceSheetRegex, Pattern.CASE_INSENSITIVE);
	public static Pattern incomeStmtPattern = Pattern.compile(incomeStmtRegex, Pattern.CASE_INSENSITIVE);
	public static Pattern shareholderEquityPattern = Pattern.compile(shareholderEquityRegex, Pattern.CASE_INSENSITIVE);
	
	public PresentationLink(String role, String title){
		super(role, title);
	}
	
	//for serialization
	private PresentationLink(){}
	
	public void link(String fromLabel, String toLabel, String preferredLabel, Double order, String use, Integer priority) throws XbrlException{
		
		DomainMemberCollection collection = null;
		if(fromLabelToDomainMemberMap.containsKey(fromLabel))
			collection = fromLabelToDomainMemberMap.get(fromLabel);
		else{
			collection = new DomainMemberCollection(fromLabel);
			fromLabelToDomainMemberMap.put(fromLabel, collection);
		}
		
		collection.addDomainMember(new PresentationMemberLink(toLabel, order, use, priority, preferredLabel));
		
	}
	
	//called after extended link is complete
	public void verifyPresentations() throws XbrlException{
		for(String fromLabel : fromLabelToDomainMemberMap.keySet()){
			List<XbrlTaxonomy> fromResources = getResourceByLabel(fromLabel);
			
			if(fromResources == null || fromResources.size() == 0)
				throw new XbrlException("Cannot find resource: " + fromLabel + " from presentation link:" + getRole());
			
			XbrlTaxonomy fromResource = fromResources.get(0);
			DomainMemberCollection collection = fromLabelToDomainMemberMap.get(fromLabel);
			collection.setParentXbrlTaxonomy(fromResource);
			
			for(DomainMemberLink link : collection.getDomainMembers()){
				List<XbrlTaxonomy> toResources = getResourceByLabel(link.getTaxonomyLabel());
				if(toResources == null || toResources.size() == 0)
					throw new XbrlException("Cannot find resource: " + link.getTaxonomyLabel() + " from presentation link:" + getRole());
				
				link.setXbrlTaxonomy(toResources.get(0));
			}
			
			//possible that collection for fromResource already exist.  If so, add it to
			DomainMemberCollection existingCollection = getPresentationMemberCollectionIfExist(fromResource);
			if(existingCollection != null){
				for(DomainMemberLink link : collection.getDomainMembers()){
					existingCollection.addDomainMember(link);
				}
			}
			else
				presentationItemCache.add(collection);
			
		}
		
		//clear these in case another extended link with same role is used again, the labels can duplicate
		fromLabelToDomainMemberMap.clear();
		clearResources();
	}
	
	//top level presentation links are parent links that are not a child of any other links
	public List<DomainMemberCollection> getTopLevelPresentationLinks(){
		List<DomainMemberCollection> topLevelLinks = new ArrayList<DomainMemberCollection>();
		
		for(DomainMemberCollection collection : presentationItemCache){
			if(!isXbrlTaxonomyAChildLink(collection.getParentXbrlTaxonomy()))
				topLevelLinks.add(collection);
		}
		
		return topLevelLinks;
	}
	
	private FinancialStatementType getFinancialStatementTypeHelper(String text){
		if(text == null)
			return null;
		
		String lowerCaseText = text.toLowerCase();
		if(text.lastIndexOf("/") >= 0)
			lowerCaseText = text.substring(text.lastIndexOf("/") + 1).toLowerCase();
		
		Matcher matcher = balanceSheetPattern.matcher(lowerCaseText);
		if(matcher.matches())
			return FinancialStatementType.balance_sheet;
		
		matcher = shareholderEquityPattern.matcher(lowerCaseText);
		if(matcher.matches())
			return FinancialStatementType.shareholder_equity;
		
		if(lowerCaseText.contains("cashflow") || lowerCaseText.contains("cash flow")){
			return FinancialStatementType.cashflow_statement;
		}
		
		matcher = incomeStmtPattern.matcher(lowerCaseText);
		if(matcher.matches())
			return FinancialStatementType.income_statement;
		
		return null;
	}
	
	public FinancialStatementType getFinancialStatementType(){
		
		//cash flow stmt role can be loose so check if stmt contain cashflow items first
		if(hasCashflowStatementItems())
			return FinancialStatementType.cashflow_statement;
		
		FinancialStatementType stmtType = getFinancialStatementTypeHelper(this.role);
		if(stmtType != null){
			//ignoring shareholder equity stmt for now
			if(stmtType == FinancialStatementType.shareholder_equity)
				return null;
			
			return stmtType;
		}
		
		if(hasBalanceSheetItems())
			return FinancialStatementType.balance_sheet;
		else if(hasIncomeStatementItems())
			return FinancialStatementType.income_statement;
		
		return null;
	}
	
	private boolean hasStockHolderEquityItems(){

		for(DomainMemberCollection col : presentationItemCache){
			XbrlTaxonomy tax = col.getParentXbrlTaxonomy();
			
			boolean isDone = false;
			int count = 0;
			while(!isDone){
				if(NamespaceResolver.isUsGaapNamespace(tax.getNamespace())){
					if(tax.getName().equals("StatementOfStockholdersEquityAbstract")){
						return true;
					}
					else if(tax.getName().equals("StatementEquityComponentsAxis")){
						return true;
					}
				}
				
				if(count < col.getDomainMembers().size()){
					PresentationMemberLink childItem = (PresentationMemberLink)col.getDomainMembers().get(count);
					tax = childItem.getXbrlTaxonomy();
					count++;
				}
				else
					isDone = true;
			}

		}
		
		return false;
	}
	
	private boolean hasBalanceSheetItems(){
		boolean foundAsset = false;
		boolean foundLiability = false;
		boolean foundStockholderEquity = false;
		for(DomainMemberCollection col : presentationItemCache){
			XbrlTaxonomy tax = col.getParentXbrlTaxonomy();
			boolean isDone = false;
			int count = 0;
			while(!isDone){

				if((tax.getName().equals("Assets") || tax.getName().equals("AssetsCurrent")) &&
						NamespaceResolver.isUsGaapNamespace(tax.getNamespace())){
					foundAsset = true;
				}
				else if((tax.getName().equals("Liabilities") || tax.getName().equals("LiabilitiesCurrent")) &&
						NamespaceResolver.isUsGaapNamespace(tax.getNamespace())){
					foundLiability = true;
				}
				else if(tax.getName().equals("LiabilitiesAndStockholdersEquity") &&
						NamespaceResolver.isUsGaapNamespace(tax.getNamespace())){
					foundStockholderEquity = true;
				}
				if(count < col.getDomainMembers().size()){
					PresentationMemberLink childItem = (PresentationMemberLink)col.getDomainMembers().get(count);
					tax = childItem.getXbrlTaxonomy();
					count++;
				}
				else
					isDone = true;
			}
		}

		if(foundAsset && foundLiability && foundStockholderEquity)
			return true;

		return false;
	}
	
	private boolean hasIncomeStatementItems(){
		boolean foundEarningsPerShare = false;
		boolean foundNetIncome = false;
		for(DomainMemberCollection col : presentationItemCache){
			XbrlTaxonomy tax = col.getParentXbrlTaxonomy();
			boolean isDone = false;
			int count = 0;
			while(!isDone){
				
				if(NamespaceResolver.isUsGaapNamespace(tax.getNamespace())){
					if(tax.getName().equals("IncomeStatementAbstract")){
						return true;
					}
				}
				
				if((tax.getName().equals("NetIncomeLoss") || tax.getName().equals("ProfitLoss")) &&
						NamespaceResolver.isUsGaapNamespace(tax.getNamespace())){
					foundNetIncome = true;
				}
				else if((tax.getName().equals("EarningsPerShareBasic") || tax.getName().equals("EarningsPerShareBasicAndDiluted")) &&
						NamespaceResolver.isUsGaapNamespace(tax.getNamespace())){
					foundEarningsPerShare = true;
				}
				if(count < col.getDomainMembers().size()){
					PresentationMemberLink childItem = (PresentationMemberLink)col.getDomainMembers().get(count);
					tax = childItem.getXbrlTaxonomy();
					count++;
				}
				else
					isDone = true;
			}
		}

		if(foundEarningsPerShare && foundNetIncome)
			return true;

		return false;

	}
	
	private boolean hasCashflowStatementItems(){
		
		boolean foundCashFromOperating = false;
		boolean foundCashFromInvesting = false;
		boolean foundCashFromFinancing = false;
		for(DomainMemberCollection col : presentationItemCache){
			XbrlTaxonomy tax = col.getParentXbrlTaxonomy();
			boolean isDone = false;
			int count = 0;
			while(!isDone){
				
				if(NamespaceResolver.isUsGaapNamespace(tax.getNamespace())){
					if(tax.getName().equals("StatementOfCashFlowsAbstract")){
						return true;
					}
				}
				
				if(NamespaceResolver.isUsGaapNamespace(tax.getNamespace())){
					if(tax.getName().equals("NetCashProvidedByUsedInOperatingActivities") ||
							tax.getName().equals("NetCashProvidedByUsedInOperatingActivitiesAbstract") ||
							tax.getName().equals("NetCashProvidedByUsedInOperatingActivitiesContinuingOperations") ||
							tax.getName().equals("NetCashProvidedByUsedInOperatingActivitiesContinuingOperationsAbstract")){
						foundCashFromOperating = true;
					}
					else if(tax.getName().equals("NetCashProvidedByUsedInInvestingActivities") ||
							tax.getName().equals("NetCashProvidedByUsedInInvestingActivitiesAbstract") ||
							tax.getName().equals("NetCashProvidedByUsedInInvestingActivitiesContinuingOperations") ||
							tax.getName().equals("NetCashProvidedByUsedInInvestingActivitiesContinuingOperationsAbstract")){
						foundCashFromInvesting = true;
					}
					else if(tax.getName().equals("NetCashProvidedByUsedInFinancingActivities") || 
							tax.getName().equals("NetCashProvidedByUsedInFinancingActivitiesAbstract") || 
							tax.getName().equals("NetCashProvidedByUsedInFinancingActivitiesContinuingOperations") ||
							tax.getName().equals("NetCashProvidedByUsedInFinancingActivitiesContinuingOperationsAbstract")){
						foundCashFromFinancing = true;
					}
				}
				if(count < col.getDomainMembers().size()){
					PresentationMemberLink childItem = (PresentationMemberLink)col.getDomainMembers().get(count);
					tax = childItem.getXbrlTaxonomy();
					count++;
				}
				else
					isDone = true;
				
			}
		}

		//some has no investing
		if(foundCashFromOperating && foundCashFromFinancing && foundCashFromInvesting)
			return true;

		return false;

	}
	
	public DomainMemberCollection getPresentationMemberCollectionIfExist(XbrlTaxonomy fromResource){
		for(DomainMemberCollection collection : presentationItemCache){
			if(collection.getParentXbrlTaxonomy().equals(fromResource))
				return collection;
		}

		return null;
	}
	
	public PresentationMemberLink getPresentationMemberLink(String identifier){
		for(DomainMemberCollection collection : presentationItemCache){
			for(int i = 0; i < collection.getDomainMembers().size(); i++){
				PresentationMemberLink link = (PresentationMemberLink)collection.getDomainMembers().get(i);
				if(link.getXbrlTaxonomy().getIdentifier().equals(identifier))
					return link;
			}
		}
		
		return null;
	}
	
	public PresentationMemberLink getPresentationMemberLink(XbrlTaxonomy xbrlTaxonomy){
		for(DomainMemberCollection collection : presentationItemCache){
			for(int i = 0; i < collection.getDomainMembers().size(); i++){
				PresentationMemberLink link = (PresentationMemberLink)collection.getDomainMembers().get(i);
				if(link.getXbrlTaxonomy().equals(xbrlTaxonomy))
					return link;
			}
		}
		
		return null;
	}
	
	private boolean isXbrlTaxonomyAChildLink(XbrlTaxonomy xbrlTaxonomy){
		for(DomainMemberCollection collection : presentationItemCache){
			for(int i = 0; i < collection.getDomainMembers().size(); i++){
				PresentationMemberLink link = (PresentationMemberLink)collection.getDomainMembers().get(i);
				if(link.getXbrlTaxonomy().equals(xbrlTaxonomy))
					return true;
			}
		}
		
		return false;
	}

}
