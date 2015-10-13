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
import com.actinarium.rhythm.RhythmPattern;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p></p>
 *
 * @author Paul Danyliuk
 * @version $Id$
 */
public class RhythmicFrameLayout extends FrameLayout {

    /**
     * Use this value to indicate that this view is not connected to any {@link RhythmGroup} and shouldn’t display any
     * pattern
     */
    public static final int NO_GROUP = -1;

    // The following are to control where RhythmDrawable will be placed
    /**
     * Draw the pattern under the background of this view. Pretty useless if this view has opaque background.
     */
    public static final int PATTERN_POSITION_UNDER_BACKGROUND = 0;
    /**
     * Draw the pattern over view’s background but under child views. Default choice: useful yet non-obtrusive.
     */
    public static final int PATTERN_POSITION_UNDER_CONTENT = 1;
    /**
     * Draw the pattern over the view’s content and overlay (sans foreground). Use this mode if you have nested opaque
     * views that occlude the pattern, and there are elements within, which you still need to align.
     */
    public static final int PATTERN_POSITION_OVER_CONTENT = 2;
    /**
     * Same as {@link #PATTERN_POSITION_OVER_CONTENT}, but this also draws over any foreground (ripples, touch
     * highlights etc).
     */
    public static final int PATTERN_POSITION_OVER_FOREGROUND = 3;

    /**
     * Index of the group this view should get its {@link RhythmDrawable} from, or {@link #NO_GROUP}.
     */
    protected int mRhythmGroupIndex;
    @PatternPosition
    protected int mPatternPosition;

    /**
     * Obtained from {@link RhythmGroup}, which then controls this drawable, telling it what {@link RhythmPattern} to
     * draw. Or <code>null</code>.
     */
    protected RhythmDrawable mRhythmDrawable;
    /**
     * Pattern bounds, relative to this view (since canvas is already translated to the origin point of this view)
     */
    protected Rect mBounds = new Rect();

    // Constructors

    public RhythmicFrameLayout(Context context) {
        super(context);
        mRhythmGroupIndex = NO_GROUP;
        mPatternPosition = PATTERN_POSITION_UNDER_CONTENT;
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
            int position = array.getInteger(R.styleable.RhythmicFrameLayout_patternPosition, PATTERN_POSITION_UNDER_CONTENT);
            if (position == PATTERN_POSITION_UNDER_BACKGROUND
                    || position == PATTERN_POSITION_UNDER_CONTENT
                    || position == PATTERN_POSITION_OVER_CONTENT
                    || position == PATTERN_POSITION_OVER_FOREGROUND) {
                //noinspection ResourceType
                mPatternPosition = position;
            } else {
                mPatternPosition = PATTERN_POSITION_UNDER_CONTENT;
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
        } else if (mPatternPosition == PATTERN_POSITION_UNDER_BACKGROUND) {
            mRhythmDrawable.draw(canvas);
            super.draw(canvas);
        } else if (mPatternPosition == PATTERN_POSITION_OVER_FOREGROUND) {
            super.draw(canvas);
            mRhythmDrawable.draw(canvas);
        } else {
            super.draw(canvas);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw before content? (if there's anything to draw)
        if (mRhythmDrawable != null && mPatternPosition == PATTERN_POSITION_UNDER_CONTENT) {
            mRhythmDrawable.draw(canvas);
        }
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        // Draw over content and children? (if there's anything to draw)
        if (mRhythmDrawable != null && mPatternPosition == PATTERN_POSITION_OVER_CONTENT) {
            mRhythmDrawable.draw(canvas);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return (mRhythmDrawable != null && mRhythmDrawable == who) || super.verifyDrawable(who);
    }

    // Getters/setters

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
     * @return Pattern position
     * @see #PATTERN_POSITION_UNDER_BACKGROUND
     * @see #PATTERN_POSITION_UNDER_CONTENT
     * @see #PATTERN_POSITION_OVER_CONTENT
     * @see #PATTERN_POSITION_OVER_FOREGROUND
     */
    public int getPatternPosition() {
        return mPatternPosition;
    }

    /**
     * Set another pattern position
     *
     * @param patternPosition New pattern position, one of pattern constants
     * @see #PATTERN_POSITION_UNDER_BACKGROUND
     * @see #PATTERN_POSITION_UNDER_CONTENT
     * @see #PATTERN_POSITION_OVER_CONTENT
     * @see #PATTERN_POSITION_OVER_FOREGROUND
     */
    public void setPatternPosition(int patternPosition) {
        if (mPatternPosition != patternPosition) {
            mPatternPosition = patternPosition;
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
     * Type def annotation for pattern position enum
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PATTERN_POSITION_UNDER_BACKGROUND, PATTERN_POSITION_UNDER_CONTENT, PATTERN_POSITION_OVER_CONTENT, PATTERN_POSITION_OVER_FOREGROUND})
    public @interface PatternPosition {
    }

}
