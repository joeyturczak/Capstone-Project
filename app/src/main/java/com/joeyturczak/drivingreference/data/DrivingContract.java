package com.joeyturczak.drivingreference.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Copyright (C) 2016 Joey Turczak
 */
public class DrivingContract {

    public static final String CONTENT_AUTHORITY = "com.joeyturczak.drivingreference";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MANUALS = "manuals";

    public static final class DrivingManualEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MANUALS).build();

        public static final String TABLE_NAME = "manuals";

        public static final String COLUMN_BACKEND_ID = "backend_id";

        public static final String COLUMN_LOCATION = "location";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_LAST_UPDATED = "last_updated";
        public static final String COLUMN_DOWNLOADED = "downloaded";
        public static final String COLUMN_LAST_PAGE = "last_page";

        public static Uri buildManualUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
