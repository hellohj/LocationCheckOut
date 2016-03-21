package com.hjchoi.locationcheckout.ui.presenter;

import android.content.Context;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.hjchoi.locationcheckout.BuildConfig;
import com.hjchoi.locationcheckout.model.MyPlace;

public class FirebaseInteractorImpl implements
        FirebaseInteractor {

    private static final String TAG = "FirebaseInteractorImpl";

    private Firebase mFirebase;

    @Override
    public void setUpFirebase(Context context) {
        Log.d(TAG, "setUpFirebase");
        // Set up Firebase
        Firebase.setAndroidContext(context);
        mFirebase = new Firebase(BuildConfig.FIREBASE_URL);
    }

    @Override
    public void addFirebasChildEventListener(ChildEventListener childEventListener) {
        // childEventListener can handle more sophisticated events handling than addValueListener
        mFirebase.child(BuildConfig.FIREBASE_ROOT_NODE).addChildEventListener(childEventListener);
    }

    @Override
    public void addFirebaseValueEventListener(ValueEventListener valueEventListener, String markerTitle) {
        mFirebase.child(BuildConfig.FIREBASE_ROOT_NODE).child(markerTitle).addValueEventListener(valueEventListener);
    }

    @Override
    public void setChildOnActivityResult(String placeId, MyPlace myPlace) {
        mFirebase.child(BuildConfig.FIREBASE_ROOT_NODE).child(placeId).setValue(myPlace);
    }

}
