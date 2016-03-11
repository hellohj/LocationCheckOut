package com.hjchoi.locationcheckout.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hjchoi.locationcheckout.R;

import butterknife.ButterKnife;

public class ImagePagerAdapter extends PagerAdapter {

    private static final String LOG_TAG = ImagePagerAdapter.class.getSimpleName();
    private final Context context;
    private final Bitmap[] photos;

    public ImagePagerAdapter(Context context, @NonNull Bitmap[] photos) {
        Log.d(LOG_TAG, "ImagePagerAdapter constructor");
        this.context = context;
        this.photos = photos;
    }

    @Override
    public int getCount() {
        return photos.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

//    @Override
//    public int getItemPosition(Object object){
//        return POSITION_NONE;
//    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.d(LOG_TAG, "instantiateItem populate: position: photos length: " + position + " - " + photos.length);
//        final ImageLoader imageLoader = ImageLoader.getInstance();
        View view = LayoutInflater.from(context).inflate(R.layout.item_pager_image, container,
                false);

        container.addView(view);
        Log.d(LOG_TAG, "display photos here");
        ImageView imageView = ButterKnife.findById(view, R.id.photo);
        imageView.setImageBitmap(photos[position]);
//        imageLoader.displayImage(eachPhoto, imageView);

//        TextView slideNumberTextView = ButterKnife.findById(view, R.id.slide_number);
//        String slideNumber = (position + 1) + "/" + photos.length;
//        Log.d(LOG_TAG, "slideNumber: " + slideNumber);
//        slideNumberTextView.setText(slideNumber);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}