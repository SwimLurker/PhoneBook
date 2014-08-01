package com.nnit.phonebook.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import com.nnit.phonebook.data.DataPackageManager;

public class ConfigManager {
	public static String CONFIG_FAVORITELIST = "favorite_list";
	public static String CONFIG_SHOWGUIDEPAGE = "show_guide_page";
	public static String CONFIG_START_WIDGETUPDATE_SERVICE="start_update_widget_service";
	public static String CONFIG_WIDGETUPDATE_INTERVAL="update_widget_interval";
	
	public static String CONFIG_GAME_MUSIC_ON = "game_music";
	public static String CONFIG_GAME_SFX_ON = "game_sfx";
	public static String CONFIG_GAME_ROWNUMBER = "game_row_number";
	public static String CONFIG_GAME_COLUMNNUMBER = "game_column_number";
	public static String CONFIG_GAME_SAMEIMAGECOUNT = "game_same_image_count";
	public static String CONFIG_GAME_MAXHINTNUMBER = "game_max_hint_number";
	public static String CONFIG_GAME_MAXTIME = "game_max_time";
	public static String CONFIG_GAME_BONUSTIME = "game_bonus_time";
	
	
	private static ConfigManager _instance = null;
	
	private boolean bLoaded = false;
	private Properties props = null;
	private static Object lock = new Object();
	
	private ConfigManager(){
		 props = new Properties();
	}
	
	public static ConfigManager getInstance(){
		if(_instance == null){
			_instance = new ConfigManager();
		}
		return _instance;
	}

	protected void loadConfiguration(){
		synchronized(lock){
			FileInputStream fis = null;
			String filename = DataPackageManager.getInstance().getPropertiesFilename();
			try{
				File f = new File(filename);
				if(f.exists() && f.isFile()){
					 fis = new FileInputStream(f);
					 props.load(fis);			 
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
	}
	
	
	protected boolean saveConfiguration(){
		synchronized(lock){
			FileOutputStream fos = null;
			String filename = DataPackageManager.getInstance().getPropertiesFilename();
			try{
				File f = new File(filename);
				if(f.exists() && f.isFile()){
					f.delete();
				}
				f.createNewFile();
				
				fos = new FileOutputStream(f);
				props.store(fos, null);
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
	}
	
	public String getConfigure(String key){
		if(!bLoaded){
			loadConfiguration();
		}
		return props.getProperty(key);
	}
	
	public boolean saveConfigure(String key, String value){
		if(!bLoaded){
			loadConfiguration();
		}
		synchronized(lock){
			props.setProperty(key, value);
		}
		return saveConfiguration();
	}

	public void reloadConfigures() {
		synchronized(lock){
			props.clear();
			bLoaded = false;
		}
	}
}
