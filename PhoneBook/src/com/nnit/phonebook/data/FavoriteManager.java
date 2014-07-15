package com.nnit.phonebook.data;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.nnit.phonebook.config.ConfigManager;

public class FavoriteManager {
	private static FavoriteManager _instance = null;
	
	private List<String> favoriteList = null;
	
	private FavoriteManager(){
		favoriteList = new ArrayList<String>();
		String favoriteListString = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_FAVORITELIST);
		if(favoriteListString != null){
			StringTokenizer st  = new StringTokenizer(favoriteListString, ",");
			while(st.hasMoreTokens()){
				favoriteList.add(st.nextToken().toUpperCase());
			}
		}
	}
	
	public static FavoriteManager getInstance(){
		if(_instance == null){
			_instance = new FavoriteManager();			
		}
		return _instance;
	}
	
	
	public List<String> getFavoriteInitialsList(){	
		List<String> result  = new ArrayList<String>();
		result.addAll(favoriteList);
		return result;
	}
	
	public boolean addToFavoriteList(String initials){
		if(!favoriteList.contains(initials.toUpperCase())){
			favoriteList.add(initials.toUpperCase());
		}
		return ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_FAVORITELIST, toFavoriteListString());
	}
	
	public boolean removeFromFavoriteList(String initials){
		if(favoriteList.contains(initials.toUpperCase())){
			favoriteList.remove(initials.toUpperCase());
		}
		return ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_FAVORITELIST, toFavoriteListString());
	}
	
	public boolean hasFavoriteList(){
		return favoriteList.size() > 0;
	}
	
	public boolean isInFavoriteList(String initials){
		return favoriteList.contains(initials.toUpperCase());
	}
	
	private String toFavoriteListString() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < favoriteList.size(); i++){
			String initials = favoriteList.get(i);
			sb.append(initials);
			if(i != favoriteList.size()-1){
				sb.append(",");
			}
		}
		return sb.toString();
	}


}
