/* * Android USB Serial Monitor Lite *  * Copyright (C) 2012 Keisuke SUZUKI * Licensed under the Apache License, Version 2.0 * http://www.apache.org/licenses/LICENSE-2.0 *  * thanks to Arun. */package com.reyurnible.syncronizeddrivind.view.activity;import android.animation.ValueAnimator;import android.app.Activity;import android.app.FragmentTransaction;import android.content.BroadcastReceiver;import android.content.Context;import android.content.Intent;import android.content.IntentFilter;import android.hardware.Sensor;import android.hardware.SensorEvent;import android.hardware.SensorEventListener;import android.hardware.SensorManager;import android.hardware.usb.UsbManager;import android.location.Location;import android.os.Handler;import android.util.Log;import android.view.animation.RotateAnimation;import android.widget.FrameLayout;import android.widget.ImageView;import android.widget.LinearLayout;import android.widget.TextView;import android.widget.Toast;import com.physicaloid.lib.Physicaloid;import com.physicaloid.lib.usb.driver.uart.UartConfig;import com.reyurnible.syncronizeddrivind.R;import com.reyurnible.syncronizeddrivind.model.object.CarData;import com.reyurnible.syncronizeddrivind.model.object.DrivingPoint;import com.reyurnible.syncronizeddrivind.model.object.VehicleInfo;import com.reyurnible.syncronizeddrivind.model.system.Account;import com.reyurnible.syncronizeddrivind.model.system.AccountHelper;import com.reyurnible.syncronizeddrivind.model.system.Constants;import com.reyurnible.syncronizeddrivind.view.fragment.LocationManagerFragment;import com.reyurnible.syncronizeddrivind.view.fragment.ToyotaGetCarInfoFragment;import org.androidannotations.annotations.AfterViews;import org.androidannotations.annotations.EActivity;import org.androidannotations.annotations.Extra;import org.androidannotations.annotations.ViewById;import org.json.JSONException;import org.json.JSONObject;import java.util.List;@EActivity(R.layout.activity_main)public class MainActivity extends Activity implements ToyotaGetCarInfoFragment.OnUpdateDataListener, SensorEventListener , LocationManagerFragment.OnUpdateLocationListener{    // Defines of Display Settings    private static final int DISP_CHAR = 0;    private static final int DISP_DEC = 1;    private static final int DISP_HEX = 2;    // Linefeed Code Settings    private static final int LINEFEED_CODE_CR = 0;    private static final int LINEFEED_CODE_CRLF = 1;    private static final int LINEFEED_CODE_LF = 2;    private Physicaloid mSerial;    private StringBuilder mText = new StringBuilder();    private boolean mStop = false;    String TAG = "AndroidSerialTerminal";    Handler mHandler = new Handler();    private int mDisplayType = DISP_CHAR;    private int mReadLinefeedCode = LINEFEED_CODE_LF;    private int mBaudrate = 9600;    private int mDataBits = UartConfig.DATA_BITS8;    private int mParity = UartConfig.PARITY_NONE;    private int mStopBits = UartConfig.STOP_BITS1;    private int mFlowControl = UartConfig.FLOW_CONTROL_OFF;    private boolean mRunningMainLoop = false;    private static final String ACTION_USB_PERMISSION = "jp.ksksue.app.terminal.USB_PERMISSION";    // Linefeed    private static final String BR = System.getProperty("line.separator");    /**     * **********************************Original***********************************************     */    private static final int ANIMATION_DURATION = 700;    private boolean isStarted = false;    @ViewById(R.id.main_textview_meter_left)    TextView mLeftMeterTextView;    // スピードメーター    @ViewById(R.id.main_imageview_myspeed)    ImageView mMySpeedImageView;    // ハンドル    @ViewById(R.id.main_imageview_hundle_user)    ImageView mUserHundleImageView;    @ViewById(R.id.main_imageview_hundle_car)    ImageView mCarHundleImageView;    // アクセル    @ViewById(R.id.main_framelayout_accel_user)    FrameLayout mUserAccelFrameLayout;    @ViewById(R.id.main_imageview_accel_user)    ImageView mUserAccelImageView;    @ViewById(R.id.main_imageview_accel_car)    ImageView mCarAccelImageView;    @ViewById(R.id.main_layout_accel_car)    LinearLayout mCarAccelLayout;    @ViewById(R.id.main_imageview_brake)    ImageView mBrakeImageView;    private VehicleInfo mLastCarInfo;    private CarData mUserCarData = new CarData(0, false, 0);    private CarData mLastUserCarData = new CarData(0, false, 0);    private DrivingPoint mDrivingPoint = new DrivingPoint(0, 0, 0, 0, 0, 0);    private SensorManager mSensorManager;    private Location mGPSLocation;    @Extra("is_serial")    boolean mIsSerial = false;    /**     * ******************************************************************     */    //値の取得    private Runnable mLoop = new Runnable() {        @Override        public void run() {            int len;            byte[] rbuf = new byte[4096];            while (true) {                // this is the main loop for transferring                // ////////////////////////////////////////////////////////                // Read and Display to Terminal                // ////////////////////////////////////////////////////////                len = mSerial.read(rbuf);                rbuf[len] = 0;                if (len > 0) {                    Log.d(TAG, "Read  Length : " + len);                    switch (mDisplayType) {                        case DISP_CHAR:                            setSerialDataToTextView(mDisplayType, rbuf, len, "", "");                            break;                        case DISP_DEC:                            setSerialDataToTextView(mDisplayType, rbuf, len, "013", "010");                            break;                        case DISP_HEX:                            setSerialDataToTextView(mDisplayType, rbuf, len, "0d", "0a");                            break;                    }                    mHandler.post(new Runnable() {                        public void run() {                            if (!mText.toString().contains("{\"")) {                                mText.insert(0, "{\"");                            }                            try {                                JSONObject dataObject = new JSONObject(mText.toString());                                mUserCarData = new CarData(                                        (int) dataObject.getDouble("hnd"),                                        (dataObject.getInt("ACC1") == 1),                                        dataObject.getInt("BK1")                                );                                updateSpeedMeter(null);                                updateHundle(null);                                updateAccel(null);                                updaateBrake();                            } catch (JSONException e) {                                e.printStackTrace();                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();                            }                            mText.setLength(0);                        }                    });                }                try {                    Thread.sleep(200);                } catch (InterruptedException e) {                    e.printStackTrace();                }                if (mStop) {                    mRunningMainLoop = false;                    return;                }            }        }    };    @AfterViews    void onAfterViews() {        // get service        mSerial = new Physicaloid(this);        Log.d(TAG, "New instance : " + mSerial);        Account account = AccountHelper.getAccount(getApplicationContext());        {            FragmentTransaction transaction = getFragmentManager().beginTransaction();            ToyotaGetCarInfoFragment toyotaGetCarInfoFragment = ToyotaGetCarInfoFragment.newInstance(account.getVid(), 500);            transaction.add(toyotaGetCarInfoFragment, "toyota_get_carinfo");            transaction.commit();        }        {            FragmentTransaction transaction = getFragmentManager().beginTransaction();            LocationManagerFragment locationManagerFragment = LocationManagerFragment.newInstance(500);            transaction.add(locationManagerFragment, "location_uppdate");            transaction.commit();        }        // listen for new devices        IntentFilter filter = new IntentFilter();        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);        registerReceiver(mUsbReceiver, filter);        openUsbSerial();        if (!mIsSerial) {            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);        }    }    @Override    protected void onResume() {        super.onResume();        if (!mIsSerial) {            // Listenerの登録            List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);            if (sensors.size() > 0) {                Sensor s = sensors.get(0);                mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);            }        }    }    @Override    protected void onStop() {        super.onStop();        if (!mIsSerial) {            //Listenerの登録解除            mSensorManager.unregisterListener(this);        }    }    @Override    public void onDestroy() {        unregisterReceiver(mUsbReceiver);        closeUsbSerial();        super.onDestroy();    }    private void mainloop() {        mStop = false;        mDrivingPoint.initialize();        mRunningMainLoop = true;        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();        new Thread(mLoop).start();    }    private String IntToHex2(int Value) {        char HEX2[] = {                Character.forDigit((Value >> 4) & 0x0F, 16),                Character.forDigit(Value & 0x0F, 16)        };        return new String(HEX2);    }    boolean lastDataIs0x0D = false;    void setSerialDataToTextView(int disp, byte[] rbuf, int len, String sCr, String sLf) {        int tmpbuf;        for (int i = 0; i < len; ++i) {            Log.d(TAG, "Read  Data[" + i + "] : " + rbuf[i]);            // "\r":CR(0x0D) "\n":LF(0x0A)            if ((mReadLinefeedCode == LINEFEED_CODE_CR) && (rbuf[i] == 0x0D)) {                mText.append(sCr);                mText.append(BR);            } else if ((mReadLinefeedCode == LINEFEED_CODE_LF) && (rbuf[i] == 0x0A)) {                mText.append(sLf);                mText.append(BR);            } else if ((mReadLinefeedCode == LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D)                    && (rbuf[i + 1] == 0x0A)) {                mText.append(sCr);                if (disp != DISP_CHAR) {                    mText.append(" ");                }                mText.append(sLf);                mText.append(BR);                ++i;            } else if ((mReadLinefeedCode == LINEFEED_CODE_CRLF) && (rbuf[i] == 0x0D)) {                // case of rbuf[last] == 0x0D and rbuf[0] == 0x0A                mText.append(sCr);                lastDataIs0x0D = true;            } else if (lastDataIs0x0D && (rbuf[0] == 0x0A)) {                if (disp != DISP_CHAR) {                    mText.append(" ");                }                mText.append(sLf);                mText.append(BR);                lastDataIs0x0D = false;            } else if (lastDataIs0x0D && (i != 0)) {                // only disable flag                lastDataIs0x0D = false;                --i;            } else {                switch (disp) {                    case DISP_CHAR:                        mText.append((char) rbuf[i]);                        break;                    case DISP_DEC:                        tmpbuf = rbuf[i];                        if (tmpbuf < 0) {                            tmpbuf += 256;                        }                        mText.append(String.format("%1$03d", tmpbuf));                        mText.append(" ");                        break;                    case DISP_HEX:                        mText.append(IntToHex2((int) rbuf[i]));                        mText.append(" ");                        break;                    default:                        break;                }            }        }    }    private void openUsbSerial() {        if (mSerial == null) {            Toast.makeText(this, "cannot open", Toast.LENGTH_SHORT).show();            return;        }        if (!mSerial.isOpened()) {            Log.d(TAG, "onNewIntent begin");            if (!mSerial.open()) {                Toast.makeText(this, "cannot open", Toast.LENGTH_SHORT).show();                return;            } else {                boolean dtrOn = false;                boolean rtsOn = false;                if (mFlowControl == UartConfig.FLOW_CONTROL_ON) {                    dtrOn = true;                    rtsOn = true;                }                mSerial.setConfig(new UartConfig(mBaudrate, mDataBits, mStopBits, mParity, dtrOn, rtsOn));                Log.d(TAG, "setConfig : baud : " + mBaudrate + ", DataBits : " + mDataBits + ", StopBits : " + mStopBits + ", Parity : " + mParity + ", dtr : " + dtrOn + ", rts : " + rtsOn);                Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();            }        }        if (!mRunningMainLoop) {            mainloop();        }    }    private void closeUsbSerial() {        mStop = true;        mSerial.close();    }    protected void onNewIntent(Intent intent) {        Log.d(TAG, "onNewIntent");        openUsbSerial();    }    // BroadcastReceiver when insert/remove the device USB plug into/from a USB    // port    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {        public void onReceive(Context context, Intent intent) {            String action = intent.getAction();            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {                Log.d(TAG, "Device attached");                if (!mSerial.isOpened()) {                    Log.d(TAG, "Device attached begin");                    openUsbSerial();                }                if (!mRunningMainLoop) {                    Log.d(TAG, "Device attached mainloop");                    mainloop();                }            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {                Log.d(TAG, "Device detached");                closeUsbSerial();            } else if (ACTION_USB_PERMISSION.equals(action)) {                Log.d(TAG, "Request permission");                synchronized (this) {                    if (!mSerial.isOpened()) {                        Log.d(TAG, "Request permission begin");                        openUsbSerial();                    }                }                if (!mRunningMainLoop) {                    Log.d(TAG, "Request permission mainloop");                    mainloop();                }            }        }    };    //0->no 1->right 2->left    public int judgeHundle(boolean isParent, int hundleValue) {        if (isParent) {            if (hundleValue < -90) {                return 1;            } else if (hundleValue > 90) {                return 2;            }        } else {            if (hundleValue < -60) {                return 1;            } else if (hundleValue > 60) {                return 2;            }        }        return 0;    }    @Override    public void onAccuracyChanged(Sensor arg0, int arg1) {        // TODO Auto-generated method stub    }    @Override    public void onSensorChanged(SensorEvent event) {        if (!mIsSerial) {            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {                float x = event.values[SensorManager.DATA_X];                float y = event.values[SensorManager.DATA_Y];                float z = event.values[SensorManager.DATA_Z];                double hnd = Math.asin((double) x) / 3.14 * 180;                mUserCarData.hundle = (int) hnd;            }        }    }    @Override    public void onUpdateCarInfo(VehicleInfo carInfo) {        if (carInfo == null) return;        if (mLastCarInfo == null) {            mLastCarInfo = carInfo;        }        if (isStarted) {            //ハンドル            if (judgeHundle(true, carInfo.data.steerAg) == judgeHundle(false, mUserCarData.hundle)) {                mDrivingPoint.hundle++;            }            mDrivingPoint.hundleTotal++;            //アクセル            boolean parentAccel = (mLastCarInfo.data.accrPedlRat < carInfo.data.accrPedlRat);            if (parentAccel == mUserCarData.isAccel) {                mDrivingPoint.accel++;            }            mDrivingPoint.accelTotal++;            //ブレーキ            if (carInfo.data.brkIndcr == mUserCarData.brake) {                mDrivingPoint.brake++;            }            mDrivingPoint.brakeTotal++;        }        updateSpeedMeter(carInfo);        updateHundle(carInfo);        updateAccel(carInfo);        updaateBrake();        if (mLastCarInfo.data.trsmGearPosn.equals("D") && mLastCarInfo.data.engN > 0 && !isStarted) {            isStarted = true;            mDrivingPoint.initialize();        } else if (mLastCarInfo.data.trsmGearPosn.equals("P") && mLastCarInfo.data.brkIndcr == 1 && isStarted) {            //終了。結果画面に            Intent intent = new Intent(getApplicationContext(), FinishActivity.class);            intent.putExtra(Constants.EXTRA_FINISH_INT_ACCEL, mDrivingPoint.getAccelSyncro());            intent.putExtra(Constants.EXTRA_FINISH_INT_BRAKE, mDrivingPoint.getBrakeSyncro());            intent.putExtra(Constants.EXTRA_FINISH_INT_HUNDLE, mDrivingPoint.getHundleSyncro());            startActivity(intent);        }        mLastUserCarData = mUserCarData;        mLastCarInfo = carInfo;    }    void updateSpeedMeter(VehicleInfo carInfo) {        if (carInfo == null) return;        //左のスピードメータを動かす        mLeftMeterTextView.setText(Double.toString(carInfo.data.spd));        RotateAnimation rotate = new RotateAnimation((int) (mLastCarInfo.data.spd * 2.0f), (int) (carInfo.data.spd * 2.0f), mMySpeedImageView.getWidth() / 2, mMySpeedImageView.getHeight() / 2); // imgの中心を軸に、0度から360度にかけて回転        rotate.setDuration(ANIMATION_DURATION);        rotate.setFillAfter(true);        rotate.setFillEnabled(true);        mMySpeedImageView.startAnimation(rotate); // アニメーション適用    }    void updateHundle(VehicleInfo carInfo) {        RotateAnimation rotateUser = new RotateAnimation((int) (mLastUserCarData.hundle * 2.0f), (int) (mUserCarData.hundle * 2.0f), mUserHundleImageView.getWidth() / 2, mUserHundleImageView.getHeight() / 2); // imgの中心を軸に、0度から360度にかけて回転        rotateUser.setDuration(ANIMATION_DURATION);        rotateUser.setFillAfter(true);        rotateUser.setFillEnabled(true);        mUserHundleImageView.startAnimation(rotateUser); // アニメーション適用        if (carInfo == null) return;        RotateAnimation rotateCar = new RotateAnimation((int) (mLastCarInfo.data.steerAg * 2.0f), (int) (carInfo.data.steerAg * 2.0f), mCarHundleImageView.getWidth() / 2, mCarHundleImageView.getHeight() / 2); // imgの中心を軸に、0度から360度にかけて回転        rotateCar.setDuration(ANIMATION_DURATION);        rotateCar.setFillAfter(true);        rotateCar.setFillEnabled(true);        mCarHundleImageView.startAnimation(rotateCar); // アニメーション適用    }    void updateAccel(VehicleInfo carInfo) {        int userTargetYPosition = mUserAccelFrameLayout.getHeight() - mUserAccelImageView.getHeight();        ValueAnimator userValueAnimator;        if (mUserCarData.isAccel) {            userValueAnimator = ValueAnimator.ofFloat(userTargetYPosition, mUserAccelImageView.getTranslationY());        } else {            userValueAnimator = ValueAnimator.ofFloat(mUserAccelImageView.getTranslationY(), userTargetYPosition);        }        userValueAnimator.setDuration(300);        userValueAnimator.setRepeatCount(0);        userValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {            @Override            public void onAnimationUpdate(ValueAnimator animation) {                mUserAccelImageView.setTranslationY((float) (animation.getAnimatedValue()));            }        });        userValueAnimator.start();        if (carInfo == null) return;        int carTargetYPosition = mCarAccelLayout.getHeight();        ValueAnimator carValueAnimator;        carValueAnimator = ValueAnimator.ofFloat(mCarAccelImageView.getTranslationY(), carTargetYPosition - (carTargetYPosition * carInfo.data.accrPedlRat / 100.0f));        Log.d("MainActivity", "carTargetYPosition:" + String.valueOf(carTargetYPosition) + ", TargetValue:" + String.valueOf(carTargetYPosition - (carTargetYPosition * carInfo.data.accrPedlRat / 100.0f)) + "TransactioY:" + String.valueOf(mCarAccelImageView.getTranslationY()));        carValueAnimator.setDuration(300);        carValueAnimator.setRepeatCount(0);        carValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {            @Override            public void onAnimationUpdate(ValueAnimator animation) {                mCarAccelImageView.setTranslationY((float) (animation.getAnimatedValue()));            }        });        carValueAnimator.start();    }    void updaateBrake() {        mBrakeImageView.setImageResource(mUserCarData.brake == 0 ? R.drawable.img_brake : R.drawable.img_brake_on);    }    @Override    public void onUpdateLocation(Location location) {        mGPSLocation = location;        //左のスピードメータを動かす        mLeftMeterTextView.setText(Double.toString(location.getSpeed()));        RotateAnimation rotate = new RotateAnimation((int) (mLastCarInfo.data.spd * 2.0f), (int) (location.getSpeed() * 2.0f), mMySpeedImageView.getWidth() / 2, mMySpeedImageView.getHeight() / 2); // imgの中心を軸に、0度から360度にかけて回転        rotate.setDuration(ANIMATION_DURATION);        rotate.setFillAfter(true);        rotate.setFillEnabled(true);        mMySpeedImageView.startAnimation(rotate); // アニメーション適用    }}