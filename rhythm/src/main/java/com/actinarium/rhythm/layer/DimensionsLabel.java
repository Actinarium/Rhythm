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

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.ArgumentsBundle;
import com.actinarium.rhythm.RhythmSpecLayerFactory;

import java.text.DecimalFormat;

/**
 * A layer that draws a small box with dimensions of the current view. Inspect the dimensions of your views at glance.
 * noticing the issues asap. By default, the box is placed in the bottom right corner, but you can change its gravity
 * with {@link #setGravity(int)}. <b>Experimental at the moment, meaning its behavior, appearance, and parameters may
 * change.</b>
 *
 * @author Paul Danyliuk
 */
public class DimensionsLabel implements RhythmSpecLayer {

    public static final int DEFAULT_BACKGROUND = 0x80000000;
    public static final int DEFAULT_TEXT_COLOR = 0xA0FFFFFF;
    public static final float DEFAULT_SCALE_FACTOR = 1f;
    public static final int DEFAULT_TEXT_SIZE = 12;              // px

    // Pretty print chars
    public static final char ONE_HALF = '\u00bd';
    public static final char ONE_FOURTH = '\u00bc';
    public static final char THREE_FOURTHS = '\u00be';
    public static final char ONE_THIRD = '\u2153';
    public static final char TWO_THIRDS = '\u2154';
    public static final char MULTIPLY = '\u00d7';

    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    @FloatRange(from = 0.0, fromInclusive = false)
    protected float mScaleFactor;
    @SuppressLint("RtlHardcoded")
    protected int mGravity = Gravity.BOTTOM | Gravity.RIGHT;
    protected Paint mBackgroundPaint;
    protected TextPaint mTextPaint;

    private Rect mTemp = new Rect();

