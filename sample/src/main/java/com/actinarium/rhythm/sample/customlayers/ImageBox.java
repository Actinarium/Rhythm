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

package com.actinarium.rhythm.sample.customlayers;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Gravity;
import com.actinarium.rhythm.ArgumentsBundle;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.RhythmInflationException;
import com.actinarium.rhythm.RhythmSpecLayerFactory;
import com.actinarium.rhythm.layer.DimensionsLabel;
import com.actinarium.rhythm.layer.Inset;

/**
 * An example of a custom spec layer drawing a translucent overlay of specified width and height, gravity and margin,
 * with dimensions label in the center. In fact, due to the introduction of a more universal {@link Inset}, this
 * spec layer could be significantly simplified, but all the w/h/gravity/distance parameters are purposefully kept to
 * provide an example of a more complex custom layer with a rather complex {@link Factory}
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

    private static final int COLOR = 0x40000000;

    public ImageBox(int width, int height, int distanceX, int distanceY, int gravity, float scaleFactor) {
        mWidth = width;
        mHeight = height;
        mDistanceX = distanceX;
        mDistanceY = distanceY;
        mGravity = gravity;

        mDimensionsLabel = new DimensionsLabel()
                .setScaleFactor(scaleFactor)
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

        mDimensionsLabel = new DimensionsLabel()
                .setScaleFactor(scaleFactor)
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
     * A factory to add inflater support for this custom layer. See how you can get various values from the ArgumentsBundle
     * object
     */
    public static class Factory implements RhythmSpecLayerFactory<ImageBox> {

        public static final String LAYER_TYPE = "image-box";

        @Override
        public ImageBox getForArguments(ArgumentsBundle argsBundle) {
            ImageBox box = new ImageBox(argsBundle.getDisplayMetrics().density);

            box.mGravity = argsBundle.getGravity("gravity", Gravity.NO_GRAVITY);
            if (box.mGravity == Gravity.NO_GRAVITY) {
                throw new RhythmInflationException(
                        RhythmInflationException.ERROR_ARGUMENT_MISSING_OR_NOT_ONE_OF,
                        "Error when inflating image-box: 'gravity' argument missing or invalid",
                        LAYER_TYPE, "gravity", argsBundle.getString("gravity"), "Gravity constants"
                );
            }

            box.mWidth = argsBundle.getDimensionPixelSize("width", 0);
            box.mHeight = argsBundle.getDimensionPixelSize("height", 0);
            box.mDistanceX = argsBundle.getDimensionPixelOffset("distance-x", 0);
            box.mDistanceY = argsBundle.getDimensionPixelOffset("distance-y", 0);

            return box;
        }
    }
}
