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

import android.support.annotation.NonNull;

/**
 * Defines a single overlay configuration, i.e. which spec layers (grid lines, keylines etc) must be drawn in the {@link
 * RhythmDrawable}(s) where this overlay is currently set. Composed of granular {@link RhythmSpecLayer}s, which hold
 * their own configuration (see descriptions of respectable implementations) and are drawn in the order of adding.
 *
 * @author Paul Danyliuk
 */
public class RhythmOverlay extends AbstractSpecLayerGroup<RhythmOverlay> {

    protected String mTitle;

    /**
     * Create a new untitled overlay
     */
    public RhythmOverlay() {
        super();
    }

    /**
     * Set the title for this overlay. The title is only displayed in Rhythm control notification &mdash; you don't need
     * it in programmatically created anonymous overlays.
     *
     * @param title A convenient title for this overlay.
     * @return this for chaining
     */
    public RhythmOverlay setTitle(String title) {
        mTitle = title;
        return this;
    }

    /**
     * Get overlay title
     *
     * @return overlay title of <code>null</code> if the overlay is anonymous
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * <p>Add all layers to this overlay from another. Convenient if you have a common set of layers that you wish to
     * include in multiple overlays, or want to create an overlay that combines a few others.</p> <p><b>Warning:</b> for
     * simplicity and performance reasons, not copies but the same layer objects are used &mdash; keep this in mind if
     * you plan to mutate them (advice: don’t mutate layers after adding to an overlay!)</p>
     *
     * @param source Existing overlay to add all layers from
     * @return this for chaining
     */
    public RhythmOverlay addLayersFrom(@NonNull RhythmOverlay source) {
        mLayers.addAll(source.mLayers);
        return this;
    }

    /**
     * A shorthand for {@link RhythmGroup#addOverlay(RhythmOverlay)}
     *
     * @param group Rhythm group to add this overlay to
     * @return this for chaining (e.g. for adding this overlay to other groups as well)
     */
    public RhythmOverlay addToGroup(@NonNull RhythmGroup group) {
        group.addOverlay(this);
        return this;
    }

    @Override
    public String toString() {
        return mTitle != null ? mTitle : "Untitled overlay";
    }
}
