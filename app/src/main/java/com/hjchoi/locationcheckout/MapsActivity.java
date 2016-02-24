package com.hjchoi.locationcheckout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ListView;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        ChildEventListener,
        LocationListener,
        SlidingUpPanelLayout.PanelSlideListener,
        HeaderAdapter.ItemClickListener {

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();

    @Bind(R.id.checkout_button) Button mCheckoutButton;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds.Builder mBounds = new LatLngBounds.Builder();
    private static final int REQUEST_PLACE_PICKER = 1;
    private Firebase mFirebase;

    // related to SlidingUpPanel
    @Bind(android.R.id.list) LockableRecyclerView mListView;
    @Bind(R.id.sliding_layout) SlidingUpPanelLayout mSlidingUpPanelLayout;
    @Bind(R.id.transparentView) View mTransparentView;
    private HeaderAdapter mHeaderAdapter;

//    @Bind(R.id.sliding_layout) SlidingUpPanelLayout mLayout;
//    @Bind(R.id.name) TextView mName;
//    @Bind(R.id.list) ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        setUpGoogleMap();
        setUpFireBase();
        setUpSlidingUpPanel();
//        setUpSlidingUpPanel2();
    }

//    private void setUpSlidingUpPanel2() {
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MapsActivity.this, "onItemClick", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        List<String> your_array_list = Arrays.asList(
//                "item 1"
//        );
//
//        // This is the array adapter, it takes the context of the activity as a
//        // first parameter, the type of list view as a second parameter and your
//        // array as a third parameter.
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
//                this,
//                android.R.layout.simple_list_item_1,
//                your_array_list );
//
//        mListView.setAdapter(arrayAdapter);
//
//        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
//        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
//            @Override
//            public void onPanelSlide(View panel, float slideOffset) {
//                Log.i(LOG_TAG, "onPanelSlide, offset " + slideOffset);
//            }
//
//            @Override
//            public void onPanelExpanded(View panel) {
//                Log.i(LOG_TAG, "onPanelExpanded");
//
//            }
//
//            @Override
//            public void onPanelCollapsed(View panel) {
//                Log.i(LOG_TAG, "onPanelCollapsed");
//
//            }
//
//            @Override
//            public void onPanelAnchored(View panel) {
//                Log.i(LOG_TAG, "onPanelAnchored");
//            }
//
//            @Override
//            public void onPanelHidden(View panel) {
//                Log.i(LOG_TAG, "onPanelHidden");
//            }
//        });
//        mLayout.setFadeOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
//            }
//        });
//
//        mName.setText("panel content");
////        t.setText(Html.fromHtml(getString(R.string.hello)));
////        Button f = (Button) findViewById(R.id.follow);
////        f.setText(Html.fromHtml(getString(R.string.follow)));
////        f.setMovementMethod(LinkMovementMethod.getInstance());
////        f.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                Intent i = new Intent(Intent.ACTION_VIEW);
////                i.setData(Uri.parse("http://www.twitter.com/umanoapp"));
////                startActivity(i);
////            }
////        });
//    }

    private void setUpGoogleMap() {
        Log.d(LOG_TAG, "setUpGoogleMap");
        // Set up Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    private void setUpSlidingUpPanel() {
        Log.d(LOG_TAG, "setUpSlidingUpPanel");
        // Set up slidingUpPanel
        mListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        mSlidingUpPanelLayout.setEnableDragViewTouchEvents(true);
        int mapHeight = getResources().getDimensionPixelSize(R.dimen.map_height);
        mSlidingUpPanelLayout.setPanelHeight(mapHeight); // you can use different height here
        mSlidingUpPanelLayout.setScrollableView(mListView, mapHeight);
        mSlidingUpPanelLayout.setPanelSlideListener(this);
//        collapseMap();
        expandMap();
        mSlidingUpPanelLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSlidingUpPanelLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mSlidingUpPanelLayout.onPanelDragged(0);
            }
        });

        ArrayList<String> testData = new ArrayList<>();
        testData.add("item 1");

        mHeaderAdapter = new HeaderAdapter(this, testData, this);
        mListView.setItemAnimator(null);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListView.setLayoutManager(layoutManager);
        mListView.setAdapter(mHeaderAdapter);
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
                return false;
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

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }

    @Override
    public void onItemClicked(int position) {
        Log.d(LOG_TAG, "item clicked: " + position);
        mSlidingUpPanelLayout.collapsePane();
    }

    private void collapseMap() {
        Log.d(LOG_TAG, "collapseMap");
        if (mHeaderAdapter != null) {
            Log.d(LOG_TAG, "header adapter is not null: " + mHeaderAdapter.getItemCount());
            mHeaderAdapter.showSpace();
        }
        mTransparentView.setVisibility(View.GONE);
        if (mMap != null) {
            Log.d(LOG_TAG, "mMap is not null at this moment");
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 11f), 1000, null);
        }
        //mListView.setScrollingEnabled(true);
    }

    private void expandMap() {
        Log.d(LOG_TAG, "expandMap");
        if (mHeaderAdapter != null) {
            Log.d(LOG_TAG, "header adapter is not null: " + mHeaderAdapter.getItemCount());
            mHeaderAdapter.hideSpace();
        }
        mTransparentView.setVisibility(View.INVISIBLE);
        if (mMap != null) {
            Log.d(LOG_TAG, "mMap is not null at this moment");
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(14f), 1000, null);
        }
        //mListView.setScrollingEnabled(false);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        Log.d(LOG_TAG, "onPanelSlide");
    }

    @Override
    public void onPanelCollapsed(View panel) {
        Log.d(LOG_TAG, "onPanelCollapsed - expandMap");
        expandMap();
    }

    @Override
    public void onPanelExpanded(View panel) {
        Log.d(LOG_TAG, "onPanelExpanded - collapseMap");
        collapseMap();
    }

    @Override
    public void onPanelAnchored(View panel) {
        Log.d(LOG_TAG, "onPanelAnchored");
    }
}