package com.actinarium.rhythm.layers;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmDrawable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An interface describing a lightweight reusable drawable, which can be drawn multiple times using provided bounds
 * (unlike {@link Drawable} subclasses). Implement this interface to create custom drawables for Rhythm overlays.
 */
public interface RhythmDrawableLayer {

    /**
     * Draw this layer to the canvas using provided bounds
     *
     * @param canvas         Canvas to draw this layer to
     * @param drawableBounds Bounds where this layer should draw itself. Since these are the bounds of a {@link
     *                       RhythmDrawable} connected to the view, they are usually the same as the view's bounds,
     *                       so you can get the view's dimensions if you need it.
     */
    void draw(Canvas canvas, Rect drawableBounds);

    /**
     * Type definition for layer gravity, allowing to use only 4 states
     */
    @SuppressLint("RtlHardcoded")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Gravity.TOP, Gravity.BOTTOM, Gravity.LEFT, Gravity.RIGHT})
    @interface LayerGravity {}
}
