package com.joeyturczak.drivingreference.ui;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.joeyturczak.drivingreference.R;
import com.joeyturczak.drivingreference.adapters.MainListAdapter;
import com.joeyturczak.drivingreference.data.DrivingContract;
import com.joeyturczak.drivingreference.models.Manual;
import com.joeyturczak.drivingreference.utils.LocationUtility;
import com.joeyturczak.drivingreference.utils.ManualUtility;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2016 Joey Turczak
 */
public class MainListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int LOADER_MANUAL = 1;

    private MainListAdapter mMainListAdapter;
    private RecyclerView mRecyclerView;

    private boolean mIsLargeLayout;

    public static final int COLUMN_BACKEND_ID = 1;
    public static final int COLUMN_LOCATION = 2;
    public static final int COLUMN_TYPE = 3;
    public static final int COLUMN_LANGUAGE = 4;
    public static final int COLUMN_URL = 5;
    public static final int COLUMN_DISPLAY_NAME = 6;
    public static final int COLUMN_LAST_UPDATED = 7;
    public static final int COLUMN_DOWNLOADED = 8;
    public static final int COLUMN_LAST_PAGE = 9;

    private Cursor mManualsCursor;

    private List<Integer> mViewTypes;
    private List<Object> mData;

    public MainListFragment() {
        // Required empty public constructor
    }

    public static MainListFragment newInstance() {
        return new MainListFragment();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_MANUAL, null, this);
    }

    /**
     * Restarts the loader if a different location was selected
     */
    public void onLocationChanged() {
        mManualsCursor = null;
        getLoaderManager().restartLoader(LOADER_MANUAL, null, this);

        if(mIsLargeLayout) {
            ((MainActivity)getActivity()).removeDetailFragment();
        }

        updateWidget();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mIsLargeLayout) {
            mMainListAdapter.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        View rootView = inflater.inflate(R.layout.fragment_main_list, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_menu);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.recyclerview_menu_empty);

        mMainListAdapter = new MainListAdapter(getActivity(), new MainListAdapter.MainListOnClickHandler() {
            @Override
            public void onClick(Object object, RecyclerView.ViewHolder viewHolder) {
                ((OnItemSelectedListener) getActivity())
                        .onItemSelected(object, MainActivity.PDFFRAGMENT,
                                viewHolder
                        );
            }
        }, emptyView, AbsListView.CHOICE_MODE_SINGLE);

        mRecyclerView.setAdapter(mMainListAdapter);

        if (savedInstanceState != null) {
            mMainListAdapter.onRestoreInstanceState(savedInstanceState);
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case LOADER_MANUAL:
                return getManualsLoader();
            default:
                return null;
        }

    }

    /**
     * Returns a CursorLoader for Manuals database selection
     */
    private CursorLoader getManualsLoader() {
        String location = LocationUtility.getLocationConfig(getContext());

        String sortOrder = DrivingContract.DrivingManualEntry.COLUMN_TYPE + getString(R.string.sort_order_asc);

        //Show only the items from the selected location
        String selection = DrivingContract.DrivingManualEntry.COLUMN_LOCATION + " = '" + location + "'";

        return new CursorLoader(getActivity(),
                DrivingContract.DrivingManualEntry.CONTENT_URI,
                null,
                selection,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case LOADER_MANUAL:
                mManualsCursor = data;
                break;
        }

        if(mManualsCursor != null) {
            fillDataArrays();
            mMainListAdapter.swapData(mViewTypes, mData);

            if ( data.getCount() == 0 ) {
                getActivity().supportStartPostponedEnterTransition();
            }
        }
    }

    /**
     * Adds data from the cursors to the arrays that will be sent to the RecyclerView adapter
     */
    private void fillDataArrays() {

        mViewTypes = new ArrayList<>();
        mData = new ArrayList<>();

        if(mManualsCursor.getCount() > 0) {
            mViewTypes.add(MainListAdapter.VIEW_TYPE_TITLE);
            mData.add(getString(R.string.title_driving_manuals));
            mManualsCursor.moveToFirst();
            do {
                Manual manual = ManualUtility.getManualFromCursor(mManualsCursor);

                mViewTypes.add(MainListAdapter.VIEW_TYPE_MANUAL);
                mData.add(manual);
            } while(mManualsCursor.moveToNext());
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mManualsCursor = null;
        mMainListAdapter.swapData(null, null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getContext().getString(R.string.shared_preference_location_key))) {
            onLocationChanged();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnItemSelectedListener {
        void onItemSelected(Object object, int fragmentId, RecyclerView.ViewHolder viewHolder);
    }

    /**
     * Updates the widget with relevant information
     */
    private void updateWidget() {

        Context context = getContext();
        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }
}
