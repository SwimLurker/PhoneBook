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
			if(match(item.getInitials(), initials)){
				result.add(item);
			}
		}
		return result;
	}
	
	private boolean match(String target, String pattern){
		if(pattern == null || pattern.trim().equals("")){
			return true;
		}
		if(target == null){
			return false;
		}
		String patternStr = getPatternString(pattern);
		
		return target.toUpperCase().matches(patternStr);
	}
	
	private String getPatternString(String str){
		StringBuffer result = new StringBuffer();
		for(char c : str.toUpperCase().toCharArray()){
			if(c == '*'){
				result.append("([A-Z]*)");
			}else{
				result.append(c);
			}
		}
		return result.toString();
	}

}
