/*
 * Copyright (C) 2015 Actinarium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.actinarium.rhythm.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.actinarium.rhythm.R;
import com.actinarium.rhythm.RhythmControl;
import com.actinarium.rhythm.RhythmDrawable;
import com.actinarium.rhythm.RhythmGroup;
import com.actinarium.rhythm.RhythmOverlay;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@link FrameLayout} implementation with rich Rhythm support. You can use this layout to wrap existing views and
 * draw a Rhythm overlay from specified group. The overlay can be positioned either under the view, over the view, or
 * just under/over the content (see {@link #setOverlayPosition(int)}). Both the group and overlay position can be set in
 * the layout XML with attributes <code>app:rhythmGroup</code> and <code>app:overlayPosition</code> respectively.
 *
 * @author Paul Danyliuk
 * @version $Id$
 */
public class RhythmicFrameLayout extends FrameLayout {

    /**
     * Use this value to indicate that this view is not connected to any {@link RhythmGroup} and shouldn’t display any
     * overlay
     */
    public static final int NO_GROUP = -1;

    // The following are to control where the overlay will be drawn
    // todo: test how overlay position affects its properties on view's translation, rotation, scaling etc
    /**
     * Draw the overlay under the background of this view. Pretty useless if this view has opaque background.
     */
    public static final int OVERLAY_POSITION_UNDER_BACKGROUND = 0;
    /**
     * Draw the overlay over view’s background but under child views. Default choice: useful yet non-obtrusive.
     */
    public static final int OVERLAY_POSITION_UNDER_CONTENT = 1;
    /**
     * Draw the overlay over the view’s content (sans foreground). Use this mode if you have nested opaque views that
     * occlude the overlay, and there are elements within, which you still need to align.
     */
    public static final int OVERLAY_POSITION_OVER_CONTENT = 2;
    /**
     * Same as {@link #OVERLAY_POSITION_OVER_CONTENT}, but this also draws over any foreground (ripples, touch
     * highlights etc).
     */
    public static final int OVERLAY_POSITION_OVER_FOREGROUND = 3;

    /**
     * Index of the group this view should get its {@link RhythmDrawable} from, or {@link #NO_GROUP}.
     */
    protected int mRhythmGroupIndex;
    @OverlayPosition
    protected int mOverlayPosition;

    /**
     * Obtained from {@link RhythmGroup}, which then controls this drawable, telling it what {@link RhythmOverlay} to
     * draw. Or <code>null</code>.
     */
    protected RhythmDrawable mRhythmDrawable;
    /**
     * Overlay bounds, relative to this view (since canvas is already translated to the origin point of this view)
     */
    protected Rect mBounds = new Rect();

    // Constructors

    public RhythmicFrameLayout(Context context) {
        super(context);
        mRhythmGroupIndex = NO_GROUP;
        mOverlayPosition = OVERLAY_POSITION_UNDER_CONTENT;
    }

