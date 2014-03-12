package com.nnit.phonebook.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArrayListPBDataSet implements IPBDataSet{
	private IPBDataSource dataSource = null;
	private ArrayList<PhoneBookItem> data = null;
	
	public ArrayListPBDataSet(IPBDataSource dataSource , Collection<PhoneBookItem> data){
		this.dataSource = dataSource;
		this.data = new ArrayList<PhoneBookItem>(data);
	}

	@Override
	public IPBDataSet filter(PhoneBookField field, Object value) {
		ArrayList<PhoneBookItem> result = null;
		
		switch(field){
			case INITIALS:
				result = filterByInitials((String)value);
				break;		
			default:
				result = new ArrayList<PhoneBookItem>();
				break;
		}
		return new ArrayListPBDataSet(dataSource, result);
	}

	@Override
	public List<PhoneBookItem> getPBItems() {
		return data;
	}
	
	private ArrayList<PhoneBookItem> filterByInitials(String initials){
		ArrayList<PhoneBookItem> result = new ArrayList<PhoneBookItem>();
		
		for(PhoneBookItem item:data){
			if(item.getInitials().equalsIgnoreCase(initials)){
				result.add(item);
			}
		}
		return result;
	}

}
