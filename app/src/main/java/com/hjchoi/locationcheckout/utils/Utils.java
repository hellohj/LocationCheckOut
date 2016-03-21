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
import com.hjchoi.locationcheckout.model.MyPlace;

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

    public static MyPlace populatePlaceModel(Place place) {
        MyPlace myPlace = new MyPlace();
        myPlace.setPlaceId(place.getId());
        if (!TextUtils.isEmpty(place.getName())) {
            myPlace.setName(place.getName().toString());
        }
        myPlace.setLocation(place.getLatLng());
        if (!TextUtils.isEmpty(place.getAddress())) {
            myPlace.setAddress(place.getAddress().toString());
        }
        if ((place.getWebsiteUri() != null) && !TextUtils.isEmpty(place.getWebsiteUri().toString())) {
            myPlace.setWebsite(place.getWebsiteUri().toString());
        }
        myPlace.setRating(place.getRating());
        if (!TextUtils.isEmpty(place.getPhoneNumber())) {
            myPlace.setPhone(place.getPhoneNumber().toString());
        }
        myPlace.setTimestamp(ServerValue.TIMESTAMP);
        return myPlace;
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
