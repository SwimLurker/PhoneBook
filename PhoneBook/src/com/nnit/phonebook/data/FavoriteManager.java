package com.nnit.phonebook.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class FavoriteManager {
	private static FavoriteManager _instance = null;
	
	private List<String> favoriteList = null;
	private boolean bLoaded = false;
	
	private FavoriteManager(){
		favoriteList = new ArrayList<String>();
	}
	
	public static FavoriteManager getInstance(){
		if(_instance == null){
			_instance = new FavoriteManager();			
		}
		return _instance;
	}
	
	public boolean persistFavoriteInitialsList(){
		FileOutputStream fos = null;
		String filename = DataPackageManager.getInstance().getFavoriteListFileName();
		try{
			File f = new File(filename);
			if(f.exists() && f.isFile()){
				f.delete();
			}
			f.createNewFile();
			
			fos = new FileOutputStream(f);
			for(int i = 0; i < favoriteList.size(); i++){
				String initials = favoriteList.get(i);
				fos.write(initials.getBytes());
				if(i != favoriteList.size() -1){
					fos.write(",".getBytes());
				}
			}
			fos.flush();
			return true;
		}catch(Exception exp){
			exp.printStackTrace();
			return false;
		}finally{
			if(fos != null){
				try {
					fos.close();
				} catch (IOException e) {
				}
				fos = null;
			}
		}
		
	}
	
	public void loadFavoriteInitialsList(){
		favoriteList.clear();
		FileInputStream fis = null;
		String filename = DataPackageManager.getInstance().getFavoriteListFileName();
		try{
			File f = new File(filename);
			if(f.exists() && f.isFile()){
				 fis = new FileInputStream(f);
				 StringBuffer result = new StringBuffer();
				 byte[] buf = new byte[256];
				 int num = -1;
				 while((num = fis.read(buf))!=-1){
					 result.append(new String(buf, num));
				 }
				 StringTokenizer st = new StringTokenizer(result.toString(), ",");
				 while(st.hasMoreTokens()){
					 String initials = st.nextToken();
					 favoriteList.add(initials);
				 }
			}
		}catch(Exception exp){
			exp.printStackTrace();
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
				}
				fis = null;
			}
		}
		bLoaded = true;
	}
	
	public List<String> getFavoriteInitialsList(){
		if(!bLoaded){
			loadFavoriteInitialsList();
		}
		
		List<String> result  = new ArrayList<String>();
		result.addAll(favoriteList);
		return result;
	}
	
	public void addToFavoriteList(String initials){
		if(!bLoaded){
			loadFavoriteInitialsList();
		}
		if(!favoriteList.contains(initials.toUpperCase())){
			favoriteList.add(initials.toUpperCase());
		}
	}
	
	public void removeFromFavoriteList(String initials){
		if(!bLoaded){
			loadFavoriteInitialsList();
		}
		if(favoriteList.contains(initials.toUpperCase())){
			favoriteList.remove(initials.toUpperCase());
		}
	}
	
	public boolean hasFavoriteList(){
		if(!bLoaded){
			loadFavoriteInitialsList();
		}
		return favoriteList.size() > 0;
	}
	
	public boolean isInFavoriteList(String initials){
		if(!bLoaded){
			loadFavoriteInitialsList();
		}
		return favoriteList.contains(initials.toUpperCase());
	}

}
