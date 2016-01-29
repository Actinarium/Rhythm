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

package com.actinarium.rhythm.sample;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Gravity;
import com.actinarium.rhythm.config.LayerConfig;
import com.actinarium.rhythm.config.RhythmInflationException;
import com.actinarium.rhythm.config.SpecLayerFactory;
import com.actinarium.rhythm.spec.DimensionsLabel;
import com.actinarium.rhythm.RhythmSpecLayer;

/**
 * An example of a custom spec layer drawing a translucent overlay of specified width and height, gravity and margin,
 * with dimensions label in the center
 *
 * @author Paul Danyliuk
 */
public class ImageBox implements RhythmSpecLayer {

    protected int mWidth;
    protected int mHeight;
    protected int mDistanceX;
    protected int mDistanceY;
    protected int mGravity;
    protected Rect mTemp;
    protected Paint mPaint;
    protected DimensionsLabel mDimensionsLabel;

    private static final int COLOR = 0x20000000;

    public ImageBox(int width, int height, int distanceX, int distanceY, int gravity, float scaleFactor) {
        mWidth = width;
        mHeight = height;
        mDistanceX = distanceX;
        mDistanceY = distanceY;
        mGravity = gravity;

        mDimensionsLabel = new DimensionsLabel(scaleFactor)
                .setGravity(Gravity.CENTER)
                .setBackgroundColor(Color.TRANSPARENT)
                .setTextColor(Color.WHITE);

        mTemp = new Rect();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(COLOR);
    }

    /**
     * Private minimalistic constructor for the factory
     */
    private ImageBox(float scaleFactor) {
        mTemp = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(COLOR);

        mDimensionsLabel = new DimensionsLabel(scaleFactor)
                .setGravity(Gravity.CENTER)
                .setBackgroundColor(Color.TRANSPARENT)
                .setTextColor(Color.WHITE);
    }

    @Override
    public void draw(Canvas canvas, Rect drawableBounds) {
        // Calculate the rect where we should draw the grid
        Gravity.apply(mGravity, mWidth, mHeight, drawableBounds, mDistanceX, mDistanceY, mTemp);

        // Draw the box
        canvas.drawRect(mTemp, mPaint);

        // Draw dimensions in the center of the box
        mDimensionsLabel.draw(canvas, mTemp);
    }

    /**
     * A factory to add inflater support for this custom layer. See how you can get various values from the LayerConfig
     * object
     */
    public static class Factory implements SpecLayerFactory<ImageBox> {

        @Override
        public ImageBox createFromConfig(LayerConfig config) {
            ImageBox box = new ImageBox(config.getDisplayMetrics().density);

            box.mGravity = config.getGravity("gravity", Gravity.NO_GRAVITY);
            if (box.mGravity == Gravity.NO_GRAVITY) {
                throw new RhythmInflationException("Error when inflating image-box: 'gravity' argument missing or invalid");
            }

            box.mWidth = config.getDimensionPixelSize("width", 0);
            box.mHeight = config.getDimensionPixelSize("height", 0);
            box.mDistanceX = config.getDimensionPixelOffset("distance-x", 0);
            box.mDistanceY = config.getDimensionPixelOffset("distance-y", 0);

            return box;
        }
    }
}
