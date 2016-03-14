package com.hjchoi.locationcheckout.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.SupportMapFragment;

public interface MapsPresenter {

    void onActivityResult(int resultCode, Intent data, Activity activity);

    void onDestroy();

//    void setMapCoverOnTouchListener();
//
//    void setFabOnClickListener();

    void setGoogleMap(Context context, SupportMapFragment mapFragment);

    void setFirebase(Context context);

    void setSlidingUpPanel(Context context);

    void setUI();

    void connectGoogleApiClient(boolean connect);

    GoogleApiClient getGoogleApiClient();
}