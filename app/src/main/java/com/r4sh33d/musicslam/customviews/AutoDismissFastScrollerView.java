package com.r4sh33d.musicslam.customviews;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.reddit.indicatorfastscroll.FastScrollItemIndicator;
import com.reddit.indicatorfastscroll.FastScrollerView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.jvm.functions.Function1;

public class AutoDismissFastScrollerView extends FastScrollerView {
    private int mAutoHideDelay = 1500;

    boolean isShown;

    public AutoDismissFastScrollerView(@NotNull Context context, @Nullable AttributeSet attrs,
                                       int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    public AutoDismissFastScrollerView(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AutoDismissFastScrollerView(@NotNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoDismissFastScrollerView(@Nullable Context context) {
        super(context);
    }

    public void setUpWithRecyclerViewJava(RecyclerView recyclerView,
                                          Function1<Integer, FastScrollItemIndicator> function1) {
        this.setupWithRecyclerView(recyclerView, function1);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                show();
                postAutoHideDelayed();
            }
        });
    }


    void show() {
        if (!isShown) {
            setVisibility(View.VISIBLE);
            isShown = true;
        }
    }

    private Runnable mHideRunnable = () -> {
        setVisibility(View.GONE);
        isShown = false;
    };


    protected void postAutoHideDelayed() {
        if (getRecyclerView() != null) {
            cancelAutoHide();
            getRecyclerView().postDelayed(mHideRunnable, mAutoHideDelay);
        }
    }

    protected void cancelAutoHide() {
        if (getRecyclerView() != null) {
            getRecyclerView().removeCallbacks(mHideRunnable);
        }
    }
}
