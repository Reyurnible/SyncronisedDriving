/*
 * Android USB Serial Monitor Lite
 * 
 * Copyright (C) 2012 Keisuke SUZUKI
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * thanks to Arun.
 */
package com.reyurnible.syncronizeddrivind;

import java.text.BreakIterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request.Priority;
import com.physicaloid.lib.Physicaloid;
import com.physicaloid.lib.usb.driver.uart.UartConfig;
import com.reyurnible.network.BasicJSONRequestTask;
import com.reyurnible.network.MySSLSocketFactory;
import com.reyurnible.network.NetworkTaskCallback;
import com.reyurnible.network.NetworkTasks;
import com.reyurnible.network.TranslateJSON;
import com.reyurnible.network.UriManager;
import com.reyurnible.network.VolleyHelper;
import com.reyurnible.object.VehicleData;
import com.reyurnible.object.VehicleInfo;
import com.reyurnible.system.Account;
import com.reyurnible.system.AccountHelper;
import com.reyurnible.system.AppConfig;
import com.reyurnible.system.Constants;

public class MainActivity extends Activity implements NetworkTaskCallback, SensorEventListener, OnClickListener {
    static final int PREFERENCE_REV_NUM = 1;
    // debug settings
    private static final boolean SHOW_DEBUG                 = false;
    public static final boolean isICSorHigher = ( Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR2 );
    // Defines of Display Settings
    private static final int DISP_CHAR  = 0;
    private static final int DISP_DEC   = 1;
    private static final int DISP_HEX   = 2;
    // Linefeed Code Settings
    private static final int LINEFEED_CODE_CR   = 0;
    private static final int LINEFEED_CODE_CRLF = 1;
    private static final int LINEFEED_CODE_LF   = 2;
    private Physicaloid mSerial;
    private StringBuilder mText = new StringBuilder();
    private boolean mStop = false;
    String TAG = "AndroidSerialTerminal";
    Handler mHandler = new Handler();
    private int mDisplayType        = DISP_CHAR;
    private int mReadLinefeedCode   = LINEFEED_CODE_LF;
    private int mBaudrate           = 9600;
    private int mDataBits           = UartConfig.DATA_BITS8;
    private int mParity             = UartConfig.PARITY_NONE;
    private int mStopBits           = UartConfig.STOP_BITS1;
    private int mFlowControl        = UartConfig.FLOW_CONTROL_OFF;
    private boolean mRunningMainLoop = false;
    private static final String ACTION_USB_PERMISSION ="jp.ksksue.app.terminal.USB_PERMISSION";
    // Linefeed
    private final static String BR = System.getProperty("line.separator");
    
    
    /*************************************Original************************************************/
    private boolean isStarted = false;
    
    private TextView mLeftMeterTextView;
    private TextView mGearPosTextView;
    //private TextView mRightMeterTextView;    
    private TextView mSyncroMeterAcceleratorTextView;
    private TextView mSyncroMeterBrakeTextView;
    private TextView mSyncroMeterWheelTextView;
    
    private Account mAccount;
    private VehicleInfo mLastCarInfo;
    private JSONObject mParams;
    //ハンドル
    private int mLastChildHundle = 0;
    private int mTotalHundlePoint = 0;
    private int mHundlePoint = 0;
    //アクセル
    private boolean mLastChildAccel = false;
    private int mTotalAccelPoint = 0;
    private int mAccelPoint = 0;
    //ブレーキ
    private int mLastChildBrake = 0;
    private int mTotalBrakePoint = 0;
    private int mBrakePoint = 0;
    
    private int mHundleSyncro = 0;
    private int mAccelSyncro = 0;
    private int mBrakeSyncro = 0;
    
    private SensorManager mSensorManager;
    private boolean mIsSerial = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mLeftMeterTextView = (TextView)findViewById(R.id.main_textview_meter_left);
        //mRightMeterTextView = (TextView)findViewById(R.id.main_textview_meter_right);    
        mGearPosTextView = (TextView)findViewById(R.id.main_textview_meter_gearposn);
        mSyncroMeterAcceleratorTextView =  (TextView)findViewById(R.id.main_textview_top_meter_accelerator); 
        mSyncroMeterBrakeTextView =  (TextView)findViewById(R.id.main_textview_top_meter_brake);
        mSyncroMeterWheelTextView =  (TextView)findViewById(R.id.main_textview_top_meter_wheel);
        findViewById(R.id.main_button_stop).setOnClickListener(this);
        
        // get service
        mSerial = new Physicaloid(this);

        if (SHOW_DEBUG) {
            Log.d(TAG, "New instance : " + mSerial);
        }

