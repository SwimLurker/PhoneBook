package com.nnit.phonebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nnit.phonebook.config.ConfigManager;
import com.nnit.phonebook.data.DataPackageManager;
import com.nnit.phonebook.data.FavoriteManager;
import com.nnit.phonebook.data.IPBDataSet;
import com.nnit.phonebook.data.JSONPBDataSource;
import com.nnit.phonebook.data.PhoneBookField;
import com.nnit.phonebook.data.PhoneBookItem;
import com.nnit.phonebook.data.PhotoManager;
import com.nnit.phonebook.game.Game;
import com.nnit.phonebook.service.UpdateContactCardWidgetService;
import com.nnit.phonebook.ui.IFrameAnimationListener;
import com.nnit.phonebook.ui.MenuView;
import com.nnit.phonebook.ui.MyAnimationDrawable;
import com.nnit.phonebook.ui.OpenFileDialog;
import com.nnit.phonebook.ui.PhoneBookListAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.graphics.drawable.BitmapDrawable;

public class MainActivity extends Activity {

	public static final String SELECTED_PBITEM = "com.nnit.phonebook.SELECTED_PBITEM";
	public static final String SEARCH_INITIALS = "com.nnit.phonebook.SEARCH_INITIALS";
	
	private List<PhoneBookItem> pbItems = null;
	private IPBDataSet fullPBDS = null;
	public static boolean isDetailList = true;
	private ListView briefList = null;
	private ListView detailList = null;
	//private PopupWindow menuWindow = null;
	private LayoutInflater inflater = null;
	private MenuView menuListView = null;
	private TextView titleTextView = null;
	public static boolean showFavorite = true;
	
	private EditText updateDataPackageFilenameET = null;
	private ToggleButton detailListBtn = null;
	private ToggleButton favoriteListBtn = null;
	
	
	private Resources resources = null;
	
	
	public List<PhoneBookItem> getPhoneBookItems(){
		return this.pbItems;
	}
	public void setPhoneBookItems(List<PhoneBookItem> pbItems){
		this.pbItems = pbItems;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        resources = getResources();
        //first unpack data package if not
        unpackDataPackage(this);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        
        setContentView(R.layout.activity_main);
        
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_main);
		
		inflater = getLayoutInflater();  
		
        titleTextView = (TextView)findViewById(R.id.textview_title);
        
