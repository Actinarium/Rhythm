package com.actinarium.rhythm.layers;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.view.Gravity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An interface describing a lightweight reusable drawable, which can be drawn multiple times using provided bounds
 * (unlike {@link Drawable} subclasses). Implement this interface to create custom drawables for Rhythm overlays.
 */
public interface RhythmDrawableLayer {
    void draw(Canvas canvas, Rect drawableBounds);

    /**
     * Type definition for layer gravity, allowing to use only 4 states
     */
    @SuppressLint("RtlHardcoded")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Gravity.TOP, Gravity.BOTTOM, Gravity.LEFT, Gravity.RIGHT})
    @interface LayerGravityConstraint {
    }
}
