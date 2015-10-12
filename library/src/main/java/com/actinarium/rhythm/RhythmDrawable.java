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

package com.actinarium.rhythm;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import com.actinarium.rhythm.layers.RhythmDrawableLayer;

import java.util.List;

/**
 * <p>Draws grids, keylines etc. May have optional background.</p> <p>Made final intentionally. If you need to perform
 * custom drawing, consider creating a custom {@link RhythmDrawableLayer} implementation.</p>
 *
 * @author Paul Danyliuk
 */
public final class RhythmDrawable extends Drawable {

    private RhythmPattern mPattern;
    private Drawable mDecoratedBackground;

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();

        // Draw background if present
        if (mDecoratedBackground != null) {
            mDecoratedBackground.setBounds(bounds);
            mDecoratedBackground.draw(canvas);
        }

        // Draw overlay if present
        if (mPattern != null) {
            final List<RhythmDrawableLayer> layers = mPattern.mLayers;
            // Draw each layer
            for (int i = 0, size = layers.size(); i < size; i++) {
                layers.get(i).draw(canvas, bounds);
            }
        }
    }

    /**
     * Get current pattern
     *
     * @return Currently active Rhythm pattern, or <code>null</code> if no pattern is set
     */
    public RhythmPattern getPattern() {
        return mPattern;
    }

    /**
     * Set a {@link RhythmPattern} to draw by this drawable. Will request redraw of the view where this drawable is
     * used.
     *
     * @param pattern Pattern to draw. Provide <code>null</code> to disable overlay.
     */
    public void setPattern(@Nullable RhythmPattern pattern) {
        mPattern = pattern;
        invalidateSelf();
    }

    /**
     * Get decorated background drawable (i.e. the one drawn under the pattern), if present
     *
     * @return Background drawable or <code>null</code>
     */
    public Drawable getDecoratedBackground() {
        return mDecoratedBackground;
    }

    /**
     * Set background {@link Drawable}. Should be used when decorating existing views, which already have background
     * &mdash; this way the background will be preserved and the pattern will be drawn atop. <b>Note:</b> for background
     * drawable to function properly you must ensure that its {@link Drawable#setCallback(Callback)} has been called.
     *
     * @param decoratedBackground Background drawable to draw below the pattern, can be <code>null</code>
     */
    public void setDecoratedBackground(@Nullable Drawable decoratedBackground) {
        mDecoratedBackground = decoratedBackground;
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        if (mDecoratedBackground != null) {
            mDecoratedBackground.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (mDecoratedBackground != null) {
            mDecoratedBackground.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


}
