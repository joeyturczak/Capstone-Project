package com.joeyturczak.drivingreference.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.ResultReceiver;
import android.support.v4.content.LocalBroadcastManager;

import com.joeyturczak.drivingreference.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Copyright (C) 2016 Joey Turczak
 *
 *
 * Acquires admin area from last known location and sends it in a broadcast.
 */
public class LocationIntentService extends IntentService {

    public static final String LOCATION_DATA_EXTRA = "LOCATION_DATA_EXTRA";
    public static final String LOCATION_RESULT_RECEIVER_EXTRA = "LOCATION_RESULT_RECEIVER_EXTRA";

    public static final int ERROR = 0;
    public static final int SUCCESS = 1;

    protected ResultReceiver mResultReceiver;

    public LocationIntentService() {
        super("LocationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Location location = intent.getParcelableExtra(LOCATION_DATA_EXTRA);
        mResultReceiver = intent.getParcelableExtra(LOCATION_RESULT_RECEIVER_EXTRA);

        List<Address> addresses = new ArrayList<>();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(addresses.size() == 0) {
            sendLocationBroadcast(ERROR, getString(R.string.location_error));
        } else {
            Address address = addresses.get(0);
            String state = address.getAdminArea();
            sendLocationBroadcast(SUCCESS, state);
        }
    }

    /** Sends a broadcast to notify the ui of the current location. */
    public void sendLocationBroadcast(int resultCode, String message) {
        Intent intent = new Intent(getString(R.string.location_broadcast_intent_filter));
        intent.putExtra(getString(R.string.location_intent_result_key), resultCode);
        intent.putExtra(getString(R.string.location_intent_message_key), message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
