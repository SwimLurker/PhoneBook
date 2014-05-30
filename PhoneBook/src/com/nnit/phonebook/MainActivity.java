package com.nnit.phonebook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nnit.phonebook.data.DataFileManager;
import com.nnit.phonebook.data.IPBDataSet;
import com.nnit.phonebook.data.JSONPBDataSource;
import com.nnit.phonebook.data.PhoneBookField;
import com.nnit.phonebook.data.PhoneBookItem;
import com.nnit.phonebook.data.PhotoFileManager;
import com.nnit.phonebook.ui.MenuView;
import com.nnit.phonebook.ui.OpenFileDialog;
import com.nnit.phonebook.ui.PhoneBookListAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
	
	private EditText updateSeatDBFilenameET = null;
	private EditText updatePBDataFilenameET = null;
	private EditText updatePhotoPackFilenameET = null;
	
	public List<PhoneBookItem> getPhoneBookItems(){
		return this.pbItems;
	}
	public void setPhoneBookItems(List<PhoneBookItem> pbItems){
		this.pbItems = pbItems;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        
        setContentView(R.layout.activity_main);
        
        initDataFile();
        unpackPhotos(this);
        
        
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_main);
        
        inflater = LayoutInflater.from(this);
        
        
        /*
    	View menuView = inflater.inflate(R.layout.more_menu, null);
    	
    	ListView lv = (ListView)menuView.findViewById(R.id.menu_more);
     	String[] menuItems = {"Search By", "About"};
     	ArrayAdapter menuAdapter = new ArrayAdapter(this, R.layout.menu_item, menuItems);
     	lv.setAdapter(menuAdapter);
     	
    	menuWindow = new PopupWindow(menuView, 150, LayoutParams.WRAP_CONTENT);
    	menuWindow.setBackgroundDrawable(new BitmapDrawable());
    	menuWindow.setFocusable(true);
    	menuWindow.setOutsideTouchable(true);
//    	menuWindow.update();
 */
        titleTextView = (TextView)findViewById(R.id.textview_title);
        
        ImageButton detailListBtn = (ImageButton) findViewById(R.id.imagebtn_detaillist);
        detailListBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				isDetailList = true;
				updateLayout();
			}
        });
        
        ImageButton briefListBtn = (ImageButton) findViewById(R.id.imagebtn_brieflist);
        briefListBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				isDetailList = false;
				updateLayout();
			}      	
        });
               
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
             
             
        briefListBtn.requestFocus();
        
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
        titleTextView.setText("PhoneBook(" + pbItems.size() +")");
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
        
    }
    
    private void unpackPhotos(Context context) {
		try{
			PhotoFileManager.getInstance().unpackPhotosFromAssets(context);
		}catch(IOException e){
			Log.e("PhotoFileManager", "Unpack photo files error");
			e.printStackTrace();
		}
		
	}
	@Override
    protected Dialog onCreateDialog(int id){
    	
    	if(id == R.layout.dialog_dbfileselect){
    		
    		Map<String, Integer> images = new HashMap<String, Integer>();
    		images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
	    	images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
	    	images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
	    	images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_file);
	    	images.put("db", R.drawable.filedialog_dbfile);
	    	
	    	Dialog dialog = OpenFileDialog.createDialog(id, this, "Select Seat DB File", 
	    			new OpenFileDialog.CallbackBundle() {
	    				@Override
						public void callback(Bundle bundle) {
	    					
	    		    		String fullFileName = bundle.getString("path");
	    		    		updateSeatDBFilenameET.setText(fullFileName);
						}
					}, ".db", images);
	    	
	    	return dialog;
    	}else if(id == R.layout.dialog_pbfileselect){
    		
    		Map<String, Integer> images = new HashMap<String, Integer>();
    		images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
	    	images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
	    	images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
	    	images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_file);
	    	
	    	
	    	Dialog dialog = OpenFileDialog.createDialog(id, this, "Select PhoneBook Data File", 
	    			new OpenFileDialog.CallbackBundle() {
	    				@Override
						public void callback(Bundle bundle) {
	    					
	    		    		String fullFileName = bundle.getString("path");
	    		    		updatePBDataFilenameET.setText(fullFileName);
						}
					}, ".json", images);
	    	
	    	return dialog;
    	}else if(id == R.layout.dialog_photopackselect){
    		
    		Map<String, Integer> images = new HashMap<String, Integer>();
    		images.put(OpenFileDialog.sRoot, R.drawable.filedialog_root);
	    	images.put(OpenFileDialog.sParent, R.drawable.filedialog_folder_up);
	    	images.put(OpenFileDialog.sFolder, R.drawable.filedialog_folder);
	    	images.put(OpenFileDialog.sEmpty, R.drawable.filedialog_file);
	    	images.put("zip", R.drawable.filedialog_zipfile);
	    	
	    	Dialog dialog = OpenFileDialog.createDialog(id, this, "Select Photo Package File", 
	    			new OpenFileDialog.CallbackBundle() {
	    				@Override
						public void callback(Bundle bundle) {
	    					
	    		    		String fullFileName = bundle.getString("path");
	    		    		updatePhotoPackFilenameET.setText(fullFileName);
						}
					}, ".zip", images);
	    	
	    	return dialog;
    	}
    	return null;
    }
    
    private void initDataFile() {
    	DataFileManager dfManager = DataFileManager.getInstance();
		dfManager.setContext(this);
		dfManager.addImporter("iNNIT.json", R.raw.phonebook);
		dfManager.addImporter("iNNIT.db", R.raw.seatdb);
		
		dfManager.importAllFiles();
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
    	
    	Spinner depNoSpinner = (Spinner)dialogView.findViewById(R.id.searchby_depNo);
    	List<String> depNoList = new ArrayList<String>();
    	depNoList.add("Please Select ...");
		depNoList.addAll(getAllDeportMentNo());
		
		ArrayAdapter<String> depNoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, depNoList);
		depNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		depNoSpinner.setAdapter(depNoAdapter);
		
    	
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
					
					EditText mobile_et = (EditText) dialogView.findViewById(R.id.searchby_mobile);
					String mobile = mobile_et.getText().toString();
					
					Spinner depNo_spinner = (Spinner) dialogView.findViewById(R.id.searchby_depNo);
					String depNo = depNo_spinner.getSelectedItemPosition() == 0? null:(String)depNo_spinner.getSelectedItem();
					
					EditText manager_et = (EditText) dialogView.findViewById(R.id.searchby_manager);
					String manager = manager_et.getText().toString();
					
					setPhoneBookItems(fullPBDS.filter(PhoneBookField.INITIALS, initials)
							.filter(PhoneBookField.NAME, name)
							.filter(PhoneBookField.MOBILE, mobile)
							.filter(PhoneBookField.DEPARTMENTNO, depNo)
							.filter(PhoneBookField.MANAGER, manager)
							.getPBItems());
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
    	final View dialogView = inflater.inflate(R.layout.dialog_updatedatafile, null);
    	updateSeatDBFilenameET = (EditText)dialogView.findViewById(R.id.et_seatdbfilename);
    	updatePBDataFilenameET = (EditText)dialogView.findViewById(R.id.et_phonebookfilename);
    	updatePhotoPackFilenameET = (EditText)dialogView.findViewById(R.id.et_photopackfilename);
    	
    	Button btnDB = (Button)dialogView.findViewById(R.id.btn_browserseatdb);
    	btnDB.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(R.layout.dialog_dbfileselect);
			}
    		
    	});
    	
    	Button btnPB = (Button)dialogView.findViewById(R.id.btn_browserphonebook);
    	btnPB.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(R.layout.dialog_pbfileselect);
			}
    		
    	});
    	
    	Button btnPP = (Button)dialogView.findViewById(R.id.btn_browserphotopack);
    	btnPP.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				showDialog(R.layout.dialog_photopackselect);
			}
    		
    	});
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle("Update Data File")
        	.setView(dialogView)
        	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String dbfilePath = updateSeatDBFilenameET.getText().toString();
					
					String pbfilePath = updatePBDataFilenameET.getText().toString();
					
					String ppfilePath = updatePhotoPackFilenameET.getText().toString();
					
					if(dbfilePath.equals("")&&pbfilePath.equals("")&&ppfilePath.equals("")){
						Toast.makeText(MainActivity.this, "Please select data file!", Toast.LENGTH_SHORT).show();
						return;
					}
					
					if((!dbfilePath.equals("")) && (!updateDataFile("iNNIT.db", dbfilePath))){
							Toast.makeText(MainActivity.this, "Seat database file update fail", Toast.LENGTH_SHORT).show();		
							return;
					}
					if((!pbfilePath.equals("")) && ((!updateDataFile("iNNIT.json", pbfilePath)) || (!reloadPhonebook()))){
							Toast.makeText(MainActivity.this, "Phonebook data file update fail", Toast.LENGTH_SHORT).show();
							return;
					}
					
					if((!ppfilePath.equals("")) && ((!updatePhotoPackage(ppfilePath))||(!reloadPhonebook()))){
						Toast.makeText(MainActivity.this, "Photo package file update fail", Toast.LENGTH_SHORT).show();
						return;
					}
					
					Toast.makeText(MainActivity.this, "Update data file succeed", Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					
				}

				private boolean reloadPhonebook() {
					try{
			        	pbItems = getPhoneBook(); 	
			        }catch(Exception exp){
			        	exp.printStackTrace();
			        	Toast.makeText(MainActivity.this, "Reload Phone Book Data Error:" + exp.getMessage(), Toast.LENGTH_LONG).show();
			        	return false;
			        }
					updateLayout();
					
					return true;
				}

				private boolean updateDataFile(String datafileName, String dbfilePath) {
					// TODO Auto-generated method stub
					File f = new File(dbfilePath);
					if((!f.exists())||(!f.isFile())){
						return false;
					}
					FileInputStream fis = null;
					try{
						fis = new FileInputStream(f);
						return DataFileManager.getInstance().updateDataFile(datafileName, fis);
					}catch(Exception exp){
						Log.e("DataFileManager", "Update data file:" + dbfilePath +" failed");
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
				
				private boolean updatePhotoPackage(String packageFileName) {
					// TODO Auto-generated method stub
					File f = new File(packageFileName);
					if((!f.exists())||(!f.isFile())){
						return false;
					}
					
					FileInputStream fis = null;
					try{
						fis = new FileInputStream(f);
						PhotoFileManager.getInstance().unpackPhotos(MainActivity.this, fis);
						return true;
					}catch(Exception exp){
						Log.e("DataFileManager", "Update photo package file:" + packageFileName +" failed");
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
    
    private List<String> getAllDeportMentNo(){
    	ArrayList<String> result = new ArrayList<String>();
    	List<PhoneBookItem> pbis = fullPBDS.getPBItems();
    	for(PhoneBookItem pbi: pbis){
    		String depNo = pbi.getDepartmentNo();
    		if(!result.contains(depNo)){
    			result.add(depNo);
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
		ds.setJsonFilePath(DataFileManager.getInstance().getDataFileAbsolutePath("iNNIT.json"));
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
    }
    
}
