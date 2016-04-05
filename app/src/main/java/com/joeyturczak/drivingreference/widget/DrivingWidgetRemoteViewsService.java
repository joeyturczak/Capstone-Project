package com.joeyturczak.drivingreference.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.joeyturczak.drivingreference.R;
import com.joeyturczak.drivingreference.data.DrivingContract;
import com.joeyturczak.drivingreference.models.Manual;
import com.joeyturczak.drivingreference.utils.ManualUtility;
import com.joeyturczak.drivingreference.utils.LocationUtility;
import com.joeyturczak.drivingreference.utils.Utility;

/**
 * Copyright (C) 2016 Joey Turczak
 */
public class DrivingWidgetRemoteViewsService extends RemoteViewsService {

    public static final int COLUMN_BACKEND_ID = 1;
    public static final int COLUMN_LOCATION = 2;
    public static final int COLUMN_TYPE = 3;
    public static final int COLUMN_LANGUAGE = 4;
    public static final int COLUMN_URL = 5;
    public static final int COLUMN_DISPLAY_NAME = 6;
    public static final int COLUMN_LAST_UPDATED = 7;
    public static final int COLUMN_DOWNLOADED = 8;
    public static final int COLUMN_LAST_PAGE = 9;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                String location = LocationUtility.getLocationConfig(DrivingWidgetRemoteViewsService.this);

                String sortOrder = DrivingContract.DrivingManualEntry.COLUMN_TYPE + getString(R.string.sort_order_asc);
                String selection = DrivingContract.DrivingManualEntry.COLUMN_LOCATION + " = '" + location + "'";

                data = getContentResolver().query(DrivingContract.DrivingManualEntry.CONTENT_URI, null, selection, null, sortOrder);

                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                Manual manual = ManualUtility.getManualFromCursor(data);

                views.setTextViewText(R.id.manual_name, manual.getDisplayName());

                int imageId = Utility.getResourceIconId(data.getString(COLUMN_TYPE));

                views.setImageViewResource(R.id.manual_icon, imageId);

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra(getString(R.string.widget_manual_key), manual);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
