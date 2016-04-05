package com.joeyturczak.drivingreference.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.joeyturczak.drivingreference.R;
import com.joeyturczak.drivingreference.adapters.PDFViewPagerAdapter;
import com.joeyturczak.drivingreference.models.Manual;
import com.joeyturczak.drivingreference.utils.DownloadManualTask;
import com.joeyturczak.drivingreference.utils.ManualUtility;
import com.joeyturczak.drivingreference.utils.Utility;

import java.io.File;
import java.io.IOException;

/**
 * Copyright (C) 2016 Joey Turczak
 */
public class PDFFragment extends Fragment {

    private ProgressDialog mProgressDialog;
    private View mRootView;

    private PDFViewPagerAdapter mPDFViewPagerAdapter;
    private PDFViewPager mPDFViewPager;

    private Context mContext;

    private String mFileName;

    private Manual mManual;

    private int mCurrentPageNumber;

    private ProgressBar mProgressBar;

    private TextView mPlaceholderText;

    private Toast mToast;

    public static PDFFragment newInstance() {
        return new PDFFragment();
    }

    private BroadcastReceiver mDownloadProgressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getIntExtra(context.getString(R.string.download_progress_key), 0);
            if(mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.setIndeterminate(false);
                    mProgressDialog.setMax(100);
                    mProgressDialog.setProgress(progress);
                }
            }
        }
    };

    private BroadcastReceiver mDownloadCompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            String result = intent.getStringExtra(context.getString(R.string.download_result_key));
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            if(result.equals(DownloadManualTask.RESULT_SUCCESS)) {
                showToast(getString(R.string.toast_download_complete));
                mManual.setDownloaded(Manual.DOWNLOADED);
                ManualUtility.setFileDownloaded(mContext, mManual.getId());
                try {
                    openPdf();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                showToast(result);
                getFragmentManager().popBackStackImmediate();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();

        if(bundle != null) {
            mManual = bundle.getParcelable(getString(R.string.pdf_fragment_manual_key));
        }

        if(savedInstanceState != null) {
            Bundle savedBundle = savedInstanceState.getBundle(mContext.getString(R.string.pdf_fragment_bundle_key));
            if(savedBundle != null) {
                mManual = savedBundle.getParcelable(getString(R.string.pdf_fragment_manual_key));
            }
        }

        mFileName = String.valueOf(mManual.getId()) + getString(R.string.pdf_file_extension);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_pdf, menu);
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        String message = mManual.getDisplayName() + ": " + mManual.getUrl();
        shareActionProvider.setShareIntent(createShareIntent(message));
    }

    /** Creates an intent for sharing the currently displayed manual's url */
    private Intent createShareIntent(String shareMessage) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareIntent.setType(mContext.getString(R.string.share_manual_type));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        return shareIntent;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDownloadProgressReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDownloadCompleteReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDownloadProgressReceiver,
                new IntentFilter(mContext.getString(R.string.download_progress_intent_filter)));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDownloadCompleteReceiver,
                new IntentFilter(mContext.getString(R.string.download_complete_intent_filter)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.pdf_fragment_manual_key), mManual);
        outState.putBundle(getString(R.string.pdf_fragment_bundle_key), bundle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_pdf, container, false);
        mPlaceholderText = (TextView) mRootView.findViewById(R.id.placeholder_text);
        mPlaceholderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializePdfView();
            }
        });

        initializePdfView();

        return mRootView;
    }

    /**
     * Sets up the ProgressDialog and starts the download task
     */
    private void initializePdfView() {

        if(mManual.getDownloaded() == Manual.NOT_DOWNLOADED) {
            if(!Utility.isNetworkAvailable(mContext)) {
                showToast(mContext.getString(R.string.toast_network_unavailable));
                mPlaceholderText.setText(R.string.pdf_placeholder_network_unavailable);
            } else {
                mPlaceholderText.setText(R.string.pdf_placeholder_text);
                mProgressDialog = new ProgressDialog(getContext());
                mProgressDialog.setMessage(mContext.getString(R.string.pdf_download_dialog_message));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);

                final DownloadManualTask downloadManualTask = new DownloadManualTask(mContext, mFileName, mManual.getUrl());

                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        downloadManualTask.cancel(true);
                        getFragmentManager().popBackStackImmediate();
                    }
                });
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                downloadManualTask.execute();
                mProgressDialog.show();
            }
        } else {
            try {
                openPdf();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets up the ViewPager to display the downloaded pdf and sets it to the last page the user was reading.
     * @throws IOException
     */
    private void openPdf() throws IOException {
        File file = new File(getContext().getFilesDir(), mFileName);
        PdfRenderer pdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
        mPDFViewPagerAdapter = new PDFViewPagerAdapter(getChildFragmentManager(), mContext, pdfRenderer.getPageCount(), file.getName());
        mPDFViewPager = (PDFViewPager) mRootView.findViewById(R.id.pdf_view);

        pdfRenderer.close();

        mPDFViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mCurrentPageNumber = position;
                ManualUtility.setPageNumber(mContext, mManual.getId(), mCurrentPageNumber);
            }
        });

        mPDFViewPager.setAdapter(mPDFViewPagerAdapter);

        mPDFViewPager.setCurrentItem(mManual.getLastPage());
    }

    /**
     * Shows a toast with the given message.
     */
    private void showToast(String message) {
        if(mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
        mToast.show();
    }
}