        mAccount = AccountHelper.getAccount(getApplicationContext());
        mParams = new JSONObject();
        try {
			mParams.put("developerkey", AppConfig.DEVELOPERKEY);
			mParams.put("responseformat", "json");
			mParams.put("infoids", "[Posn,Spd,EngN,SteerAg,BrkIndcr,AccrPedlRat,HdLampLtgIndcn,WiprSts,TrsmGearPosn]");
			mParams.put("vid",mAccount.getVid());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        BasicJSONRequestTask task = new BasicJSONRequestTask(this,getClass().getSimpleName());
        task.onRequest(VolleyHelper.getRequestQueue(getApplicationContext()),
        		Priority.HIGH,
        		UriManager.postUriCarInfo(mAccount.getVid()),
        		NetworkTasks.CarInfo,
        		mParams);
        // listen for new devices
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
        openUsbSerial();
        
        Intent intent = getIntent();
        mIsSerial = intent.getBooleanExtra(Constants.EXTRA_MAIN_BOOLEAN_TYPE, true);
        if(!mIsSerial){
        	mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        }
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	if(!mIsSerial){
    		//Listenerの登録解除
        	mSensorManager.unregisterListener(this);
    	}
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	VolleyHelper.RequestStart();
    	if(!mIsSerial){
	  		// Listenerの登録
	    	List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
	    	if(sensors.size() > 0) {
	    		Sensor s = sensors.get(0);
	    		mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
	    	}
    	}
    }
    
	@Override
	protected void onPause(){
		super.onPause();
		VolleyHelper.RequestStop();
		//リクエストのキャンセル処理
		VolleyHelper.RequestCancell(this.getClass().getSimpleName());
	}
    
    @Override
    public void onDestroy() {
        mSerial.close();
        mStop = true;
        unregisterReceiver(mUsbReceiver);
        closeUsbSerial();
        super.onDestroy();
    }

