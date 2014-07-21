package com.nnit.phonebook.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nnit.phonebook.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class OpenFileDialog {
	
	public static String tag = "OpenFileDialog";
	public static final String sRoot = "/";
	public static final String sParent = "..";
	public static final String sFolder = ".";
	public static final String sEmpty = "";
	
	public static Dialog createDialog(int id, 
			Context context, 
			String title, 
			CallbackBundle callback, 
			String suffix,
			Map<String, Integer> images){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context).setNegativeButton(context.getString(R.string.lable_cancelbtn), null);
		builder.setView(new FileSelectView(context, id, callback, suffix, images));
		Dialog dialog = builder.create();
		dialog.setTitle(title);
		
		return dialog;
	}

	public interface CallbackBundle{
		abstract void callback(Bundle bundle);
	}
	
	static class FileSelectView extends ListView implements OnItemClickListener{
		
		private CallbackBundle callback = null;
		private String path = sRoot;
		private List<Map<String, Object>> list = null;
		private int dialogID = 0;
		private String suffix = null;
		private Map<String, Integer> imageMap = null;
		
		public FileSelectView(Context context, 
				int dialogID, 
				CallbackBundle callback,
				String suffix,
				Map<String, Integer> images){
			super(context);
			this.imageMap = images;
			this.suffix = suffix;
			this.callback = callback;
			this.dialogID = dialogID;
			this.setOnItemClickListener(this);
			refreshFileList();
		}
		
		private String getSuffix(String filename){
			int dix = filename.lastIndexOf('.');
			if(dix<0){
				return "";
			}else{
				return filename.substring(dix+1);
			}
		}
		
		private int getImageId(String s){
			if(imageMap == null){
				return 0;
			}else if(imageMap.containsKey(s)){
				return imageMap.get(s);
			}else if(imageMap.containsKey(sEmpty)){
				return imageMap.get(sEmpty);
			}else{
				return 0;
			}
		}
		
		private int refreshFileList(){
			File[] files = null;
			try{
				files = new File(path).listFiles();
			}catch(Exception e){
				files = null;
			}
			
			if(files == null){
				Toast.makeText(getContext(), getContext().getString(R.string.error_no_right_to_access), Toast.LENGTH_SHORT).show();
				return -1;
			}
			
			if(list != null){
				list.clear();
			}else{
				list = new ArrayList<Map<String, Object>>(files.length);
			}
			
			ArrayList<Map<String, Object>> lFolders = new ArrayList<Map<String, Object>>();
			
			ArrayList<Map<String, Object>> lFiles = new ArrayList<Map<String, Object>>();
			
			if(!this.path.equals(sRoot)){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("name", sRoot);
				map.put("path", sRoot);
				map.put("img", getImageId(sRoot));
				list.add(map);
				
				map = new HashMap<String, Object>();
				map.put("name", sParent);
				map.put("path", path);
				map.put("img", getImageId(sParent));
				list.add(map);
			}
			
			for(File file: files){
				boolean bDir = file.isDirectory();
				File[] sfiles = file.listFiles();
				if(bDir && sfiles != null){
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("name", file.getName());
					map.put("path", file.getPath());
					map.put("img", getImageId(sFolder));
					lFolders.add(map);
				}else if(file.isFile()){
					String sf = getSuffix(file.getName().toLowerCase());
					if(suffix == null || suffix.length() == 0 || (sf.length()>0 && suffix.indexOf("." + sf) >= 0)){
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("name", file.getName());
						map.put("path", file.getPath());
						map.put("img", getImageId(sf));
						lFiles.add(map);
					}
				}
			}
			
			list.addAll(lFolders);
			list.addAll(lFiles);
			
			SimpleAdapter adapter = new SimpleAdapter(getContext(), 
					list, 
					R.layout.listitem_filelist, 
					new String[]{"img", "name", "path"}, 
					new int[]{R.id.filedialogitem_img, R.id.filedialogitem_name, R.id.filedialogitem_path});
			this.setAdapter(adapter);
			return files.length;
			
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id){
			String pt = (String) list.get(position).get("path");
			String fn = (String) list.get(position).get("name");
			if(fn.equals(sRoot) || fn.equals(sParent)){
				File fl = new File(pt);
				String ppt = fl.getParent();
				if(ppt != null){
					path = ppt;
				}else{
					path = sRoot;
				}
			}else{
				File fl = new File(pt);
				if(fl.isFile()){
					((Activity)getContext()).dismissDialog(this.dialogID);
					
					Bundle bundle = new Bundle();
					bundle.putString("path", pt);
					bundle.putString("name", fn);
					this.callback.callback(bundle);
					return;
				}else if(fl.isDirectory()){
					path = pt;
				}
			}
			this.refreshFileList();
		}
	}
}
