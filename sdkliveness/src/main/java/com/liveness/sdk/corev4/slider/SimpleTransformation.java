package com.liveness.sdk.corev4.slider;

import android.view.View;

public class SimpleTransformation implements SliderPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.

        } else if (position <= 1) { // [-1,1]

            // Counteract the default slide transition
            page.setTranslationX(page.getWidth() * -position);

            //set Y position to swipe in from top
            float yPosition = position * page.getHeight();
            page.setTranslationY(yPosition);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
        }
    }
}