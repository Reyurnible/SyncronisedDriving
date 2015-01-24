package com.reyurnible.syncronizeddrivind;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class SplashActivity extends Activity {

	private Canvas mCanvas;
	private Bitmap mBitmap;
	private Paint mPaint;
	private ImageView mImageView;
	private int mRadius;
	private Point mCenter;
	private int mAngle;
	private Handler mHandler;
	private Timer mTimer;
	private Bitmap mCarBitmap;
	private BitmapDrawable mDrawable;
	private boolean isFirst = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		
		mImageView = (ImageView)findViewById(R.id.splash_imageview_container);
		mCarBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.img_cartopview);
		//BitmapからBitmapDrawableを生成
        mDrawable = new BitmapDrawable(mCarBitmap);

		mHandler = new Handler();
		mTimer = new Timer(false);
		
      	// タイマーの設定
     	mTimer.schedule(new TimerTask(){
     		@Override
     		public void run() {
     			mHandler.post(new Runnable() {
     				@Override
     				public void run() {
     					if(mRadius==0)return ;
     					if(mAngle>720){
     						Intent intent = new Intent(getApplicationContext(),StartActivity.class);
     						startActivity(intent);
     						finish();
     						mTimer.cancel();
     					}else{
     						onProgressDraw();
         					mAngle+=15;
     					}
     				}
     			});
     		}
     	}, 300, 100); // 300 ミリ秒ごとにタイマーを起動
	      	
	}

	@Override 
	public void onWindowFocusChanged(boolean hasFocus) {  
	    super.onWindowFocusChanged(hasFocus);
	    
	    mRadius = (int)(Math.min(mImageView.getWidth(), mImageView.getHeight())/3);
		mCenter = new Point(mImageView.getWidth()/2, mImageView.getHeight()/2);
		mAngle = 0;
	    
	    mBitmap=Bitmap.createBitmap(mImageView.getWidth(),mImageView.getHeight(),Bitmap.Config.ARGB_8888);
	    //drawableの描画領域設定（必須）
	    mDrawable.setBounds(0,0,mCarBitmap.getWidth(),mCarBitmap.getHeight());
      	mCanvas = new Canvas(mBitmap);
      	
      	//背景を描画、（）の引数が塗りつぶす色
      	mCanvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);
      	mPaint=new Paint();
      	mPaint.setAntiAlias(true);
      	mImageView.setImageBitmap(mBitmap);
	}
	
	 private void onProgressDraw(){
	    	Double angleRadian = (Math.PI/180*mAngle);
	    	float x = 0;
	    	float y = 0;
	    	mCanvas.drawColor(Color.WHITE);
	    	mPaint.setColor(Color.argb(30,0,0,0));
	    	mPaint.setFilterBitmap(true);
	    	for(int i=0;i<mAngle%720;i++){
	    		if(mAngle%720 >180 && mAngle%720 < 540){
		    		x = (float)(mCenter.x - mRadius* Math.cos(angleRadian) - mRadius);
		    	}else{
		    		x = (float)(mCenter.x + mRadius* Math.cos(angleRadian) + mRadius);
		    	}
	    		y =(float)(mRadius*Math.sin(angleRadian)+mCenter.y);
	    		//mCanvas.drawCircle(x,y,20,mPaint);
	    	}
	    	mPaint.setColor(Color.argb(255,0,0,0));
	    	mCanvas.rotate(mAngle, x, y);
	        mCanvas.drawBitmap(mCarBitmap, x,y, mPaint);
	    	
	    	mImageView.setImageBitmap(mBitmap);
	 }
}
