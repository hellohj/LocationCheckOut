package com.hjchoi.locationcheckout.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.hjchoi.locationcheckout.R;
import com.hjchoi.locationcheckout.ui.fragment.BaseFragment;
import com.hjchoi.locationcheckout.ui.fragment.MapsFragment;

public class MapsActivity extends FragmentActivity {

    private static final String LOG_TAG = MapsActivity.class.getSimpleName();

    private BaseFragment _currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (savedInstanceState == null) {
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.fragment_maps, MapsFragment.newInstance());
            trans.commit();
        }
    }

    @Override
    public void onBackPressed()
    {
        // intercept back press from details panel
        _currentFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_maps);
        if ((_currentFragment != null) && (_currentFragment.onBackPressed())) {
            Log.d(LOG_TAG, "onBackPressed - from details to main map view");
            super.onBackPressed();
        } else {
            Log.d(LOG_TAG, "onBackPressed - something else");
        }
    }
}