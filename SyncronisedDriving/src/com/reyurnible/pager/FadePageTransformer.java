package com.reyurnible.pager;

import android.support.v4.view.ViewPager;
import android.view.View;

public class FadePageTransformer implements ViewPager.PageTransformer{
	
	@Override
	public void transformPage(View page, float position) {
	    float alpha = 0;
	    int pageWidth = page.getWidth();
	    if (-1 < position && position < 0) {
	        // 左にスワイプしていくにつれ透明にする
	        alpha = position + 1;
	    } else if (0 <= position && position <= 1) {
	        // 右にスワイプしていくにつれ透明にする
	        alpha = 1 - position;
	    }
	    page.setAlpha(alpha);
	    // 逆方向に移動させることで位置を固定する
	    page.setTranslationX(pageWidth * -position);
	}
}
