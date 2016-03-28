package org.tempeh.xbrl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.time.DateUtils;
import org.tempeh.TempehException;


public class XbrlPeriod {
	
	public enum PeriodType{
		instant,
		duration,
		forever
	}
	
	private static final String MONTH = "m";
	private static final String YEAR = "y";
	private static final String DAY = "d";
	
	private static String DATE_FORMAT = "yyyy-M-d"; //same format used for database
	private static final String JSON_DATE_FORMAT = "M-d-yyyy"; 
	private PeriodType periodType;
	private String start;
	private String end;
	private Date startDate;
	private Date endDate;
	
	public XbrlPeriod(){
		
	}

	@Override
	public int hashCode(){
		return Objects.hash(startDate, endDate, periodType);
	}
	
	@Override
	public boolean equals(Object object){
		
		if(!(object instanceof XbrlPeriod))
			return false;
		
		XbrlPeriod period = (XbrlPeriod)object;
		return Objects.equals(startDate, period.startDate) &&
				Objects.equals(endDate, period.endDate) &&
				periodType == period.getPeriodType();
	}
	
	public int compareTo(XbrlPeriod period)  throws TempehException{

		//first sort by Period Type
		if(periodType != period.getPeriodType()){
			if(periodType == PeriodType.instant)
				return -1;
			if(periodType == PeriodType.duration && period.getPeriodType() == PeriodType.forever)
				return -1;
			else
				return 1;
		}
		else if(periodType == PeriodType.duration && period.getPeriodType() == PeriodType.duration && this.getDurationInMonths() != period.getDurationInMonths()){
			//If duration different, sort by duration
			return Integer.compare(this.getDurationInMonths(), period.getDurationInMonths());
		}
		else if(periodType == PeriodType.forever && period.getPeriodType() == PeriodType.forever){
			return 0;
		}
		else{
			
			//Lastly, sort first by end date then by start date
			if(this.endDate == null && period.getEndDate() != null)
				return -1;
			else if(this.endDate != null && period.getEndDate() == null)
				return 1;
			else if(this.endDate == null && period.getEndDate() == null)
				return 0;
			else{
				if(this.endDate.equals(period.getEndDate())){
					if(this.startDate == null && period.getStartDate() != null)
						return -1;
					else if(this.startDate != null && period.getStartDate() == null)
						return 1;
					else if(this.startDate == null && period.getStartDate() == null)
						return 0;
					else
						return this.startDate.compareTo(period.getStartDate()) * -1; //want more recent date first
				}
				else
					return this.endDate.compareTo(period.getEndDate()) * -1; //want more recent date first
			}
		}
	}
	
	public static Date parseDateString(String dateStr) throws ParseException{
		DateFormat format = new SimpleDateFormat(DATE_FORMAT);
		return format.parse(dateStr);
	}
	
	@Override
	public String toString(){
		if(periodType == PeriodType.instant)
			return start;
		else if(periodType == PeriodType.duration)
			return start + " - " + end;
		else if(periodType == PeriodType.forever)
			return "forever";
		
		return super.toString();
	}
	
	public PeriodType getPeriodType() {
		return periodType;
	}

	public void setPeriodType(PeriodType periodType) {
		this.periodType = periodType;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) throws ParseException {
		DateFormat format = new SimpleDateFormat(DATE_FORMAT);
		this.start = start;
		startDate = format.parse(this.start);
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) throws ParseException {
		DateFormat format = new SimpleDateFormat(DATE_FORMAT);
		this.end = end;
		endDate = format.parse(this.end);
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}
	
	public String getDbStartDate(){
		return start;
	}
	
	public String getDbEndDate(){
		return end;
	}
	
	public String getJsonStartDate(){
		DateFormat format = new SimpleDateFormat(JSON_DATE_FORMAT);
		return format.format(startDate);
	}
	
	public String getJsonEndDate(){
		DateFormat format = new SimpleDateFormat(JSON_DATE_FORMAT);
		return format.format(endDate);
	}
	
	//be careful if changing output of this function.  Used as part of key for financial data time series id
	public String formatDurationDisplay()  throws TempehException{
		if(periodType == PeriodType.duration){
			
			int months = getDurationInMonths();
			if(months == 0)
				return getDurationInDays() + DAY;
			
			if(months < 12)
				return months + MONTH;
			else if(months % 12 == 0)
				return (months/12) + YEAR;
			else
				return months + MONTH;
		}
		
		return null;
	}
	
	public int getDurationInMonths()  throws TempehException{
		if(periodType == PeriodType.duration){
			Date sd = this.getStartDate();
			Date ed = this.getEndDate();
			
			return getDurationInMonths(sd, ed);
		}
		
		return 0;
	}
	
	public static int getDurationInMonths(Date start, Date end) throws TempehException{

		if(end.before(start))
			throw new TempehException("start date has to be before end date");
		
		Calendar startCal = Calendar.getInstance();
		Calendar endCal = Calendar.getInstance();
		startCal.setTime(start);
		endCal.setTime(end);
		
		int startYear = startCal.get(Calendar.YEAR);
		int startMonth = startCal.get(Calendar.MONTH) + 1;
		int startDate = startCal.get(Calendar.DATE);
		int endYear = endCal.get(Calendar.YEAR);
		int endMonth = endCal.get(Calendar.MONTH) + 1;
		int endDate = endCal.get(Calendar.DATE);
		
		int numMonths = 0;
		if(endYear > startYear){
			numMonths += 12 * (endYear - startYear - 1); //-1 to count ONLY the in-between years.
			if(startDate >= 14)
				numMonths += 12 - startMonth;
			else
				numMonths += 12 - startMonth + 1; //include the start month if date is before 14th
			
			if(endDate >= 14)
				numMonths += endMonth;
			else
				numMonths += endMonth - 1;
		}
		else{
			numMonths += endMonth - startMonth + 1;
			if(startDate >= 14)
				numMonths -= 1; //if date is past the 14th, don't count the month
			if(endDate < 14)
				numMonths -= 1; 
		}
		return numMonths;
		
	}
	
	private int getDurationInDays() throws TempehException{
		Date start = this.getStartDate();
		Date end = this.getEndDate();
		
		if(end.before(start))
			throw new TempehException("start date has to be before end date");
		
		int numDays = 0;
		Date cur = start;
		while(!DateUtils.isSameDay(cur, end)){
			cur = DateUtils.addDays(cur, 1);
			numDays ++;
		}
		
		return numDays;
	}
	
}
