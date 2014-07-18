package com.nnit.phonebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import com.nnit.phonebook.ui.IFrameAnimationListener;
import com.nnit.phonebook.ui.MenuView;
import com.nnit.phonebook.ui.MyAnimationDrawable;
import com.nnit.phonebook.ui.OpenFileDialog;
import com.nnit.phonebook.ui.PhoneBookListAdapter;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
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
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
	
	private ViewPager viewPager;
	private ArrayList<View> pageViews;
	private ImageView[] imageViews;
	
	private CheckBox showGuidePageCB = null;
	private RelativeLayout guidePageLayout;
	private LinearLayout mainPageLayout;
	
	public List<PhoneBookItem> getPhoneBookItems(){
		return this.pbItems;
	}
	public void setPhoneBookItems(List<PhoneBookItem> pbItems){
		this.pbItems = pbItems;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //first unpack data package if not
        unpackDataPackage(this);
         
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        inflater = getLayoutInflater();
        
        //prepare guide pages
        pageViews = new ArrayList<View>();
        View guide1PageView = inflater.inflate(R.layout.pageview_guide1, null);
        View guide2PageView = inflater.inflate(R.layout.pageview_guide2, null);
		View guide3PageView = inflater.inflate(R.layout.pageview_guide3, null);
		View guide4PageView = inflater.inflate(R.layout.pageview_guide4, null);
		View guide5PageView = inflater.inflate(R.layout.pageview_guide5, null);
		
		pageViews.add(guide1PageView);
		pageViews.add(guide2PageView);
		pageViews.add(guide3PageView);
		pageViews.add(guide4PageView);
		pageViews.add(guide5PageView);
		
		imageViews = new ImageView[pageViews.size()];
		
		ViewGroup main = (ViewGroup) inflater.inflate(R.layout.activity_main, null);
		ViewGroup group = (ViewGroup) main.findViewById(R.id.viewGroup);
		viewPager = (ViewPager) main.findViewById(R.id.guidePages);

		for (int i = 0; i < pageViews.size(); i++) {
			ImageView imageView = new ImageView(this);
			imageView.setLayoutParams(new LayoutParams(20, 20));
			imageView.setPadding(200, 0, 200, 0);
			imageViews[i] = imageView;

			if (i == 0) {
				imageViews[i].setBackgroundResource(R.drawable.page_indicator_focused_1);
			} else {
				imageViews[i].setBackgroundResource(R.drawable.page_indicator);
			}
			group.addView(imageViews[i]);
		}
		viewPager.setAdapter(new GuidePageAdapter());
		viewPager.setOnPageChangeListener(new GuidePageChangeListener());
		
		setContentView(main);
		
		
		ImageButton enterBtn= (ImageButton) guide5PageView.findViewById(R.id.guidepage_enterbtn);
		enterBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {			
				showMainPageWithAnimation();
				
			}
		});
		showGuidePageCB = (CheckBox) guide5PageView.findViewById(R.id.guidepage_showguide);
		
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
        	Toast.makeText(this, "Get Phonebook Item Error:" + exp.getMessage(), Toast.LENGTH_LONG).show();
        }
        
        if(FavoriteManager.getInstance().hasFavoriteList()){
        	showFavorite = true;
        	setPhoneBookItems(getFavoriteList(fullPBDS.getPBItems()));
        	titleTextView.setText("Favorite(" + pbItems.size() +")");
        	favoriteListBtn.setChecked(true);
        	detailListBtn.setChecked(false);
        }else{
        	showFavorite = false;
        	titleTextView.setText("All(" + pbItems.size() +")");
        	favoriteListBtn.setChecked(false);
        	detailListBtn.setChecked(true);
        }
        
        
        briefList.setAdapter(new PhoneBookListAdapter(this, pbItems));     
        
    	detailList.setAdapter(new PhoneBookListAdapter(this, pbItems));
       
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
        
        
        
        guidePageLayout = (RelativeLayout)findViewById(R.id.guidePageLayout);
		mainPageLayout =  (LinearLayout)findViewById(R.id.mainPageLayout);
		
		if(isShowGuidePage()){
			guidePageLayout.setVisibility(View.VISIBLE);
			mainPageLayout.setVisibility(View.GONE);
		}else{
			guidePageLayout.setVisibility(View.GONE);
			mainPageLayout.setVisibility(View.VISIBLE);
		}
        
    }
    
    @Override
	public void onBackPressed() {  	
    	finish();
    	//kill the process
    	android.os.Process.killProcess(android.os.Process.myPid());
    }
    
    private boolean isShowGuidePage() {
    	String showGuidePage = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_SHOWGUIDEPAGE);
    	if(showGuidePage != null && (showGuidePage.equals("0")||showGuidePage.equalsIgnoreCase("false"))){
    		return false;
    	}
		return true;
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
        	.setTitle("Please input Initials")
        	.setView(dialogView)
        	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
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
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
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
    	depNameList.add("Please Select ...");
    	depNameList.addAll(getAllDeportMents());
		
		ArrayAdapter<String> depNameAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, depNameList);
		depNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		depNameSpinner.setAdapter(depNameAdapter);
		
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle("Please input search criteria:")
        	.setView(dialogView)
        	.setPositiveButton("OK",new DialogInterface.OnClickListener() {
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
        	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
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
        	.setTitle("Update Data Package")
        	.setView(dialogView)
        	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String dataPackagePath = updateDataPackageFilenameET.getText().toString();
					
					
					if(dataPackagePath.equals("")){
						Toast.makeText(MainActivity.this, "Please select data package file!", Toast.LENGTH_SHORT).show();
						return;
					}
					
					
					if((!dataPackagePath.equals("")) && ((!updateDataPackageFile(dataPackagePath)) || (!reloadData()))){
							Toast.makeText(MainActivity.this, "Phonebook data file update fail", Toast.LENGTH_SHORT).show();
							return;
					}
					
					Toast.makeText(MainActivity.this, "Update data package succeed", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					
				}

				private boolean reloadData() {
					try{
			        	pbItems = getPhoneBook(); 	
			        }catch(Exception exp){
			        	exp.printStackTrace();
			        	Toast.makeText(MainActivity.this, "Reload Phone Book Data Error:" + exp.getMessage(), Toast.LENGTH_LONG).show();
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
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
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
    
    private void showAboutDialog() {
    	final View dialogView = inflater.inflate(R.layout.dialog_about, null);
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle("About")
        	.setView(dialogView)
        	.setNegativeButton("Close", new DialogInterface.OnClickListener() {
				
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
				case MenuView.MENU_ABOUT:
					showAboutDialog();
					break;
				default:
					break;
			}
			menuListView.close();
		}
    	
    };
    
    
    private List<PhoneBookItem> getPhoneBook() throws Exception{
    	JSONPBDataSource ds =  new JSONPBDataSource();
		ds.setJsonFilePath(DataPackageManager.getInstance().getPhoneBookDataFileAbsolutePath());
		List<PhoneBookItem> result = null;
		
		this.fullPBDS = ds.getDataSet();
		return fullPBDS.getPBItems();
		//return set.filter(PhoneBookField.INITIALS, "cqdi").getPBItems();
		
    }
    
    private void updateLayout(){
    	titleTextView.setText("PhoneBook(" + pbItems.size() +")");
    	
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
    		titleTextView.setText("Favorite(" + pbItems.size() +")");
    		favoriteListBtn.setChecked(true);
    		detailListBtn.setChecked(false);
    	}else{
    		titleTextView.setText("All(" + pbItems.size() +")");
    		favoriteListBtn.setChecked(false);
    		detailListBtn.setChecked(true);
    	}
    }
    
    private void showMainPage(){
    	ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_SHOWGUIDEPAGE, showGuidePageCB.isChecked()?"1":"0");
    	guidePageLayout.setVisibility(View.GONE);
    	mainPageLayout.setVisibility(View.VISIBLE);
    }
    
    private void showMainPageWithAnimation() {
    	ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_SHOWGUIDEPAGE, showGuidePageCB.isChecked()?"1":"0");
    	
    	
    	Animation ani = new AlphaAnimation(1.0f, 0.0f);
		ani.setDuration(1000);			
		
		guidePageLayout.setAnimation(ani);
		
		ani.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				guidePageLayout.setVisibility(View.GONE);
		    	mainPageLayout.setVisibility(View.VISIBLE);
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				
				
			}
			
		});
		
		ani.startNow();
    }
    
    private void showMainPageWithFrameAnimation(){
    	
    	ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_SHOWGUIDEPAGE, showGuidePageCB.isChecked()?"1":"0");
    	
    	Bitmap screenShot = getScreenShot(MainActivity.this);
    	
    	final ImageView aniImageView = (ImageView)findViewById(R.id.animationImage);
		
		MyAnimationDrawable anim = new MyAnimationDrawable();
		
		int totalFrameCount = 20;
		for(int i = 0;i<=totalFrameCount; i++){
			anim.addFrame(getAnimationFrame(screenShot, i, totalFrameCount), 1000/totalFrameCount);
		}
		
		anim.addFrameAnimationListener(new IFrameAnimationListener(){
			
			@Override
			public void onAnimationStart() {
				guidePageLayout.setVisibility(View.GONE);
				aniImageView.setVisibility(View.VISIBLE);
				Log.d("Animation", "animation start");
			}
			
			@Override
			public void onAnimationEnd() {
				aniImageView.setVisibility(View.GONE);
				mainPageLayout.setVisibility(View.VISIBLE);
				Log.d("Animation", "animation end");
			}
			
		});
		anim.setOneShot(true);
		aniImageView.setBackgroundDrawable(anim);
		anim.start();
		
	}
    
    private Drawable getAnimationFrame(Bitmap sourceBitmap, int index, int totalFrameCount) {
		Bitmap bitmap = getFrameBitmap(sourceBitmap, index, totalFrameCount);
		if(bitmap != null){
			return new BitmapDrawable(bitmap);
		}
		return null;
	}

	private Bitmap getFrameBitmap(Bitmap sourceBitmap, int index, int totalFrameCount) {
		
		int bitmapWidth = sourceBitmap.getWidth();
		int bitmapHeight = sourceBitmap.getHeight();

		Bitmap resultBMP = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);

		Canvas canvas = new Canvas(resultBMP);
		
		Matrix m = new Matrix();
		m.setScale((float)(totalFrameCount-index)/totalFrameCount, (float)(totalFrameCount -index)/totalFrameCount);
		
		Paint paint = new Paint();
		
		
		canvas.drawBitmap(sourceBitmap, m, paint);
		
		return resultBMP;
	}

	private Bitmap getScreenShot(Activity activity) {
		 
		View decorView = activity.getWindow().getDecorView();
		decorView.setDrawingCacheEnabled(true);
		decorView.buildDrawingCache();
		Bitmap bitmap = decorView.getDrawingCache();
		
		Rect rect = new Rect();
		decorView.getWindowVisibleDisplayFrame(rect);
		int statusBarHeight = rect.top;
		int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		int height = activity.getWindowManager().getDefaultDisplay().getHeight();
		
		Bitmap result = Bitmap.createBitmap(bitmap, 0, statusBarHeight, width, height - statusBarHeight);
		
		decorView.destroyDrawingCache();
		
		return result;
	}
    
    class GuidePageAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return pageViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			// TODO Auto-generated method stub
			((ViewPager) arg0).removeView(pageViews.get(arg1));
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			// TODO Auto-generated method stub
			((ViewPager) arg0).addView(pageViews.get(arg1));
			return pageViews.get(arg1);
		}
	}

	class GuidePageChangeListener implements OnPageChangeListener {

		boolean isScrolled = false;
		
		@Override
		public void onPageScrollStateChanged(int status) {
			switch (status){
				case 1:
					isScrolled = false;
					break;
				case 2:
					isScrolled =true;
					break;
				case 0:
					if(viewPager.getCurrentItem() == viewPager.getAdapter().getCount()-1 && !isScrolled){
						showMainPage();
					}
					break;
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPageSelected(int index) {
			if(index == 0){
				imageViews[index].setBackgroundResource(R.drawable.page_indicator_focused_1);
			}else if(index == 1){
				imageViews[index].setBackgroundResource(R.drawable.page_indicator_focused_2);
			}else if(index == 2){
				imageViews[index].setBackgroundResource(R.drawable.page_indicator_focused_3);
			}else if(index == 3){
				imageViews[index].setBackgroundResource(R.drawable.page_indicator_focused_4);
			}else{
				imageViews[index].setBackgroundResource(R.drawable.page_indicator_focused_5);
			}
			for (int i = 0; i < imageViews.length; i++) {
				if (index != i) {
					imageViews[i].setBackgroundResource(R.drawable.page_indicator);
				}
			}
		}

	}
}
