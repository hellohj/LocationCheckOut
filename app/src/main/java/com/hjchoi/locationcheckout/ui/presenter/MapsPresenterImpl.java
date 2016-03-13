package com.hjchoi.locationcheckout.ui.presenter;

import com.hjchoi.locationcheckout.ui.view.MapsView;

public class MapsPresenterImpl implements MapsPresenter {

    private MapsView view;
    private GoogleMapsInteractor googleMapsInteractor;
    private FirebaseInteractor firebaseInteractor;

    public MapsPresenterImpl(MapsView view) {
        this.view = view;
        googleMapsInteractor = new GoogleMapsInteractorImpl();
        firebaseInteractor = new FirebaseInteractorImpl();
    }

    @Override
    public void onActivityCreated() {
        googleMapsInteractor.setUpGoogleMap();
        firebaseInteractor.setUpFirebase();
    }

    @Override
    public void onItemClicked(int position) {

    }

    @Override
    public void onDestroy() {
        view = null;
    }
}
