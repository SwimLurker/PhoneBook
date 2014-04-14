package com.nnit.phonebook;

import java.io.IOException;
import java.io.InputStream;

import com.nnit.phonebook.data.SeatMapInfo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MapActivity extends Activity{
	public static final float MIN_SCALE = 0.2f;
	public static final float MAX_SCALE = 2f;
	
	private ImageView mapImageView;
	private PointF start = new PointF();
	private PointF mid = new PointF();
	
	private int mapViewWidth, mapViewHeight;
	private int bitmapWidth, bitmapHeight;
	private float beforeLength;
	
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	
	private SeatMapInfo seatInfo = null;
	
	
	private enum MODE{
		NONE, DRAG, ZOOM
	}
	
	private MODE mode = MODE.NONE;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_map);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
//		screenWidth = dm.widthPixels;
//		screenHeight = dm.heightPixels;
		
		
		String initials = (String) getIntent().getExtras().get(DetailActivity.TARGET_INITIAL);
		
		seatInfo = getSeatInfo(initials);
		
		Bitmap bitmap = prepareSeatBitmap(seatInfo);
		
		
		
		mapImageView = (ImageView)findViewById(R.id.image_map);
		
		
		mapImageView.setImageBitmap(bitmap);
		mapImageView.setImageMatrix(matrix);
		
		mapImageView.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				ImageView view = (ImageView)v;
				switch(event.getAction() & MotionEvent.ACTION_MASK){
					case MotionEvent.ACTION_DOWN:
						onTouchDown(event);
						break;
					case MotionEvent.ACTION_UP:
						onTouchUp(event);
						break;
					case MotionEvent.ACTION_POINTER_UP:
						onPointerUp(event);
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						onPointerDown(event);
						break;
					case MotionEvent.ACTION_MOVE:
						onTouchMove(event);
						break;
				}
				view.setImageMatrix(matrix);
				
				checkScale();
				center();
				
				
				return true;
			}
		});
		
		mapImageView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener(){

			@Override
			public void onGlobalLayout() {
				if(mapViewHeight == 0){
					Rect frame = new Rect();
					getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
					mapViewHeight = frame.height();
					mapViewWidth = frame.width();
				}
			}
		});
		
		ImageButton closeBtn = (ImageButton)findViewById(R.id.btn_close);
		
		closeBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MapActivity.this.finish();
			}
			
		});
		
		ImageButton zoomOutBtn = (ImageButton)findViewById(R.id.btn_zoom_out);
		
		zoomOutBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*matrix.postScale(0.8f, 0.8f);
				mapImageView.setImageMatrix(matrix);
				checkScale();				
				center();*/
				
				float p[] = new float[9];
				matrix.getValues(p);
				if((p[0] * 0.8f)>=MIN_SCALE){
					matrix.postScale(0.8f, 0.8f);
				}
				center();
				mapImageView.setImageMatrix(matrix);	
			}
			
		});
		
		ImageButton zoomInBtn = (ImageButton)findViewById(R.id.btn_zoom_in);
		
		zoomInBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				float p[] = new float[9];
				matrix.getValues(p);
				if((p[0] * 1.25f)<=MAX_SCALE){
					matrix.postScale(1.25f, 1.25f);
				}
				center();
				mapImageView.setImageMatrix(matrix);
				
			}
			
		});
		
		ImageButton locateBtn = (ImageButton)findViewById(R.id.btn_locate);
		
		locateBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				float p[] = new float[9];
				matrix.getValues(p);
				float currentScale = p[0];
				
				RectF seatRect = new RectF(seatInfo.getSeatRect());
				RectF mapRect = new RectF(0, 0, bitmapWidth, bitmapHeight);
				
				Matrix m = new Matrix();
				m.set(matrix);
				m.mapRect(seatRect);
				m.mapRect(mapRect);
				
				
				
				float centerX = seatRect.left + seatRect.width()/2 ;
				float centerY = seatRect.top + seatRect.height()/2;
				
				float mapViewCenterX = mapViewWidth /2;
				float mapViewCenterY = mapViewHeight /2;
				
				float deltaX = 0, deltaY = 0;
				
				deltaX = mapViewCenterX - centerX;
				deltaY = mapViewCenterY - centerY;
				
				Matrix m1 = new Matrix();
				m1.set(matrix);
				m1.setScale(p[0], p[0]);
				//matrix.setTranslate(deltaX, deltaY);
				m1.postTranslate(deltaX, deltaY);
				
				float p2[] = new float[9];
				
				m1.getValues(p2);
				
				matrix.set(m1);
				mapImageView.setImageMatrix(matrix);
				
			}
			
		});
		
	}
	
	private Bitmap prepareSeatBitmap(SeatMapInfo seatInfo) {
		
		Bitmap seatBmp = null;
	
		try {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.RGB_565;
			opt.inPurgeable = true;
			opt.inInputShareable = true;
			
			InputStream is = getResources().getAssets().open(seatInfo.getMapFilename());
			
			if(is != null){
				Bitmap mapBmp = BitmapFactory.decodeStream(is, null, opt);
				
				is.close();
			
				bitmapWidth = mapBmp.getWidth();
				bitmapHeight = mapBmp.getHeight();
				
				seatBmp = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565);
			
				Canvas canvas = new Canvas(seatBmp);
			
				Paint paint = new Paint();
				paint.setColor(Color.RED);
				paint.setStrokeWidth(10);
			
				Matrix m1 = new Matrix();
				//m1.setScale(0.1f, 0.1f);
			
				canvas.drawBitmap(mapBmp, m1, paint);
			
				RectF rect = seatInfo.getSeatRect();
			
				canvas.drawLine(rect.left, rect.top, rect.left, rect.bottom, paint);
				canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, paint);
				canvas.drawLine(rect.right, rect.bottom, rect.right, rect.top, paint);
				canvas.drawLine(rect.right, rect.top, rect.left, rect.top, paint);
			}
		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return seatBmp;
	}

	private SeatMapInfo getSeatInfo(String initials) {
		SeatMapInfo result = new SeatMapInfo();
		result.setInitials(initials);
		result.setSeatRect(new RectF(285, 729, 285 + 72, 729 + 44));
		result.setDirection(0);
		result.setFloorNo(28);
		result.setMapFilename("CN.TJ.JW.28.png");
		return result;
	}

	private void onTouchDown(MotionEvent event){
		savedMatrix.set(matrix);
		start.set(event.getX(), event.getY());
		mode = MODE.DRAG;
	}
	
	private void onTouchUp(MotionEvent event){
		mode = MODE.NONE;
	}
	
	private void onPointerUp(MotionEvent event){
		mode = MODE.NONE;
	}
	
	private void onPointerDown(MotionEvent event){
		if(event.getPointerCount() == 2){
			beforeLength = getDistance(event);
			if(beforeLength > 10f){
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = MODE.ZOOM;
			}
		}
	}
	private void onTouchMove(MotionEvent event){
		if(mode == MODE.DRAG){
			matrix.set(savedMatrix);
			matrix.postTranslate(event.getX()-start.x, event.getY() - start.y);
		}else if(mode == MODE.ZOOM){
			float afterLength = getDistance(event);
			if(afterLength > 10f){
				matrix.set(savedMatrix);
				float scale = afterLength / beforeLength;
				matrix.postScale(scale, scale, mid.x, mid.y);
			}
		}
	}
	private float getDistance(MotionEvent event){
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		
		return FloatMath.sqrt(x * x + y * y);
	}
	
	private void midPoint(PointF point, MotionEvent event){
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		
		point.set(x/2, y/2);
	}
	
	protected void checkScale(){
		float p[] = new float[9];
		matrix.getValues(p);
		if(mode == MODE.ZOOM){
			if(p[0] < MIN_SCALE){
				matrix.setScale(MIN_SCALE, MIN_SCALE);
			}
			if(p[0] > MAX_SCALE){
				matrix.set(savedMatrix);
			}
		}
	}
	
	protected void center(){
		center(true, true);
	}
	
	private void center(boolean horizontal, boolean vertical){
		Matrix m = new Matrix();
		m.set(matrix);
		RectF rect = new RectF(0, 0, bitmapWidth, bitmapHeight);
		m.mapRect(rect);
		float height = rect.height();
		float width = rect.width();
		
		float deltaX = 0, deltaY = 0;
		
		if(vertical){
			if(height < mapViewHeight){
				deltaY = (mapViewHeight - height)/2 - rect.top;				
			}else if(rect.top > 0){
				deltaY = -rect.top;
			}else if(rect.bottom < mapViewHeight){
				deltaY = mapViewHeight - rect.bottom;
			}
		}
		
		if(horizontal){
			if(width < mapViewWidth){
				deltaX = (mapViewWidth - width)/2 - rect.left;
			}else if(rect.left > 0){
				deltaX = -rect.left;
			}else if(rect.right < mapViewWidth){
				deltaX = mapViewWidth - rect.right;
			}
		}
		matrix.postTranslate(deltaX, deltaY);
		
	}
}
