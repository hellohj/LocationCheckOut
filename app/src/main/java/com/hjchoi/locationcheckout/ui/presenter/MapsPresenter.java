package com.hjchoi.locationcheckout.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.SupportMapFragment;

public interface MapsPresenter {

    /**
     * onActivityResult
     * @param resultCode
     * @param data
     * @param activity
     */
    void onActivityResult(int resultCode, Intent data, Activity activity);

    /**
     * onDestroy
     */
    void onDestroy();

    /**
     * Set up google map
     * @param context
     * @param mapFragment
     */
    void setGoogleMap(Context context, SupportMapFragment mapFragment);

    /**
     * Set up firebase
     * @param context
     */
    void setFirebase(Context context);

    /**
     * Set up Sliding Up Panel for marker details
     * @param context
     */
    void setSlidingUpPanel(Context context);

    /**
     * Set up miscellaneous UI
     */
    void setUI();

    /**
     * Connect/disconnect google api client
     * @param connect
     */
    void connectGoogleApiClient(boolean connect);

    /**
     * Get google api client
     * @return
     */
    GoogleApiClient getGoogleApiClient();
}