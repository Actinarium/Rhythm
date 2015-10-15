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

package com.actinarium.rhythm.spec;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmDrawable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Spec layer a descriptor of a granular piece of overlay (e.g. a single line, a repeating line etc), which both
 * holds the configuration of its appearance (hence the spec), but is also capable of drawing itself onto the provided
 * canvas (hence the layer). Unlike Drawables, where separate instances are required each time they are used, spec layer
 * instances are created per configuration and can be reused across many {@link RhythmDrawable}s (views,
 * overlays).</p><p>You can create custom spec layers by implementing this interface.</p>
 */
public interface RhythmSpecLayer {

    /**
     * Draw itself to the provided canvas within provided bounds, according to configuration (if any)
     *
     * @param canvas         Canvas for the layer to draw itself to
     * @param drawableBounds Bounds where this layer should draw itself. Since these are the bounds of a {@link
     *                       RhythmDrawable} connected to the view, they are usually the same as the view’s bounds, so
     *                       you can get the view’s dimensions if you need them.
     */
    void draw(Canvas canvas, Rect drawableBounds);

    /**
     * Type definition for layer gravity, allowing to use only 4 states (2 for horizontal, 2 for vertical orientation)
     */
    @SuppressLint("RtlHardcoded")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Gravity.TOP, Gravity.BOTTOM, Gravity.LEFT, Gravity.RIGHT})
    @interface LayerGravity {
    }
}
