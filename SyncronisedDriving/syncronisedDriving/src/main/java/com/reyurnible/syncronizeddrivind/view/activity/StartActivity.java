package com.reyurnible.syncronizeddrivind.view.activity;

import android.app.Activity;
import android.widget.ToggleButton;

import com.reyurnible.syncronizeddrivind.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_start)
public class StartActivity extends Activity {
    @ViewById(R.id.start_togglebutton_serial)
    ToggleButton mIsSerialButton;

    @Click(R.id.start_imagebutton_start)
    void clickStart() {
        MainActivity_.intent(this).mIsSerial(mIsSerialButton.isChecked()).start();
    }

    @Click(R.id.start_imagebutton_setting)
    void clickSetting() {
        SettingActivity_.intent(this).start();
    }
}
