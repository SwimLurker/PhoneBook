package com.nnit.phonebook.ui;

import java.util.ArrayList;
import java.util.Collections;

import com.nnit.phonebook.R;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MenuView extends View{
	
	public final static int MENU_SEARCHBY = 0;
	
	public final static int MENU_ABOUT = 1;
	
	public static float SET_TYPE_TEXT_SIZE = 16;
	
	private Context mContext;
	
	private PopupWindow popWindow;
	
	private View popView;
	
	public ListView listView;
	
	public ArrayList<MenuItem> mItems = null;
	
	RelativeLayout layout;
	
	/**
     * 横屏菜单距离最大高度
     */
	private int bottomLength_h = 77;
	
	/**
     * 竖屏菜单距离最大高度
     */
	private int bottomLength_v = 173;
	
	
	private Display display;
	
	private float currentDensity;
	
	
	/**
     * 画板用于测字符宽度
     */
	private Paint paint = null;
	
	 /**
     * 菜单中最大的字符宽度
     */
	private float maxWidth = 0;
	
	private int topMargin = 0;
	

    /**
     * 菜单的最大左边距
     */
    private int maxLeftWidth = 88;

    /**
     * 菜单的最小左边距
     */
    private int minLeftWidth = 188;

    /**
     * 横屏菜单最小左边距
     */
    private int maxLeftWidth_h = 282;

    /**
     * 横屏菜单最小左边距
     */
    private int minLeftWidth_h = 371;

    /**
     * 菜单的背景和文字两边占间距
     */
    private int contentSpaceWidth = 38;


	private TranslateAnimation myMenuOpen;
	
	private TranslateAnimation myMenuClose;
	
	private int menuOpenMillis = 500;
	
	private int menuCloseMillis = 400;
	
	private final int MENU_OPEN_ANIM = 1;
	
	private final int MENU_CLOSE_ANIM = 2;
	
	private MyHandler myHandler = new MyHandler();
	
	private boolean isDismissing = false;
	
	
	ItemTextListAdapter adapter;
	
	
	class MyHandler extends Handler{
		@Override
		public void handleMessage(Message msg){
			if(msg == null){
				return;
			}
			super.handleMessage(msg);
			switch(msg.what){
				case MENU_OPEN_ANIM:
					startMenuOpenAnimation();
					break;
				case MENU_CLOSE_ANIM:
					if(popWindow != null){
						popWindow.dismiss();
					}
					isDismissing = false;
					break;
			}
		}
	}
	
	public MenuView(Context context){
		super(context);
		mContext = context;
		mItems = new ArrayList<MenuItem>();
		LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		popView = layoutInflater.inflate(R.layout.menu_more, null);
		listView = (ListView)popView.findViewById(R.id.menu_more_lv);
		layout = (RelativeLayout)popView.findViewById(R.id.menu_more_layout);
		adapter = new ItemTextListAdapter(mContext);
		popWindow = new PopupWindow(popView, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		display = ((WindowManager)mContext.getSystemService(mContext.WINDOW_SERVICE)).getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);
		currentDensity = displayMetrics.density;
		
		
		initValue();
		
		layout.setOnClickListener(onclick);
		listView.setFocusable(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setAdapter(adapter);
		listView.setFocusableInTouchMode(true);
		listView.setMinimumHeight(200);
		listView.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(!isDismissing){
					isDismissing = true;
					if((keyCode == KeyEvent.KEYCODE_MENU)&&(popWindow.isShowing())){
						close();
					}else if(((keyCode == KeyEvent.KEYCODE_BACK)&&(popWindow.isShowing()))){
						close();
					}
				}
				return false;
			}
			
		});
		
		
	}
	
	public void setTopMargin(int topMargin){
		this.topMargin = topMargin;
	}
	private void initValue(){
		
		paint = new Paint();
		maxLeftWidth = (int) (maxLeftWidth * currentDensity);  
        minLeftWidth = (int) (minLeftWidth * currentDensity);  
        maxLeftWidth_h = (int) (maxLeftWidth_h * currentDensity);  
        minLeftWidth_h = (int) (minLeftWidth_h * currentDensity);  
        contentSpaceWidth = (int) (contentSpaceWidth * currentDensity);  

		topMargin = (int)(topMargin * currentDensity);
		bottomLength_h = (int) (bottomLength_h * currentDensity);  
        bottomLength_v = (int) (bottomLength_v * currentDensity);  
        
        
        
	}
	
	public void add(int key, String value){
		remove(key);
		MenuItem item =  new MenuItem(key, value);
		mItems.add(item);
		adapter.notifyDataSetChanged();
	}
	
	public void add(int position, int key, String value){
		MenuItem item = new MenuItem(key, value);
		int size = mItems.size();
		for(int i = 0; i<size ; i++){
			if(position == i){
				mItems.add(position, item);
				adapter.notifyDataSetChanged();
			}
		}
		
	}
	
	private void getContextMaxLength(){
		adapter.notifyDataSetChanged();
		if(mItems!= null && mItems.size()>0){
			maxWidth = 0;
			if (Build.VERSION.SDK_INT >= 14){
				TextView tv = new TextView(mContext);  
				tv.setTextSize(SET_TYPE_TEXT_SIZE);  
				float size = tv.getTextSize() / SET_TYPE_TEXT_SIZE;  
				paint.setTextSize((int) ((size * 12) * currentDensity));
			}else{  
				paint.setTextSize((int) ((SET_TYPE_TEXT_SIZE + 1) * currentDensity));  
			}
			for(int i=0; i< mItems.size(); i++){
				if(paint.measureText(mItems.get(i).MenuValue) > maxWidth){
					maxWidth = paint.measureText(mItems.get(i).MenuValue);
				}
			}
		}
	}
	
	public void updateItem(int position, int key, String value){  
		if (mItems.size() > position){  
			remover(position);  
			MenuItem item = new MenuItem(key, value);  
			mItems.add(position, item);  
		}  
	}
	
	public void remover(int position){
		if(mItems.size() > position){
			mItems.remove(position);
		}
	}
	
	public void remove(int key){
		MenuItem item = null;
		for(int i = 0; i<mItems.size(); i++){
			item = mItems.get(i);
			if(item.MenuKey ==  key){
				mItems.remove(i);
				break;
			}
		}
	}
	
	public void clear(){
		mItems.clear();
		maxWidth = 0;
	}
	
	
	public void setMenuPosition(int left, int top, int right, int bottom){
		layout.setPadding(left, topMargin + top, right, bottom);
	}
	
	private OnClickListener onclick = new OnClickListener(){
		@Override
		public void onClick(View v){
			switch(v.getId()){
				case R.id.menu_more_layout:
					close();
					break;
				default:
					break;
			}
		}
	};
	
	public void setItems(ArrayList<String> items){
		mItems.clear();
		if(items != null && items.size()>0){
			for(int i=0;i<items.size(); i++){
				MenuItem item = new MenuItem(0, items.get(i));
				mItems.add(item);
			}
		}
	}
	
	
	public void show(){
		try{
			if(popWindow != null && popView != null){
				if(popWindow.isShowing()){
					startMenuCloseAnimation();
				}else{
					if(mItems != null && mItems.size() > 0){
						int orientation = display.getOrientation();
						if(orientation == 0){													
							getContextMaxLength();
							int left = (int)((320 * currentDensity) - (maxWidth + contentSpaceWidth));
							if(left < maxLeftWidth){
								left = maxLeftWidth;
							}else if(left > minLeftWidth){
								left = minLeftWidth;
							}
							setMenuPosition(left, 0, 0, bottomLength_v);
						}else{
							getContextMaxLength();
							int left = (int)((533 * currentDensity) - ( maxWidth + contentSpaceWidth));
							if(left < maxLeftWidth_h){
								left = maxLeftWidth_h;
							}else if(left > minLeftWidth_h){
								left = minLeftWidth_h;
							}
							setMenuPosition(left, 0, 0, bottomLength_h);
						}
						Collections.sort(mItems);
						popWindow.setFocusable(true);
						popWindow.update();
						popWindow.showAtLocation(listView, Gravity.FILL, 0, 0);
						myHandler.sendEmptyMessage(MENU_OPEN_ANIM);
					}
				}
			}
		}catch(Exception e){
			Log.i("MenuView", "show() e:" + e.toString());
			close();
		}
	}
	
	public boolean getIsShow(){
		return popWindow.isShowing();
	}
	
	public void close(){
		if(popWindow != null && popWindow.isShowing()){
			startMenuCloseAnimation();
		}
	}
	
	private void startMenuOpenAnimation(){
		menuOpenMillis = (mItems.size() * 100) + 100;
		if(menuOpenMillis > 500){
			menuOpenMillis = 500;
		}
		myMenuOpen = new TranslateAnimation(0f, 0f, -(listView.getHeight() + 1), 0f);
		myMenuOpen.setDuration(menuOpenMillis);
		listView.startAnimation(myMenuOpen);
	}
	
	private void startMenuCloseAnimation(){
		myMenuClose = new TranslateAnimation(0f, 0f, 0f, -(listView.getHeight() +1));
		myMenuClose.setDuration(menuCloseMillis);
		listView.startAnimation(myMenuClose);
		myHandler.sendEmptyMessageDelayed(MENU_CLOSE_ANIM, menuCloseMillis);
	}
	
	public class ItemTextListAdapter extends SimpleAdapter{
		public ItemTextListAdapter(Context context){
			super(context, null, 0, null, null);
		}
		
		@Override
		public int getCount(){
			return mItems.size();
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent){
			ItemHolder holder;
			if(convertView == null || convertView.getTag() == null || !(convertView.getTag() instanceof ItemHolder)){
				convertView =  LayoutInflater.from(mContext).inflate(R.layout.menu_item, null, true);
				holder = new ItemHolder();
				holder.menuName = (TextView) convertView.findViewById(R.id.menuItem_tv);
			}else{
				holder = (ItemHolder)convertView.getTag(R.layout.menu_item);
			}
			convertView.setTag(holder);
			convertView.setTag(R.layout.menu_item, holder);
			MenuItem item = mItems.get(position);
			holder.menuName.setText(item.MenuValue);
			holder.menuName.setTextSize(SET_TYPE_TEXT_SIZE);
			convertView.setTag(item.MenuKey);
			return convertView;
		}
		
	}
	
	public static class ItemHolder{
		TextView menuName;
	}
	
	public class MenuItem implements Comparable{
		int MenuKey;
		String MenuValue;
		
		public MenuItem(int key, String value){
			MenuKey = key;
			MenuValue = value;
		}
		
		@Override
		public int compareTo(Object o){
			return this.MenuKey - ((MenuItem)o).MenuKey;
		}
	}

}
