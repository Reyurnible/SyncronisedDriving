package com.reyurnible.syncronizeddrivind;

import com.reyurnible.system.Account;
import com.reyurnible.system.AccountHelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SettingActivity extends Activity {
	EditText mIdText;
	Account mAccount;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		mIdText = (EditText)findViewById(R.id.setting_edittext_id);
		mAccount = AccountHelper.getAccount(getApplicationContext());
		mIdText.setText(mAccount.getVid());
		
		Button mRegistButton = (Button)findViewById(R.id.setting_button_regist);
		mRegistButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAccount.saveAccount(mIdText.getText().toString());
				finish();
			}
		});
	}

}
