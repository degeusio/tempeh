package org.tempeh.xbrl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class XbrlRoleType {
	
	public enum RoleCategory{
		STATEMENT,
		DISCLOSURE,
		DOCUMENT,
		UNKNOWN,
	}
	
	public static final String DETAILS_REGEX = "[\\[(]{1,1}\\s*detail.*[\\])]{1,1}";
	private static final Pattern detailsPattern = Pattern.compile(DETAILS_REGEX, Pattern.CASE_INSENSITIVE);
	
	public static final String TABLES_REGEX = "[\\[(]{1,1}\\s*table.*[\\])]{1,1}";
	private static final Pattern tablesPattern = Pattern.compile(TABLES_REGEX, Pattern.CASE_INSENSITIVE);
	
	public static final String POLICIES_REGEX = "[\\[(]{1,1}\\s*(policies|policy).*[\\])]{1,1}";
	private static final Pattern policiesPattern = Pattern.compile(POLICIES_REGEX, Pattern.CASE_INSENSITIVE);
	
	public static final String PARENTHETICAL_REGEX = "[\\[(]{1,1}\\s*parenthetical.*[\\])]{1,1}";
	private static final Pattern parentheticalPattern = Pattern.compile(PARENTHETICAL_REGEX, Pattern.CASE_INSENSITIVE);
	
	private String id;
	private String roleURI;
	private String definition;
	private List<String> splittedDef;
	private List<String> usedOn = new ArrayList<String>();
	
	public XbrlRoleType(String id, String roleURI){
		this.id = id;
		this.roleURI = roleURI;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
		splittedDef = Splitter.on('-').omitEmptyStrings().trimResults().splitToList(definition);
	}

	public String getId() {
		return id;
	}

	public String getRoleURI() {
		return roleURI;
	}

	public List<String> getUsedOn() {
		return usedOn;
	}
	
	public RoleCategory getRoleCategory(){
		if(definition == null)
			return RoleCategory.UNKNOWN;
		
		if(splittedDef.size() < 2)
			return RoleCategory.UNKNOWN;
		
		if(splittedDef.get(1).toLowerCase().equals("disclosure"))
			return RoleCategory.DISCLOSURE;
		else if(splittedDef.get(1).toLowerCase().equals("statement"))
			return RoleCategory.STATEMENT;
		else if(splittedDef.get(1).toLowerCase().equals("document"))
			return RoleCategory.DOCUMENT;
		else
			return RoleCategory.UNKNOWN;
	}
	
	public String getSequence(){
		if(definition == null)
			return "";
		
		if(splittedDef.size() == 1)
			return "";
		
		try{
			return splittedDef.get(0);
		}
		catch(Exception e){
			return "";
		}
	}
	
	private String cleanUpTitleHelper(Pattern pattern, String text){
		Matcher m = pattern.matcher(text);
		if(m.find()){
			String editedText = text.substring(0, m.start());
			editedText += text.substring(m.end());
			return editedText;
		}
		else
			return text;
	}
	
	private String cleanUpTitle(String text){
		text = cleanUpTitleHelper(detailsPattern, text);
		text = cleanUpTitleHelper(tablesPattern, text);
		return cleanUpTitleHelper(policiesPattern, text);
		//return cleanUpTitleHelper(parentheticalPattern, text);
	}
	
	public String getTitle(){
		if(definition == null)
			return "";
		
		if(splittedDef.size() == 1)
			return cleanUpTitle(splittedDef.get(0));
		else if(splittedDef.size() == 3)
			return cleanUpTitle(splittedDef.get(2));
		else if(splittedDef.size() > 3)
			return cleanUpTitle(Joiner.on(" - ").join(splittedDef.subList(2, splittedDef.size())));
		
		return "";
	}
	
	public boolean isDetails(){
		/*
		if(definition.toLowerCase().contains("(details)") || 
				definition.toLowerCase().contains("(details textual)"))
			return true;
		*/
		Matcher m = detailsPattern.matcher(definition);
		if(m.find())
			return true;
		
		return false;
	}
	
	public boolean isTables(){
		//if(definition.toLowerCase().contains("(tables)"))
		//	return true;
		
		Matcher m = tablesPattern.matcher(definition);
		if(m.find())
			return true;
		
		return false;
	}
	
	public boolean isParenthetical(){
		//if(definition.toLowerCase().contains("(parenthetical)"))
		//	return true;
		
		Matcher m = parentheticalPattern.matcher(definition);
		if(m.find() || this.roleURI.toLowerCase().contains("parenthetical"))
			return true;
		
		return false;
	}
	
	public boolean isPolicies(){
		//if(definition.toLowerCase().contains("(policies)"))
		//	return true;
		
		Matcher m = policiesPattern.matcher(definition);
		if(m.find())
			return true;
		
		return false;
	}
}
