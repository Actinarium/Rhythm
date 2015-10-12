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