    public RhythmicFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFromAttrs(context, attrs, 0, 0);
    }

    public RhythmicFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initFromAttrs(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RhythmicFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttrs(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttrs(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray array = context
                .getTheme()
                .obtainStyledAttributes(attrs, R.styleable.RhythmicFrameLayout, defStyleAttr, defStyleRes);
        try {
            int position = array.getInteger(R.styleable.RhythmicFrameLayout_overlayPosition, OVERLAY_POSITION_UNDER_CONTENT);
            if (position == OVERLAY_POSITION_UNDER_BACKGROUND
                    || position == OVERLAY_POSITION_UNDER_CONTENT
                    || position == OVERLAY_POSITION_OVER_CONTENT
                    || position == OVERLAY_POSITION_OVER_FOREGROUND) {
                //noinspection ResourceType
                mOverlayPosition = position;
            } else {
                mOverlayPosition = OVERLAY_POSITION_UNDER_CONTENT;
            }

            mRhythmGroupIndex = array.getInteger(R.styleable.RhythmicFrameLayout_rhythmGroup, NO_GROUP);
        } finally {
            array.recycle();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (changed) {
            mBounds.set(0, 0, right - left, bottom - top);
        }

        if (mRhythmDrawable != null && changed) {
            // If there's a drawable, and layout has changed, we need to update its bounds
            mRhythmDrawable.setBounds(mBounds);
        } else if (mRhythmDrawable == null && mRhythmGroupIndex != NO_GROUP) {
            // If the group is set but there's no drawable yet, try to pull it from the group
            onRhythmGroupSet();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        // Draw before or after everything? (if there's anything to draw)
        if (mRhythmDrawable == null) {
            super.draw(canvas);
        } else if (mOverlayPosition == OVERLAY_POSITION_UNDER_BACKGROUND) {
            mRhythmDrawable.draw(canvas);
            super.draw(canvas);
        } else if (mOverlayPosition == OVERLAY_POSITION_OVER_FOREGROUND) {
            super.draw(canvas);
            mRhythmDrawable.draw(canvas);
        } else {
            super.draw(canvas);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw before content? (if there's anything to draw)
        if (mRhythmDrawable != null && mOverlayPosition == OVERLAY_POSITION_UNDER_CONTENT) {
            mRhythmDrawable.draw(canvas);
        }
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // Draw over content and children? (if there's anything to draw)
        if (mRhythmDrawable != null && mOverlayPosition == OVERLAY_POSITION_OVER_CONTENT) {
            mRhythmDrawable.draw(canvas);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return (mRhythmDrawable != null && mRhythmDrawable == who) || super.verifyDrawable(who);
    }

    // Getters/setters

    /**
     * @return Current Rhythm drawable
     */
    public RhythmDrawable getRhythmDrawable() {
        return mRhythmDrawable;
    }

    /**
     * Set a different {@link RhythmDrawable} to this view.
     *
     * @param drawable Rhythm drawable to set, or <code>null</code> to unlink this view from Rhythm
     */
    public void setRhythmDrawable(@Nullable RhythmDrawable drawable) {
        doSetRhythmDrawable(drawable);
        invalidate();
    }

    /**
     * @return Index of the Rhythm group this view is linked to, or {@link #NO_GROUP}
     */
    public int getRhythmGroupIndex() {
        return mRhythmGroupIndex;
    }

    /**
     * Link this drawable to a different {@link RhythmGroup} (identified by index), or to no group at all.
     *
     * @param rhythmGroupIndex Index of required {@link RhythmGroup} in {@link RhythmControl}, or {@link #NO_GROUP}
     */
    public void setRhythmGroupIndex(int rhythmGroupIndex) {
        mRhythmGroupIndex = rhythmGroupIndex;
        if (mRhythmGroupIndex != NO_GROUP) {
            onRhythmGroupSet();
        } else {
            doSetRhythmDrawable(null);
        }
        invalidate();
    }

    /**
     * @return Overlay position
     * @see #OVERLAY_POSITION_UNDER_BACKGROUND
     * @see #OVERLAY_POSITION_UNDER_CONTENT
     * @see #OVERLAY_POSITION_OVER_CONTENT
     * @see #OVERLAY_POSITION_OVER_FOREGROUND
     */
    public int getOverlayPosition() {
        return mOverlayPosition;
    }

    /**
     * Set overlay position
     *
     * @param overlayPosition New overlay position, one of overlay position constants
     * @see #OVERLAY_POSITION_UNDER_BACKGROUND
     * @see #OVERLAY_POSITION_UNDER_CONTENT
     * @see #OVERLAY_POSITION_OVER_CONTENT
     * @see #OVERLAY_POSITION_OVER_FOREGROUND
     */
    public void setOverlayPosition(int overlayPosition) {
        if (mOverlayPosition != overlayPosition) {
            mOverlayPosition = overlayPosition;
            invalidate();
        }
    }

    /**
     * Retrieves proper drawable from current group and links it to this view
     */
    private void onRhythmGroupSet() {
        // Request rhythm group from application context
        Context context = getContext().getApplicationContext();
        if (context instanceof RhythmControl.Host) {
            // This may fail with index out of bounds exception if incorrect group index is provided
            // But, IMHO, it is better to throw the exception than suppress it and leave the developer clueless
            final RhythmDrawable drawable = ((RhythmControl.Host) context).getRhythmControl()
                    .getGroup(mRhythmGroupIndex)
                    .makeDrawable();
            doSetRhythmDrawable(drawable);
        } else {
            // Uh-oh
            throw new ClassCastException(this + " cannot connect to RhythmControl. " +
                    "Check if your Application implements RhythmControl.Host");
        }
    }

    /**
     * Links new drawable to this view
     */
    private void doSetRhythmDrawable(@Nullable RhythmDrawable drawable) {
        if (mRhythmDrawable != null) {
            mRhythmDrawable.setCallback(null);
        }
        mRhythmDrawable = drawable;
        if (mRhythmDrawable != null) {
            mRhythmDrawable.setBounds(mBounds);
            mRhythmDrawable.setCallback(this);
        }
    }

    /**
     * Type def annotation for overlay position enum
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({OVERLAY_POSITION_UNDER_BACKGROUND, OVERLAY_POSITION_UNDER_CONTENT, OVERLAY_POSITION_OVER_CONTENT, OVERLAY_POSITION_OVER_FOREGROUND})
    public @interface OverlayPosition {
    }

}
