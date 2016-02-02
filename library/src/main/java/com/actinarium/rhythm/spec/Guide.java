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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.config.LayerConfig;
import com.actinarium.rhythm.config.RhythmInflationException;
import com.actinarium.rhythm.config.SpecLayerFactory;

/**
 * A layer that draws a horizontal or vertical full-bleed guide at the specified distance from the specified edge of a
 * view. Can be used to draw &ldquo;thin&rdquo; keylines, as well as thick highlights (e.g. margins in avatar list
 * view), although it's recommended to use {@link Fill} inside a {@link InsetGroup} for the latter. The guide is drawn
 * towards the specified edge by default (i.e. touching aligned child views), but this can be tweaked using {@link
 * #setAlignOutside(boolean)} method.
 *
 * @author Paul Danyliuk
 */
public class Guide implements RhythmSpecLayer {

    public static final int DEFAULT_KEYLINE_COLOR = 0x60F50057;
    /**
     * Default guide thickness (3px)
     */
    public static final int DEFAULT_THICKNESS = 3;

    public static final boolean ALIGN_INSIDE = false;
    public static final boolean ALIGN_OUTSIDE = true;

    protected int mThickness = DEFAULT_THICKNESS;
    @LayerGravity
    protected int mGravity;
    protected int mDistance;
    protected boolean mAlignOutside;
    protected Paint mPaint;

    /**
     * Create a layer that draws a horizontal or vertical guide (keyline or highlight) at a specified distance from
     * required edge
     *
     * @param gravity  Defines the edge of the view this guide must be anchored to. Values ({@link Gravity#LEFT} and
     *                 {@link Gravity#RIGHT}) will result in a vertical guide, and values ({@link Gravity#TOP} and
     *                 {@link Gravity#BOTTOM}) will result in a horizontal guide.
     * @param distance Distance of this guide from the specified edge, in pixels.
     * @see #setAlignOutside(boolean)
     */
    public Guide(@LayerGravity int gravity, int distance) {
        mGravity = gravity;
        mDistance = distance;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(DEFAULT_KEYLINE_COLOR);
    }

    /**
     * Minimum constructor for the factory
     */
    private Guide() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Set grid line color
     *
     * @param color Grid line color, in #AARRGGBB format as usual
     * @return this for chaining
     */
    public Guide setColor(@ColorInt int color) {
        mPaint.setColor(color);
        return this;
    }

    /**
     * Set guide thickness
     *
     * @param thickness Guide thickness, in pixels. For keylines keep thickness around a few pixels, whereas for
     *                  highlights feel free to use as many dips as required.
     * @return this for chaining
     * @see #setAlignOutside(boolean)
     */
    public Guide setThickness(int thickness) {
        mThickness = thickness;
        return this;
    }

    /**
     * Set guide alignment. By default, the guide is drawn towards the specified edge, i.e. if the gravity is BOTTOM,
     * distance is 24px and thickness is 6px, the guide will appear as a horizontal rectangle starting at the 18th and
     * ending at the 23rd pixel row from the bottom. You can use this method to override that behavior and make the
     * guide face outwards (24th to 29th pixel rows in aforementioned example).
     *
     * @param alignOutside either <code>false</code> ({@link #ALIGN_INSIDE}, default) for the guide to extend towards
     *                     the edge defined by gravity, or <code>true</code> ({@link #ALIGN_OUTSIDE}) to extend away
     *                     from the edge
     * @return this for chaining
     */
    public Guide setAlignOutside(boolean alignOutside) {
        mAlignOutside = alignOutside;
        return this;
    }

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
     * A factory that creates new Guides from config lines like <code>guide gravity=left distance=16dp thickness=3px
     * outside</code>
     */
    public static class Factory implements SpecLayerFactory<Guide> {

        public static final String LAYER_TYPE = "guide";

        @Override
        public Guide getForConfig(LayerConfig config) {
            Guide guide = new Guide();

            guide.mGravity = config.getLayerGravity("gravity", Gravity.NO_GRAVITY);
            if (guide.mGravity == Gravity.NO_GRAVITY) {
                throw new RhythmInflationException("Error when inflating guide: 'gravity' argument missing or invalid");
            }

            if (!config.hasArgument("distance")) {
                throw new RhythmInflationException("Error when inflating guide: 'distance' argument is missing");
            }
            guide.mDistance = config.getDimensionPixelOffset("distance", 0);

            guide.mPaint.setColor(config.getColor("color", DEFAULT_KEYLINE_COLOR));
            guide.mThickness = config.getDimensionPixelSize("thickness", DEFAULT_THICKNESS);
            guide.mAlignOutside = config.getBoolean("outside", ALIGN_INSIDE, ALIGN_OUTSIDE);

            return guide;
        }
    }

}
