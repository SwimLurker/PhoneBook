package com.nnit.phonebook;

import java.util.ArrayList;

import com.nnit.phonebook.config.ConfigManager;
import com.nnit.phonebook.ui.IFrameAnimationListener;
import com.nnit.phonebook.ui.MyAnimationDrawable;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class GuideActivity extends Activity{
	private ViewPager viewPager;
	private ArrayList<View> pageViews;
	private ImageView[] imageViews;
	
	private CheckBox showGuidePageCB = null;
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        
	        LayoutInflater inflater = getLayoutInflater();
	        
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
			
			ViewGroup main = (ViewGroup) inflater.inflate(R.layout.activity_guide, null);
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
					startMainActivityWithAnimation();
					//startMainActivityWithFrameAnimation();
				}
			});
			showGuidePageCB = (CheckBox) guide5PageView.findViewById(R.id.guidepage_showguide);
	 }
	 
	 private void startMainActivity(){
		
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_SHOWGUIDEPAGE, showGuidePageCB.isChecked()?"1":"0");
		Intent intent = new Intent();
		intent.setAction("com.nnit.phonebook.MainActivity");
		startActivity(intent);
		finish();
    }
    
    private void startMainActivityWithAnimation() {
    	Animation ani = new AlphaAnimation(1.0f, 0.0f);
    	
    	ani.setDuration(1000);	
    	ani.setFillAfter(true);
    	
    	RelativeLayout guidePageLayout= (RelativeLayout)findViewById(R.id.guidepage_layout);
		guidePageLayout.setAnimation(ani);
		
		ani.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				startMainActivity();
				
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				
				
			}
			
		});
		guidePageLayout.startAnimation(ani);
    }
    
    private void startMainActivityWithFrameAnimation(){
    	
    	Bitmap screenShot = getScreenShot(this);
    	
    	final ImageView aniImageView = (ImageView)findViewById(R.id.guidepage_animation_image);
		
		MyAnimationDrawable anim = new MyAnimationDrawable();
		
		int totalFrameCount = 20;
		for(int i = 0;i<=totalFrameCount; i++){
			anim.addFrame(getAnimationFrame(screenShot, i, totalFrameCount), 1000/totalFrameCount);
		}
		
		anim.addFrameAnimationListener(new IFrameAnimationListener(){
			
			@Override
			public void onAnimationStart() {
				RelativeLayout guidePageLayout= (RelativeLayout)findViewById(R.id.guidepage_layout);
				guidePageLayout.setVisibility(View.GONE);
				aniImageView.setVisibility(View.VISIBLE);
				Log.d("Animation", "animation start");
			}
			
			@Override
			public void onAnimationEnd() {
				startMainActivity();
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
							startMainActivityWithAnimation();
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
