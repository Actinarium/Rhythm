/*
 * Copyright (C) 2016 Actinarium
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

package com.actinarium.rhythm.layer;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import com.actinarium.rhythm.AbstractSpecLayerGroup;
import com.actinarium.rhythm.ArgumentsBundle;
import com.actinarium.rhythm.RhythmSpecLayerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A group that clips and/or insets its child layers. Allows setting left, right, top, and bottom insets, positive or
 * negative, as well as width and height, all in either absolute dimensions or as percent of parent. Setting width or
 * height takes precedence over setting insets in the following way: if width (height) and right (bottom) are set but
 * left (top) is not, the block floats to the right (bottom), otherwise it stays on the left (top).
 *
 * @author Paul Danyliuk
 */
public class Inset extends AbstractSpecLayerGroup<Inset> {

    /**
     * Inset the bounds and clip the overlay. Default behavior
     */
    public static final int MODE_DEFAULT = 0;
    /**
     * Inset the bounds but don't clip drawing to the overlay. Best used for margins
     */
    public static final int MODE_NO_CLIP = 1;
    /**
     * Clip the group according to inset rect but keep the coordinates. Best for clipping an absolutely positioned
     * overlay
     */
    public static final int MODE_CLIP_ONLY = 2;

    /**
     * Dimension specified in pixels
     */
    public static final boolean UNITS_PX = false;
    /**
     * Dimension specified in percent of container
     */
    public static final boolean UNITS_PERCENT = true;

    @Mode
    protected int mMode = MODE_DEFAULT;

    // Insets
    protected boolean mIsLeftPercent;
    protected boolean mIsTopPercent;
    protected boolean mIsRightPercent;
    protected boolean mIsBottomPercent;
    protected int mLeft;
    protected int mTop;
    protected int mRight;
    protected int mBottom;

    // Dimensions - override insets
    protected boolean mIsWidthPercent;
    protected boolean mIsHeightPercent;
    protected int mWidth;
    protected int mHeight;

    // Flags for set values - determine how insets are calculated
    protected boolean mIsLeftSet;
    protected boolean mIsRightSet;
    protected boolean mIsTopSet;
    protected boolean mIsBottomSet;
    protected boolean mIsWidthSet;
    protected boolean mIsHeightSet;

    // Reusable resulting rect
    protected Rect mInsetRect = new Rect();

    /**
     * Create a layer group that clips and/or insets its child layers
     */
    public Inset() {
        super();
    }

