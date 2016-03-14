package com.hjchoi.locationcheckout.utils;

import android.content.res.Resources;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.ServerValue;
import com.google.android.gms.location.places.Place;
import com.hjchoi.locationcheckout.R;
import com.hjchoi.locationcheckout.model.PlaceModel;

public class Utils {

    private static final String LOG_TAG = Utils.class.getSimpleName();

    /**
     * Helper method to format information about a place nicely.
     */
    public static Spanned formatPlaceDetails(Resources res, String name,
                                              String address, String phoneNumber, String websiteUri) {
        Log.e(LOG_TAG, res.getString(R.string.place_details, name, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, address, phoneNumber,
                websiteUri));

    }

    public static PlaceModel populatePlaceModel(Place place) {
        PlaceModel placeModel = new PlaceModel();
        placeModel.setPlaceId(place.getId());
        if (!TextUtils.isEmpty(place.getName())) {
            placeModel.setName(place.getName().toString());
        }
        placeModel.setLocation(place.getLatLng());
        if (!TextUtils.isEmpty(place.getAddress())) {
            placeModel.setAddress(place.getAddress().toString());
        }
        if ((place.getWebsiteUri() != null) && !TextUtils.isEmpty(place.getWebsiteUri().toString())) {
            placeModel.setWebsite(place.getWebsiteUri().toString());
        }
        placeModel.setRating(place.getRating());
        if (!TextUtils.isEmpty(place.getPhoneNumber())) {
            placeModel.setPhone(place.getPhoneNumber().toString());
        }
        placeModel.setTimestamp(ServerValue.TIMESTAMP);
        return placeModel;
    }

    public static void setDetailsOfPlace(String text, TextView tv, ImageView iv) {
        if (!TextUtils.isEmpty(text)) {
            tv.setText(text);
            tv.setVisibility(View.VISIBLE);
            iv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
            iv.setVisibility(View.GONE);
        }
    }
}
