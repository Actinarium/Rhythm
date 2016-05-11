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

package com.actinarium.rhythm.spec;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.common.RhythmInflationException;
import com.actinarium.rhythm.ArgumentsBundle;
import com.actinarium.rhythm.config.RhythmSpecLayerFactory;

/**
 * A layer that draws a horizontal or vertical full-bleed keyline at the specified distance from the specified edge of a
 * view. Can be used to draw &ldquo;thin&rdquo; keylines, as well as thick highlights (e.g. margins in avatar list
 * view), although it's recommended to use {@link Fill} inside a {@link InsetGroup} for the latter. The keyline is drawn
 * towards the specified edge by default (i.e. touching aligned child views), but this can be tweaked using {@link
 * #setAlignOutside(boolean)} method.
 *
 * @author Paul Danyliuk
 */
public class Keyline implements RhythmSpecLayer {

    public static final int DEFAULT_KEYLINE_COLOR = 0x60F50057;
    /**
     * Default keyline thickness (2px)
     */
    public static final int DEFAULT_THICKNESS = 2;

    public static final boolean ALIGN_INSIDE = false;
    public static final boolean ALIGN_OUTSIDE = true;

    protected int mThickness = DEFAULT_THICKNESS;
    @ArgumentsBundle.EdgeAffinity
    protected int mGravity;
    protected int mDistance;
    protected boolean mAlignOutside;
    protected Paint mPaint;

    /**
     * Create a layer that draws a horizontal or vertical keyline at a specified distance from
     * required edge
     *
     * @param gravity  Defines the edge of the view this keyline must be anchored to. Values ({@link Gravity#LEFT} and
     *                 {@link Gravity#RIGHT}) will result in a vertical keyline, and values ({@link Gravity#TOP} and
     *                 {@link Gravity#BOTTOM}) will result in a horizontal keyline.
     * @param distance Distance of this keyline from the specified edge, in pixels.
     * @see #setAlignOutside(boolean)
     */
    public Keyline(@ArgumentsBundle.EdgeAffinity int gravity, int distance) {
        mGravity = gravity;
        mDistance = distance;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(DEFAULT_KEYLINE_COLOR);
    }

    /**
     * Minimum constructor for the factory
     */
    protected Keyline() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Set grid line color
     *
     * @param color Grid line color, in #AARRGGBB format as usual
     * @return this for chaining
     */
    public Keyline setColor(@ColorInt int color) {
        mPaint.setColor(color);
        return this;
    }

    /**
     * Set keyline thickness
     *
     * @param thickness Keyline thickness, in pixels. For keylines keep thickness around a few pixels, whereas for
     *                  highlights feel free to use as many dips as required.
     * @return this for chaining
     * @see #setAlignOutside(boolean)
     */
    public Keyline setThickness(int thickness) {
        mThickness = thickness;
        return this;
    }

    /**
     * Set keyline alignment. By default, the keyline is drawn towards the specified edge, i.e. if the gravity is BOTTOM,
     * distance is 24px and thickness is 6px, the keyline will appear as a horizontal rectangle starting at the 18th and
     * ending at the 23rd pixel row from the bottom. You can use this method to override that behavior and make the
     * keyline face outwards (24th to 29th pixel rows in aforementioned example).
     *
     * @param alignOutside either <code>false</code> ({@link #ALIGN_INSIDE}, default) for the keyline to extend towards
     *                     the edge defined by gravity, or <code>true</code> ({@link #ALIGN_OUTSIDE}) to extend away
     *                     from the edge
     * @return this for chaining
     */
    public Keyline setAlignOutside(boolean alignOutside) {
        mAlignOutside = alignOutside;
        return this;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void draw(Canvas canvas, Rect drawableBounds) {
        if (mGravity == Gravity.LEFT) {
            // Vertical line at offset points from the left
            final int rightX = drawableBounds.left + mDistance + (mAlignOutside ? mThickness : 0);
            canvas.drawRect(rightX - mThickness, drawableBounds.top, rightX, drawableBounds.bottom, mPaint);
        } else if (mGravity == Gravity.RIGHT) {
            // Vertical line at offset points from the right
            final int leftX = drawableBounds.right - mDistance - (mAlignOutside ? mThickness : 0);
            canvas.drawRect(leftX, drawableBounds.top, leftX + mThickness, drawableBounds.bottom, mPaint);
        } else if (mGravity == Gravity.TOP) {
            // Horizontal line at offset points from the top
            final int bottomY = drawableBounds.top + mDistance + (mAlignOutside ? mThickness : 0);
            canvas.drawRect(drawableBounds.left, bottomY - mThickness, drawableBounds.right, bottomY, mPaint);
        } else if (mGravity == Gravity.BOTTOM) {
            // Horizontal line at offset points from the top
            final int topY = drawableBounds.bottom - mDistance - (mAlignOutside ? mThickness : 0);
            canvas.drawRect(drawableBounds.left, topY, drawableBounds.right, topY + mThickness, mPaint);
        }
    }

    /**
     * A factory that creates new Keylines from config lines like <code>keyline gravity=left distance=16dp thickness=3px
     * outside</code>
     */
    public static class Factory implements RhythmSpecLayerFactory<Keyline> {

        public static final String LAYER_TYPE = "keyline";

        @Override
        public Keyline getForConfig(ArgumentsBundle argsBundle) {
            Keyline keyline = new Keyline();

            keyline.mGravity = argsBundle.getEdgeAffinity("from", Gravity.NO_GRAVITY);
            if (keyline.mGravity == Gravity.NO_GRAVITY) {
                throw new RhythmInflationException(
                        RhythmInflationException.ERROR_ARGUMENT_MISSING_OR_NOT_ONE_OF,
                        "Error in keyline config: 'from' argument is mandatory and must be either 'left', 'right', 'top', 'bottom'",
                        LAYER_TYPE, "from", argsBundle.getString("from"), "left|right|top|bottom"
                );
            }

            if (!argsBundle.hasArgument("distance")) {
                throw new RhythmInflationException(
                        RhythmInflationException.ERROR_ARGUMENT_MISSING_OR_NOT_EXPECTED_TYPE,
                        "Error in keyline config: 'distance' argument is mandatory and must be a dimension value (e.g. 'distance=16dp')",
                        LAYER_TYPE, "step", "{dimen}" , "distance=16dp"
                );
            }
            keyline.mDistance = argsBundle.getDimensionPixelOffset("distance", 0);

            keyline.mPaint.setColor(argsBundle.getColor("color", DEFAULT_KEYLINE_COLOR));
            keyline.mThickness = argsBundle.getDimensionPixelSize("thickness", DEFAULT_THICKNESS);
            keyline.mAlignOutside = argsBundle.getBoolean("outside", ALIGN_INSIDE, ALIGN_OUTSIDE);

            return keyline;
        }
    }

}
