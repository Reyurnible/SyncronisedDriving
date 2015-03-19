package com.reyurnible.syncronizeddrivind.view.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import com.reyurnible.syncronizeddrivind.model.object.VehicleInfo;

public class LocationManagerFragment extends Fragment implements LocationListener {
    private OnUpdateLocationListener mListener;
    private int mPeriodTime;

    public static LocationManagerFragment newInstance(final int request_period) {
        LocationManagerFragment fragment = new LocationManagerFragment();
        Bundle args = new Bundle();
        args.putInt("request_period", request_period);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mPeriodTime = args.getInt("request_period");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnUpdateLocationListener) {
            mListener = (OnUpdateLocationListener) activity;

            // LocationManagerを取得
            LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            // Criteriaオブジェクトを生成
            Criteria criteria = new Criteria();
            // Accuracyを指定(低精度)
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            // PowerRequirementを指定(低消費電力)
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            // ロケーションプロバイダの取得
            String provider = mLocationManager.getBestProvider(criteria, true);
            // LocationListenerを登録
            mLocationManager.requestLocationUpdates(provider, 0, 0, this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mListener != null) {
            mListener.onUpdateLocation(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public interface OnUpdateLocationListener {
        public void onUpdateLocation(Location location);
    }

}
