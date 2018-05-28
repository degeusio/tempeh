package org.tempeh.data;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractCellPosition {
	
	protected int ordinal;
	
	public AbstractCellPosition(int ordinal){
		//addMember(member);
		this.ordinal = ordinal;
	}

	//for serialization
	protected AbstractCellPosition(){}

	public int getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

}
