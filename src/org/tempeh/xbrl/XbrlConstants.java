package org.tempeh.xbrl;

import javax.xml.XMLConstants;

public class XbrlConstants {
	
    public final static String XMLPrefix = "xml";
    public final static String XLinkPrefix = "xlink";
    public final static String XBRL21Prefix = "xbrli";
    public final static String GenericLinkPrefix = "gen";
    public final static String XBRL21LinkPrefix = "link";
    public final static String GenericLabelPrefix = "genlab";
    public final static String GenericReferencePrefix = "genref";
    public final static String XBRLAPIPrefix = "xbrlapi";
    public final static String CompPrefix = "comp";
    public final static String XBRLAPIEntitiesPrefix = "entity";
    public final static String XBRLAPILanguagesPrefix = "lang";
    public final static String XMLSchemaPrefix = "xsd";
    public final static String XMLSchemaInstancePrefix = "xsi";
    public final static String XBRLDTPrefix = "xbrldt";
    public final static String XBRLDIPrefix = "xbrldi";

    public final static String FragmentRootElementName = "fragment";
    public final static String FragmentDataContainerElementName = "data";

    public final static String XMLNamespace = XMLConstants.XML_NS_URI;
    public final static String XLinkNamespace = "http://www.w3.org/1999/xlink";
    public final static String XBRL21Namespace = "http://www.xbrl.org/2003/instance";
    public final static String XBRL21LinkNamespace = "http://www.xbrl.org/2003/linkbase";
    public final static String XBRLDTNamespace = "http://xbrl.org/2005/xbrldt";
    public final static String XBRLDINamespace = "http://xbrl.org/2006/xbrldi";
    public final static String GenericLinkNamespace = "http://xbrl.org/2008/generic";
    public final static String GenericLabelNamespace = "http://xbrl.org/2008/label";
    public final static String GenericReferenceNamespace = "http://xbrl.org/2008/reference";
    public final static String XBRLNonNumNamespace = "http://www.xbrl.org/dtr/type/non-numeric";
    public final static String XBRLNumNamespace = "http://www.xbrl.org/dtr/type/numeric";
       
