package com.reyurnible.syncronizeddrivind.view.fragment;

import com.reyurnible.syncronizeddrivind.model.system.Constants;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;

public class YesNoAlertDialogFragment extends DialogFragment{
	
	private YesNoAlertDialogListener mListener;
	private int mTitleId;
	private int mMessageId;
	
	public interface YesNoAlertDialogListener{
		public void onDialogPositiveClick(DialogFragment dialog);
		public void onDialogNegativeClick(DialogFragment dialog);
	}
	
	public static YesNoAlertDialogFragment newInstance(int titleId,int messageId){
		Bundle args = new Bundle();
		args.putInt(Constants.EXTRA_ALERT_DIALOG_TITLE_ID,titleId);
		args.putInt(Constants.EXTRA_ALERT_DIALOG_MESSAGE_ID,messageId);
		YesNoAlertDialogFragment yesNoAlertDialogFragment = new YesNoAlertDialogFragment();
		yesNoAlertDialogFragment.setArguments(args);
		return yesNoAlertDialogFragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		mTitleId = getArguments().getInt(Constants.EXTRA_ALERT_DIALOG_TITLE_ID);
		mMessageId = getArguments().getInt(Constants.EXTRA_ALERT_DIALOG_MESSAGE_ID);
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try{
			mListener = (YesNoAlertDialogListener)activity;
		}catch(ClassCastException e){
			throw new ClassCastException(activity.toString() + "must implement YesNoAlertDialogListener");
		}
	}
}
