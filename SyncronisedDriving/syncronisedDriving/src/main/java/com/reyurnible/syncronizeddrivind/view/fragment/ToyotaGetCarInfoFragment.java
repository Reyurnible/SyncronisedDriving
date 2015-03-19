package com.reyurnible.syncronizeddrivind.view.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.reyurnible.syncronizeddrivind.controller.provider.NetworkTaskCallback;
import com.reyurnible.syncronizeddrivind.controller.provider.VolleyHelper;
import com.reyurnible.syncronizeddrivind.controller.util.JSONParseUtil;
import com.reyurnible.syncronizeddrivind.controller.util.ToyotaApiRequestUtil;
import com.reyurnible.syncronizeddrivind.controller.util.UriUtil;
import com.reyurnible.syncronizeddrivind.model.enumerate.NetworkTasks;
import com.reyurnible.syncronizeddrivind.model.object.VehicleData;
import com.reyurnible.syncronizeddrivind.model.object.VehicleInfo;
import com.reyurnible.syncronizeddrivind.model.system.AppConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ToyotaGetCarInfoFragment extends Fragment {
    private OnUpdateDataListener mListener;

    private String mCarVid = "ITCJP_VID_001";
    private int mPeriodTime = 1000;
    private List<NameValuePair> mParams;
    private Timer mTimer;
    private Handler mHandler = new Handler();
    private boolean mNowRequest = false;

    public static ToyotaGetCarInfoFragment newInstance(final String car_vid, final int request_period) {
        ToyotaGetCarInfoFragment fragment = new ToyotaGetCarInfoFragment();
        Bundle args = new Bundle();
        args.putString("car_vid", car_vid);
        args.putInt("request_period", request_period);
        fragment.setArguments(args);
        return fragment;
    }

    private TimerTask mGetCarInfoTask = new TimerTask() {
        @Override
        public void run() {
            // mHandlerを通じてUI Threadへ処理をキューイング
            mHandler.post(new Runnable() {
                public void run() {
                    if (!mNowRequest) {
                        mParams = new ArrayList<>();
                        mParams.add(new BasicNameValuePair("developerkey", AppConfig.DEVELOPERKEY));
                        mParams.add(new BasicNameValuePair("responseformat", "json"));
                        mParams.add(new BasicNameValuePair("infoids", "[Posn,Spd,EngN,SteerAg,BrkIndcr,AccrPedlRat,HdLampLtgIndcn,WiprSts,TrsmGearPosn]"));
                        mParams.add(new BasicNameValuePair("vid", mCarVid));

                        ToyotaApiRequestUtil toyotaRequest = new ToyotaApiRequestUtil(new NetworkTaskCallback() {
                            @Override
                            public void onSuccessNetworkTask(int taskId, Object object) {
                                Log.d("ToyotaGetCarInfo", object.toString());
                                if (mListener != null) {
                                    mListener.onUpdateCarInfo(JSONParseUtil.vehicleParse((JSONObject) object));
                                }
                            }
                            @Override
                            public void onFailedNetworkTask(int taskId, Object object) {
                                Log.d("ToyotaGetCarInfo", "error");
                            }
                        });
                        toyotaRequest.onRequest(getActivity().getApplicationContext(), UriUtil.postVehicleInfoUri(), NetworkTasks.CarInfo, mParams);
                        mNowRequest = true;
                    }
                    /*
                    VehicleInfo result = new VehicleInfo();
                    VehicleData data = new VehicleData();
                    data.spd = 20;
                    data.accrPedlRat = 30;
                    data.steerAg = 60;
                    data.trsmGearPosn = "D";
                    data.engN = 10;
                    result.data = data;
                    if (mListener != null) {
                        mListener.onUpdateCarInfo(result);
                    }
                    */
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mCarVid = args.getString("car_vid");
            mPeriodTime = args.getInt("request_period");
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnUpdateDataListener) {
            mListener = (OnUpdateDataListener) activity;
        }
        if (mTimer == null) {
            //タイマーの初期化処理
            mTimer = new Timer(true);
            mTimer.schedule(mGetCarInfoTask, 100, mPeriodTime);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        //タイマーの停止処理
        mTimer.cancel();
        mTimer = null;
    }

    public interface OnUpdateDataListener {
        public void onUpdateCarInfo(VehicleInfo carInfo);
    }

}
