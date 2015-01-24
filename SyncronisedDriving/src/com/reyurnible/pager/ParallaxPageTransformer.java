package com.reyurnible.pager;

import android.support.v4.view.ViewPager;
import android.view.View;

public class ParallaxPageTransformer implements ViewPager.PageTransformer {
	 
    private int mImageId;
    
    /**
     * The constructor takes a target Id as param, later on we'll use the Id to get the target
     * view and animate only that view instead of the entire page.
     *
     * @param mImageId  Target View ID.
     */
    public ParallaxPageTransformer(int mImageId) {
        this.mImageId = mImageId;
    }
 
    @Override
    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();
 
        // Find target View.
        if (view.findViewById(mImageId) != null) {
            view = view.findViewById(mImageId);
        }
 
        if (position <= 0) { // [-1,0]
            if (view.getId() == mImageId) {
                view.setTranslationX(pageWidth * -position / 1.4f);
            } else {
                // Use the default slide transition when moving to the left page if the target view
                // is not found.
                view.setTranslationX(0);
            }
 
        } else if (position <= 1) { // (0,1]
            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position / 1.4f);
 
 
        }
 
 
    }
}