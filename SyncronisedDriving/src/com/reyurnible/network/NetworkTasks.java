package com.reyurnible.network;

import com.android.volley.Request.Method;

import android.net.Uri;

public enum NetworkTasks {
	CarInfo(0,"/GetVehicleInfo",true,Method.POST),
	;
	public int id;
	//URL
	public String path;
	//引数の有り無し
	public boolean isQuery;
	//Request
	public int method;
	
	private NetworkTasks(int id,String path,boolean isQuery,int method) {
		this.id = id;
		this.path = path;
		this.isQuery = isQuery;
		this.method = method;
	}	
}
