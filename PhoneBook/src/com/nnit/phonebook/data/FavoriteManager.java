package com.nnit.phonebook.data;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.util.Log;

import com.nnit.phonebook.config.ConfigManager;

public class FavoriteManager {
	private static FavoriteManager _instance = null;
	
	private List<String> favoriteList = null;
	
	private boolean bLoaded = false;
	
	private static Object lock = new Object();
	
	private FavoriteManager(){
		favoriteList = new ArrayList<String>();
		load();
	}
	
	public static FavoriteManager getInstance(){
		if(_instance == null){
			_instance = new FavoriteManager();			
		}
		return _instance;
	}
	
	public void reload(){
		ConfigManager.getInstance().reloadConfigures();
		synchronized(lock){
			favoriteList.clear();
			bLoaded =false;
		}
	}
	
	protected void load(){
		
		String favoriteListString = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_FAVORITELIST);
		
		if(favoriteListString != null){
			synchronized(lock){
				StringTokenizer st  = new StringTokenizer(favoriteListString, ",");
				while(st.hasMoreTokens()){
					String initials = st.nextToken();
					favoriteList.add(initials.toUpperCase());
					Log.d("FavoriteManager", initials);
				}
				bLoaded = true;
			}
		}
	}
	
	public List<String> getFavoriteInitialsList(){	
		if(!bLoaded){
			load();
		}
		List<String> result  = new ArrayList<String>();
		result.addAll(favoriteList);
		return result;
	}
	
	public boolean addToFavoriteList(String initials){
		if(!bLoaded){
			load();
		}
		synchronized(lock){
			if(!favoriteList.contains(initials.toUpperCase())){
				favoriteList.add(initials.toUpperCase());
			}
			Log.d("FavoriteManager","Add:" + initials.toUpperCase());
		}
		return ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_FAVORITELIST, toFavoriteListString());
	}
	
	public boolean removeFromFavoriteList(String initials){
		if(!bLoaded){
			load();
		}
		synchronized(lock){
			if(favoriteList.contains(initials.toUpperCase())){	
				favoriteList.remove(initials.toUpperCase());
			}
		}
		return ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_FAVORITELIST, toFavoriteListString());
	}
	
	public boolean hasFavoriteList(){
		if(!bLoaded){
			load();
		}
		return favoriteList.size() > 0;
	}
	
	public boolean isInFavoriteList(String initials){
		if(!bLoaded){
			load();
		}
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
		String result = sb.toString();
		Log.d("FavoriteManager", "Save Favorite List:" + result);
		return result;
	}


}
