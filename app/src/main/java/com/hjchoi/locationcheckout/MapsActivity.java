package com.hjchoi.locationcheckout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        ChildEventListener,
        LocationListener,
        SlidingUpPanelLayout.PanelSlideListener {

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();

    @Bind(R.id.checkout_button) Button mCheckoutButton;
    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds.Builder mBounds = new LatLngBounds.Builder();
    private static final int REQUEST_PLACE_PICKER = 1;
    private Firebase mFirebase;
    // related to SlidingUpPanel
    @Bind(R.id.sliding_layout) SlidingUpPanelLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        setUpGoogleMap();
        setUpFireBase();
        setUpSlidingUpPanel();
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


        TextView t = (TextView) findViewById(R.id.name);
        t.setText(Html.fromHtml("hello"));
        Button f = (Button) findViewById(R.id.follow);
        f.setText(Html.fromHtml("follow"));
//        f.setMovementMethod(LinkMovementMethod.getInstance());
//        f.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.setData(Uri.parse("http://www.twitter.com/umanoapp"));
//                startActivity(i);
//            }
//        });
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
        mFirebase.child(BuildConfig.FIREBASE_ROOT_NODE).addChildEventListener(this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult: results from a Place Picker intent");
        // intent return from pick a place
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                Log.d("place name: ", place.getName().toString());
                Log.d("place address: ", place.getAddress().toString());
                Log.d("place latLng: ", place.getLatLng().toString());
                Log.d("place rating: ", String.valueOf(place.getRating()));
                Log.d("place website url: ", place.getWebsiteUri().toString());
                // creates a record in Firebase with a key matching the Place ID and with a value
                Map< String, Object > checkoutData = new HashMap<>();
                checkoutData.put("time", ServerValue.TIMESTAMP);
                mFirebase.child(BuildConfig.FIREBASE_ROOT_NODE).child(place.getId()).setValue(checkoutData);

            } else if (resultCode == PlacePicker.RESULT_ERROR) {
                Toast.makeText(this, "Places API failure! Check that the API is enabled for your key",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Map setup. This is called when the GoogleMap is available to manipulate.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "onMapReady");
        mMap = googleMap;
        if (mMap != null) {
            // Pad the map controls to make room for the button - note that the button may not have
            // been laid out yet.
            mCheckoutButton.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mMap.setPadding(0, mCheckoutButton.getHeight(), 0, 0);
                        }
                    }
            );

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
        addPointToViewPort(ll);
    }

    private void addPointToViewPort(LatLng newPoint) {
        Log.d(LOG_TAG, "addPointToViewPort");
        mBounds.include(newPoint);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mBounds.build(), mCheckoutButton.getHeight()));
        BitmapDescriptor defaultMarker =
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        Log.d(LOG_TAG, "add a market to a map");
        mMap.addMarker(new MarkerOptions()
                .position(newPoint)
                .icon(defaultMarker));
        Log.d(LOG_TAG, "map is ready and add marker click listener");
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                // Handle marker click here
                Log.d(LOG_TAG, "marker clicked: " + marker.getPosition());
//                LatLng points = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                if (mLayout != null) {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    TextView tvName = (TextView) findViewById(R.id.name);
                    tvName.setText(marker.getPosition().toString());
                } else {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                }
                return true;
            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d(LOG_TAG, "map onCameraChange: let's set panel state hidden");
                // when a user moves on, disable sliding panel so that a user can click a marker
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });
    }

    /**
     * Prompt the user to check out of their location. Called when the "Check Out!" button
     * is clicked.
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

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
        Log.d(LOG_TAG, "onChildAdded");
        String placeId = dataSnapshot.getKey();
        if (placeId != null) {
            Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId)
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                                           @Override
                                           public void onResult(PlaceBuffer places) {
                                               Log.d(LOG_TAG, "add a child to firebase and display it to a map with a marker");
                                               LatLng location = places.get(0).getLatLng();
                                               addPointToViewPort(location);
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
        //mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
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

}