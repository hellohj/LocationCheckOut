package com.hjchoi.locationcheckout.ui.view;

import android.graphics.Bitmap;
import android.view.View;

import com.hjchoi.locationcheckout.model.MyPlace;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public interface MapsView {
    /**
     * when a user clicks a floating action button to pin a place
     * This action will open Google Place intent
     * @param view
     */
    void checkOut(View view);

    /**
     * update a state of SlidingUpPanel
     * @param state
     */
    void setPanelState(SlidingUpPanelLayout.PanelState state);

    /**
     * Set up UI related listeners
     */
    void setUIListener();

    /**
     * Set up panel slide listener
     * @param panelSlideListener
     */
    void setAddPanelSlideListener(SlidingUpPanelLayout.PanelSlideListener panelSlideListener);


    void setFadeOnClickListener(View.OnClickListener onClickListener);
    /**
     * Set a photo to ImageView
     * @param bitmap
     */
    void setImageBitmap(Bitmap bitmap);

    /**
     * Display a photo of a place in details panel
     * @param placeId
     */
    void showPhotoOfPlace(String placeId);

    /**
     * Check whether or not Sliding Panel layout exists
     * @return
     */
    boolean existSlidingUpPanelLayout();

    /**
     * Display details of a place
     * @param model
     */
    void showDetailsOfPlace(MyPlace model);

}
