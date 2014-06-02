package com.nnit.phonebook.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.Environment;

public class PhotoManager {
	private static PhotoManager _instance = null;
	
	private HashMap<String, String> photos = new HashMap<String, String>();
	
	private PhotoManager(){
		
	}
	
	public static PhotoManager getInstance(){
		if(_instance == null){
			_instance = new PhotoManager();
		}
		return _instance;
	}
	
	public String getPhotoFilenameByInitials(String initials){
		if(photos.containsKey(initials)){
			return photos.get(initials);
		}
		return null;
	}
	
	public boolean loadPhotosInfo(){
		return loadPhotosInfo(new File(DataPackageManager.getInstance().getPhotoDirAbsolutePath()));
	}
	
	private boolean loadPhotosInfo(File rootDir) {
		if(!rootDir.exists()||(!rootDir.isDirectory())){
			return false;
		}
		
		File[] subFiles = rootDir.listFiles();
		for(File f: subFiles){
			if(f.isDirectory()){
				loadPhotosInfo(f);
			}else if(f.isFile()){
				String initials = getInitilas(f);
				if(initials != null){
					photos.put(initials.toLowerCase(), f.getAbsolutePath());
				}
			}	
		}
		return true;
	}
	
	private String getInitilas(File file){
		int pos = -1;
		String filename = file.getName();
		if((pos = filename.lastIndexOf(".")) != -1){
			String initials = filename.substring(0, pos);
			return initials;
		}else{
			return filename;
		}
		
	}

}
