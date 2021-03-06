package com.hjchoi.locationcheckout.ui.presenter;

import android.content.Context;

import com.firebase.client.ChildEventListener;
import com.firebase.client.ValueEventListener;
import com.hjchoi.locationcheckout.model.MyPlace;

public interface FirebaseInteractor {
    /**
     * Set up firebase essentials
     * @param context
     */
    void setUpFirebase(Context context);

    /**
     * Add a new place to firebase
     * @param placeId
     * @param myPlace
     */
    void setChildOnActivityResult(String placeId, MyPlace myPlace);

    /**
     * Add a listener for root note data changes
     * @param childEventListener
     */
    void addFirebasChildEventListener(ChildEventListener childEventListener);

    /**
     * Add a listener for child nodes on a marker click
     * @param valueEventListener
     * @param markerTitle
     */
    void addFirebaseValueEventListener(ValueEventListener valueEventListener, String markerTitle);
}
