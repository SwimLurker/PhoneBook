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
			case NAME:
				result = filterByName((String)value);
				break;
			case PHONE:
				result = filterByPhone((String)value);
				break;
			case DEPARTMENTNO:
				result = filterByDepNo((String)value);
				break;
			case DEPARTMENT:
				result = filterByDepartment((String)value);
				break;
			case MANAGER:
				result = filterByManager((String)value);
				break;
			default:
				result = new ArrayList<PhoneBookItem>();
				result.addAll(this.data);
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
	
	private ArrayList<PhoneBookItem> filterByName(String name){
		ArrayList<PhoneBookItem> result = new ArrayList<PhoneBookItem>();
		
		for(PhoneBookItem item:data){
			if(contains(item.getName(), name)){
				result.add(item);
			}
		}
		return result;
	}
	
	private ArrayList<PhoneBookItem> filterByPhone(String phone){
		ArrayList<PhoneBookItem> result = new ArrayList<PhoneBookItem>();
		
		for(PhoneBookItem item:data){
			if(contains(item.getPhone(),phone)){
				result.add(item);
			}
		}
		return result;
	}
	
	private ArrayList<PhoneBookItem> filterByDepNo(String depNo){
		ArrayList<PhoneBookItem> result = new ArrayList<PhoneBookItem>();
		
		for(PhoneBookItem item:data){
			if(matchEquals(item.getDepartmentNo(), depNo)){
				result.add(item);
			}
		}
		return result;
	}
	
	private ArrayList<PhoneBookItem> filterByDepartment(String department){
		ArrayList<PhoneBookItem> result = new ArrayList<PhoneBookItem>();
		
		for(PhoneBookItem item:data){
			if(matchEquals(item.getDepartment(), department)){
				result.add(item);
			}
		}
		return result;
	}
	
	private ArrayList<PhoneBookItem> filterByManager(String manager){
		ArrayList<PhoneBookItem> result = new ArrayList<PhoneBookItem>();
		
		for(PhoneBookItem item:data){
			if(matchEquals(item.getManager(), manager)){
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
		
		boolean result =  target.toUpperCase().matches(patternStr);
		return result;
	}
	
	
	private boolean matchEquals(String target, String pattern){
		if(pattern == null || pattern.trim().equals("")){
			return true;
		}
		if(target == null){
			return false;
		}
		return target.equalsIgnoreCase(pattern);
	}
	
	private boolean contains(String target, String pattern){
		if(pattern == null || pattern.trim().equals("")){
			return true;
		}
		if(target == null){
			return false;
		}
		
		return target.toUpperCase().indexOf(pattern.toUpperCase())!=-1;
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
		result.append("([A-Z]*)");
		return result.toString();
	}

}