        favoriteListBtn = (ToggleButton) findViewById(R.id.imagebtn_favoritelist);
        favoriteListBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				setPhoneBookItems(getFavoriteList(fullPBDS.getPBItems()));
				showFavorite = true;
				updateLayout();
				
			}

			      	
        });
        
        
        detailListBtn = (ToggleButton) findViewById(R.id.imagebtn_detaillist);
        detailListBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				isDetailList = true;
				setPhoneBookItems(fullPBDS.getPBItems());
				showFavorite = false;
				updateLayout();
			}
        });
        
        
        /*
        ImageButton briefListBtn = (ImageButton) findViewById(R.id.imagebtn_brieflist);
        briefListBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				isDetailList = false;
				updateLayout();
			}      	
        });
        */
               
        ImageButton searchBtn = (ImageButton) findViewById(R.id.imagebtn_search);
        searchBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				showSearchDialog();
			}      	
        });
        
       
    	
    	
        
        ImageButton moreBtn = (ImageButton) findViewById(R.id.imagebtn_more);
        moreBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View parent) {
				// TODO Auto-generated method stub
				switchSysMenuShow();
			}      	
        });
        
        
             
        if(briefList == null){
			briefList = (ListView) findViewById(R.id.brief_list);
		}
        
        if(detailList == null){
			detailList = (ListView) findViewById(R.id.detail_list);
		}
        
        try{
        	pbItems = getPhoneBook(); 	
        }catch(Exception exp){
        	exp.printStackTrace();
        	Toast.makeText(this, resources.getString(R.string.error_load_phonebook) + exp.getMessage(), Toast.LENGTH_LONG).show();
        }
        
        
        //briefList.setAdapter(new PhoneBookListAdapter(this, pbItems));     
        
    	//detailList.setAdapter(new PhoneBookListAdapter(this, pbItems));
       
        briefList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				Intent intent = new Intent();
				intent.putExtra(SELECTED_PBITEM, pbItems.get(position));
				intent.setAction("com.nnit.phonebook.DetailActivity");
				startActivity(intent);
			}
        	
        });
        
        
        detailList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				Intent intent = new Intent();
				intent.putExtra(SELECTED_PBITEM, pbItems.get(position));
				intent.setAction("com.nnit.phonebook.DetailActivity");
				startActivity(intent);
			}
        	
        });
        
        detailList.setVisibility(View.GONE);
        
        
		String searchInitials = null;
		Intent intent = getIntent();
		if(intent != null){
			Bundle bundle = intent.getExtras();
			if(bundle != null){
				searchInitials = (String)bundle.get(SEARCH_INITIALS);
			}
		}
        if(searchInitials != null){
        	showFavorite = false;
        	setPhoneBookItems(fullPBDS.filter(PhoneBookField.INITIALS, searchInitials).getPBItems());

		}else{
			if(FavoriteManager.getInstance().hasFavoriteList()){
	        	setPhoneBookItems(getFavoriteList(fullPBDS.getPBItems()));	  
	        	showFavorite = true;
	        }else{
	        	showFavorite = false;	        	
	        }
		}
        updateLayout();

    }
    
    @Override
	public void onBackPressed() {  	
    	finish();
    	//kill the process
    	android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    
    
    private boolean startWidgetUpdateService() {
    	String startService = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_START_WIDGETUPDATE_SERVICE);
    	if(startService != null && (startService.equals("1")||startService.equalsIgnoreCase("true"))){
    		return true;
    	}
		return false;
	}
    
    
	private void unpackDataPackage(Context context) {
		try{
			DataPackageManager.getInstance().unpackDataPackageFromAssets(context, false);
		}catch(IOException e){
			Log.e("DataPackageManager", "Unpack data files error");
			e.printStackTrace();
		}
		
	}
    
	@Override
    protected Dialog onCreateDialog(int id){
    	
    	if(id == R.layout.dialog_datapackageselect){
    		
    		Map<String, Integer> images = new HashMap<String, Integer>();
    		images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
	    	images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
	    	images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
	    	images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_file);
	    	images.put("zip", R.drawable.filedialog_zipfile);
	    	
	    	Dialog dialog = OpenFileDialog.createDialog(id, this, "Select Data Package File", 
	    			new OpenFileDialog.CallbackBundle() {
	    				@Override
						public void callback(Bundle bundle) {
	    					
	    		    		String fullFileName = bundle.getString("path");
	    		    		updateDataPackageFilenameET.setText(fullFileName);
						}
					}, ".zip", images);
	    	
	    	return dialog;
    	}
    	return null;
    }
    
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
    	menu.add("menu");
    	return super.onCreateOptionsMenu(menu);
        //return true;
    } 
    
    @Override
    public boolean onMenuOpened(int featureId, Menu menu){
    	switchSysMenuShow();
    	return false;
    }
    
    private List<PhoneBookItem> getFavoriteList(List<PhoneBookItem> pbis) {
		List<PhoneBookItem> result = new ArrayList<PhoneBookItem>();
		for(PhoneBookItem pbi: pbis){
			if(FavoriteManager.getInstance().isInFavoriteList(pbi.getInitials())){
				result.add(pbi);
			}
		}
		return result;
	}
    
    private void showSearchDialog() {
    	final View dialogView = inflater.inflate(R.layout.dialog_search, null);
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.title_searchdialog))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.lable_okbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					EditText initials_et = (EditText) dialogView.findViewById(R.id.search_initials_editview);
					String initials = initials_et.getText().toString();
					setPhoneBookItems(fullPBDS.filter(PhoneBookField.INITIALS, initials).getPBItems());
					showFavorite = false;
					updateLayout();
				}
			})
        	.setNegativeButton(resources.getString(R.string.lable_cancelbtn), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
				}
			})
        	.show();
    }

    private void showSearchByDialog() {
    	final View dialogView = inflater.inflate(R.layout.dialog_searchby, null);
    	
    	Spinner depNameSpinner = (Spinner)dialogView.findViewById(R.id.searchby_depName);
    	List<String> depNameList = new ArrayList<String>();
    	depNameList.add(resources.getString(R.string.lable_spinner_select));
    	depNameList.addAll(getAllDeportMents());
		
		ArrayAdapter<String> depNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, depNameList);
		depNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		depNameSpinner.setAdapter(depNameAdapter);
		
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.title_searchbydialog))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.lable_okbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					EditText initials_et = (EditText) dialogView.findViewById(R.id.searchby_initials);
					String initials = initials_et.getText().toString();
					
					EditText name_et = (EditText) dialogView.findViewById(R.id.searchby_name);
					String name = name_et.getText().toString();
					
					EditText phone_et = (EditText) dialogView.findViewById(R.id.searchby_phone);
					String phone = phone_et.getText().toString();
					
					Spinner depName_spinner = (Spinner) dialogView.findViewById(R.id.searchby_depName);
					String depName = depName_spinner.getSelectedItemPosition() == 0? null:(String)depName_spinner.getSelectedItem();
					
					EditText manager_et = (EditText) dialogView.findViewById(R.id.searchby_manager);
					String manager = manager_et.getText().toString();
					
					setPhoneBookItems(fullPBDS.filter(PhoneBookField.INITIALS, initials)
							.filter(PhoneBookField.NAME, name)
							.filter(PhoneBookField.PHONE, phone)
							.filter(PhoneBookField.DEPARTMENT, depName)
							.filter(PhoneBookField.MANAGER, manager)
							.getPBItems());
					showFavorite = false;
					updateLayout();
				}
			})
        	.setNegativeButton(resources.getString(R.string.lable_cancelbtn), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
				}
			})
        	.show();
    }
    
   
    private void showUpdateDataFileDialog(){
    	final View dialogView = inflater.inflate(R.layout.dialog_updatedatapackage, null);
    	updateDataPackageFilenameET = (EditText)dialogView.findViewById(R.id.et_datapackagefilename);
    	
    	Button btnDataPackage = (Button)dialogView.findViewById(R.id.btn_browserdatapackage);
    	btnDataPackage.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(R.layout.dialog_datapackageselect);
			}
    		
    	});
    	
    	
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.title_updatepackagedialog))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.lable_okbtn), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String dataPackagePath = updateDataPackageFilenameET.getText().toString();
					
					
					if(dataPackagePath.equals("")){
						Toast.makeText(MainActivity.this, resources.getString(R.string.error_select_package), Toast.LENGTH_SHORT).show();
						return;
					}
					
					
					if((!dataPackagePath.equals("")) && ((!updateDataPackageFile(dataPackagePath)) || (!reloadData()))){
							Toast.makeText(MainActivity.this, resources.getString(R.string.error_update_package), Toast.LENGTH_SHORT).show();
							return;
					}
					
					Toast.makeText(MainActivity.this, resources.getString(R.string.info_update_package), Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					
				}

			})
			.setNegativeButton(resources.getString(R.string.lable_cancelbtn), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					
				}
			})
        	.show();
    }
    
    /*
    private void showUpdateDataFileDialog() {
    	Map<String, Integer> images = new HashMap<String, Integer>();
    	images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
    	images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
    	images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
    	images.put("xml", R.drawable.filedialog_xmlfile);
    	images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_root);
    	
    	Dialog dialog = OpenFileDialog.createDialog(0, this, "Open Data File", 
    			new OpenFileDialog.CallbackBundle() {
    				@Override
					public void callback(Bundle bundle) {
						// TODO Auto-generated method stub
						
					}
				}, ".xml", images);
    	dialog.show();
    	
    }
    */
   
    private void showSettingsDialog() {
    	final View dialogView = inflater.inflate(R.layout.dialog_settings, null);
    	
    	CheckBox showGuideCB = (CheckBox)dialogView.findViewById(R.id.settings_showguide);
    	showGuideCB.setChecked(isShowGuidePage());
    	
    	CheckBox startServiceCB = (CheckBox)dialogView.findViewById(R.id.settings_startupdateservice);
    	startServiceCB.setChecked(startWidgetUpdateService());
    	
		final EditText updateIntervalET = (EditText)dialogView.findViewById(R.id.settings_updateserviceinterval);
		String intervalStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_WIDGETUPDATE_INTERVAL);
		if(intervalStr != null && !intervalStr.equals("")){
			updateIntervalET.setText(intervalStr);
		}
		
		if(startWidgetUpdateService()){
			updateIntervalET.setEnabled(true);
			updateIntervalET.setFocusable(true);
			updateIntervalET.setFocusableInTouchMode(true);
		}else{
			updateIntervalET.setEnabled(false);
			updateIntervalET.setFocusable(false);
			updateIntervalET.setFocusableInTouchMode(false);
		}
		
		startServiceCB.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				if(isChecked){
					updateIntervalET.setEnabled(true);
					updateIntervalET.setFocusable(true);
					updateIntervalET.setFocusableInTouchMode(true);
				}else{
					updateIntervalET.setEnabled(false);
					updateIntervalET.setFocusable(false);
					updateIntervalET.setFocusableInTouchMode(true);
				}
			}
			
		});
		
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.title_settingsdialog))
        	.setView(dialogView)
        	.setNeutralButton(resources.getString(R.string.lable_okbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try{
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						field.setAccessible(true);  
			            field.set(dialog, false);
			        }catch(Exception e) {
			        	e.printStackTrace();  
			        }  
			           
					CheckBox showGuideCB = (CheckBox)dialogView.findViewById(R.id.settings_showguide);
					ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_SHOWGUIDEPAGE, showGuideCB.isChecked()?"1":"0");
					CheckBox startServiceCB = (CheckBox)dialogView.findViewById(R.id.settings_startupdateservice);
					ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_START_WIDGETUPDATE_SERVICE, startServiceCB.isChecked()?"1":"0");
					EditText updateIntervalET = (EditText)dialogView.findViewById(R.id.settings_updateserviceinterval);
					String intervalStr = updateIntervalET.getText().toString();
					if(startServiceCB.isChecked()){
						long interval = -1;
						try{
							interval = Long.parseLong(intervalStr);
						}catch(Exception exp){
							Toast.makeText(MainActivity.this, resources.getString(R.string.error_invalid_update_interval), Toast.LENGTH_SHORT).show();
							return;
						}
						
						if(interval <=0){
							Toast.makeText(MainActivity.this, resources.getString(R.string.error_invalid_update_interval), Toast.LENGTH_SHORT).show();
							return;
						}
						
						ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_WIDGETUPDATE_INTERVAL, intervalStr);
					}
					
					try{
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						field.setAccessible(true);  
			            field.set(dialog, true);
			        }catch(Exception e) {
			        	e.printStackTrace();  
			        }
					dialog.dismiss();
				}
			})
        	.setNegativeButton(resources.getString(R.string.lable_cancelbtn), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try{
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						field.setAccessible(true);  
			            field.set(dialog, true);
			        }catch(Exception e) {
			        	e.printStackTrace();  
			        }  
					dialog.dismiss();
					
				}
			})
        	.show();
    }
    
    private void showAboutDialog() {
    	final View dialogView = inflater.inflate(R.layout.dialog_about, null);
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.title_aboutdialog))
        	.setView(dialogView)
        	.setNegativeButton(resources.getString(R.string.lable_closebtn), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
        	.show();
    }
    
    private List<String> getAllDeportMents(){
    	ArrayList<String> result = new ArrayList<String>();
    	List<PhoneBookItem> pbis = fullPBDS.getPBItems();
    	for(PhoneBookItem pbi: pbis){
    		String depName = pbi.getDepartment();
    		if(!result.contains(depName)){
    			result.add(depName);
    		}
    	}
    	Collections.sort(result);
    	return result;
    }
    
    protected void switchSysMenuShow(){
    	
    	initSysMenu();
    	if(!menuListView.getIsShow()){
    		menuListView.show();
    	}else{
    		menuListView.close();
    	}
    }
    
    private void initSysMenu(){
    	if(menuListView == null){
    		menuListView = new MenuView(this);
    	}
    	RelativeLayout layout = (RelativeLayout)findViewById(R.id.titlebar_layout);
        int height = layout.getHeight();
        menuListView.setTopMargin(height);
    	menuListView.listView.setOnItemClickListener(listClickListener);
    	menuListView.clear();
    	menuListView.add(MenuView.MENU_SEARCHBY, getString(R.string.menuitem_searchby));
    	menuListView.add(MenuView.MENU_UPDATEDATAFILE, getString(R.string.menuitem_updatedatafile));
    	menuListView.add(MenuView.MENU_SETTINGS, getString(R.string.menuitem_settings));
    	menuListView.add(MenuView.MENU_ABOUT, getString(R.string.menuitem_about));
    	
    }
    
    OnItemClickListener listClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int arg2,
				long arg3) {
			int key = Integer.parseInt(view.getTag().toString());
			switch(key){
				case MenuView.MENU_SEARCHBY:
					showSearchByDialog();
					break;
				case MenuView.MENU_UPDATEDATAFILE:
					showUpdateDataFileDialog();
					break;
				case MenuView.MENU_SETTINGS:
					showSettingsDialog();
					break;
				case MenuView.MENU_ABOUT:
					showAboutDialog();
					break;
				default:
					break;
			}
			menuListView.close();
		}
    	
    };
    
    private boolean reloadData() {
		try{
        	pbItems = getPhoneBook(); 	
        }catch(Exception exp){
        	exp.printStackTrace();
        	Toast.makeText(MainActivity.this, resources.getString(R.string.error_reload_data) + exp.getMessage(), Toast.LENGTH_LONG).show();
        	return false;
        }
		PhotoManager.getInstance().reload();
		showFavorite = false;
		updateLayout();
		
		return true;
	}
	
	private boolean updateDataPackageFile(String packageFileName) {
		// TODO Auto-generated method stub
		File f = new File(packageFileName);
		if((!f.exists())||(!f.isFile())){
			return false;
		}
		
		FileInputStream fis = null;
		try{
			fis = new FileInputStream(f);
			DataPackageManager.getInstance().unpackDataPackageFromInputStream(fis, true);
			return true;
		}catch(Exception exp){
			Log.e("DataPackageManager", "Update data package file:" + packageFileName +" failed");
			return false;
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
				}
				fis = null;
			}
		}
	}
    
    private List<PhoneBookItem> getPhoneBook() throws Exception{
    	JSONPBDataSource ds =  new JSONPBDataSource();
		ds.setJsonFilePath(DataPackageManager.getInstance().getPhoneBookDataFileAbsolutePath());
		List<PhoneBookItem> result = null;
		
		this.fullPBDS = ds.getDataSet();
		return fullPBDS.getPBItems();
		//return set.filter(PhoneBookField.INITIALS, "cqdi").getPBItems();
		
    }
    
    private void updateLayout(){
    	//titleTextView.setText("PhoneBook(" + pbItems.size() +")");
    	
    	if(isDetailList){
    		if(detailList == null){
    			detailList = (ListView) findViewById(R.id.detail_list);
    		}
    		detailList.setVisibility(View.VISIBLE);
    		detailList.setAdapter(new PhoneBookListAdapter(this, pbItems));
    		
    		if(briefList == null){
    			briefList = (ListView) findViewById(R.id.brief_list);
    		}
    		briefList.setVisibility(View.GONE);
    	}else{
    		if(briefList == null){
    			briefList = (ListView) findViewById(R.id.brief_list);
    		}
    		briefList.setVisibility(View.VISIBLE);
    		briefList.setAdapter(new PhoneBookListAdapter(this, pbItems));
    		
    		if(detailList == null){
    			detailList = (ListView) findViewById(R.id.detail_list);
    		}
    		detailList.setVisibility(View.GONE);
    	}
    	
    	if(showFavorite){
    		titleTextView.setText(resources.getString(R.string.title_favoritelist) + "(" + pbItems.size() +")");
    		favoriteListBtn.setChecked(true);
    		detailListBtn.setChecked(false);
    	}else{
    		titleTextView.setText(resources.getString(R.string.title_phonebooklist) + "(" + pbItems.size() +")");
    		favoriteListBtn.setChecked(false);
    		detailListBtn.setChecked(true);
    	}
    }

	private boolean isShowGuidePage() {
    	String showGuidePage = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_SHOWGUIDEPAGE);
    	if(showGuidePage != null && (showGuidePage.equals("0")||showGuidePage.equalsIgnoreCase("false"))){
    		return false;
    	}
		return true;
	}
	
	
}
