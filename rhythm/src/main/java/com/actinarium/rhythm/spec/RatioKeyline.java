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
import android.graphics.Path;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.config.LayerConfig;
import com.actinarium.rhythm.config.RhythmInflationException;
import com.actinarium.rhythm.config.RhythmSpecLayerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * todo: write the doc
 *
 * @author Paul Danyliuk
 */
public class RatioKeyline implements RhythmSpecLayer {

    public static final int DEFAULT_FILL_COLOR = 0xB03F51B5;
    public static final int DEFAULT_TEXT_COLOR = 0xC0FFFFFF;
    public static final int DEFAULT_TEXT_SIZE = 10;   // dp
    public static final int DEFAULT_THICKNESS = 1;    // dp

    protected static final int DEFAULT_LABEL_HEIGHT = 12;  // dp

    protected int mRatioX;
    protected int mRatioY;
    protected int mThickness;

    protected String mRatioString;
    protected Paint mBackgroundPaint;
    protected TextPaint mTextPaint;
    protected Rect mTempRect;
    protected Path mLabelPath;

    // Text adjustment
    protected int mLabelRectWidth;
    protected int mLabelHeight;
    protected int mLabelSideWidth;

    protected Rect mTempThumbnailRect;

    public RatioKeyline(int ratioX, int ratioY, DisplayMetrics metrics) {
        this(metrics);
        mRatioX = ratioX;
        mRatioY = ratioY;
        mBackgroundPaint.setColor(DEFAULT_FILL_COLOR);
        mTextPaint.setColor(DEFAULT_TEXT_COLOR);
    }

    protected RatioKeyline(DisplayMetrics metrics) {
        mTempRect = new Rect();
        mTempThumbnailRect = new Rect();
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

        // Determine keyline label size/bounds
        StaticLayout layout = new StaticLayout(mRatioString, mTextPaint, Integer.MAX_VALUE, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        int labelTextWidth = (int) (layout.getLineMax(0) + 0.5);

        // If text too big, re-measure
        while (labelTextWidth > mLabelRectWidth) {
            mTextPaint.setTextSize(mTextPaint.getTextSize() * 0.8f);
            layout = new StaticLayout(mRatioString, mTextPaint, Integer.MAX_VALUE, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
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

    public static class Factory implements RhythmSpecLayerFactory<RatioKeyline> {

        public static final String LAYER_TYPE = "ratio-keyline";
        private static Pattern RATIO_VALUE_PATTERN = Pattern.compile("(\\d+):(\\d+)");

        @Override
        public RatioKeyline getForConfig(LayerConfig config) {
            RatioKeyline keyline = new RatioKeyline(config.getDisplayMetrics());

            String ratio = config.getString("ratio");
            if (ratio == null) {
                throw new RhythmInflationException("Error when inflating ratio-keyline: 'ratio' argument is missing");
            }
            Matcher matcher = RATIO_VALUE_PATTERN.matcher(ratio);
            if (!matcher.matches()) {
                throw new RhythmInflationException("Error when inflating ratio-keyline: 'ratio' argument is invalid, expected \"x:y\" pattern");
            }
            keyline.mRatioX = Integer.parseInt(matcher.group(1));
            keyline.mRatioY = Integer.parseInt(matcher.group(2));
            keyline.mRatioString = String.format("%d:%d", keyline.mRatioX, keyline.mRatioY);

            keyline.mThickness = config.getDimensionPixelSize("thickness",
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_THICKNESS, config.getDisplayMetrics()));
            keyline.mBackgroundPaint.setColor(config.getColor("color", DEFAULT_FILL_COLOR));
            keyline.mTextPaint.setColor(config.getColor("text-color", DEFAULT_TEXT_COLOR));

            return keyline;
        }
    }

}