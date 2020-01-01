package com.r4sh33d.musicslam.fragments.pager;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customviews.AutoDismissFastScrollerView;
import com.r4sh33d.musicslam.customviews.ColoredFastScrollerThumbView;
import com.r4sh33d.musicslam.fragments.BaseListenerFragment;
import com.r4sh33d.musicslam.interfaces.FastScrollerAdapter;
import com.reddit.indicatorfastscroll.FastScrollItemIndicator;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Rasheed Sualayman (@r4sh33d)
 */
public abstract class PagerFragment extends BaseListenerFragment {
    @BindView(R.id.fastscroller)
    AutoDismissFastScrollerView fastScrollerView;

    @BindView(R.id.fastscroller_thumb)
    ColoredFastScrollerThumbView fastScrollerThumbView;

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;

    @BindView(R.id.empty_data_textview)
    TextView emptyDataTextView;

    @Override
    public LoaderManager getLoaderManager() {
        if (getParentFragment() != null) {
            return getParentFragment().getLoaderManager();
        }
        return super.getLoaderManager();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(getLayoutResourceId(), container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setAdapter(getAdapter());
        fastScrollerThumbView.initColorSubs();
        fastScrollerView.setUpWithRecyclerViewJava(recyclerView, integer -> {
            FastScrollItemIndicator.Text fastScrollerItemIndicator;
            String letter = ((FastScrollerAdapter) getAdapter()).getFastScrollerThumbCharacter(integer);
            if (TextUtils.isEmpty(letter) || !Character.isLetter(letter.charAt(0))) {
                fastScrollerItemIndicator = new FastScrollItemIndicator.Text("#");
            } else {
                fastScrollerItemIndicator =
                        new FastScrollItemIndicator.Text(letter.toUpperCase());
            }
            return fastScrollerItemIndicator;
        });

        fastScrollerThumbView.setupWithFastScroller(fastScrollerView);
        fastScrollerView.setUseDefaultScroller(false);
        fastScrollerView.getItemIndicatorSelectedCallbacks().add(
                (indicator, indicatorCenterY, itemPosition) -> recyclerView.scrollToPosition(itemPosition));
        addAdapterDataObserver();
    }

    public void addAdapterDataObserver() {
        getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                emptyDataTextView.setText(getEmptyDataMessage());
                emptyDataTextView.setVisibility(getAdapter().getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    public abstract String getEmptyDataMessage();

    public abstract RecyclerView.Adapter getAdapter();

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    @LayoutRes
    public abstract int getLayoutResourceId();


    @Override
    public void onDestroyView() {
        fastScrollerThumbView.cancelColorSubs();
        super.onDestroyView();
    }

}

