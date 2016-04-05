package com.joeyturczak.drivingreference.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.joeyturczak.drivingreference.R;
import com.joeyturczak.drivingreference.ui.PDFPage;

/**
 * Copyright (C) 2016 Joey Turczak
 *
 *
 * Populates ViewPager with PDFPage fragment
 */
public class PDFViewPagerAdapter extends FragmentStatePagerAdapter {

    Context mContext;
    int mNumberOfPages;
    String mFileName;

    public PDFViewPagerAdapter(FragmentManager fm, Context context, int numberOfPages, String fileName) {
        super(fm);
        mContext = context;
        mNumberOfPages = numberOfPages;
        mFileName = fileName;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = PDFPage.newInstance();
        Bundle args = new Bundle();

        args.putInt(mContext.getString(R.string.pdf_page_number_key), position);
        args.putString(mContext.getString(R.string.pdf_file_name_key), mFileName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return mNumberOfPages;
    }
}
