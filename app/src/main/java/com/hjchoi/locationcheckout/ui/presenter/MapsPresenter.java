package com.hjchoi.locationcheckout.ui.presenter;

public interface MapsPresenter {

    void onActivityCreated();

    void onItemClicked(int position);

    void onDestroy();
}