package com.joeyturczak.drivingreference.utils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;

import com.joeyturczak.drivingreference.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Copyright (C) 2016 Joey Turczak
 *
 * Downloads a file from the given url and stores it with the given file name to the app's
 * file directory. Sends out a broadcast when the task is complete or if there is an error.
 */
public class DownloadManualTask extends AsyncTask<Void, Integer, String> {

    public static final String RESULT_SUCCESS = "Success";
    public static final String RESULT_ERROR = "Error";

    private PowerManager.WakeLock mWakeLock;
    private Context mContext;
    private String mFileName;
    private File mFile;
    private String mUrl;

    public DownloadManualTask(Context context, String fileName, String url) {
        mContext = context;
        mFileName = fileName;
        mFile = new File(mContext.getFilesDir(), mFileName);
        mUrl = url;
    }

    @Override
    protected String doInBackground(Void... params) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(mUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(true);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return mContext.getString(R.string.download_server_error) + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();

            input = connection.getInputStream();

            output = new FileOutputStream(mFile);

            byte data[] = new byte[1024];
            long total = 0;
            int count;

            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    if(mFile.exists()) {
                        mFile.delete();
                    }
                    return RESULT_ERROR;
                }

                total += count;

                if (fileLength > 0) {
                    publishProgress((int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                mFile.delete();
            }
        }

        if(!mFile.exists()) {
            return RESULT_ERROR;
        } else {
            return RESULT_SUCCESS;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        // Send progress update
        Intent intent = new Intent(mContext.getString(R.string.download_progress_intent_filter));
        intent.putExtra(mContext.getString(R.string.download_progress_key), values[0]);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    protected void onPostExecute(String result) {
        // Send done broadcast
        Intent intent = new Intent(mContext.getString(R.string.download_complete_intent_filter));
        intent.putExtra(mContext.getString(R.string.download_result_key), result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        mWakeLock.release();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        File file = new File(mContext.getFilesDir(), mFileName);
        if(file.exists()) {
            file.delete();
        }
    }
}
