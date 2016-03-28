package org.tempeh.xbrl;

import java.util.Map;
import java.util.Objects;

import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSTypeDefinition;
import org.tempeh.xbrl.XbrlPeriod.PeriodType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class XbrlTaxonomy {
	public enum BalanceType{
		debit,
		credit
	}
	
	private String namespace;
	private String name;
	private boolean isNillable;
	private boolean isAbstract;
	private String substitutionGroupNamespace;
	private String substitutionGroupName;
	private String typeNamespace;
	private String typeName;
	private PeriodType periodType;
	private BalanceType balanceType;
	
	public XbrlTaxonomy(XSElementDeclaration elementDec, NamedNodeMap attributes){
		
		this.namespace = elementDec.getNamespace();
		this.name = elementDec.getName();
		this.isAbstract = elementDec.getAbstract();
		this.isNillable = elementDec.getNillable();
		//this.type = elementDec.getTypeDefinition();
		this.substitutionGroupNamespace = elementDec.getSubstitutionGroupAffiliation().getNamespace();
		this.substitutionGroupName = elementDec.getSubstitutionGroupAffiliation().getName();
		this.typeNamespace = elementDec.getTypeDefinition().getNamespace();
		this.typeName = elementDec.getTypeDefinition().getName();
		//this.substitutionGroup = elementDec.getSubstitutionGroupAffiliation();
		//this.hasBalanceType = false;
		//this.hasPeriodType = false;
		
		for(int i = 0; i < attributes.getLength(); i++){
			Node attrNode = attributes.item(i);
			//String attrNS = attrNode.getNamespaceURI();
			String attrName = attrNode.getNodeName().toLowerCase();
			String attrValue = attrNode.getNodeValue();

			if(attrName.equals("xbrli:periodtype")){
				//this.hasPeriodType = true;
				periodType = PeriodType.valueOf(attrValue.toLowerCase());
			}
			else if(attrName.equals("xbrli:balance")){
				//this.hasBalanceType = true;
				balanceType = BalanceType.valueOf(attrValue.toLowerCase());
			}
		}
		
	}
	
	//for serialization
	private XbrlTaxonomy(){}
	
	public static XbrlTaxonomy getTestXbrlTaxonomy(String namespace, String name){
		XbrlTaxonomy taxonomy = new XbrlTaxonomy();
		taxonomy.namespace = namespace;
		taxonomy.name = name;
		taxonomy.typeNamespace = XbrlConstants.XBRL21Namespace;
		taxonomy.typeName = "monetaryItemType";
		//if(this.xbrlTaxonomy.getTypeNamespace().equals(Constants.XBRL21Namespace) && this.xbrlTaxonomy.getTypeName().equals("monetaryItemType"))
		
		return taxonomy;
	}

	@Override
	public int hashCode(){
		return Objects.hash(name, namespace);
	}
	
	@Override
	public boolean equals(Object object){
		
		if(!(object instanceof XbrlTaxonomy))
			return false;
		
		
		XbrlTaxonomy taxonomy = (XbrlTaxonomy)object;
		return Objects.equals(namespace, taxonomy.getNamespace()) &&
				Objects.equals(name, taxonomy.getName());
	}
	
	public static String getIdentifier(String namespace, String name){
		return namespace + "/" + name;
	}
	
	/*
	//if namespace is local, use securityIdentifier instead because local namespace is not guaranteed to be unique out in the wild
	public static String getLocalNamespaceAwareIdentifier(String securityIdentifier, String localNamespace, String namespace, String name){
		if(namespace.equals(localNamespace))
			return getIdentifier(securityIdentifier, name);
		else
			return getIdentifier(namespace, name);
	}*/
	
	public String getIdentifier(){
		return getIdentifier(this.namespace, this.name);
	}

	public String getNamespace() {
		return namespace;
	}

	public String getName() {
		return name;
	}

	public boolean isNillable() {
		return isNillable;
	}

	public boolean isAbstract() {
		return isAbstract;
	}

	public String getBalanceType() {
		if(this.balanceType != null)
			return this.balanceType.toString().toLowerCase();
		
		return null;
	}

	public String getTypeNamespace() {
		return typeNamespace;
	}

	public String getTypeName() {
		return typeName;
	}

	public PeriodType getPeriodType() {
		return periodType;
	}

	public boolean isTextBlockItem(){
		if((this.getTypeNamespace().equals(XbrlConstants.XBRLNonNumNamespace) || NamespaceResolver.isUSTypeNamespace(this.getTypeNamespace())) 
				&& this.getTypeName().equals("textBlockItemType"))
			return true;
		
		return false;
	}
	
	public boolean isHyperCubeItem(){
		if(this.substitutionGroupNamespace.equals(XbrlConstants.XBRLDTNamespace) && this.substitutionGroupName.equals("hypercubeItem"))
			return true;
		
		return false;
	}
	
	public boolean isDimensionItem(){
		if(this.substitutionGroupNamespace.equals(XbrlConstants.XBRLDTNamespace) && this.substitutionGroupName.equals("dimensionItem"))
			return true;
		
		return false;
	}
	
	public boolean isDomainItem(){
		if((this.getTypeNamespace().equals(XbrlConstants.XBRLNonNumNamespace) || NamespaceResolver.isUSTypeNamespace(this.getTypeNamespace())) 
				&& this.getTypeName().equals("domainItemType"))
			return true;
		
		return false;
	}
	
}
