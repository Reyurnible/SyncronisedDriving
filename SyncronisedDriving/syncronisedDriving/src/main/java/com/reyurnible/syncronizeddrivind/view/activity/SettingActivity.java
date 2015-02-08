package com.reyurnible.syncronizeddrivind.view.activity;

import android.app.Activity;
import android.widget.EditText;

import com.reyurnible.syncronizeddrivind.R;
import com.reyurnible.syncronizeddrivind.model.system.Account;
import com.reyurnible.syncronizeddrivind.model.system.AccountHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_setting)
public class SettingActivity extends Activity {
	@ViewById(R.id.setting_edittext_id)
    EditText mIdText;
	Account mAccount;

	@AfterViews
    void onAfterViews() {
        mAccount = AccountHelper.getAccount(getApplicationContext());
        mIdText.setText(mAccount.getVid());
    }

    @Click(R.id.setting_button_regist)
    void clickRegist() {
        mAccount.saveAccount(mIdText.getText().toString());
        finish();
    }

}
