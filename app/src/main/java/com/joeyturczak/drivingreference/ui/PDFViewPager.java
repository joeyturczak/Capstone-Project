package com.joeyturczak.drivingreference.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Copyright (C) 2016 Joey Turczak
 */
public class PDFViewPager extends ViewPager {

    public PDFViewPager(Context context) {
        super(context);
    }

    public PDFViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // PhotoView with ViewPager throws out of bounds exceptions when zooming
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }
}
