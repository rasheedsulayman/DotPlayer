package com.r4sh33d.musicslam.fragments.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.r4sh33d.musicslam.R;
import com.r4sh33d.musicslam.customviews.ColoredStatusBarView;
import com.r4sh33d.musicslam.dataloaders.SearchResultsLoader;
import com.r4sh33d.musicslam.fragments.BaseListenerFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchFragment extends BaseListenerFragment implements SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<List<Object>> {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.status_bar_view)
    ColoredStatusBarView statusBarView;
    @BindView(R.id.empty_data_textview)
    TextView emptyDataTextView;

    private String queryEntered;
    private SearchResultsAdapter searchResultsAdapter;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchResultsAdapter = new SearchResultsAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(searchResultsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        addAdapterDataObserver();
        recyclerView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });

        if (isAlbumArtTheme) {
            statusBarView.setVisibility(View.GONE);
        }
        setUpToolbar();
    }

    void setUpToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_white_24dp);
        toolbar.setNavigationOnClickListener(v -> {
            hideKeyboard();
            getActivity().onBackPressed();
        });
        toolbar.inflateMenu(R.menu.menu_fragment_search);
        // Get the MenuItem for the action item
        MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                hideKeyboard();
                getActivity().onBackPressed();
                return false;
            }
        });

        // Configure the search info and add any event listeners...
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(this);
        searchItem.expandActionView();
    }

    public void addAdapterDataObserver() {
        searchResultsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                emptyDataTextView.setVisibility(searchResultsAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        onQueryTextChange(query);
        hideKeyboard();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.equals(queryEntered)) {
            return true;
        }
        queryEntered = newText.trim();
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @NonNull
    @Override
    public Loader<List<Object>> onCreateLoader(int id, @Nullable Bundle args) {
        return new SearchResultsLoader(getContext(), queryEntered);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Object>> loader, List<Object> data) {
        searchResultsAdapter.updateResultsLists(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Object>> loader) {
    }


    public void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
