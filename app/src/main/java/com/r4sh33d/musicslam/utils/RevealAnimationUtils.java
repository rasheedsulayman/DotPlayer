package com.r4sh33d.musicslam.utils;

import android.animation.Animator;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.LinearInterpolator;

public class RevealAnimationUtils {

    public static void revealAnimation(View myView, OnAnimationEndListener listener) {
        // previously invisible view
        // Check if the runtime version is at least Lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // get the center for the clipping circle
            int cx = Math.max(myView.getWidth(), myView.getHeight());
            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.
                    createCircularReveal(myView, 0, 0, 0f, cx);
            anim.setInterpolator(new LinearInterpolator());
            anim.setDuration(200);
            // make the view visible and start the animation
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    listener.onAnimationEnd(animation);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            myView.setVisibility(View.VISIBLE);
            anim.start();
        } else {
            // set the view to visible without a circular reveal animation below Lollipop
            myView.setVisibility(View.VISIBLE);
        }
    }


    public interface OnAnimationEndListener {
        void onAnimationEnd(Animator animation);
    }

}
