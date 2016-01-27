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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base class with common functionality for Rhythm overlay layers that have sub-layers
 *
 * @author Paul Danyliuk
 */
public abstract class AbstractSpecLayerGroup<T extends AbstractSpecLayerGroup> implements RhythmSpecLayer, RhythmSpecLayerParent {

    protected static final int ESTIMATED_AVG_LAYERS = 8;
    protected List<RhythmSpecLayer> mLayers;

    public AbstractSpecLayerGroup() {
        mLayers = new ArrayList<>(ESTIMATED_AVG_LAYERS);
    }

    public AbstractSpecLayerGroup(int initialCapacity) {
        mLayers = new ArrayList<>(initialCapacity);
    }

    @Override
    public void draw(Canvas canvas, Rect drawableBounds) {
        for (int i = 0, size = mLayers.size(); i < size; i++) {
            mLayers.get(i).draw(canvas, drawableBounds);
        }
    }

    /**
     * Add a spec layer to this group. <b>Note:</b> by default, calling this method DOESN'T trigger redraw. If you are
     * calling this when overlay is already displayed and want to have changes displayed immediately, you also must call
     * {@link View#invalidateDrawable(Drawable)} or similar yourself.
     *
     * @param layer A Rhythm spec layer
     * @return this for chaining
     */
    @Override
    public T addLayer(RhythmSpecLayer layer) {
        mLayers.add(layer);
        //noinspection unchecked
        return (T) this;
    }
}
