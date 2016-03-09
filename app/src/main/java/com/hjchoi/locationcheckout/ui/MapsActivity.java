package com.hjchoi.locationcheckout.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hjchoi.locationcheckout.BuildConfig;
import com.hjchoi.locationcheckout.R;
import com.hjchoi.locationcheckout.model.PlaceModel;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        ChildEventListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraChangeListener,
        SlidingUpPanelLayout.PanelSlideListener {

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();

    @Bind(R.id.fab_checkOut) FloatingActionButton mFabCheckOut;
    @Bind(R.id.sliding_layout) SlidingUpPanelLayout mLayout;
    @Bind(R.id.tvName) TextView mLocationName;
    @Bind(R.id.map_cover) View mMapCover;
    @Bind(R.id.ivPlacePhoto) ImageView mPlacePhoto;
    @Bind(R.id.ivPlaceAddress) ImageView mIvPlaceAddress;
    @Bind(R.id.ivPlacePhone) ImageView mIvPlacePhone;
    @Bind(R.id.ivPlaceWebsite) ImageView mIvPlaceWebsite;
    @Bind(R.id.tvPlaceAddress) TextView mTvPlaceAddress;
    @Bind(R.id.tvPlacePhone) TextView mTvPlacePhone;
    @Bind(R.id.tvPlaceWebsite) TextView mTvPlaceWebsite;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds.Builder mBounds = new LatLngBounds.Builder();
    private static final int REQUEST_PLACE_PICKER = 1;
    private Firebase mFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        setUpGoogleMap();
        setUpFireBase();
        setUpSlidingUpPanel();
        setUpUI();
    }

    private void setUpGoogleMap() {
        Log.d(LOG_TAG, "setUpGoogleMap");
        // Set up Google Maps
        mMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(AppIndex.API).build();
        }
    }

    private void setUpFireBase() {
        Log.d(LOG_TAG, "setUpFireBase");
        // Set up Firebase
        Firebase.setAndroidContext(this);
        mFirebase = new Firebase(BuildConfig.FIREBASE_URL);
        // childEventListend can handle more sophisticated events handling than addValueListenr
        mFirebase.child(BuildConfig.FIREBASE_ROOT_NODE).addChildEventListener(this);
    }

    private void setUpSlidingUpPanel() {
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mLayout.addPanelSlideListener(this);
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });
    }

    private void setUpUI() {
        // intercept touch event from SlidingUpPanel
        mMapCover.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                }
                return false;
            }
        });
        mFabCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                checkOut(view);
            }
        });
    }

    protected void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        Log.d(LOG_TAG, "onStop");
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
        ButterKnife.unbind(this);
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

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
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
        // listen a maker click event
        mMap.setOnMarkerClickListener(this);
        // listen a map move
        mMap.setOnCameraChangeListener(this);
    }

    @Override
    public void onBackPressed() {
        if ((mLayout != null) && (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)) {
            Log.d(LOG_TAG, "onBackPressed here");
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            Log.d(LOG_TAG, "onBackPressed before returning");
            super.onBackPressed();
        }
    }

    /**
     * Marker click event from a map
     *
     * @param marker
     * @return
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        // Handle marker click here
        Log.d(LOG_TAG, "marker clicked: " + marker.getId() + " -- " + marker.getPosition());
        if (mLayout != null) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
//            mLocationName.setText(marker.getPosition().toString());
            Log.d(LOG_TAG, "market title should be place id: " + marker.getTitle());
            mFirebase.child(BuildConfig.FIREBASE_ROOT_NODE).child(marker.getTitle()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    PlaceModel model = dataSnapshot.getValue(PlaceModel.class);
                    if (model != null) {
                        Log.d(LOG_TAG, "market value from firebase child node: " + model.getPlaceId() + "--" + model.getName());
                        mLocationName.setText(model.getName());
                        ///////////////////
                        // populate info to panel here????
                        placePhotosAsync(model.getPlaceId());
                        // Format the returned place's details and display them in the TextView.
//                        mPlaceDetails.setText(Util.formatPlaceDetails(getResources(), model.getName(),
//                                model.getAddress(), model.getPhone(), model.getWebsite()));
                        if (!TextUtils.isEmpty(model.getAddress())) {
                            mTvPlaceAddress.setText(model.getAddress());
                            mIvPlaceAddress.setVisibility(View.VISIBLE);
                        } else {
                            mTvPlaceAddress.setVisibility(View.GONE);
                            mIvPlaceAddress.setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(model.getPhone())) {
                            mTvPlacePhone.setText(model.getPhone());
                            mIvPlacePhone.setVisibility(View.VISIBLE);
                        } else {
                            mTvPlacePhone.setVisibility(View.GONE);
                            mIvPlacePhone.setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(model.getWebsite())) {
                            mTvPlaceWebsite.setText(model.getWebsite());
                            mIvPlaceWebsite.setVisibility(View.VISIBLE);
                        } else {
                            mTvPlaceWebsite.setVisibility(View.GONE);
                            mIvPlaceWebsite.setVisibility(View.GONE);
                        }
                        ///////////////////
                    } else {
                        Log.d(LOG_TAG, "market value from firebase child node: none");
                        finish();
                    }
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        } else {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
        // return true to prevent a map from showing default behaviors like direction, marker title, etc.
        return true;
    }

    /**
     * Camera change event when a user moves around in a map
     *
     * @param cameraPosition
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.d(LOG_TAG, "map onCameraChange: let's set panel state hidden");
        Log.i("centerLat", String.valueOf(cameraPosition.target.latitude));
        Log.i("centerLong", String.valueOf(cameraPosition.target.longitude));
        // when a user moves on, disable sliding panel so that a user can click a marker
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    /**
     * Prompt the user to check out of their location. Called when the "Check Out!" button clicked
     *
     * @param view
     */
    public void checkOut(View view) {
        try {
            Log.d(LOG_TAG, "checkout button clicked");
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent, REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Please install Google Play Services!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * intent return from pick a place
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult: results from a Place Picker intent");
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    PlaceModel placeModel = new PlaceModel();

                    Log.d(LOG_TAG, "******** attributions:" + place.getAttributions());
                    Log.d(LOG_TAG, "******** reviews:" + place.getRating());

                    placeModel.setPlaceId(place.getId());
                    if (!TextUtils.isEmpty(place.getName())) {
                        placeModel.setName(place.getName().toString());
                    }
                    placeModel.setLocation(place.getLatLng());
                    if (!TextUtils.isEmpty(place.getAddress())) {
                        placeModel.setAddress(place.getAddress().toString());
                    }
                    if ((place.getWebsiteUri() != null) && !TextUtils.isEmpty(place.getWebsiteUri().toString())) {
                        placeModel.setWebsite(place.getWebsiteUri().toString());
                    }
                    placeModel.setRating(place.getRating());
                    if (!TextUtils.isEmpty(place.getPhoneNumber())) {
                        placeModel.setPhone(place.getPhoneNumber().toString());
                    }
                    placeModel.setTimestamp(ServerValue.TIMESTAMP);
                    Log.d("place name: ", place.getName().toString());
                    Log.d("place latLng: ", place.getLatLng().toString());

                    mFirebase.child(BuildConfig.FIREBASE_ROOT_NODE).child(place.getId()).setValue(placeModel);
                }
            } else if (resultCode == PlacePicker.RESULT_ERROR) {
                Toast.makeText(this, "Places API failure! Check that the API is enabled for your key",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Firebase - add a child
     *
     * @param dataSnapshot
     * @param previousChildKey
     */
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
        Log.d(LOG_TAG, "onChildAdded");
        String placeId = dataSnapshot.getKey();
//        PlaceModel model = dataSnapshot.getValue(PlaceModel.class);
//        String placeId = model.getPlaceId();
        Log.d(LOG_TAG, "placeId from dataSnapShot: " + placeId);
        if (placeId != null) {
            Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                           @Override
                                           public void onResult(PlaceBuffer places) {
                                               Log.d(LOG_TAG, "add a child to firebase and display it to a map with a marker");
                                               LatLng location = places.get(0).getLatLng();
                                               // move camera to a new marker
//                                               CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(location);
//                                               mMap.animateCamera(cameraUpdate);
                                               addPointToViewPort(location, places.get(0).getId());
                                               // release places to prevent a memory leak
                                               places.release();
                                           }
                                       }
                    );
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Log.d(LOG_TAG, "onChildChanged");
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        Log.d(LOG_TAG, "onChildChanged");
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d(LOG_TAG, "onChildRemoved");
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        Log.d(LOG_TAG, "onChildRemoved");
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        Log.d(LOG_TAG, "onPanelSlide, offset " + slideOffset);
        mMap.getUiSettings().setAllGesturesEnabled(true);
    }

    @Override
    public void onPanelExpanded(View panel) {
        Log.d(LOG_TAG, "onPanelExpanded");
        mMap.getUiSettings().setAllGesturesEnabled(false);
    }

    @Override
    public void onPanelCollapsed(View panel) {
        Log.d(LOG_TAG, "onPanelCollapsed");
        mMap.getUiSettings().setAllGesturesEnabled(true);
    }

    @Override
    public void onPanelAnchored(View panel) {
        Log.d(LOG_TAG, "onPanelAnchored");
    }

    @Override
    public void onPanelHidden(View panel) {
        Log.d(LOG_TAG, "onPanelHidden");
        mMap.getUiSettings().setAllGesturesEnabled(true);
    }

    /////////////////////////
    // place photo callback
    /////////////////////////
    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback
            = new ResultCallback<PlacePhotoResult>() {
        @Override
        public void onResult(PlacePhotoResult placePhotoResult) {
            if (!placePhotoResult.getStatus().isSuccess()) {
                return;
            }
            mPlacePhoto.setImageBitmap(placePhotoResult.getBitmap());
        }
    };

    /**
     * Load a bitmap from the photos API asynchronously
     * by using buffers and result callbacks.
     */
    private void placePhotosAsync(String placeId) {
        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, placeId)
                .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {

                    @Override
                    public void onResult(PlacePhotoMetadataResult photos) {
                        if (!photos.getStatus().isSuccess()) {
                            Log.d(LOG_TAG, "photo get failure");
                            return;
                        }

                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                        if (photoMetadataBuffer.getCount() > 0) {
                            mPlacePhoto.setVisibility(View.VISIBLE);
                            Log.d(LOG_TAG, "photo get success - more than one");
                            // Display the first bitmap in an ImageView in the size of the view
                            photoMetadataBuffer.get(0)
                                    .getScaledPhoto(mGoogleApiClient, mPlacePhoto.getWidth(),
                                            mPlacePhoto.getHeight())
                                    .setResultCallback(mDisplayPhotoResultCallback);
                        } else {
                            Log.d(LOG_TAG, "photo get success - no photo");
                            mPlacePhoto.setVisibility(View.GONE);
                        }
                        photoMetadataBuffer.release();
                    }
                });
    }
}