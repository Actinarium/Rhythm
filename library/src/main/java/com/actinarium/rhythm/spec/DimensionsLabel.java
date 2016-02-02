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
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.config.LayerConfig;
import com.actinarium.rhythm.config.SpecLayerFactory;

import java.text.DecimalFormat;

/**
 * A layer that draws a small box with dimensions of the current view. Inspect the dimensions of your views at glance.
 * noticing the issues asap. By default, the box is placed in the bottom right corner, but you can change its gravity
 * with {@link #setGravity(int)}.
 *
 * @author Paul Danyliuk
 */
public class DimensionsLabel implements RhythmSpecLayer {

    public static final int DEFAULT_BACKGROUND = 0x80000000;
    public static final int DEFAULT_TEXT_COLOR = 0xA0FFFFFF;
    public static final int DEFAULT_TEXT_SIZE = 12;              // dp

    // Pretty print chars
    public static final char ONE_HALF = '\u00bd';
    public static final char ONE_FOURTH = '\u00bc';
    public static final char THREE_FOURTHS = '\u00be';
    public static final char ONE_THIRD = '\u2153';
    public static final char TWO_THIRDS = '\u2154';
    public static final char MULTIPLY = '\u00d7';

    protected static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    protected float mScaleFactor;
    protected int mGravity = Gravity.BOTTOM | Gravity.RIGHT;
    protected Paint mBackgroundPaint;
    protected TextPaint mTextPaint;
    private Rect mTempRect;

    /**
     * Create a spec layer that displays dimensions label
     *
     * @param scaleFactor Scale factor to divide pixels by. Provide {@link DisplayMetrics#density} here to get your
     *                    dimensions displayed in dips, or set to 1f to get pixels.
     */
    public DimensionsLabel(float scaleFactor) {
        mScaleFactor = scaleFactor;

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(DEFAULT_BACKGROUND);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(DEFAULT_TEXT_COLOR);
        mTextPaint.setTextSize(DEFAULT_TEXT_SIZE * mScaleFactor);

        mTempRect = new Rect();
    }

    /**
     * Minimum constructor for the factory
     */
    private DimensionsLabel() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTempRect = new Rect();
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
     * Set the color of text box background
     *
     * @param color Text box background color, in #AARRGGBB format as usual
     * @return this for chaining
     */
    public DimensionsLabel setBackgroundColor(@ColorInt int color) {
        mBackgroundPaint.setColor(color);
        return this;
    }

    /**
     * Set the color of the text itself
     *
     * @param color Text color, in #AARRGGBB format as usual
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
    public DimensionsLabel setTextSize(float size) {
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
        Gravity.apply(mGravity, (int) (layout.getLineMax(0) + 0.5), layout.getHeight(), drawableBounds, mTempRect);

        // Draw background
        canvas.drawRect(mTempRect, mBackgroundPaint);

        // We have to translate the canvas ourselves, since layout can only draw itself at (0, 0)
        canvas.save();
        canvas.translate(mTempRect.left, mTempRect.top);
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
     * A factory that creates new DimensionsLabel layers from config lines like <code>dimensions-label gravity=top|left
     * text-color=black text-size=8sp</code>
     */
    public static class Factory implements SpecLayerFactory<DimensionsLabel> {

        public static final String LAYER_TYPE = "dimensions-label";

        @Override
        public DimensionsLabel getForConfig(LayerConfig config) {
            DimensionsLabel label = new DimensionsLabel();

            final float density = config.getDisplayMetrics().density;
            label.mScaleFactor = density;

            label.mGravity = config.getGravity("gravity", Gravity.BOTTOM | Gravity.RIGHT);
            label.mBackgroundPaint.setColor(config.getColor("background-color", DEFAULT_BACKGROUND));
            label.mTextPaint.setColor(config.getColor("text-color", DEFAULT_TEXT_COLOR));
            label.mTextPaint.setTextSize(config.getDimensionPixelExact("text-size", DEFAULT_TEXT_SIZE * density));

            return label;
        }
    }
}
