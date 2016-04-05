package com.joeyturczak.drivingreference.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.joeyturczak.drivingreference.R;
import com.joeyturczak.drivingreference.backend.drivingManualApi.DrivingManualApi;
import com.joeyturczak.drivingreference.backend.drivingManualApi.model.DrivingManual;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2016 Joey Turczak
 *
 * Downloads data from GCE backend.
 */
public class EndpointsAsyncTask extends AsyncTask<Object, Void, List<DrivingManual>> {
    public static final int ALL = 0;
    public static final int AFTER_DATE = 1;

    private static DrivingManualApi myApiService = null;

    private Context mContext;

    public EndpointsAsyncTask(Context context) {
        mContext = context;
    }

    @Override
    protected List<DrivingManual> doInBackground(Object... params) {

        if(myApiService == null) {  // Only do this once
            DrivingManualApi.Builder builder = new DrivingManualApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                    .setRootUrl(mContext.getString(R.string.gce_root_url));
            // end options for devappserver

            myApiService = builder.build();
        }

        List<DrivingManual> drivingManuals = new ArrayList<>();

        int request = (int)params[0];

        try {
            switch (request) {
                case ALL:
                    drivingManuals = myApiService.getDrivingManuals().execute().getItems();
                    break;
                case AFTER_DATE:
                    Long lastUpdated = (Long) params[1];
                    drivingManuals = myApiService.getDrivingManualsAfterDate(lastUpdated).execute().getItems();
                    break;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

        return drivingManuals;
    }
}
