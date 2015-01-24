package com.reyurnible.pager;

import com.reyurnible.syncronizeddrivind.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ViewPagerIndicator extends RadioGroup{

	public ViewPagerIndicator(Context context) {
		this(context,null);
	}
	public ViewPagerIndicator(Context context,AttributeSet attrs){
		super(context,attrs);
		setOrientation(LinearLayout.HORIZONTAL);
		setGravity(Gravity.CENTER);
	}
	private int mCount;
	/*
	 * ページ数をセットする
	 */
	public void setCount(int count){
		mCount = count;
		removeAllViews();
		//指定されたページ数分だけRadioButtonを追加
		for(int i=0;i<count;i++){
			//RadioButtonにインディケータの画像をセット
			RadioButton rb = new RadioButton(getContext());
			rb.setButtonDrawable(R.drawable.ic_indicator);
			addView(rb);
		}
		setCurrentPosition(-1);
	}
	/*
	 * 現在の位置をセットする
	 */
	public void setCurrentPosition(int position){
		if(position>=mCount){
			position = mCount-1;
		}
		if(position<0){
			position = mCount>0?0:-1;
		}
		if(position>=0&&position<mCount){
			//現在の位置のRadioButtonをチェック状態にする
			RadioButton rb = (RadioButton)getChildAt(position);
			rb.setChecked(true);
		}
	}
}
