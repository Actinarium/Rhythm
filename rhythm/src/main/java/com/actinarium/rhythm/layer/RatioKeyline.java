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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import com.actinarium.rhythm.ArgumentsBundle;
import com.actinarium.rhythm.RhythmInflationException;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.RhythmSpecLayerFactory;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A single horizontal keyline whose distance from the top is calculated from the width of current bounds and given
 * aspect ratio. Displays the label in the bottom right corner of the enclosed rectangle. <b>Experimental at the
 * moment, meaning its behavior, appearance, and parameters may change.</b> As of now, the keyline label is always 12dp
 * high, 24dp wide, has 10dp font that shrinks if not fitting, and is always within the enclosed rectangle, i.e.
 * overdrawing its bottom edge.
 *
 * @author Paul Danyliuk
 */
public class RatioKeyline implements RhythmSpecLayer {

    public static final int DEFAULT_FILL_COLOR = 0xB03F51B5;
    public static final int DEFAULT_TEXT_COLOR = 0xC0FFFFFF;

    public static final int DEFAULT_THICKNESS = 2;         // px

    // todo: all defaults must be in px, not depending on density
    public static final int DEFAULT_TEXT_SIZE = 10;        // dp
    protected static final int DEFAULT_LABEL_HEIGHT = 12;  // dp

    @IntRange(from = 0)
    protected int mRatioX;
    @IntRange(from = 0)
    protected int mRatioY;
    @IntRange(from = 1)
    protected int mThickness;

    protected String mText;
    protected Paint mBackgroundPaint;
    protected TextPaint mTextPaint;
    protected Rect mTempRect;
    protected Path mLabelPath;

    // Text adjustment
    protected int mLabelRectWidth;
    protected int mLabelHeight;
    protected int mLabelSideWidth;

    public RatioKeyline(@IntRange(from = 0) int ratioX, @IntRange(from = 0) int ratioY, DisplayMetrics metrics) {
        this(metrics);
        mRatioX = ratioX;
        mRatioY = ratioY;
        mBackgroundPaint.setColor(DEFAULT_FILL_COLOR);
        mTextPaint.setColor(DEFAULT_TEXT_COLOR);
    }

