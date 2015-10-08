package com.actinarium.rhythm;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A manager that controls all linked grid overlays &mdash; namely tells them what overlay configuration to draw.
 *
 * @author Paul Danyliuk
 */
public class RhythmManager {

    Context mContext;
    int mId;
    String mTitle;
    RhythmConfiguration mCurrentConfiguration;
    List<WeakReference<RhythmDrawable>> mDrawables;

    ArrayList<WeakReference<RhythmDrawable>> mRhythmDrawables = new ArrayList<>();

    /**
     * Create a new Rhythm manager
     * @param context Context for the manager to operate in
     * @param id      ID for this manager, must be unique across your application
     */
    public RhythmManager(Context context, int id) {
        mContext = context;
        mId = id;
        mDrawables = new ArrayList<>();

        mCurrentConfiguration = new RhythmConfiguration();
        mCurrentConfiguration.mIncrement = 3 * 4;
    }

    public RhythmManager displayNotification(int notificationId) {
        RhythmNotificationService.showNotification(mContext, notificationId, mId);
        return this;
    }

    public RhythmDrawable makeDrawable() {
        RhythmDrawable drawable = new RhythmDrawable();
        drawable.setConfiguration(mCurrentConfiguration);
        mDrawables.add(new WeakReference<>(drawable));
        return drawable;
    }

    void updateConfiguration() {
        mCurrentConfiguration.mIncrement += 3;
        Iterator<WeakReference<RhythmDrawable>> iterator = mDrawables.iterator();
        while (iterator.hasNext()) {
            final RhythmDrawable item = iterator.next().get();
            if (item == null) {
                // Clean up dead references
                iterator.remove();
            } else {
                item.setConfiguration(mCurrentConfiguration);
            }
        }
    }

}
