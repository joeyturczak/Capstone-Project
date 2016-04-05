package com.joeyturczak.drivingreference.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.joeyturczak.drivingreference.R;
import com.joeyturczak.myjavalibrary.DrivingValues;

/**
 * Copyright (C) 2016 Joey Turczak
 *
 * Helps retrieve and set the selected location to SharedPreferences
 */
public class LocationUtility {

    /**
     * Gets stored location from SharedPreferences
     */
    public static String getLocationConfig(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPreferences.getString(context.getString(R.string.shared_preference_location_key), DrivingValues.Location.Alaska.toString());
    }

    /**
     * Sets location value in SharedPreferences
     */
    public static void setLocationConfig(Context context, String location) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        location = location.replace(" ", "_");
        editor.putString(context.getString(R.string.shared_preference_location_key), location);
        editor.apply();
    }

    /**
     * Returns the image icon resource id for the given location
     */
    public static int getLocationImageResource(Context context, String location) {

        String resourceName = location;

        resourceName = resourceName.replace(" ", "_");

        resourceName = resourceName.toLowerCase();

        return context.getResources().getIdentifier(resourceName, context.getString(R.string.drawable_identifier), context.getString(R.string.package_name));
    }
}
