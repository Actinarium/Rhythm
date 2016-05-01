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

package com.actinarium.rhythm.control;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import com.actinarium.rhythm.RhythmDrawable;
import com.actinarium.rhythm.RhythmOverlay;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Controls a group of {@link RhythmDrawable}s, namely propagates the same {@link RhythmOverlay} for all registered
 * <code>RhythmDrawables</code> to render. A {@link RhythmGroup} object holds onto a list of <code>RhythmOverlays</code>
 * and allows cycling through them. Usually, Rhythm groups are used within a {@link RhythmControl} and instantiated via
 * {@link RhythmControl#makeGroup(String)}) method, however you are free to make &ldquo;orphaned&rdquo; groups and
 * control them explicitly from your app.</p><p>Also contains convenience methods for easy decoration of existing views
 * &mdash; those can simplify the scenario when you don’t want to include Rhythm into production builds.</p>
 *
 * @author Paul Danyliuk
 */
public final class RhythmGroup {

    public static int NO_OVERLAY = -1;

    private static final int ESTIMATED_OVERLAYS_PER_GROUP = 4;

    String mTitle;

    // Assigned by RhythmControl upon instantiation via {@link RhythmControl#makeGroup(String)}; makes no sense otherwise
    int mIndex;
    RhythmControl mControl;

    private List<WeakReference<RhythmDrawable>> mDrawables;
    private List<RhythmOverlay> mOverlays;
    private int mCurrentOverlayIndex = NO_OVERLAY;

    /**
     * <p>Create a new Rhythm group.</p><p><b>Heads up:</b> do not explicitly call <code>new RhythmGroup()</code> unless
     * you specifically don’t want it attached to a {@link RhythmControl} (i.e. don’t want it to appear in the Quick
     * Control notification). Instead, you should use {@link RhythmControl#makeGroup(String)}.</p>
     */
    public RhythmGroup() {
        mDrawables = new LinkedList<>();
        mOverlays = new ArrayList<>(ESTIMATED_OVERLAYS_PER_GROUP);
    }

    /**
     * Set the title for this group. The title is only displayed in Rhythm control notification &mdash; you don't need it in
     * programmatically created anonymous groups.
     *
     * @param title A convenient title for this group.
     * @return this for chaining
     */
    public RhythmGroup setTitle(String title) {
        mTitle = title;
        return this;
    }

    /**
     * Get group title
     *
     * @return group title of <code>null</code> if the group is anonymous
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Add Rhythm overlay to this group
     *
     * @param overlay The Rhythm overlay to add
     * @return this for chaining
     */
    public RhythmGroup addOverlay(RhythmOverlay overlay) {
        mOverlays.add(overlay);
        if (mCurrentOverlayIndex == NO_OVERLAY) {
            selectOverlay(0);
        }
        return this;
    }

    /**
     * Add multiple Rhythm overlays to this group
     *
     * @param overlays The Rhythm overlays to add
     * @return this for chaining
     */
    public RhythmGroup addOverlays(Collection<RhythmOverlay> overlays) {
        mOverlays.addAll(overlays);
        if (mCurrentOverlayIndex == NO_OVERLAY) {
            selectOverlay(0);
        }
        return this;
    }

    /**
     * Make a new {@link RhythmDrawable} that will draw the group’s active {@link RhythmOverlay} and can be used as any
     * other {@link Drawable} in Android SDK. You must always make separate drawables for using them in different
     * places, as reusing the same drawable instance may lead to unexpected results.
     *
     * @return A new {@link RhythmDrawable} controlled by this group.
     */
    public RhythmDrawable makeDrawable() {
        RhythmDrawable drawable = new RhythmDrawable(getCurrentOverlay());
        mDrawables.add(new WeakReference<>(drawable));
        return drawable;
    }

    /**
     * <p>A handy method that will decorate provided views with {@link RhythmDrawable}s controlled by this group.</p>
     * <p><b>Note:</b> the backgrounds of all provided views will be wrapped and replaced by
     * <code>RhythmDrawables</code>. To obtain the original background drawables you have to call {@link
     * RhythmDrawable#getDecorated() view.getBackground().getDecorated()}.</p>
     *
     * @param views Views whose backgrounds should be decorated with Rhythm drawables
     * @see #decorateForeground(FrameLayout...)
     */
    @SuppressWarnings("deprecation")
    public void decorate(View... views) {
        for (View view : views) {
            RhythmDrawable decoratingRhythmDrawable = makeDrawable();
            decoratingRhythmDrawable.setDecorated(view.getBackground());
            view.setBackgroundDrawable(decoratingRhythmDrawable);
        }
    }

    /**
     * Similar to {@link #decorate(View...)}, but decorates foregrounds instead of backgrounds of provided views
     * (available only for {@link FrameLayout}), therefore drawing the overlay over the view’s content. Similarly to
     * <code>decorate(View...)</code>, wraps and replaces existing foreground drawable with {@link RhythmDrawable}.
     *
     * @param views Frame layouts whose foregrounds should be decorated
     * @see #decorate(View...)
     */
    public void decorateForeground(FrameLayout... views) {
        for (FrameLayout view : views) {
            RhythmDrawable decoratingRhythmDrawable = makeDrawable();
            decoratingRhythmDrawable.setDecorated(view.getForeground());
            view.setForeground(decoratingRhythmDrawable);
        }
    }

    /**
     * @return Index of currently selected overlay, or {@link #NO_OVERLAY}
     * @see #getCurrentOverlay()
     * @see #getOverlayCount()
     */
    public int getCurrentOverlayIndex() {
        return mCurrentOverlayIndex;
    }

    /**
     * @return Number of overlays associated with this group
     * @see #getCurrentOverlayIndex()
     */
    public int getOverlayCount() {
        return mOverlays.size();
    }

    /**
     * @return Currently selected overlay, or null if overlay is disabled
     * @see #getCurrentOverlayIndex()
     */
    public RhythmOverlay getCurrentOverlay() {
        return mCurrentOverlayIndex != NO_OVERLAY ? mOverlays.get(mCurrentOverlayIndex) : null;
    }

    /**
     * Select overlay by index. Provide {@link #NO_OVERLAY} to hide overlay.
     *
     * @param index Overlay index, or {@link #NO_OVERLAY}
     * @see #selectNextOverlay()
     * @see #getOverlayCount()
     */
    public void selectOverlay(int index) {
        if (index == NO_OVERLAY || (index >= 0 && index < mOverlays.size())) {
            if (mCurrentOverlayIndex != index) {
                mCurrentOverlayIndex = index;
                doSetOverlay(getCurrentOverlay());
            }
        } else {
            throw new IndexOutOfBoundsException("The index is neither NO_OVERLAY nor valid.");
        }
    }

    /**
     * Convenience method to cycle through overlays. Meant primarily for use in Quick Control notification, but can be
     * invoked programmatically.
     *
     * @see #selectOverlay(int)
     */
    public void selectNextOverlay() {
        if (mCurrentOverlayIndex == NO_OVERLAY) {
            if (mOverlays.isEmpty()) {
                // Still no overlay, so no-op.
                return;
            }
            mCurrentOverlayIndex = 0;
        } else {
            mCurrentOverlayIndex = ++mCurrentOverlayIndex % mOverlays.size();
            // Disabling overlay after the last one
            if (mCurrentOverlayIndex == 0) {
                mCurrentOverlayIndex = NO_OVERLAY;
            }
        }
        doSetOverlay(getCurrentOverlay());
    }

    @Override
    public String toString() {
        return mTitle != null ? mTitle : "Group #" + mIndex;
    }

    /**
     * Propagates current overlay to all linked {@link RhythmDrawable}s, removing dead references on the way. Also
     * updates the notification to reflect current overlay’s name
     *
     * @todo add possibility to propagate arbitrary overlay, not just one of those in the list
     */
    private void doSetOverlay(RhythmOverlay overlay) {
        // Using iterator here because we need to remove elements halfway
        Iterator<WeakReference<RhythmDrawable>> iterator = mDrawables.iterator();
        while (iterator.hasNext()) {
            final RhythmDrawable item = iterator.next().get();
            if (item == null) {
                // Clean up dead references
                iterator.remove();
            } else {
                item.setOverlay(overlay);
            }
        }

        // If this group is attached to control, request Quick Control notification update
        if (mControl != null) {
            mControl.requestNotificationUpdate();
        }
    }

}
