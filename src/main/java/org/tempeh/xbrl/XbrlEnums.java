package org.tempeh.xbrl;

import org.tempeh.TempehException;

public class XbrlEnums {
	public enum PeriodFocus{
		q1,
		q2,
		q3,
		q4,
		fy
	}
	
	public enum PeriodCompare{
		yoy,
		qoq,
		ttm
	}
	
	//IF YOU ADD AN AMEND FORMTYPE, MAKE SURE THE KEYS APPEND ACCESSID IN GADOGADO
	public enum FormType{
		ten_q,
		ten_k,
		ten_q_amend,
		ten_k_amend,
		ten_q_transition,
		ten_k_transition,
		ten_q_transition_amend,
		ten_k_transition_amend,
		twenty_f,
		twenty_f_amend
	}
	
	public enum FinancialStatementType{
		balance_sheet,
		income_statement,
		cashflow_statement,
		shareholder_equity
	}
	
	public static FormType getFormType(String formType) throws TempehException{
		formType = formType.toUpperCase();
		if(formType.equals("10-Q"))
			return FormType.ten_q;
		else if(formType.equals("10-K"))
			return FormType.ten_k;
		else if(formType.equals("10-K/A"))
			return FormType.ten_k_amend;
		else if(formType.equals("10-Q/A"))
			return FormType.ten_q_amend;
		else if(formType.equals("10-KT"))
			return FormType.ten_k_transition;
		else if(formType.equals("10-QT"))
			return FormType.ten_q_transition;
		else if(formType.equals("10-KT/A"))
			return FormType.ten_k_transition_amend;
		else if(formType.equals("10-QT/A"))
			return FormType.ten_q_transition_amend;
		else if(formType.equals("20-F"))
			return FormType.twenty_f;
		else if(formType.equals("20-F/A"))
			return FormType.twenty_f_amend;
		
		throw new TempehException("Unrecognized formType: " + formType);
	}
	
	public static int getPeriodValueForSorting(PeriodFocus period) throws TempehException{
		if(period == PeriodFocus.q1)
			return 1;
		else if(period == PeriodFocus.q2)
			return 2;
		else if(period == PeriodFocus.q3)
			return 3;
		else if(period == PeriodFocus.q4)
			return 4;
		else if(period == PeriodFocus.fy)
			return 5;
		
		throw new TempehException("Unrecognized periodFocus: " + period.toString());
	}
	
	public static PeriodFocus getPeriodFocus(String periodFocus) throws TempehException{
		periodFocus = periodFocus.toUpperCase();
		if(periodFocus.equals("Q1"))
			return PeriodFocus.q1;
		else if(periodFocus.equals("Q2"))
			return PeriodFocus.q2;
		else if(periodFocus.equals("Q3"))
			return PeriodFocus.q3;
		else if(periodFocus.equals("Q4"))
			return PeriodFocus.q4;
		else if(periodFocus.equals("FY"))
			return PeriodFocus.fy;
		
		throw new TempehException("Unrecognized periodFocus: " + periodFocus);
	}
	
}