    private void mainloop() {
        mStop = false;
        mAccelPoint = 0;
        mBrakePoint = 0;
        mHundlePoint = 0;
        mTotalAccelPoint = 0;
        mTotalBrakePoint = 0;
        mTotalHundlePoint = 0;
        
        mRunningMainLoop = true;
        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
        if (SHOW_DEBUG) {
            Log.d(TAG, "start mainloop");
        }
        new Thread(mLoop).start();
    }
    /**********************************************************************/
    //値の取得
    private Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            int len;
            byte[] rbuf = new byte[4096];
            for (;;) {// this is the main loop for transferring
                // ////////////////////////////////////////////////////////
                // Read and Display to Terminal
                // ////////////////////////////////////////////////////////
                len = mSerial.read(rbuf);
                rbuf[len] = 0;
                if (len > 0) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Read  Length : " + len);
                    }
                    switch (mDisplayType) {
                    case DISP_CHAR:
                        setSerialDataToTextView(mDisplayType, rbuf, len, "", "");
                        break;
                    case DISP_DEC:
                        setSerialDataToTextView(mDisplayType, rbuf, len, "013", "010");
                        break;
                    case DISP_HEX:
                        setSerialDataToTextView(mDisplayType, rbuf, len, "0d", "0a");
                        break;
                    }
	                mHandler.post(new Runnable() {
	                    public void run() {
	                    	if(!mText.toString().contains("{\"")){
	                        	mText.insert(0, "{\"");
	                        }
	                        try {
								JSONObject dataObject = new JSONObject(mText.toString());
								//TODO
								
								//ハンドルの値を取得
								double hnd = dataObject.getDouble("hnd");
								mLastChildHundle = judgeHundle(false, (int)hnd);
								//アクセル
								int accel = dataObject.getInt("ACC1");
								mLastChildAccel = accel==1?true:false;
								//ブレーキ
								mLastChildBrake = dataObject.getInt("BK1");
								
							} catch (JSONException e) {
								e.printStackTrace();
							}
	                        mText.setLength(0);
	                    }
	                });
                }
	            try {
	                Thread.sleep(100);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	
	            if (mStop) {
	                mRunningMainLoop = false;
	                return;
	            }
            }
        }
    };
    /**********************************************************************/
    
    private String IntToHex2(int Value) {
        char HEX2[] = {
                Character.forDigit((Value >> 4) & 0x0F, 16),
                Character.forDigit(Value & 0x0F, 16)
        };
        String Hex2Str = new String(HEX2);
        return Hex2Str;
    }

    boolean lastDataIs0x0D = false;

    void setSerialDataToTextView(int disp, byte[] rbuf, int len, String sCr, String sLf) {
        int tmpbuf;
        for (int i = 0; i < len; ++i) {
            if (SHOW_DEBUG) {
                Log.d(TAG, "Read  Data[" + i + "] : " + rbuf[i]);
            }
            // "\r":CR(0x0D) "\n":LF(0x0A)
            if ((mReadLinefeedCode == LINEFEED_CODE_CR) && (rbuf[i] == 0x0D)) {
                mText.append(sCr);
                mText.append(BR);
            } else if ((mReadLinefeedCode == LINEFEED_CODE_LF) && (rbuf[i] == 0x0A)) {
                mText.append(sLf);
                mText.append(BR);
            } else if ((mReadLinefeedCode == LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D)
                    && (rbuf[i + 1] == 0x0A)) {
                mText.append(sCr);
                if (disp != DISP_CHAR) {
                    mText.append(" ");
                }
                mText.append(sLf);
                mText.append(BR);
                ++i;
            } else if ((mReadLinefeedCode == LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D)) {
                // case of rbuf[last] == 0x0D and rbuf[0] == 0x0A
                mText.append(sCr);
                lastDataIs0x0D = true;
            } else if (lastDataIs0x0D && (rbuf[0] == 0x0A)) {
                if (disp != DISP_CHAR) {
                    mText.append(" ");
                }
                mText.append(sLf);
                mText.append(BR);
                lastDataIs0x0D = false;
            } else if (lastDataIs0x0D && (i != 0)) {
                // only disable flag
                lastDataIs0x0D = false;
                --i;
            } else {
                switch (disp) {
                    case DISP_CHAR:
                        mText.append((char) rbuf[i]);
                        break;
                    case DISP_DEC:
                        tmpbuf = rbuf[i];
                        if (tmpbuf < 0) {
                            tmpbuf += 256;
                        }
                        mText.append(String.format("%1$03d", tmpbuf));
                        mText.append(" ");
                        break;
                    case DISP_HEX:
                        mText.append(IntToHex2((int) rbuf[i]));
                        mText.append(" ");
                        break;
                    default:
                        break;
                }
            }
        }
    }

    // Load default baud rate
    int loadDefaultBaudrate() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String res = pref.getString("baudrate_list", Integer.toString(9600));
        return Integer.valueOf(res);
    }
    
    private void openUsbSerial() {
        if(mSerial == null) {
            Toast.makeText(this, "cannot open", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mSerial.isOpened()) {
            if (SHOW_DEBUG) {
                Log.d(TAG, "onNewIntent begin");
            }
            if (!mSerial.open()) {
                Toast.makeText(this, "cannot open", Toast.LENGTH_SHORT).show();
                return;
            } else {
                boolean dtrOn=false;
                boolean rtsOn=false;
                if(mFlowControl == UartConfig.FLOW_CONTROL_ON) {
                    dtrOn = true;
                    rtsOn = true;
                }
                mSerial.setConfig(new UartConfig(mBaudrate, mDataBits, mStopBits, mParity, dtrOn, rtsOn));
                if(SHOW_DEBUG) {
                    Log.d(TAG, "setConfig : baud : "+mBaudrate+", DataBits : "+mDataBits+", StopBits : "+mStopBits+", Parity : "+mParity+", dtr : "+dtrOn+", rts : "+rtsOn);
                }
                Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
            }
        }
        
        if (!mRunningMainLoop) {
            mainloop();
        }
    }
    //TODO どこかで接続を解除する
    private void closeUsbSerial() {
        mStop = true;
        mSerial.close();
    }

    protected void onNewIntent(Intent intent) {
        if (SHOW_DEBUG) {
            Log.d(TAG, "onNewIntent");
        }
        openUsbSerial();
    };

    // BroadcastReceiver when insert/remove the device USB plug into/from a USB
    // port
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Device attached");
                }
                if (!mSerial.isOpened()) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Device attached begin");
                    }
                    openUsbSerial();
                }
                if (!mRunningMainLoop) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Device attached mainloop");
                    }
                    mainloop();
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Device detached");
                }
                mStop = true;
                //mSerial.usbDetached(intent);
                mSerial.close();
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                if (SHOW_DEBUG) {
                    Log.d(TAG, "Request permission");
                }
                synchronized (this) {
                    if (!mSerial.isOpened()) {
                        if (SHOW_DEBUG) {
                            Log.d(TAG, "Request permission begin");
                        }
                        openUsbSerial();
                    }
                }
                if (!mRunningMainLoop) {
                    if (SHOW_DEBUG) {
                        Log.d(TAG, "Request permission mainloop");
                    }
                    mainloop();
                }
            }
        }
    };

	@Override
	public void onSuccessNetworkTask(int taskId, Object object) {
		VehicleInfo info = TranslateJSON.vehicleParse((JSONObject)object);
		mHundleSyncro = 0;
	    mAccelSyncro = 0;
	    mBrakeSyncro = 0;
	    
		if(isStarted || AppConfig.DEBUG){
			try{
				//ハンドル
				if(judgeHundle(true,info.data.steerAg) == mLastChildHundle){
				    mHundlePoint++;
				}
				mTotalHundlePoint++;
				//%表示
				mHundleSyncro = (int)(mHundlePoint/mTotalHundlePoint*100);
		        mSyncroMeterWheelTextView.setText(Integer.toString(mHundleSyncro)+"%");
		        if(mHundleSyncro<1){
		        	mHundlePoint = 0;
		        	mTotalHundlePoint = 0;
		        }
			}catch(NullPointerException e){
			}
	        try{
	        	//アクセル
	    		boolean parentAccel = (mLastCarInfo.data.accrPedlRat < info.data.accrPedlRat);
	    		if(parentAccel==mLastChildAccel){
	    			mAccelPoint++;
	    		}
	    		mTotalAccelPoint++;
	    		//%表示
	            mAccelSyncro = (int)(mAccelPoint/mTotalAccelPoint*100);
	            mSyncroMeterAcceleratorTextView.setText(Integer.toString(mAccelSyncro)+"%");
	            if(mAccelSyncro<1){
	            	mAccelPoint=0;
	            	mTotalAccelPoint=0;
	            }
	        }catch(NullPointerException e){
	        }
			try{
				//ブレーキ
				if(info.data.brkIndcr==mLastChildBrake){
					mBrakePoint++;
				}
				mTotalBrakePoint++;
		        //%表示
		        mBrakeSyncro = (int)(mBrakePoint/mTotalBrakePoint*100);
		        mSyncroMeterBrakeTextView.setText(Integer.toString(mBrakeSyncro)+"%");
		        if(mBrakeSyncro<1){
		        	mBrakePoint++;
		        	mTotalBrakePoint++;
		        }
			}catch(NullPointerException e){
			}
		}
		mLastCarInfo =info;
		mLeftMeterTextView.setText(Double.toString(mLastCarInfo.data.spd));
        //mRightMeterTextView.setText(Integer.toString(mLastCarInfo.data.engN));
        
        mGearPosTextView.setText(mLastCarInfo.data.trsmGearPosn);
        
        if(mLastCarInfo.data.trsmGearPosn.equals("D") && mLastCarInfo.data.engN>0 && !isStarted){
        	isStarted = true;
        	initialize();
        }else if(mLastCarInfo.data.trsmGearPosn.equals("P") && mLastCarInfo.data.brkIndcr == 1 && isStarted){
        	//終了。結果画面に
        	Intent intent = new Intent(getApplicationContext(),FinishActivity.class);
        	intent.putExtra(Constants.EXTRA_FINISH_INT_ACCEL, mAccelSyncro);
        	intent.putExtra(Constants.EXTRA_FINISH_INT_BRAKE, mBrakeSyncro);
        	intent.putExtra(Constants.EXTRA_FINISH_INT_HUNDLE, mHundleSyncro);
        	startActivity(intent);
        }
        
        //新たなリクエスト
  		BasicJSONRequestTask task = new BasicJSONRequestTask(this,getClass().getSimpleName());
          task.onRequest(VolleyHelper.getRequestQueue(getApplicationContext()),
          		Priority.HIGH,
          		UriManager.postUriCarInfo(mAccount.getVid()),
          		NetworkTasks.CarInfo,
          		mParams);
	}

	@Override
	public void onFailedNetworkTask(int taskId) {
		//TODO
	}
	
	private void initialize(){
	    mTotalHundlePoint = 0;
	    mHundlePoint = 0;
	    //アクセル
	    mTotalAccelPoint = 0;
	    mAccelPoint = 0;
	    //ブレーキ
	    mTotalBrakePoint = 0;
	    mBrakePoint = 0;
	}
	
    //0->no 1->right 2->left
	public int judgeHundle(boolean isParent,int hundleValue){
		if(isParent){
			if(hundleValue<-90){
				return 1;
			}else if(hundleValue>90){
				return 2;
			}
		}else{
			if(hundleValue<-60){
				return 1;
			}else if(hundleValue>60){
				return 2;
			}
		}
		return 0;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(!mIsSerial){
			if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				float x = event.values[SensorManager.DATA_X];
				float y = event.values[SensorManager.DATA_Y]; 
				float z = event.values[SensorManager.DATA_Z];
				double hnd = Math.asin((double)x)/3.14*180;
				mLastChildHundle = judgeHundle(false,(int)hnd);
			}
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() ==  R.id.main_button_stop){
			Intent intent = new Intent(getApplicationContext(),FinishActivity.class);
	    	intent.putExtra(Constants.EXTRA_FINISH_INT_ACCEL, mAccelSyncro);
	    	intent.putExtra(Constants.EXTRA_FINISH_INT_BRAKE, mBrakeSyncro);
	    	intent.putExtra(Constants.EXTRA_FINISH_INT_HUNDLE, mHundleSyncro);
	    	startActivity(intent);
		}
	}
}
