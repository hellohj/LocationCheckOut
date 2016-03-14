package com.hjchoi.locationcheckout.ui.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.hjchoi.locationcheckout.model.PlaceModel;
import com.hjchoi.locationcheckout.ui.view.MapsView;
import com.hjchoi.locationcheckout.utils.Utils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MapsPresenterImpl implements
        MapsPresenter,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraChangeListener,
        ChildEventListener,
        ValueEventListener,
        SlidingUpPanelLayout.PanelSlideListener,
        View.OnClickListener {

    private static final String LOG_TAG = MapsPresenterImpl.class.getSimpleName();

    private MapsView mView;
    private GoogleMapsInteractor mGoogleMapsInteractor;
    private FirebaseInteractor mFirebaseInteractor;

    public MapsPresenterImpl(MapsView view) {
        this.mView = view;
        mGoogleMapsInteractor = new GoogleMapsInteractorImpl();
        mFirebaseInteractor = new FirebaseInteractorImpl();
    }

    @Override
    public void onActivityResult(int resultCode, Intent data, Activity activity) {
        Log.d(LOG_TAG, "onActivityResult: results from a Place Picker intent");
        if (resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(activity, data);
            if (place != null) {
                PlaceModel placeModel = Utils.populatePlaceModel(place);
                // add a place to firebase
//                    mFirebase.child(BuildConfig.FIREBASE_ROOT_NODE).child(place.getId()).setValue(placeModel);
                mFirebaseInteractor.setChildOnActivityResult(place.getId(), placeModel);
            }
        } else if (resultCode == PlacePicker.RESULT_ERROR) {
            Toast.makeText(activity, "Places API failure! Check that the API is enabled for your key",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        mView = null;
    }

    @Override
    public void setGoogleMap(Context context, SupportMapFragment mapFragment) {
        mGoogleMapsInteractor.setUpGoogleMap(context, mapFragment);
    }

    @Override
    public void setFirebase(Context context) {
        mFirebaseInteractor.setUpFirebase(context);
        mFirebaseInteractor.addFirebasChildEventListener(this);
    }

    @Override
    public void setSlidingUpPanel(Context context) {
        Log.d(LOG_TAG, "setUpSlidingUpPanel");
        mView.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mView.setAddPanelSlideListener(this);
        mView.setFadeOnClickListener(this);
    }

    @Override
    public void setUI() {
        mView.setUIListener();
    }

    @Override
    public void connectGoogleApiClient(boolean connect) {
        mGoogleMapsInteractor.connectGoogleApiClient(connect);
    }

    @Override
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleMapsInteractor.getGoogleApiClient();
    }

    /**
     * Camera change event when a user moves around in a map
     *
     * @param cameraPosition
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.d(LOG_TAG, "map onCameraChange: let's set panel state hidden");
        // when a user moves on, disable sliding panel so that a user can click a marker
        mView.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
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
        if (mView.existSlidingUpPanelLayout() ){
            mView.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            mFirebaseInteractor.addFirebaseValueEventListener(this, marker.getTitle());
        }
        // return true to prevent a map from showing default behaviors like direction, marker title, etc.
        return true;
    }

    /**
     * Load a bitmap from the photos API asynchronously
     * by using buffers and result callbacks.
     */
    private void placePhotosAsync(String placeId) {
        mView.showPhotoOfPlace(placeId);
    }

    //////////////////////////////////////////////////
    // Firebase listener callbacks
    //////////////////////////////////////////////////
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Log.d(LOG_TAG, "onChildAdded");
        String placeId = dataSnapshot.getKey();
        Log.d(LOG_TAG, "placeId from dataSnapShot: " + placeId);
        if (placeId != null) {
            ///////////////////////////////////
            // should i add listeners to each market? or is this ok?
            ///////////////////////////////////
            mGoogleMapsInteractor.addOnMarkerClickListener(this);
            mGoogleMapsInteractor.addOnCameraChangeListener(this);
            // display places to a map
            mGoogleMapsInteractor.addPlaceToMap(placeId);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        PlaceModel model = dataSnapshot.getValue(PlaceModel.class);
        if (model != null) {
            Log.d(LOG_TAG, "market value from firebase child node: " + model.getPlaceId() + "--" + model.getName());
            // populate info to panel here
            mView.showDetailsOfPlace(model);
            mView.showPhotoOfPlace(model.getPlaceId());
            placePhotosAsync(model.getPlaceId());
        } else {
            Log.d(LOG_TAG, "market value from firebase child node: none");
//                        finish();
        }
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
        Log.d(LOG_TAG, "Firebase onCancelled: FirebaseError: " + firebaseError.getMessage());
    }

    //////////////////////////////////////////////////
    // SlidingUpPanel listener callbacks
    //////////////////////////////////////////////////
    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        Log.d(LOG_TAG, "onPanelSlide, offset " + slideOffset);
        mGoogleMapsInteractor.setMapGesturesEnabled(true);
    }

    @Override
    public void onPanelExpanded(View panel) {
        Log.d(LOG_TAG, "onPanelExpanded");
        mGoogleMapsInteractor.setMapGesturesEnabled(false);
    }

    @Override
    public void onPanelCollapsed(View panel) {
        Log.d(LOG_TAG, "onPanelCollapsed");
        mGoogleMapsInteractor.setMapGesturesEnabled(true);
    }

    @Override
    public void onPanelAnchored(View panel) {
        Log.d(LOG_TAG, "onPanelAnchored");
    }

    @Override
    public void onPanelHidden(View panel) {
        Log.d(LOG_TAG, "onPanelHidden");
        mGoogleMapsInteractor.setMapGesturesEnabled(true);
    }

    /**
     * Callback for setFadeOnClickListener
     * @param v
     */
    @Override
    public void onClick(View v) {
        mView.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
}
