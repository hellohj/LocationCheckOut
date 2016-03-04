package com.hjchoi.locationcheckout.utils;

import android.content.res.Resources;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.hjchoi.locationcheckout.R;

/**
 * Created by hjchoi on 3/1/16.
 */
public class Util {

    private static final String LOG_TAG = Util.class.getSimpleName();

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
}
