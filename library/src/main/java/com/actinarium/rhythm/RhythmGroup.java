package com.actinarium.rhythm;

import android.content.Context;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 *
 * An entity that controls all linked grid overlays &mdash; namely tells them what overlay configuration to draw.
 *
 * @author Paul Danyliuk
 */
public final class RhythmGroup {

    public static int OVERLAY_DISABLED = -1;
    private static final int ESTIMATED_DRAWABLES_PER_CONTROL = 4;
    private static final int ESTIMATED_MAX_CONFIGS_PER_CONTROL = 4;

    Context mContext;
    String mTitle;
    List<WeakReference<RhythmDrawable>> mDrawables;
    List<RhythmConfig> mRhythmConfigs;
    int mCurrentConfigIndex = OVERLAY_DISABLED;

    /**
     * Create a new Rhythm group
     *
     * @param title A convenient title for this group, used to identify it in the notification
     */
    RhythmGroup(@Nullable String title) {
        mTitle = title;
        mDrawables = new ArrayList<>(ESTIMATED_DRAWABLES_PER_CONTROL);
        mRhythmConfigs = new ArrayList<>(ESTIMATED_MAX_CONFIGS_PER_CONTROL);
    }

    /**
     * Add Rhythm config to this group
     *
     * @param config The Rhythm config to add
     * @return this for chaining
     */
    public RhythmGroup addConfig(RhythmConfig config) {
        mRhythmConfigs.add(config);
        if (mCurrentConfigIndex == OVERLAY_DISABLED) {
            selectConfig(mRhythmConfigs.size() - 1);
        }

        return this;
    }

    /**
     * Make a default Rhythm drawable registered in this control. This drawable then may be used as a background or view
     * overlay or such.
     *
     * @return A new {@link RhythmDrawable} attached to this control.
     */
    public RhythmDrawable makeDrawable() {
        RhythmDrawable drawable = new RhythmDrawable();
        drawable.setConfig(getCurrentConfig());
        mDrawables.add(new WeakReference<>(drawable));
        return drawable;
    }

    public List<RhythmConfig> getRhythmConfigs() {
        return mRhythmConfigs;
    }

    /**
     * @return Index of currently selected config, or {@link #OVERLAY_DISABLED}
     */
    public int getCurrentConfigIndex() {
        return mCurrentConfigIndex;
    }

    /**
     * @return Currently selected config, or null if overlay is disabled
     */
    public RhythmConfig getCurrentConfig() {
        return mCurrentConfigIndex != OVERLAY_DISABLED ? mRhythmConfigs.get(mCurrentConfigIndex) : null;
    }

    /**
     * Select config by index. Provide {@link #OVERLAY_DISABLED} to hide overlays.
     *
     * @param index Config index, or {@link #OVERLAY_DISABLED}
     */
    public void selectConfig(int index) {
        if (index == OVERLAY_DISABLED || (index >= 0 && index < mRhythmConfigs.size())) {
            mCurrentConfigIndex = index;
            updateConfig();
        } else {
            throw new IndexOutOfBoundsException("The index is neither OVERLAY_DISABLED nor valid.");
        }
    }

    /**
     * Convenience method to cycle through configs. Used by Quick Control notification, although can be invoked anywhere
     * in your code.
     */
    public void selectNextConfig() {
        if (mCurrentConfigIndex == OVERLAY_DISABLED) {
            mCurrentConfigIndex = 0;
        } else {
            mCurrentConfigIndex = ++mCurrentConfigIndex % mRhythmConfigs.size();
            // After the last config comes disabled overlay
            if (mCurrentConfigIndex == 0) {
                mCurrentConfigIndex = OVERLAY_DISABLED;
            }
        }
        updateConfig();
    }

    /**
     * Propagates current config to all linked {@link RhythmDrawable}s, removing dead references on the way
     */
    protected void updateConfig() {
        final RhythmConfig config = getCurrentConfig();
        Iterator<WeakReference<RhythmDrawable>> iterator = mDrawables.iterator();
        while (iterator.hasNext()) {
            final RhythmDrawable item = iterator.next().get();
            if (item == null) {
                // Clean up dead references
                iterator.remove();
            } else {
                item.setConfig(config);
            }
        }
    }

}