    public DimensionsLabel() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        mScaleFactor = DEFAULT_SCALE_FACTOR;
        mBackgroundPaint.setColor(DEFAULT_BACKGROUND);
        mTextPaint.setTextSize(DEFAULT_TEXT_SIZE);
    }

    /**
     * Set a scale factor that will be applied to width and height of provided bounds
     *
     * @param scaleFactor Scale factor to divide pixels by. Provide {@link DisplayMetrics#density} here to display
     *                    dimensions as dips, {@link DisplayMetrics#scaledDensity} to display them as <code>sp</code>,
     *                    or {@link #DEFAULT_SCALE_FACTOR} (<code>1f</code>) to get pixels.
     * @return this for chaining
     */
    public DimensionsLabel setScaleFactor(@FloatRange(from = 0.0, fromInclusive = false) float scaleFactor) {
        mScaleFactor = scaleFactor;
        return this;
    }

    /**
     * Set label gravity. Default is bottom right.
     *
     * @param gravity Desired gravity. Can be combinations, e.g. <code>{@link Gravity#BOTTOM} | {@link
     *                Gravity#LEFT}</code>
     * @return this for chaining
     */
    public DimensionsLabel setGravity(int gravity) {
        mGravity = gravity;
        return this;
    }

    /**
     * Set label background color
     *
     * @param color Label background color, in #AARRGGBB format as usual
     * @return this for chaining
     */
    public DimensionsLabel setBackgroundColor(@ColorInt int color) {
        mBackgroundPaint.setColor(color);
        return this;
    }

    /**
     * Set the color of the label text itself
     *
     * @param color Label text color, in #AARRGGBB format as usual
     * @return this for chaining
     */
    public DimensionsLabel setTextColor(@ColorInt int color) {
        mTextPaint.setColor(color);
        return this;
    }

    /**
     * Set text size
     *
     * @param size Text size, in pixels
     * @return this for chaining
     */
    public DimensionsLabel setTextSize(@FloatRange(from = 0.0, fromInclusive = false) float size) {
        mTextPaint.setTextSize(size);
        return this;
    }

    @Override
    public void draw(Canvas canvas, Rect drawableBounds) {
        final int intWidth = drawableBounds.width();
        // Make the label text based on width, height, and scale factor
        String text = prettyPrintDips(intWidth, mScaleFactor) + ' ' + MULTIPLY + ' '
                + prettyPrintDips(drawableBounds.height(), mScaleFactor);

        // Use StaticLayout, which will calculate text dimensions nicely, then position the box using Gravity.apply()
        // (although that's one instantiation per draw call...)
        // This is what happens if you're obsessed with perfection like me
        StaticLayout layout = new StaticLayout(text, mTextPaint, intWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        Gravity.apply(mGravity, (int) (layout.getLineMax(0) + 0.5), layout.getHeight(), drawableBounds, mTemp);

        // Draw background
        canvas.drawRect(mTemp, mBackgroundPaint);

        // We have to translate the canvas ourselves, since layout can only draw itself at (0, 0)
        canvas.save();
        canvas.translate(mTemp.left, mTemp.top);
        layout.draw(canvas);
        canvas.restore();
    }

    /**
     * Sophisticated conversion of pixels to dips with the use of vulgar fractions (to save screen space)
     *
     * @param px          Pixels to convert to dips
     * @param scaleFactor Scale factor, should be equal to {@link DisplayMetrics#density} for px to dp conversion
     * @return String formatted with vulgar fraction if needed and possible
     */
    public static String prettyPrintDips(int px, float scaleFactor) {
        String dip;
        if (scaleFactor == 1f) {
            dip = String.valueOf(px);
        } else if (scaleFactor == 2f) {
            dip = String.valueOf(px / 2);
            if (px % 2 == 1) {
                dip += ONE_HALF;
            }
        } else if (scaleFactor == 3f) {
            dip = String.valueOf(px / 3);
            if (px % 3 == 1) {
                dip += ONE_THIRD;
            } else if (px % 3 == 2) {
                dip += TWO_THIRDS;
            }
        } else if (scaleFactor == 4f) {
            dip = String.valueOf(px / 4);
            if (px % 4 == 1) {
                dip += ONE_FOURTH;
            } else if (px % 4 == 2) {
                dip += ONE_HALF;
            } else if (px % 4 == 3) {
                dip += THREE_FOURTHS;
            }
        } else {
            // Very hard to determine exactly, so falling back to decimals
            dip = DECIMAL_FORMAT.format(px / scaleFactor);
        }
        return dip;
    }

    /**
     * A default factory that creates new {@link DimensionsLabel} layers from config lines according to <a
     * href="https://github.com/Actinarium/Rhythm/wiki/Declarative-configuration#dimensions-label">the docs</a>
     */
    public static class Factory implements RhythmSpecLayerFactory<DimensionsLabel> {

        public static final String LAYER_TYPE = "dimensions-label";
        public static final String ARG_GRAVITY = "gravity";
        public static final String ARG_COLOR = "color";
        public static final String ARG_TEXT_COLOR = "text-color";
        public static final String ARG_TEXT_SIZE = "text-size";

        @SuppressLint("RtlHardcoded")
        @Override
        public DimensionsLabel getForArguments(ArgumentsBundle argsBundle) {
            DimensionsLabel label = new DimensionsLabel();

            final float density = argsBundle.getDisplayMetrics().density;
            label.mScaleFactor = density;

            label.mGravity = argsBundle.getGravity(ARG_GRAVITY, Gravity.BOTTOM | Gravity.RIGHT);
            label.mBackgroundPaint.setColor(argsBundle.getColor(ARG_COLOR, DEFAULT_BACKGROUND));
            label.mTextPaint.setColor(argsBundle.getColor(ARG_TEXT_COLOR, DEFAULT_TEXT_COLOR));
            // todo: it shouldn't be the factory's concern to pre-multiply default text size by density - think of how to handle this gracefully
            label.mTextPaint.setTextSize(argsBundle.getDimensionPixelExact(ARG_TEXT_SIZE, DEFAULT_TEXT_SIZE * density));

            return label;
        }
    }
}
