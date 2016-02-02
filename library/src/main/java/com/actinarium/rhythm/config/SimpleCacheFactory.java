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

package com.actinarium.rhythm.config;

import com.actinarium.rhythm.RhythmSpecLayer;

import java.util.HashMap;
import java.util.Map;

/**
 * A decorator for a spec layer factory that performs simple caching of previously inflated layers
 *
 * @author Paul Danyliuk
 */
public class SimpleCacheFactory<T extends RhythmSpecLayer> implements SpecLayerFactory<T> {

    private SpecLayerFactory<T> mDecoratedFactory;
    private Map<LayerConfig, T> mCache;

    public SimpleCacheFactory(SpecLayerFactory<T> decoratedFactory) {
        mDecoratedFactory = decoratedFactory;
        mCache = new HashMap<>();
    }

    /**
     * Returns layer for this configuration from cache, or creates a new one via decorated factory if not found in cache
     *
     * @param config container with arguments for this layer
     * @return layer for this configuration, either new or from cache
     */
    @Override
    public T getForConfig(LayerConfig config) {
        T layer = mCache.get(config);
        if (layer != null) {
            return layer;
        }

        // if cache miss, inflate the new one
        layer = mDecoratedFactory.getForConfig(config);
        mCache.put(config, layer);
        return layer;
    }
}