    /**
     * Create a layer group that clips and/or insets its child layers
     *
     * @param initialCapacity anticipated number of child layers
     */
    public Inset(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Set inset mode &mdash; whether the group should also clip the children, translate the coordinates, or both
     *
     * @param mode one of {@link #MODE_NO_CLIP}, {@link #MODE_CLIP_ONLY}, or {@link #MODE_DEFAULT}
     * @return this for chaining
     * @see #MODE_DEFAULT
     * @see #MODE_NO_CLIP
     * @see #MODE_CLIP_ONLY
     */
    public Inset setMode(@Mode int mode) {
        mMode = mode;
        return this;
    }

    /**
     * Set top inset
     *
     * @param value     pixels or percent
     * @param isPercent <code>true</code> if value is in percent, <code>false</code> if in pixels
     * @return this for chaining
     */
    public Inset setTop(int value, boolean isPercent) {
        mIsTopSet = true;
        mTop = value;
        mIsTopPercent = isPercent;
        return this;
    }

    /**
     * Set bottom inset
     *
     * @param value     pixels or percent
     * @param isPercent <code>true</code> if value is in percent, <code>false</code> if in pixels
     * @return this for chaining
     */
    public Inset setBottom(int value, boolean isPercent) {
        mIsBottomSet = true;
        mBottom = value;
        mIsBottomPercent = isPercent;
        return this;
    }

    /**
     * Set left inset
     *
     * @param value     pixels or percent
     * @param isPercent <code>true</code> if value is in percent, <code>false</code> if in pixels
     * @return this for chaining
     */
    public Inset setLeft(int value, boolean isPercent) {
        mIsLeftSet = true;
        mLeft = value;
        mIsLeftPercent = isPercent;
        return this;
    }

    /**
     * Set right inset
     *
     * @param value     pixels or percent
     * @param isPercent <code>true</code> if value is in percent, <code>false</code> if in pixels
     * @return this for chaining
     */
    public Inset setRight(int value, boolean isPercent) {
        mIsRightSet = true;
        mRight = value;
        mIsRightPercent = isPercent;
        return this;
    }

    /**
     * Set width. If both width, left inset, and right inset are set, right inset is ignored
     *
     * @param value     pixels or percent
     * @param isPercent <code>true</code> if value is in percent, <code>false</code> if in pixels
     * @return this for chaining
     */
    public Inset setWidth(int value, boolean isPercent) {
        mIsWidthSet = true;
        mWidth = value;
        mIsWidthPercent = isPercent;
        return this;
    }

    /**
     * Set height. If both height, top inset, and bottom inset are set, bottom inset is ignored
     *
     * @param value     pixels or percent
     * @param isPercent <code>true</code> if value is in percent, <code>false</code> if in pixels
     * @return this for chaining
     */
    public Inset setHeight(int value, boolean isPercent) {
        mIsHeightSet = true;
        mHeight = value;
        mIsHeightPercent = isPercent;
        return this;
    }

    @Override
    public void draw(Canvas canvas, Rect drawableBounds) {
        // Assume this is a) not called very often, and b) is a fast operation anyway
        recalculateInsetRect(drawableBounds);

        final int state = canvas.save();
        if (mMode != MODE_NO_CLIP) {
            canvas.clipRect(mInsetRect);
        }

        if (mMode == MODE_CLIP_ONLY) {
            // Draw sub-layers within original bounds
            super.draw(canvas, drawableBounds);
        } else {
            // Draw sub-layers within new bounds
            super.draw(canvas, mInsetRect);
        }

        canvas.restoreToCount(state);
    }

    /**
     * Update the inset bounds based on provided outer bounds and this layer's state
     *
     * @param outerBounds Outer bounds provided to this inset layer to modify
     */
    protected void recalculateInsetRect(Rect outerBounds) {
        final int parentWidth = outerBounds.width();
        final int parentHeight = outerBounds.height();

        if (!mIsWidthSet) {
            // No width - inset based on left and right. Assume those are set, otherwise those are 0 anyway
            mInsetRect.left = outerBounds.left + (mIsLeftPercent ? parentWidth * mLeft / 100 : mLeft);
            mInsetRect.right = outerBounds.right - (mIsRightPercent ? parentWidth * mRight / 100 : mRight);
        } else if (mIsRightSet && !mIsLeftSet) {
            // Width and right are set, left not set but calculated from width
            mInsetRect.right = outerBounds.right - (mIsRightPercent ? parentWidth * mRight / 100 : mRight);
            mInsetRect.left = mInsetRect.right - (mIsWidthPercent ? parentWidth * mWidth / 100 : mWidth);
        } else {
            // If right not set, or all three are set, right is ignored and calculated as left + width
            mInsetRect.left = outerBounds.left + (mIsLeftPercent ? parentWidth * mLeft / 100 : mLeft);
            mInsetRect.right = mInsetRect.left + (mIsWidthPercent ? parentWidth * mWidth / 100 : mWidth);
        }

        if (!mIsHeightSet) {
            // No height - inset based on top and bottom. Assume those are set, otherwise those are 0 anyway
            mInsetRect.top = outerBounds.top + (mIsTopPercent ? parentHeight * mTop / 100 : mTop);
            mInsetRect.bottom = outerBounds.bottom - (mIsBottomPercent ? parentHeight * mBottom / 100 : mBottom);
        } else if (mIsBottomSet && !mIsTopSet) {
            // Height and bottom are set, top not set but calculated from height
            mInsetRect.bottom = outerBounds.bottom - (mIsBottomPercent ? parentHeight * mBottom / 100 : mBottom);
            mInsetRect.top = mInsetRect.bottom - (mIsHeightPercent ? parentHeight * mHeight / 100 : mHeight);
        } else {
            // If bottom not set, or all three are set, bottom is ignored and calculated as top + height
            mInsetRect.top = outerBounds.top + (mIsTopPercent ? parentHeight * mTop / 100 : mTop);
            mInsetRect.bottom = mInsetRect.top + (mIsHeightPercent ? parentHeight * mHeight / 100 : mHeight);
        }
    }

    /**
     * A default factory that creates new {@link Inset} layers from config lines according to <a
     * href="https://github.com/Actinarium/Rhythm/wiki/Declarative-configuration#inset">the docs</a>
     */
    public static class Factory implements RhythmSpecLayerFactory<Inset> {

        public static final String LAYER_TYPE = "inset";
        public static final String ARG_NO_CLIP = "no-clip";
        public static final String ARG_CLIP_ONLY = "clip-only";
        public static final String ARG_TOP = "top";
        public static final String ARG_BOTTOM = "bottom";
        public static final String ARG_LEFT = "left";
        public static final String ARG_RIGHT = "right";
        public static final String ARG_WIDTH = "width";
        public static final String ARG_HEIGHT = "height";

        @Override
        public Inset getForArguments(ArgumentsBundle argsBundle) {
            Inset inset = new Inset();

            if (argsBundle.hasArgument(ARG_NO_CLIP)) {
                inset.mMode = MODE_NO_CLIP;
            } else if (argsBundle.hasArgument(ARG_CLIP_ONLY)) {
                inset.mMode = MODE_CLIP_ONLY;
            } else {
                inset.mMode = MODE_DEFAULT;
            }

            if (argsBundle.hasArgument(ARG_TOP)) {
                boolean isPercent = argsBundle.getDimensionUnits(ARG_TOP) == ArgumentsBundle.UNITS_PERCENT;
                int value = argsBundle.getDimensionPixelOffset(ARG_TOP, 0);
                inset.setTop(value, isPercent);
            }
            if (argsBundle.hasArgument(ARG_BOTTOM)) {
                boolean isPercent = argsBundle.getDimensionUnits(ARG_BOTTOM) == ArgumentsBundle.UNITS_PERCENT;
                int value = argsBundle.getDimensionPixelOffset(ARG_BOTTOM, 0);
                inset.setBottom(value, isPercent);
            }
            if (argsBundle.hasArgument(ARG_LEFT)) {
                boolean isPercent = argsBundle.getDimensionUnits(ARG_LEFT) == ArgumentsBundle.UNITS_PERCENT;
                int value = argsBundle.getDimensionPixelOffset(ARG_LEFT, 0);
                inset.setLeft(value, isPercent);
            }
            if (argsBundle.hasArgument(ARG_RIGHT)) {
                boolean isPercent = argsBundle.getDimensionUnits(ARG_RIGHT) == ArgumentsBundle.UNITS_PERCENT;
                int value = argsBundle.getDimensionPixelOffset(ARG_RIGHT, 0);
                inset.setRight(value, isPercent);
            }
            if (argsBundle.hasArgument(ARG_WIDTH)) {
                boolean isPercent = argsBundle.getDimensionUnits(ARG_WIDTH) == ArgumentsBundle.UNITS_PERCENT;
                int value = argsBundle.getDimensionPixelSize(ARG_WIDTH, 0);
                inset.setWidth(value, isPercent);
            }
            if (argsBundle.hasArgument(ARG_HEIGHT)) {
                boolean isPercent = argsBundle.getDimensionUnits(ARG_HEIGHT) == ArgumentsBundle.UNITS_PERCENT;
                int value = argsBundle.getDimensionPixelSize(ARG_HEIGHT, 0);
                inset.setHeight(value, isPercent);
            }

            return inset;
        }
    }

    /**
     * Type definition for inset group type
     */
    @Retention(RetentionPolicy.CLASS)
    @IntDef({MODE_DEFAULT, MODE_NO_CLIP, MODE_CLIP_ONLY})
    public @interface Mode {
    }
}