    protected RatioKeyline(DisplayMetrics metrics) {
        mTempRect = new Rect();
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mLabelPath = new Path();

        // Hard-coded defaults
        mTextPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TEXT_SIZE, metrics));

        // Make default keyline path
        mLabelHeight = (int) ((DEFAULT_LABEL_HEIGHT) * metrics.density);
        mLabelRectWidth = mLabelHeight * 2;
        mLabelSideWidth = mLabelHeight * 3 / 4;

        mLabelPath.rLineTo(0, -mLabelHeight);
        mLabelPath.rLineTo(-mLabelRectWidth, 0);
        mLabelPath.rLineTo(-mLabelSideWidth, mLabelHeight);
        mLabelPath.close();
    }

    /**
     * Set ratio of the box this keyline should define, in form of two terms
     *
     * @param ratioX antecedent, horizontal component of the ratio (e.g. 16 in 16:9)
     * @param ratioY consequent, vertical component of the ratio (e.g. 9 in 16:9)
     * @return this for chaining
     */
    public RatioKeyline setRatio(@IntRange(from = 0) int ratioX, @IntRange(from = 0) int ratioY) {
        mRatioX = ratioX;
        mRatioY = ratioY;
        return this;
    }

    /**
     * Set arbitrary label text to this ratio keyline. Defaults to displaying ratio.
     *
     * @param text Text to display in keyline label. Set null to reset the label to display ratio.
     * @return this for chaining
     */
    public RatioKeyline setText(@Nullable String text) {
        mText = text;
        return this;
    }

    /**
     * Set ratio keyline thickness
     *
     * @param thickness Ratio keyline thickness, in pixels. At the moment, ratio keyline will be drawn within enclosed
     *                  bounds, so that regardless of thickness it doesn't cover pixels not within ratio box.
     * @return this for chaining
     */
    public RatioKeyline setThickness(@IntRange(from = 1) int thickness) {
        mThickness = thickness;
        return this;
    }

    /**
     * Set the color of ratio keyline and label background
     *
     * @param color Ratio keyline color, in #AARRGGBB format as usual
     * @return this for chaining
     */
    public RatioKeyline setKeylineColor(@ColorInt int color) {
        mBackgroundPaint.setColor(color);
        return this;
    }

    /**
     * Set ratio keyline label text color
     *
     * @param color Label text color, in #AARRGGBB format as usual
     * @return this for chaining
     */
    public RatioKeyline setTextColor(@ColorInt int color) {
        mTextPaint.setColor(color);
        return this;
    }

    @Override
    public void draw(Canvas canvas, Rect drawableBounds) {
        final int distanceTop;
        if (mRatioX == 0) {
            distanceTop = drawableBounds.top;
        } else {
            distanceTop = drawableBounds.top + drawableBounds.width() * mRatioY / mRatioX;
        }
        if (distanceTop > drawableBounds.height()) {
            return;
        }

        // Draw keyline
        canvas.drawRect(drawableBounds.left, distanceTop - mThickness, drawableBounds.right, distanceTop, mBackgroundPaint);

        // If no special text is set, display ratio
        if (mText == null) {
            mText = String.format(Locale.getDefault(), "%d:%d", mRatioX, mRatioY);
        }

        // Determine keyline label size/bounds
        StaticLayout layout = new StaticLayout(mText, mTextPaint, Integer.MAX_VALUE, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        int labelTextWidth = (int) (layout.getLineMax(0) + 0.5);

        // If text too big, re-measure
        while (labelTextWidth > mLabelRectWidth) {
            mTextPaint.setTextSize(mTextPaint.getTextSize() * 0.8f);
            layout = new StaticLayout(mText, mTextPaint, Integer.MAX_VALUE, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
            labelTextWidth = (int) (layout.getLineMax(0) + 0.5);
        }

        // Draw label
        canvas.save();
        canvas.clipRect(drawableBounds.left, 0, drawableBounds.right, distanceTop - mThickness);
        canvas.translate(drawableBounds.right, distanceTop);
        canvas.drawPath(mLabelPath, mBackgroundPaint);
        canvas.restore();

        // Determine text position (on the baseline, in the center of
        canvas.save();
        mTempRect.set(-mLabelRectWidth, -mLabelHeight, 0, 0);
        Gravity.apply(Gravity.CENTER, labelTextWidth, layout.getHeight(), mTempRect, mTempRect);
        canvas.translate(drawableBounds.right + mTempRect.left, distanceTop + mTempRect.top);
        layout.draw(canvas);
        canvas.restore();
    }

    /**
     * A default factory that creates new {@link RatioKeyline} layers from config lines according to <a
     * href="https://github.com/Actinarium/Rhythm/wiki/Declarative-configuration#ratio-keyline">the docs</a>
     */
    public static class Factory implements RhythmSpecLayerFactory<RatioKeyline> {

        public static final String LAYER_TYPE = "ratio-keyline";
        public static final String ARG_RATIO = "ratio";
        public static final String ARG_TEXT = "text";
        public static final String ARG_THICKNESS = "thickness";
        public static final String ARG_COLOR = "color";
        public static final String ARG_TEXT_COLOR = "text-color";

        private static Pattern RATIO_VALUE_PATTERN = Pattern.compile("(\\d+):(\\d+)");

        @Override
        public RatioKeyline getForArguments(ArgumentsBundle argsBundle) {
            RatioKeyline keyline = new RatioKeyline(argsBundle.getDisplayMetrics());

            String ratio = argsBundle.getString(ARG_RATIO);
            if (ratio == null) {
                throw new RhythmInflationException(
                        "Error when inflating ratio-keyline: 'ratio' argument is missing"
                );
            }
            Matcher matcher = RATIO_VALUE_PATTERN.matcher(ratio);
            if (!matcher.matches()) {
                throw new RhythmInflationException(
                        "Error when inflating ratio-keyline: 'ratio' argument is invalid, expected \"x:y\" pattern"
                );
            }
            keyline.mRatioX = Integer.parseInt(matcher.group(1));
            keyline.mRatioY = Integer.parseInt(matcher.group(2));
            keyline.mText = argsBundle.getString(ARG_TEXT, ratio);

            keyline.mThickness = argsBundle.getDimensionPixelSize(ARG_THICKNESS, DEFAULT_THICKNESS);
            keyline.mBackgroundPaint.setColor(argsBundle.getColor(ARG_COLOR, DEFAULT_FILL_COLOR));
            keyline.mTextPaint.setColor(argsBundle.getColor(ARG_TEXT_COLOR, DEFAULT_TEXT_COLOR));

            return keyline;
        }
    }

}