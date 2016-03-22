package com.hjchoi.locationcheckout.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.hjchoi.locationcheckout.R;
import com.hjchoi.locationcheckout.ui.fragment.BaseFragment;
import com.hjchoi.locationcheckout.ui.fragment.MapsFragment;

public class MapsActivity extends FragmentActivity {

    private static final String TAG = "MapsActivity";

    private BaseFragment mCurrentFragment;

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
        mCurrentFragment = (MapsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_maps);
        if ((mCurrentFragment != null) && (mCurrentFragment.onBackPressed())) {
            Log.d(TAG, "onBackPressed - from details to main map view");
            super.onBackPressed();
        } else {
            Log.d(TAG, "onBackPressed - something else");
        }
    }
}