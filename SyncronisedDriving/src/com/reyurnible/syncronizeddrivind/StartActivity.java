package com.reyurnible.syncronizeddrivind;

import com.reyurnible.system.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ToggleButton;

public class StartActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		ImageButton startButton = (ImageButton)findViewById(R.id.start_imagebutton_start);
		startButton.setOnClickListener(this);
		ImageButton settingButton = (ImageButton)findViewById(R.id.start_imagebutton_setting);
		settingButton.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View view) {
		Intent intent;
		switch(view.getId()){
		case R.id.start_imagebutton_start:
			ToggleButton isSerialButton = (ToggleButton)findViewById(R.id.start_togglebutton_serial);
			intent = new Intent(getApplicationContext(),MainActivity.class);
			boolean isSerial = isSerialButton.isChecked();
			intent.putExtra(Constants.EXTRA_MAIN_BOOLEAN_TYPE, isSerial);
			startActivity(intent);
			break;
		case R.id.start_imagebutton_setting:
			intent = new Intent(getApplicationContext(),SettingActivity.class);
			startActivity(intent);
			break;
		}
		
	}
}
