package org.tempeh.xbrl;

import java.util.Objects;

public class XbrlLabel {

	private String labelId;
	private String labelRole;
	private String language;
	private String labelValue;
	
	public XbrlLabel(String labelId, String labelRole, String language){
		this.labelId = labelId;
		this.labelRole = labelRole;
		this.language = language;
	}

	//for serialization
	private XbrlLabel(){}

	@Override
	public int hashCode(){
		return Objects.hash(labelId, labelRole, language, labelValue);
	}
	
	@Override
	public boolean equals(Object object){
		
		if(!(object instanceof XbrlLabel))
			return false;
		
		//DO NOT INCLUDE ID PROPERTY
		XbrlLabel label = (XbrlLabel)object;
		
		return Objects.equals(labelRole, label.getLabelRole()) &&
				Objects.equals(language, label.getLanguage()) &&
				Objects.equals(labelValue, label.getLabelValue());
	}
	
	public String getLabelId() {
		return labelId;
	}

	public String getLabelRole() {
		return labelRole;
	}

	public String getLanguage() {
		return language;
	}

	public void setLabelValue(String labelValue) {
		this.labelValue = labelValue;
	}

	public String getLabelValue() {
		return labelValue;
	}
	
	public String getCleanUpLabelValue(){
		if(labelValue == null)
			return null;
		
		return labelValue.replace("[Member]", "")
				.replace("[Table]", "")
				.replace("[Axis]", "")
				.replace("[Domain]", "")
				.replace("[Line Items]", "")
				.replace("[Abstract]", "")
				.replace("[Text Block]", "")
				.replace("[Policy Text Block]", "")
				.trim();
	}
}
