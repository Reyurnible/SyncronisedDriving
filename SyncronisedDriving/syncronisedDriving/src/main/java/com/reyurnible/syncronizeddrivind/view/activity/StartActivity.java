package com.reyurnible.syncronizeddrivind.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.widget.ToggleButton;

import com.reyurnible.syncronizeddrivind.R;
import com.reyurnible.syncronizeddrivind.model.system.Constants;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

@EActivity(R.layout.activity_start)
public class StartActivity extends Activity{

    @Click(R.id.start_imagebutton_start)
    void clickStart() {
        ToggleButton isSerialButton = (ToggleButton)findViewById(R.id.start_togglebutton_serial);
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        boolean isSerial = isSerialButton.isChecked();
        intent.putExtra(Constants.EXTRA_MAIN_BOOLEAN_TYPE, isSerial);
        startActivity(intent);
    }

    @Click(R.id.start_imagebutton_setting)
    void clickSetting() {
        Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
        startActivity(intent);
    }
}
