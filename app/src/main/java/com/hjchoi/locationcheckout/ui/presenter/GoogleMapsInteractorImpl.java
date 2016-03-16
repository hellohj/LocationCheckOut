package com.hjchoi.locationcheckout.ui.presenter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapsInteractorImpl implements
        GoogleMapsInteractor,
        OnMapReadyCallback,
        LocationListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String LOG_TAG = GoogleMapsInteractorImpl.class.getSimpleName();
    private static final int REQUEST_CODE_LOCATION = 2;

    private Context context;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds.Builder mBounds = new LatLngBounds.Builder();

    @Override
    public void setUpGoogleMap(Context context, SupportMapFragment mapFragment) {
        Log.d(LOG_TAG, "setUpGoogleMap");
        this.context = context;
        // Set up Google Maps
        if (mMap == null) {
            mapFragment.getMapAsync(this);
            Log.d(LOG_TAG, "setUpGoogleMap - fragment getMapAsync");
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(context)
                        .addApi(Places.GEO_DATA_API)
                        .addApi(AppIndex.API).build();
            }
        }
    }

    /**
     * Map setup. This is called when the GoogleMap is available to manipulate.
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "onMapReady");
        mMap = googleMap;
        if (mMap != null) {
            Log.d(LOG_TAG, "onMapReady - map is not null");

            // ActivityCompat#requestPermissions
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_LOCATION);
//                return;
            } else {
                // Location permission has been granted, continue as usual.
//                Location myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            Log.d(LOG_TAG, "onMapReady - ready to use after settings");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // success!
                Log.d(LOG_TAG, "Map permission has now been granted. Showing preview.");
            } else {
                // Permission was denied or request was cancelled
                Log.d(LOG_TAG, "Map permission was not granted.");
            }
        }
    }

    @Override
    public void addOnMarkerClickListener(GoogleMap.OnMarkerClickListener markerClickListener) {
        if (mMap != null) {
            mMap.setOnMarkerClickListener(markerClickListener);
        } else {
            Log.d(LOG_TAG, "map is null so can't attach marker click listener");
        }
    }

    @Override
    public void addOnCameraChangeListener(GoogleMap.OnCameraChangeListener cameraChangeListener) {
        if (mMap != null) {
            mMap.setOnCameraChangeListener(cameraChangeListener);
        }else {
            Log.d(LOG_TAG, "map is null so can't attach camera change listener");
        }
    }

    @Override
    public void setMapGesturesEnabled(boolean enabled) {
        mMap.getUiSettings().setAllGesturesEnabled(enabled);
    }

    @Override
    public void connectGoogleApiClient(boolean connect) {
        if (connect) {
            mGoogleApiClient.connect();
        } else {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public GoogleApiClient getGoogleApiClient() {
        return this.mGoogleApiClient;
    }

    @Override
    public void addPlaceToMap(String placeId) {
        Places.GeoDataApi
                .getPlaceById(mGoogleApiClient, placeId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                       @Override
                                       public void onResult(PlaceBuffer places) {
                                           Log.d(LOG_TAG, "add a child to firebase and display it to a map with a marker");
                                           if (places.get(0) != null) {
                                               LatLng location = places.get(0).getLatLng();
                                               addPointToViewPort(location, places.get(0).getId());
                                           }
                                           // release places to prevent a memory leak
                                           places.release();
                                       }
                                   }
                );
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "onLocationChanged");
        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
        addPointToViewPort(ll, null);
    }

    private void addPointToViewPort(LatLng newPoint, String placeId) {
        Log.d(LOG_TAG, "addPointToViewPort");
        mBounds.include(newPoint);
        BitmapDescriptor defaultMarker =
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        Log.d(LOG_TAG, "add a market to a map");
        mMap.addMarker(new MarkerOptions()
                .position(newPoint)
                .title(placeId)
                .icon(defaultMarker));
        Log.d(LOG_TAG, "map is ready and add marker click listener");
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(newPoint);
        mMap.animateCamera(cameraUpdate);
    }
}
