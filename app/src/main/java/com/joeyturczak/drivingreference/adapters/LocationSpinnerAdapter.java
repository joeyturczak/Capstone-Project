package com.joeyturczak.drivingreference.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.joeyturczak.drivingreference.R;
import com.joeyturczak.drivingreference.utils.LocationUtility;

import java.util.List;

import me.grantland.widget.AutofitHelper;

/**
 * Copyright (C) 2016 Joey Turczak
 *
 *
 * Populates spinner list with location choices
 */
public class LocationSpinnerAdapter extends ArrayAdapter<String> {

    public static final int VIEW_TYPE_SMALL = 0;
    public static final int VIEW_TYPE_LARGE = 1;

    private List<String> mLocations;
    private boolean mIsLargeLayout;
    private int mViewType;
    private boolean mDropDown;

    public LocationSpinnerAdapter(Context context, int resource, int textViewResourceId, List<String> locations, boolean isLargeLayout) {
        super(context, resource, textViewResourceId, locations);
        mLocations = locations;
        mIsLargeLayout = isLargeLayout;
        mViewType = VIEW_TYPE_LARGE;
        mDropDown = false;
    }

    public class ViewHolder {
        public final ImageView mIconView;
        public final TextView mTitle;

        public ViewHolder(View view) {
            mIconView = (ImageView) view.findViewById(R.id.location_icon);
            mTitle = (TextView) view.findViewById(R.id.location_title);
        }

    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.location_spinner_drop_down_view, parent, false);
        }

        mDropDown = true;
        return getView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.location_spinner_item, parent, false);
        }

        boolean isLandscape = getContext().getResources().getBoolean(R.bool.landscape);

        String location = mLocations.get(position);

        ViewHolder viewHolder = new ViewHolder(convertView);

        viewHolder.mIconView.setImageResource(LocationUtility.getLocationImageResource(getContext(), location));

        final TextView titleView = viewHolder.mTitle;

        titleView.setText(location);

        if(!mDropDown) {
            AutofitHelper.create(titleView);
        }

        if(!mIsLargeLayout && mViewType == VIEW_TYPE_SMALL && !mDropDown && !isLandscape) {
            titleView.setVisibility(View.GONE);
        } else {
            if(titleView.getVisibility() == View.GONE) {
                titleView.setVisibility(View.VISIBLE);
            }
        }

        mDropDown = false;
        return convertView;
    }

    @Override
    public int getCount() {
        return mLocations.size();
    }

    @Override
    public String getItem(int position) {
        return mLocations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setViewType(int viewType) {
        mViewType = viewType;
        notifyDataSetChanged();
    }
}
