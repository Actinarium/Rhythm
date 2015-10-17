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

package com.actinarium.rhythm.sample;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.Gravity;
import com.actinarium.rhythm.spec.DimensionsLabel;
import com.actinarium.rhythm.spec.RhythmSpecLayer;

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
                .gravity(Gravity.CENTER)
                .boxColor(Color.TRANSPARENT)
                .textColor(Color.WHITE);

        mTemp = new Rect();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(COLOR);
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
}
