package com.joeyturczak.drivingreference.services;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;

import com.joeyturczak.drivingreference.R;
import com.joeyturczak.drivingreference.backend.drivingManualApi.model.DrivingManual;
import com.joeyturczak.drivingreference.data.DrivingContract;
import com.joeyturczak.drivingreference.models.Manual;
import com.joeyturczak.drivingreference.ui.MainListFragment;
import com.joeyturczak.drivingreference.utils.EndpointsAsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

/**
 * Copyright (C) 2016 Joey Turczak
 *
 *
 * Downloads content from GCE backend
 */
public class UpdateDataIntentService extends IntentService {

    public static final String ACTION_UPDATE_CONTENT = "ACTION_UPDATE_CONTENT";

    private ContentResolver mContentResolver;

    public UpdateDataIntentService() {
        super("UpdateDataIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mContentResolver = getContentResolver();

        if(intent != null) {
            String action = intent.getAction();
            boolean isDbEmpty = intent.getBooleanExtra(getString(R.string.database_empty_intent_key), true);

            switch (action) {
                case ACTION_UPDATE_CONTENT:
//                    boolean isEmpty = isDbEmpty();
//                    sendDatabaseBroadcast(isEmpty);
                    if(isDbEmpty) {
                        downloadAllManuals();
                    } else {
                        downloadNewManuals();
                    }
                    break;
            }
        }
    }

    /**
     * Downloads all available manuals from backend
     */
    private void downloadAllManuals() {

        List<DrivingManual> drivingManuals = new ArrayList<>();

        try {
            drivingManuals = new EndpointsAsyncTask(this).execute(EndpointsAsyncTask.ALL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        saveDrivingManuals(drivingManuals);

    }

    /**
     * Downloads only manuals that do not exist in the database or any that are updated.
     */
    private void downloadNewManuals() {

        // Get the last time something was updated
        String selection = DrivingContract.DrivingManualEntry.COLUMN_LAST_UPDATED;

        String sortOrder = DrivingContract.DrivingManualEntry.COLUMN_LAST_UPDATED + getString(R.string.sort_order_desc);

        Cursor cursor = mContentResolver.query(DrivingContract.DrivingManualEntry.CONTENT_URI, null, selection, null, sortOrder);

        if(cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();

            long mostRecent = cursor.getLong(MainListFragment.COLUMN_LAST_UPDATED);

            cursor.close();

            List<DrivingManual> drivingManuals = new ArrayList<>();

            // Get any entries that are newer from the datastore
            try {
                drivingManuals = new EndpointsAsyncTask(this).execute(EndpointsAsyncTask.AFTER_DATE, mostRecent).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            if (drivingManuals != null) {
                // Update any corresponding data in the database
                drivingManuals = updateOldEntries(drivingManuals);

                // Save the rest
                saveDrivingManuals(drivingManuals);
            }
        }
    }

    /**
     * Stores downloaded data in database.
     */
    private void saveDrivingManuals(List<DrivingManual> drivingManuals) {

        Vector<ContentValues> cVVector = new Vector<ContentValues>();

        long id;
        String location;
        String type;
        String language;
        String url;
        String displayName;
        long lastUpdated;

        for(DrivingManual drivingManual : drivingManuals) {

            id = drivingManual.getId();
            location = drivingManual.getLocation();
            type = drivingManual.getType();
            language = drivingManual.getLanguage();
            url = drivingManual.getUrl();
            displayName = drivingManual.getDisplayName();
            lastUpdated = drivingManual.getLastUpdated();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_BACKEND_ID, id);
            contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_LOCATION, location);
            contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_TYPE, type);
            contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_LANGUAGE, language);
            contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_URL, url);
            contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_DISPLAY_NAME, displayName);
            contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_LAST_UPDATED, lastUpdated);
            contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_DOWNLOADED, false);
            contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_LAST_PAGE, 0);

            cVVector.add(contentValues);
        }

        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = cVVector.toArray(new ContentValues[cVVector.size()]);
            mContentResolver.bulkInsert(DrivingContract.DrivingManualEntry.CONTENT_URI, cvArray);
        }
    }

    /**
     * Updates database entries with new data.
     */
    private List<DrivingManual> updateOldEntries(List<DrivingManual> drivingManuals) {

        String selection = getString(R.string.manual_table_update_selection,
                DrivingContract.DrivingManualEntry.COLUMN_LOCATION,
                DrivingContract.DrivingManualEntry.COLUMN_TYPE,
                DrivingContract.DrivingManualEntry.COLUMN_LANGUAGE);

        for(DrivingManual drivingManual : drivingManuals) {

            String location = drivingManual.getLocation();
            String type = drivingManual.getType();
            String language = drivingManual.getLanguage();

            String[] selectionArgs = {location, type, language};

            Cursor cursor = mContentResolver.query(DrivingContract.DrivingManualEntry.CONTENT_URI, null, selection, selectionArgs, null);

            if(cursor != null && cursor.moveToFirst()) {
                // Get database _ID
                int id = cursor.getInt(0);

                // Delete old file if it exists
                if(cursor.getInt(MainListFragment.COLUMN_DOWNLOADED) == Manual.DOWNLOADED) {
                    String fileName = cursor.getLong(MainListFragment.COLUMN_BACKEND_ID) + getString(R.string.pdf_file_extension);
                    File file = new File(getFilesDir(), fileName);
                    if(file.exists()) {
                        file.delete();
                    }

                }

                String select = DrivingContract.DrivingManualEntry._ID + " = ?";
                String[] selectArgs = {String.valueOf(id)};

                ContentValues contentValues = new ContentValues();
                contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_URL, drivingManual.getUrl());
                contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_DISPLAY_NAME, drivingManual.getDisplayName());
                contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_LAST_UPDATED, drivingManual.getLastUpdated());
                contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_BACKEND_ID, drivingManual.getId());

                contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_DOWNLOADED, false);
                contentValues.put(DrivingContract.DrivingManualEntry.COLUMN_LAST_PAGE, 0);

                mContentResolver.update(DrivingContract.DrivingManualEntry.CONTENT_URI, contentValues, select, selectArgs);

                drivingManuals.remove(drivingManual);

                cursor.close();
            }

        }

        return drivingManuals;
    }
}
