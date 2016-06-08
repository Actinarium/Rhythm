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
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.ArgumentsBundle;
import com.actinarium.rhythm.RhythmSpecLayerFactory;

/**
 * A layer that fills all provided area with solid color. You will usually want to use it inside {@link Inset} to draw
 * rectangles (margins, gutters etc).
 *
 * @author Paul Danyliuk
 */
public class Fill implements RhythmSpecLayer {

    public static final int DEFAULT_FILL_COLOR = 0x400091EA;

    protected Paint mPaint;

    /**
     * Create a layer that fills current bounds with solid color
     */
    public Fill() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(DEFAULT_FILL_COLOR);
    }

    /**
     * Set fill color
     *
     * @param color Fill color, in #AARRGGBB format as usual
     * @return this for chaining
     */
    public Fill setColor(@ColorInt int color) {
        mPaint.setColor(color);
        return this;
    }

    @Override
    public void draw(Canvas canvas, Rect drawableBounds) {
        canvas.drawRect(drawableBounds, mPaint);
    }

    /**
     * A default factory that creates new {@link Fill} layers from config lines according to <a
     * href="https://github.com/Actinarium/Rhythm/wiki/Declarative-configuration#fill">the docs</a>
     */
    public static class Factory implements RhythmSpecLayerFactory<Fill> {

        public static final String LAYER_TYPE = "fill";
        public static final String ARG_COLOR = "color";

        @Override
        public Fill getForArguments(ArgumentsBundle argsBundle) {
            Fill fill = new Fill();
            fill.setColor(argsBundle.getColor(ARG_COLOR, DEFAULT_FILL_COLOR));
            return fill;
        }
    }

}
