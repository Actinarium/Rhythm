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
    private Drawable mBackgroundDrawable;

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();

        // Draw background if present
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds(bounds);
            mBackgroundDrawable.draw(canvas);
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
     * Set a {@link RhythmPattern} to draw by this drawable; will request redraw of the view where this drawable is
     * used
     *
     * @param pattern Pattern to draw. Provide <code>null</code> to disable overlay.
     */
    public void setPattern(@Nullable RhythmPattern pattern) {
        mPattern = pattern;
        invalidateSelf();
    }

    /**
     * Set background {@link Drawable}. Should be used when decorating existing views, which already have background
     * &mdash; this way the background will be preserved and the pattern will be drawn atop.
     *
     * @param backgroundDrawable View's background
     */
    public void setBackgroundDrawable(@Nullable Drawable backgroundDrawable) {
        mBackgroundDrawable = backgroundDrawable;
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


}
