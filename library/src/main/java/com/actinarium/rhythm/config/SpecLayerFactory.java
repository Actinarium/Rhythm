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

/**
 * Interface for a factory that can instantiate a {@link RhythmSpecLayer} implementation from provided {@link
 * LayerConfig}. These factories are used by {@link OverlayInflater} to inflate declarative config into respective
 * overlays. If you make a custom spec layer, you should also create a corresponding <code>SpecLayerFactory</code> and
 * register it within {@link OverlayInflater#registerFactory(String, SpecLayerFactory)} method.<br>Concrete factories
 * may implement some sort of caching and provide the same {@link RhythmSpecLayer} instances for equal {@link
 * LayerConfig}s, but it's not mandatory.
 *
 * @author Paul Danyliuk
 */
public interface SpecLayerFactory<T extends RhythmSpecLayer> {

    /**
     * Create and configure a spec layer based on provided configuration. There's no need to verify {@link
     * LayerConfig#getLayerType()} - OverlayInflater does the arbitration internally
     *
     * @param config container with arguments for this layer
     * @return configured layer
     */
    T createFromConfig(LayerConfig config);

}
