package com.nnit.phonebook.ui;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;

public class MyAnimationDrawable extends AnimationDrawable {
	
	public static final int MSG_ANIMATION_FINISHED = 1;
	
	private Handler finishHandler = new AnimationHandler();
	
	
	private List<IFrameAnimationListener> listeners = new ArrayList<IFrameAnimationListener>();
	
	public void addFrameAnimationListener(IFrameAnimationListener listener){
		listeners.add(listener);
	}
	
	public void removeFrameAnimationListener(IFrameAnimationListener listener){
		listeners.remove(listener);
	}
	
	@Override
	public void start(){
		for(IFrameAnimationListener listener: listeners){
			listener.onAnimationStart();
		}
		super.start();
		checkIfAnimationDone();
				
	}

	public int getCheckInterval(){
		return 10;
	}
	
	private void checkIfAnimationDone(){
		finishHandler.postDelayed(new Runnable(){
			public void run(){
				int numberOfFrames = MyAnimationDrawable.this.getNumberOfFrames();
				if(MyAnimationDrawable.this.getCurrent()!= MyAnimationDrawable.this.getFrame(numberOfFrames -1)){
					checkIfAnimationDone();
				}else{
					finishHandler.sendEmptyMessage(MSG_ANIMATION_FINISHED);
				}
			}
		},getCheckInterval());
	}
	
	private class AnimationHandler extends Handler{
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
				case MSG_ANIMATION_FINISHED:
					for(IFrameAnimationListener listener: listeners){
						listener.onAnimationEnd();
					}
					break;
				default:
					break;
			}
		}
	}
}
