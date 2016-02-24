package com.hjchoi.locationcheckout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LockableRecyclerView extends RecyclerView {

    private boolean mScrollable = true;

    public LockableRecyclerView(Context context) {
        super(context);
    }

    public LockableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if we can scroll pass the event to the superclass
                if (mScrollable) {
                    return super.onTouchEvent(ev);
                }
                // only continue to handle the touch event if scrolling enabled
                return mScrollable; // mScrollable is always false at this point
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        if (!mScrollable) {
            return false;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }
}
