package com.hjchoi.locationcheckout.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.SupportMapFragment;
import com.hjchoi.locationcheckout.R;
import com.hjchoi.locationcheckout.model.PlaceModel;
import com.hjchoi.locationcheckout.ui.presenter.MapsPresenter;
import com.hjchoi.locationcheckout.ui.presenter.MapsPresenterImpl;
import com.hjchoi.locationcheckout.ui.view.MapsView;
import com.hjchoi.locationcheckout.utils.Utils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.Bind;
import butterknife.OnClick;

public class MapsFragment extends BaseFragment implements
        MapsView {

    private static final String LOG_TAG = MapsFragment.class.getSimpleName();

    @Bind(R.id.fab_checkOut)
    FloatingActionButton mFabCheckOut;
    @Bind(R.id.sliding_layout)
    SlidingUpPanelLayout mLayout;
    @Bind(R.id.tvName)
    TextView mLocationName;
    @Bind(R.id.map_cover)
    View mMapCover;
    @Bind(R.id.ivPlacePhoto)
    ImageView mPlacePhoto;
    @Bind(R.id.ivPlaceAddress) ImageView mIvPlaceAddress;
    @Bind(R.id.ivPlacePhone) ImageView mIvPlacePhone;
    @Bind(R.id.ivPlaceWebsite) ImageView mIvPlaceWebsite;
    @Bind(R.id.tvPlaceAddress) TextView mTvPlaceAddress;
    @Bind(R.id.tvPlacePhone) TextView mTvPlacePhone;
    @Bind(R.id.tvPlaceWebsite) TextView mTvPlaceWebsite;
    
    private static final int REQUEST_PLACE_PICKER = 1;

    private MapsPresenter mPresenter;

    public MapsFragment() {
    }

    public static MapsFragment newInstance() {
        MapsFragment f = new MapsFragment();
        return f;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPresenter = new MapsPresenterImpl(this);
        mPresenter.setGoogleMap(getContext(), getGoogleMapFragment());
        mPresenter.setFirebase(getContext());
        mPresenter.setSlidingUpPanel(getContext());
        mPresenter.setUI();
    }

    @Override
    protected int getFragmentLayout() {
        return R.layout.fragment_maps;
    }

    @Override
    public boolean onBackPressed() {
        if ((mLayout != null) && (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)) {
            Log.d(LOG_TAG, "onBackPressed here");
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return false;
        }
        Log.d(LOG_TAG, "onBackPressed before returning");
        return true;
    }

    public void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();
        mPresenter.connectGoogleApiClient(true);
    }

    public void onStop() {
        Log.d(LOG_TAG, "onStop");
        mPresenter.connectGoogleApiClient(false);
        super.onStop();
    }

    /**
     * Prompt the user to check out of their location. Called when floating action button clicked
     *
     * @param view
     */
    @OnClick (R.id.fab_checkOut)
    @Override
    public void checkOut(View view) {
        try {
            Log.d(LOG_TAG, "checkout button clicked");
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(getActivity());
            startActivityForResult(intent, REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesRepairableException e) {
            GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), e.getConnectionStatusCode(),
                    REQUEST_PLACE_PICKER);
        } catch (GooglePlayServicesNotAvailableException e) {
            Toast.makeText(getActivity(), "Please install Google Play Services!", Toast.LENGTH_LONG).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult: results from a Place Picker intent");
        if (requestCode == REQUEST_PLACE_PICKER) {
            mPresenter.onActivityResult(resultCode, data, getActivity());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //////////////////////////////
    // MVP - methods from View Interface --- testing now
    //////////////////////////////
    @Override
    public void setAddPanelSlideListener(SlidingUpPanelLayout.PanelSlideListener panelSlideListener) {
        if (mLayout != null) {
            mLayout.addPanelSlideListener(panelSlideListener);
        }
    }

    @Override
    public void setFadeOnClickListener(View.OnClickListener onClickListener) {
        if (mLayout != null) {
            mLayout.setFadeOnClickListener(onClickListener);
        }
    }

    @Override
    public void setPanelState(SlidingUpPanelLayout.PanelState state) {
        if (mLayout != null) {
            mLayout.setPanelState(state);
        }
    }

    @Override
    public void setUIListener() {
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
                Snackbar.make(view, "Let's pin a place!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                checkOut(view);
            }
        });
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mPlacePhoto.setImageBitmap(bitmap);
        }
    }

    @Override
    public boolean existSlidingUpPanelLayout() {
        if (mLayout != null) {
            return true;
        }
        return false;
    }

    @Override
    public void showDetailsOfPlace(PlaceModel model) {
        mLocationName.setText(model.getName());
        Utils.setDetailsOfPlace(model.getAddress(), mTvPlaceAddress, mIvPlaceAddress);
        Utils.setDetailsOfPlace(model.getPhone(), mTvPlacePhone, mIvPlacePhone);
        Utils.setDetailsOfPlace(model.getWebsite(), mTvPlaceWebsite, mIvPlaceWebsite);
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
            setImageBitmap(placePhotoResult.getBitmap());
        }
    };

    /**
     * Load a bitmap from the photos API asynchronously
     * by using buffers and result callbacks.
     */
    @Override
    public void showPhotoOfPlace(String placeId) {
        Places.GeoDataApi.getPlacePhotos(mPresenter.getGoogleApiClient(), placeId)
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
                            Log.d(LOG_TAG, "photo get success - more than one: " + photoMetadataBuffer.getCount());
                            // Display the first bitmap in an ImageView in the size of the view
                            photoMetadataBuffer.get(0)
                                    .getScaledPhoto(mPresenter.getGoogleApiClient(), mPlacePhoto.getWidth(),
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

    private SupportMapFragment getGoogleMapFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        return mapFragment;
    }
}
