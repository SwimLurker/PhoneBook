package com.nnit.phonebook.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONPBDataSource implements IPBDataSource {
	
	private String jsonFilePath = null;
	
	
	
	public String getJsonFilePath() {
		return jsonFilePath;
	}

	public void setJsonFilePath(String jsonFilePath) {
		this.jsonFilePath = jsonFilePath;
	}

	@Override
	public IPBDataSet getDataSet() throws Exception{
		return new ArrayListPBDataSet(this, readDataFromJsonFile());		
	}
	
	private List<PhoneBookItem> readDataFromJsonFile() throws Exception{
		
		List<PhoneBookItem> result = new ArrayList<PhoneBookItem>();
		
		StringBuilder sb = new StringBuilder();
		
		BufferedReader br = new BufferedReader(new FileReader(jsonFilePath));
		String line = null;
		while((line = br.readLine()) != null){
			sb.append(line);
		}
		
		String text = sb.toString();
		if(text != null & text.startsWith("\ufeff")){
			text = text.substring(1);
		}
		JSONObject object = new JSONObject(text);
		for(int i = 0; i<26; i++){
			char c = (char)((int)'A' + i);
			if(object.has(Character.toString(c))){
				JSONArray array = object.getJSONArray(Character.toString(c));
				int len = array.length();
				for(int j=0; j<len; j++){
					JSONObject obj = array.getJSONObject(j);
					String initials = obj.getString("Initials");
					String name = obj.getString("Name");
					String localName = obj.getString("LocalName");
					String gender = obj.getString("Gender");
					String phone = obj.getString("Phone");
					String departmentNo = obj.getString("DepartmentNO");
					String department = obj.getString("Department");
					String title = obj.getString("Title");
					String manager = obj.getString("Manager");
					
					
					PhoneBookItem item = new PhoneBookItem();
					item.setInitials(initials);
					item.setName(name);
					item.setLocalName(localName);
					if("MALE".equalsIgnoreCase(gender)){
						item.setGender(PhoneBookItem.GENDER.MALE);
					}else if("FEMALE".equalsIgnoreCase(gender)){
						item.setGender(PhoneBookItem.GENDER.FEMALE);
					}else{
						item.setGender(PhoneBookItem.GENDER.UNKNOWN);
					}
					item.setPhone(phone);
					item.setDepartmentNo(departmentNo);
					item.setDepartment(department);
					item.setTitle(title);
					item.setManager(manager);
					result.add(item);
				}
			}
		}
		
		
		return result;
		
	}
	

}
