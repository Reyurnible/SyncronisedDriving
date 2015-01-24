package com.reyurnible.system;import com.reyurnible.system.AppConfig;import android.app.Activity;import android.content.Context;import android.content.SharedPreferences;import android.util.Log;public class Account {	private static final String SP_KEY = "account_preference";	private SharedPreferences mSharedPreferences;		private final static String KEY_STRING_VID = "account_string_vid";		//認証に何を使うかは未定	//idだけあれば大丈夫な気もしてる	private String mVid;		public Account(Context context){		//load from shared preference		mSharedPreferences = context.getSharedPreferences(SP_KEY,Activity.MODE_PRIVATE);		mVid = mSharedPreferences.getString(KEY_STRING_VID,"ITCJP_VID_001");	}	//check have to account	public boolean isAccount(){		if(mVid==null||mVid.equals("")){			return false;		}		if(AppConfig.DEBUG){			Log.d(this.getClass().getSimpleName(),mVid);		}		return true;	}		public String getVid(){		return mVid;	}	//save to shared preference	public void saveAccount(String vid){		this.mVid = vid;						SharedPreferences.Editor editor = mSharedPreferences.edit();		editor.putString(KEY_STRING_VID, mVid);		editor.commit();	}}