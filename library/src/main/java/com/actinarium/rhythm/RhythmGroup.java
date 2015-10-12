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

import android.support.annotation.Nullable;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>Groups all views and drawables that should display the same {@link RhythmPattern} at the moment. A Rhythm group
 * can have multiple Rhythm patterns set up and allow all associated drawables cycle through them.</p><p>Normally, the
 * group should be attached to {@link RhythmControl} (for that you don't instantiate RhythmGroup directly but use {@link
 * RhythmControl#makeGroup(String)}), but "orphaned" groups will work too.</p>
 *
 * @author Paul Danyliuk
 */
public final class RhythmGroup {

    public static int NO_PATTERN = -1;

    private static final int ESTIMATED_PATTERNS_PER_GROUP = 4;

    private String mTitle;

    // Assigned by RhythmControl upon instantiation via {@link RhythmControl#makeGroup(String)}; make no sense otherwise
    int mIndex;
    RhythmControl mControl;

    private List<WeakReference<RhythmDrawable>> mDrawables;
    private List<RhythmPattern> mPatterns;
    private int mCurrentPatternIndex = NO_PATTERN;

    /**
     * Create a new Rhythm group. <b>Heads up:</b> create a group by directly using this constructor only if you
     * specifically don't want it attached to a {@link RhythmControl} (i.e. don't want it to appear in the Quick Control
     * notification). Instead, you should use {@link RhythmControl#makeGroup(String)}
     *
     * @param title A convenient title for this group, used to identify it in the notification. If you are calling this
     *              directly (i.e. without attaching to Rhythm control), just leave it <code>null</code>.
     */
    public RhythmGroup(@Nullable String title) {
        mTitle = title;
        mDrawables = new LinkedList<>();
        mPatterns = new ArrayList<>(ESTIMATED_PATTERNS_PER_GROUP);
    }

    /**
     * Add Rhythm pattern to this group
     *
     * @param pattern The Rhythm pattern to add
     * @return this for chaining
     */
    public RhythmGroup addPattern(RhythmPattern pattern) {
        mPatterns.add(pattern);
        if (mCurrentPatternIndex == NO_PATTERN) {
            selectPattern(0);
        }
        return this;
    }

    /**
     * Make a new Rhythm drawable that will draw the group's active {@link RhythmPattern} and can be used as an overlay
     * or a background of a view. You should always make separate drawables for using them in different places, as
     * reusing the same instance may lead to unexpected results. All drawables are controlled by this group and will
     * redraw themselves when another pattern is selected.
     *
     * @return A new {@link RhythmDrawable} attached to this group.
     */
    public RhythmDrawable makeDrawable() {
        RhythmDrawable drawable = new RhythmDrawable();
        drawable.setPattern(getCurrentPattern());
        mDrawables.add(new WeakReference<>(drawable));
        return drawable;
    }

    /**
     * <p>A handy method that will decorate provided views with Rhythm drawables connected to this
     * group.</p><p><b>Note:</b> while Rhythm patterns will be drawn over the views' existing backgrounds, their
     * original background drawables will be replaced with decorated ones. To access it, you should call
     * <code>view.getBackground().getDecoratedBackground()</code></p>
     *
     * @param views Views whose backgrounds should be decorated with Rhythm drawables
     */
    public void decorate(View... views) {
        for (View view : views) {
            RhythmDrawable decoratingRhythmDrawable = makeDrawable();
            decoratingRhythmDrawable.setDecoratedBackground(view.getBackground());
            view.setBackgroundDrawable(decoratingRhythmDrawable);
        }
    }

    /**
     * @return Index of currently selected pattern, or {@link #NO_PATTERN}
     * @see #getCurrentPattern()
     * @see #getPatternCount()
     */
    public int getCurrentPatternIndex() {
        return mCurrentPatternIndex;
    }

    /**
     * @return Number of patterns associated with this group
     * @see #getCurrentPatternIndex()
     */
    public int getPatternCount() {
        return mPatterns.size();
    }

    /**
     * @return Currently selected pattern, or null if overlay is disabled
     * @see #getCurrentPatternIndex()
     */
    public RhythmPattern getCurrentPattern() {
        return mCurrentPatternIndex != NO_PATTERN ? mPatterns.get(mCurrentPatternIndex) : null;
    }

    /**
     * Select pattern by index. Provide {@link #NO_PATTERN} to hide pattern.
     *
     * @param index Pattern index, or {@link #NO_PATTERN}
     * @see #selectNextPattern()
     * @see #getPatternCount()
     */
    public void selectPattern(int index) {
        if (index == NO_PATTERN || (index >= 0 && index < mPatterns.size())) {
            if (mCurrentPatternIndex != index) {
                mCurrentPatternIndex = index;
                doSetPattern();
            }
        } else {
            throw new IndexOutOfBoundsException("The index is neither NO_PATTERN nor valid.");
        }
    }

    /**
     * Convenience method to cycle through patterns. Meant primarily for use in Quick Control notification, but can be
     * invoked programmatically.
     *
     * @see #selectPattern(int)
     */
    public void selectNextPattern() {
        if (mCurrentPatternIndex == NO_PATTERN) {
            mCurrentPatternIndex = 0;
        } else {
            mCurrentPatternIndex = ++mCurrentPatternIndex % mPatterns.size();
            // After the last pattern comes disabled overlay
            if (mCurrentPatternIndex == 0) {
                mCurrentPatternIndex = NO_PATTERN;
            }
        }
        doSetPattern();
    }

    @Override
    public String toString() {
        return mTitle != null ? mTitle : "Group #" + mIndex;
    }

    /**
     * Propagates current pattern to all linked {@link RhythmDrawable}s, removing dead references on the way. Also
     * updates the notification to reflect current pattern's name
     */
    private void doSetPattern() {
        final RhythmPattern pattern = getCurrentPattern();

        // Using iterator here because we need to remove elements halfway
        Iterator<WeakReference<RhythmDrawable>> iterator = mDrawables.iterator();
        while (iterator.hasNext()) {
            final RhythmDrawable item = iterator.next().get();
            if (item == null) {
                // Clean up dead references
                iterator.remove();
            } else {
                item.setPattern(pattern);
            }
        }

        // If this group is attached to control, request Quick Control notification update
        if (mControl != null) {
            mControl.requestNotificationUpdate();
        }
    }

}
