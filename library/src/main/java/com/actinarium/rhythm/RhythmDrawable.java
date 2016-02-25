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

package com.actinarium.rhythm;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * <p>Renders the currently assigned {@link RhythmOverlay} and serves as an adapter between Rhythm (which sets the
 * overlay to draw by this drawable at the moment) and the views where the overlay (grids, keylines etc) must be
 * applied. You can use it as any other {@link Drawable} from Android SDK, e.g. assign it as background, foreground,
 * overlay etc, but keep in mind that for different views, separate drawable instances must be created.</p> <p>For easy
 * integration with existing layouts, <code>RhythmDrawable</code> can decorate another <code>Drawable</code> &mdash;
 * that is, draw the decorated one below and then the overlay atop. This can be especially useful when decorating the
 * views that already have backgrounds. <b>Note:</b> as of this version, decoration logic is very limited for the sake
 * of simplicity, therefore in some cases (e.g. when decorated drawable is a state list or a level list), it may not
 * respond correctly to state and level changes (e.g. pressing a decorated button won’t highlight it). But since
 * decoration is mostly intended for ViewGroups, it’s unlikely that this will be addressed.</p> <p>Normally you
 * shouldn’t extend this class. If you need to perform custom drawing, consider creating a custom {@link
 * RhythmSpecLayer} implementation.</p>
 *
 * @author Paul Danyliuk
 */
public class RhythmDrawable extends Drawable {

    protected RhythmOverlay mOverlay;
    protected Drawable mDecorated;

    /**
     * Create a Rhythm drawable for given Rhythm overlay. You can then change the displayed overlay via {@link
     * #setOverlay(RhythmOverlay)} method.
     *
     * @param overlay Rhythm overlay to render into this drawable, can be <code>null</code>.
     */
    public RhythmDrawable(@Nullable RhythmOverlay overlay) {
        mOverlay = overlay;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();

        // Draw decorated drawable if present
        if (mDecorated != null) {
            mDecorated.setBounds(bounds);
            mDecorated.draw(canvas);
        }

        // Draw overlay if present
        if (mOverlay != null) {
            mOverlay.draw(canvas, bounds);
        }
    }

    /**
     * Get current overlay
     *
     * @return Currently active Rhythm overlay, or <code>null</code> if no overlay is set
     */
    public RhythmOverlay getOverlay() {
        return mOverlay;
    }

    /**
     * Set a {@link RhythmOverlay} for this drawable. Will request redraw of this drawable’s view.
     *
     * @param overlay Overlay to draw. Provide <code>null</code> to disable overlay.
     */
    public void setOverlay(@Nullable RhythmOverlay overlay) {
        mOverlay = overlay;
        invalidateSelf();
    }

    /**
     * Get decorated drawable (the one drawn under the overlay) if present
     *
     * @return Decorated drawable or <code>null</code>
     */
    public Drawable getDecorated() {
        return mDecorated;
    }

    /**
     * Set a {@link Drawable} to decorate. Should be used when decorating an existing background or foreground of a view
     * &mdash; this way the original drawable will be preserved and the overlay will be drawn atop. <b>Note:</b> to
     * function properly, the decorated drawable’s {@link Drawable#setCallback(Callback) callbacks} must be set. Also
     * see {@link RhythmDrawable the class’ description} for more info on decoration support.
     *
     * @param decorated A drawable to draw below the overlay. Set <code>null</code> to remove decorated drawable.
     */
    public void setDecorated(@Nullable Drawable decorated) {
        mDecorated = decorated;
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        // No-op for the overlay for simplicity reasons - propagate to decorated drawable only
        if (mDecorated != null) {
            mDecorated.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // No-op for the overlay for simplicity reasons - propagate to decorated drawable only
        if (mDecorated != null) {
            mDecorated.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        final int overlayOpacity = mOverlay == null ? PixelFormat.TRANSPARENT : PixelFormat.TRANSLUCENT;
        return mDecorated != null ? Drawable.resolveOpacity(mDecorated.getOpacity(), overlayOpacity) : overlayOpacity;
    }

    @Override
    public boolean isStateful() {
        return mDecorated != null && mDecorated.isStateful();
    }

    @Override
    public boolean setState(int[] stateSet) {
        return mDecorated != null && mDecorated.setState(stateSet);
    }

    @Override
    public int[] getState() {
        return mDecorated != null ? mDecorated.getState() : super.getState();
    }

    @Override
    public boolean getPadding(@NonNull Rect padding) {
        if (mDecorated == null) {
            padding.set(0, 0, 0, 0);
            return false;
        } else {
            return mDecorated.getPadding(padding);
        }
    }

    @Override
    protected boolean onStateChange(int[] state) {
        return mDecorated != null && mDecorated.setState(state);
    }

    @Override
    protected boolean onLevelChange(int level) {
        return mDecorated != null && mDecorated.setLevel(level);
    }
}
