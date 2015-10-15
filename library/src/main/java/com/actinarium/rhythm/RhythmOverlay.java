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

import android.support.annotation.NonNull;
import com.actinarium.rhythm.spec.RhythmSpecLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a single overlay configuration, i.e. which spec layers (grid lines, keylines etc) must be drawn in the {@link
 * RhythmDrawable}(s) where this overlay is currently set. Composed of granular {@link RhythmSpecLayer}s, which hold
 * their own configuration (see descriptions of respectable implementations) and are drawn in the order of adding.
 *
 * @author Paul Danyliuk
 */
public class RhythmOverlay {

    protected static final int ESTIMATED_AVG_LAYERS = 8;

    protected String mTitle;
    protected List<RhythmSpecLayer> mLayers;

    /**
     * Create a new overlay
     *
     * @param title A convenient title for this overlay, used to identify it in the notification (not mandatory, but
     *              desirable). If you are not using this overlay within a Rhythm controlled group, you may leave this
     *              <code>null</code>.
     * @see #addLayersFrom(RhythmOverlay)
     */
    public RhythmOverlay(String title) {
        mTitle = title;
        mLayers = new ArrayList<>(ESTIMATED_AVG_LAYERS);
    }

    /**
     * Add a spec layer to this overlay. <b>Note:</b> The changes will come in effect next time the overlay is drawn.
     *
     * @param layer A Rhythm spec layer
     * @return this for chaining
     */
    public RhythmOverlay addLayer(@NonNull RhythmSpecLayer layer) {
        mLayers.add(layer);
        return this;
    }

    /**
     * <p>Add all layers to this overlay from another. Convenient if you have a common set of layers that you wish to
     * include in multiple overlays, or want to create an overlay that combines a few others.</p>
     * <p><b>Warning:</b> for simplicity and performance reasons, not copies but the same layer objects are used &mdash;
     * keep this in mind if you plan to mutate them (advice: don’t mutate layers after adding to an overlay!)</p>
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
     * @param control Rhythm group to add this overlay to
     * @return this for chaining (e.g. for adding this overlay to other groups as well)
     */
    public RhythmOverlay addToGroup(@NonNull RhythmGroup control) {
        control.addOverlay(this);
        return this;
    }

    @Override
    public String toString() {
        return mTitle != null ? mTitle : "Untitled overlay";
    }

}