    public final static String XMLSchemaNamespace = XMLConstants.W3C_XML_SCHEMA_NS_URI;
    public final static String XMLSchemaInstanceNamespace = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
    public final static String LabelArcrole = "http://www.xbrl.org/2003/arcrole/concept-label";
    public final static String GenericLabelArcrole = "http://xbrl.org/arcrole/2008/element-label";
    public final static String ReferenceArcrole = "http://www.xbrl.org/2003/arcrole/concept-reference";
    public final static String GenericReferenceArcrole = "http://xbrl.org/arcrole/2008/element-reference";
    public final static String CalculationArcrole = "http://www.xbrl.org/2003/arcrole/summation-item";
    public final static String PresentationArcrole = "http://www.xbrl.org/2003/arcrole/parent-child";
    public final static String GeneralSpecialArcrole = "http://www.xbrl.org/2003/arcrole/general-special";
    public final static String EssenceAliasArcrole = "http://www.xbrl.org/2003/arcrole/essence-alias";
    public final static String SimilarTuplesArcrole = "http://www.xbrl.org/2003/arcrole/similar-tuples";
    public final static String RequiresElementArcrole = "http://www.xbrl.org/2003/arcrole/requires-element";
    public final static String FactFootnoteArcrole = "http://www.xbrl.org/2003/arcrole/fact-footnote";
    public final static String StandardLabelRole = "http://www.xbrl.org/2003/role/label";
    public final static String TerseLabelRole = "http://www.xbrl.org/2003/role/terseLabel";
    public final static String VerboseLabelRole = "http://www.xbrl.org/2003/role/verboseLabel";
    public final static String PositiveLabelRole = "http://www.xbrl.org/2003/role/positiveLabel";
    public final static String PositiveTerseLabelRole = "http://www.xbrl.org/2003/role/positiveTerseLabel";
    public final static String PositiveVerboseLabelRole = "http://www.xbrl.org/2003/role/positiveVerboseLabel";
    public final static String NegativeLabelRole = "http://www.xbrl.org/2003/role/negativeLabel";
    public final static String NegativeTerseLabelRole = "http://www.xbrl.org/2003/role/negativeTerseLabel";
    public final static String NegativeVerboseLabelRole = "http://www.xbrl.org/2003/role/negativeVerboseLabel";
    public final static String ZeroLabelRole = "http://www.xbrl.org/2003/role/zeroLabel";
    public final static String ZeroTerseLabelRole = "http://www.xbrl.org/2003/role/zeroTerseLabel";
    public final static String ZeroVerboseLabelRole = "http://www.xbrl.org/2003/role/zeroVerboseLabel";
    public final static String TotalLabelRole = "http://www.xbrl.org/2003/role/totalLabel";
    public final static String PeriodStartLabelRole = "http://www.xbrl.org/2003/role/periodStartLabel";
    public final static String PeriodEndLabelRole = "http://www.xbrl.org/2003/role/periodEndLabel";
    public final static String DocumentationLabelRole = "http://www.xbrl.org/2003/role/documentationLabel";
    public final static String DefinitionGuidanceLabelRole = "http://www.xbrl.org/2003/role/definitionGuidanceLabel";
    public final static String DisclosureGuidanceLabelRole = "http://www.xbrl.org/2003/role/disclosureGuidanceLabel";
    public final static String PresentationGuidanceLabelRole = "http://www.xbrl.org/2003/role/presentationGuidanceLabel";
    public final static String MeasurementGuidanceLabelRole = "http://www.xbrl.org/2003/role/measurementGuidanceLabel";
    public final static String CommentaryGuidanceLabelRole = "http://www.xbrl.org/2003/role/commentaryGuidanceLabel";
    public final static String ExampleGuidanceLabelRole = "http://www.xbrl.org/2003/role/exampleGuidanceLabel";
    public final static String StandardReferenceRole = "http://www.xbrl.org/2003/role/reference";
    public final static String DefinitionReferenceRole = "http://www.xbrl.org/2003/role/definitionRef";
    public final static String DisclosureReferenceRole = "http://www.xbrl.org/2003/role/disclosureRef";
    public final static String MandatoryDisclosureReferenceRole = "http://www.xbrl.org/2003/role/mandatoryDisclosureRef";
    public final static String RecommendedDisclosureReferenceRole = "http://www.xbrl.org/2003/role/recommendedDisclosureRef";
    public final static String UnspecifiedDisclosureReferenceRole = "http://www.xbrl.org/2003/role/unspecifiedDisclosureGuidanceLabel";
    public final static String PresentationReferenceRole = "http://www.xbrl.org/2003/role/presentationRef";
    public final static String MeasurementReferenceRole = "http://www.xbrl.org/2003/role/measurementRef";
    public final static String CommentaryReferenceRole = "http://www.xbrl.org/2003/role/commentaryRef";
    public final static String ExampleReferenceRole = "http://www.xbrl.org/2003/role/exampleRef";
    public final static String StandardLinkRole = "http://www.xbrl.org/2003/role/link";
    public final static String ISO4217 = "http://www.xbrl.org/2003/iso4217";
    
    public final static String StandardGenericLabelRole = "http://www.xbrl.org/2008/role/label";
    public final static String TerseGenericLabelRole = "http://www.xbrl.org/2008/role/terseLabel";
    public final static String VerboseGenericLabelRole = "http://www.xbrl.org/2008/role/verboseLabel";
    public final static String DocumentationGenericLabelRole = "http://www.xbrl.org/2008/role/documentation";
    
    public final static String StandardGenericReferenceRole = "http://www.xbrl.org/2008/role/reference";
    public final static String LinkbaseReferenceArcrole = "http://www.w3.org/1999/xlink/properties/linkbase";

    public final static String PresentationLinkbaseReferenceRole = "http://www.xbrl.org/2003/role/presentationLinkbaseRef";
    public final static String DefinitionLinkbaseReferenceRole = "http://www.xbrl.org/2003/role/definitionLinkbaseRef";
    public final static String CalculationLinkbaseReferenceRole = "http://www.xbrl.org/2003/role/calculationLinkbaseRef";
    public final static String LabelLinkbaseReferenceRole = "http://www.xbrl.org/2003/role/labelLinkbaseRef";
    public final static String ReferenceLinkbaseReferenceRole = "http://www.xbrl.org/2003/role/referenceLinkbaseRef";

    public final static String US_GAAP_NAMESPACE = "http://fasb.org/us-gaap/2011-01-31";
    public final static String DEI_NAMESPACE = "http://xbrl.sec.gov/dei/2011-01-31";
}
