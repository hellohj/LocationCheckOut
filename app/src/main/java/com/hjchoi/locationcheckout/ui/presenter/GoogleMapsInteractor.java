package com.hjchoi.locationcheckout.ui.presenter;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

public interface GoogleMapsInteractor {
    /**
     * Set up Google Map
     * @param context
     * @param mapFragment
     */
    void setUpGoogleMap(Context context, SupportMapFragment mapFragment);

    /**
     * Add a listener for a marker click event
     * @param markerClickListener
     */
    void addOnMarkerClickListener(GoogleMap.OnMarkerClickListener markerClickListener);

    /**
     * Add a listener for camera changes on a map
     * @param cameraChangeListener
     */
    void addOnCameraChangeListener(GoogleMap.OnCameraChangeListener cameraChangeListener);

    /**
     * enable/disable gestures of a map
     *
     * @param enabled
     */
    void setMapGesturesEnabled(boolean enabled);

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

    /**
     * Display places to a map from firebase
     * @param placeId
     */
    void addPlaceToMap(String placeId);
}
