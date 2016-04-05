package com.joeyturczak.drivingreference.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.joeyturczak.drivingreference.R;
import com.joeyturczak.drivingreference.adapters.LocationSpinnerAdapter;
import com.joeyturczak.drivingreference.models.Manual;
import com.joeyturczak.drivingreference.services.LocationIntentService;
import com.joeyturczak.drivingreference.services.UpdateDataIntentService;
import com.joeyturczak.drivingreference.utils.LocationUtility;
import com.joeyturczak.drivingreference.utils.Utility;

import java.util.Arrays;
import java.util.List;

/**
 * Copyright (C) 2016 Joey Turczak
 */
public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener,
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener,
        MainListFragment.OnItemSelectedListener {

    private static final String MENU_FRAGMENT_TAG = "MF";
    private static final String PDF_FRAGMENT_TAG = "PF";

    public static final int PDFFRAGMENT = 0;
    public static final int TESTFRAGMENT = 1;

    private static final int PERMISSION_ACCESS_LOCATION = 0;

    private boolean mIsLargeLayout;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private AdView mAdView;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;

    private LocationSpinnerAdapter mLocationSpinnerAdapter;

    private Spinner mLocationSpinner;

    private int mLastPercent = 100;

    private String mTitle;

    private String mCurrentLocation;

    private boolean mIsDbEmpty;

    /** Updates the current track and updates the display when a notification button is pressed. */
    private BroadcastReceiver mLocationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int requestCode = intent.getIntExtra(getString(R.string.location_intent_result_key), 0);
            String location = intent.getStringExtra(getString(R.string.location_intent_message_key));

            switch (requestCode) {
                case LocationIntentService.SUCCESS:
                    if(mCurrentLocation == null) {
                        mCurrentLocation = location;
                        LocationUtility.setLocationConfig(context, mCurrentLocation);
                        updateSpinnerSelection();
                    }
                    break;
            }
        }
    };

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocationBroadcastReceiver,
                new IntentFilter(getString(R.string.location_broadcast_intent_filter)));
        updateAppBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocationBroadcastReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.main_activity_current_location_key), mCurrentLocation);
        bundle.putString(getString(R.string.main_activity_title_key), mTitle);
        outState.putBundle(getString(R.string.bundle_main_activity_state_key), bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle(getString(R.string.bundle_main_activity_state_key));
            if(bundle != null) {
                mCurrentLocation = bundle.getString(getString(R.string.main_activity_current_location_key));
                mTitle = bundle.getString(getString(R.string.main_activity_title_key));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

        ViewCompat.setTransitionName(mAppBarLayout, getString(R.string.transition_main_activity_image));
        supportPostponeEnterTransition();

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        displayUpButton();

        buildGoogleApiClient();

        initializeSpinner();

        initializeAppBar();

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mAdView.loadAd(adRequest);

        if(savedInstanceState == null) {
            loadContent();
        }

        // Recieve intent from widget and load appropriate content
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            Manual manual = bundle.getParcelable(getString(R.string.widget_manual_key));
            if (manual != null) {
                onItemSelected(manual, PDFFRAGMENT, null);
            }
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_LOCATION);
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                if (Geocoder.isPresent()) {
                    startLocationIntentService();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_ACCESS_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGoogleApiClient.reconnect();
                }
                break;
        }
    }

    public void startLocationIntentService() {
        Intent intent = new Intent(this, LocationIntentService.class);
        intent.putExtra(LocationIntentService.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mGoogleApiClient.reconnect();
    }

    @Override
    public void onBackStackChanged() {
        displayUpButton();
        updateAppBar();
    }

    /**
     * Displays up button if there's a fragment backStack
     */
    public void displayUpButton() {
        boolean back = getSupportFragmentManager().getBackStackEntryCount() > 0;
        getSupportActionBar().setDisplayHomeAsUpEnabled(back);
    }

    @Override
    public void onItemSelected(Object object, int fragmentId, RecyclerView.ViewHolder viewHolder) {

        mAppBarLayout.setExpanded(false, true);

        switch (fragmentId) {
            case PDFFRAGMENT:
                Manual manual = (Manual) object;
                mTitle = manual.getDisplayName();
                PDFFragment pdfFragment = PDFFragment.newInstance();

                Bundle bundle = new Bundle();
                bundle.putParcelable(getString(R.string.pdf_fragment_manual_key), manual);

                pdfFragment.setArguments(bundle);

                if (mIsLargeLayout) {
                    getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.detail_container, pdfFragment, PDF_FRAGMENT_TAG)
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(0, android.R.anim.slide_out_right, 0, android.R.anim.slide_out_right)
                            .add(R.id.main_container, pdfFragment, PDF_FRAGMENT_TAG)
                            .addToBackStack(null)
                            .commit();
                }
                break;
            case TESTFRAGMENT:
                //TODO start test fragment
                break;
        }
    }

    /**
     * Sets up the spinner list and item selected listener
     */
    private void initializeSpinner() {

        String location = LocationUtility.getLocationConfig(this);

        mLocationSpinner = (Spinner) findViewById(R.id.location_spinner);

        List<String> locations = Arrays.asList(getResources().getStringArray(R.array.locations));

        mLocationSpinnerAdapter = new LocationSpinnerAdapter(this, R.layout.location_spinner_item, R.id.location_title, locations, mIsLargeLayout);

        mLocationSpinner.setAdapter(mLocationSpinnerAdapter);
        mLocationSpinner.setSelection(Utility.getSpinnerValue(this, location));
        mLocationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String location = parent.getItemAtPosition(position).toString().replace(" ", "_");
                LocationUtility.setLocationConfig(parent.getContext(), location);
                mLocationSpinner.requestFocus();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mLocationSpinner.requestFocus();
    }

    /**
     * Sets up AppBarLayout for each orientation
     */
    public void initializeAppBar() {
        if (getResources().getBoolean(R.bool.landscape)) {
                mAppBarLayout.setFitsSystemWindows(false);
        } else {
            if (mAppBarLayout != null) {
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    mAppBarLayout.setExpanded(false);
                }

                mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        int scrollRange = appBarLayout.getTotalScrollRange();

                        // 0 doesn't like to be divided
                        if (scrollRange > 0) {
                            int percent = (verticalOffset + scrollRange) * 100 / scrollRange;
                            if (mLastPercent >= 20 && percent <= 20 || mLastPercent <= 20 && percent >= 20) {
                                if (percent < 20) {
                                    mLocationSpinnerAdapter.setViewType(LocationSpinnerAdapter.VIEW_TYPE_SMALL);
                                } else {
                                    mLocationSpinnerAdapter.setViewType(LocationSpinnerAdapter.VIEW_TYPE_LARGE);
                                }
                            }

                            mLastPercent = percent;
                        }
                    }
                });
            }
            ImageView imageView = (ImageView) findViewById(R.id.toolbar_image);
            loadImage(imageView);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getCacheDir().delete();
    }

    /**
     * Sets the title to the appropriate toolbar depending on orientation
     */
    private void setToolbarTitle(String title) {

        if (getResources().getBoolean(R.bool.landscape)) {
            setTitle(title);
        } else {
            mCollapsingToolbarLayout.setTitle(title);
        }
    }

    /**
     * Updates spinner to the currently selected location
     */
    private void updateSpinnerSelection() {
        mLocationSpinner.setSelection(Utility.getSpinnerValue(this, mCurrentLocation));
    }

    /**
     * Shows or hides spinner
     */
    private void showSpinner(boolean show) {
        if(show) {
            mLocationSpinner.setVisibility(View.VISIBLE);
        } else {
            mLocationSpinner.setVisibility(View.GONE);
        }
    }

    /**
     * Shows the appropriate Toolbar based on the current fragment
     */
    private void updateAppBar() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            setToolbarTitle(mTitle);
            showSpinner(false);
        } else {
            setToolbarTitle(getString(R.string.app_name));
            showSpinner(true);
        }
    }

    /**
     * Loads a random image into imageView with Glide
     */
    private void loadImage(ImageView imageView) {
        int randomNumber = Utility.getRandomNumber(1, 13, true);
        String resourceName = "background_" + String.valueOf(randomNumber);
        int id = getResources().getIdentifier(resourceName, getString(R.string.drawable_identifier), getString(R.string.package_name));
        Glide.with(this).load(id).fitCenter().into(imageView);
    }

    private void addMainListFragment() {

        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.main_container, MainListFragment.newInstance(), MENU_FRAGMENT_TAG)
                .commit();
    }

    private void addDetailFragment() {
        getSupportFragmentManager().beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .replace(R.id.detail_container, new Fragment())
            .commit();
    }

    public void removeDetailFragment() {
        getSupportFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                .replace(R.id.detail_container, new Fragment())
                .commit();
    }

    private void loadContent() {

        mIsDbEmpty = Utility.isDbEmpty(this);
        if(mIsDbEmpty) {
            checkNetworkAndUpdate();
        } else {
            updateData();
            addFragments();
        }
    }

    /**
     * Starts the intent service to check if there is new data
     */
    private void updateData() {
        Intent intent = new Intent(this, UpdateDataIntentService.class);
        intent.setAction(UpdateDataIntentService.ACTION_UPDATE_CONTENT);
        intent.putExtra(getString(R.string.database_empty_intent_key), mIsDbEmpty);
        startService(intent);
    }

    /**
     * Checks for network connection before trying to update
     * This method is only called if there is no data to display
     */
    private void checkNetworkAndUpdate() {
        if(Utility.isNetworkAvailable(this)) {
            updateData();
            addFragments();
        } else {
            showNetworkError();
        }
    }

    /**
     * Shows a dialog if there is no network connection
     * This method is only called if there is no data to display
     */
    private void showNetworkError() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.alert_title_no_network))
                .setMessage(getString(R.string.alert_message_no_network))
                .setPositiveButton(getString(R.string.alert_no_network_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkNetworkAndUpdate();
                    }
                })
                .setCancelable(false)
                .create();

        alertDialog.show();
    }

    /**
     * Adds the main fragments
     */
    private void addFragments() {
        addMainListFragment();

        if(mIsLargeLayout) {
            addDetailFragment();
        }
    }
}
