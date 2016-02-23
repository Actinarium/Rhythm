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

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
import android.util.DisplayMetrics;
import com.actinarium.rhythm.RhythmOverlay;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.RhythmSpecLayerParent;
import com.actinarium.rhythm.spec.DimensionsLabel;
import com.actinarium.rhythm.spec.Fill;
import com.actinarium.rhythm.spec.GridLines;
import com.actinarium.rhythm.spec.Guide;
import com.actinarium.rhythm.spec.InsetGroup;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Inflates a {@link RhythmOverlay} from text configuration
 *
 * @author Paul Danyliuk
 */
public class OverlayInflater {

    private static final Pattern ARGUMENTS_PATTERN = Pattern.compile("([^\\s=]+)(?:=([^\\s]+))?");

    private DisplayMetrics mDisplayMetrics;
    private Map<String, SpecLayerFactory> mFactories;

    /**
     * Create a new overlay inflater. It comes pre-configured to inflate all bundled {@link RhythmSpecLayer} types out
     * of the box according to the docs, but you can easily add the logic to inflate custom layers or even override
     * default one.
     *
     * @param metrics display metrics used to resolve complex dimensions (e.g. dips and sp) in configuration lines
     */
    public OverlayInflater(DisplayMetrics metrics) {
        mDisplayMetrics = metrics;
        mFactories = new HashMap<>(8);

        // Register bundled spec layers
        mFactories.put(GridLines.Factory.LAYER_TYPE, new SimpleCacheFactory<>(new GridLines.Factory()));
        mFactories.put(Guide.Factory.LAYER_TYPE, new SimpleCacheFactory<>(new Guide.Factory()));
        mFactories.put(Fill.Factory.LAYER_TYPE, new SimpleCacheFactory<>(new Fill.Factory()));
        mFactories.put(InsetGroup.Factory.LAYER_TYPE, new InsetGroup.Factory());
        mFactories.put(DimensionsLabel.Factory.LAYER_TYPE, new DimensionsLabel.Factory());
    }

    /**
     * Register a factory for provided layer type. Use this method to register factories for your custom spec layers or
     * override default behavior. You can add the same factory for multiple layer types, e.g. for aliasing.
     *
     * @param layerType string that identifies a specific spec layer class; the first argument in each config line
     * @param factory   a factory object that will inflate config line into a layer
     * @return this for chaining
     */
    public OverlayInflater registerFactory(@NonNull String layerType, @NonNull SpecLayerFactory factory) {
        mFactories.put(layerType, factory);
        return this;
    }

    /**
     * Add an alias for arbitrary layer type. This will make multiple layer type strings map to the same factory. For
     * custom layers, a slightly more efficient way would be to simply call {@link #registerFactory(String,
     * SpecLayerFactory)} multiple times with different strings and the same factory objects to avoid lookups.
     *
     * @param existingLayerType layer type string for layer to alias (used for lookup)
     * @param aliasLayerType    layer type string to map to the same factory
     * @return this for chaining
     */
    public OverlayInflater addAlias(@NonNull String existingLayerType, @NonNull String aliasLayerType) {
        SpecLayerFactory factory = mFactories.get(existingLayerType);
        if (factory != null) {
            mFactories.put(aliasLayerType, factory);
        } else {
            throw new IllegalArgumentException("No factory registered for type \"" + existingLayerType + "\"");
        }
        return this;
    }

    /**
     * Inflate the whole overlay from overlay configuration string. The string must have layer configs on separate
     * lines, nested layers being properly indented with spaces.
     *
     * @param configString layer configuration string, following the syntax rules
     * @return inflated rhythm overlay
     * @see #inflateInto(RhythmSpecLayerParent, String)
     */
    public RhythmOverlay inflateOverlay(String configString) {
        RhythmOverlay overlay = new RhythmOverlay();
        inflateInto(overlay, configString);
        return overlay;
    }

    /**
     * Inflate layers from the configuration string into provided parent, appending layers to existing ones if any.
     *
     * @param parent       parent to append layers to
     * @param configString layer configuration string, following the syntax rules
     * @see #inflateLayer(String)
     */
    public void inflateInto(RhythmSpecLayerParent parent, String configString) {
        // initialize stacks for parents and indents. Since there's no adequate stack implementations out there for API 8+, make own.
        // Assume there rarely will be more than 4-deep hierarchy
        int size = 4;
        int[] indents = new int[size];
        RhythmSpecLayerParent[] parents = new RhythmSpecLayerParent[size];
        int headIndex = 0;

        // at the bottom of the stack we have RhythmOverlay object
        parents[0] = parent;
        indents[0] = -1;

        // split configuration into lines
        String[] lines = configString.split("\\r?\\n");

        // parse line by line, nest as required
        for (String line : lines) {
            LayerConfig config = parseConfig(line);

            // If indent is <= indent of parent layer, then go up the hierarchy. Won't underflow b/c indents[0] is -1
            while (config.getIndent() <= indents[headIndex]) {
                headIndex--;
                // we could clean up the stacks but there's really no need
            }

            RhythmSpecLayer thisLayer = inflateLayer(config);
            parents[headIndex].addLayer(thisLayer);

            // if this is a layer group, add it to the stack
            if (thisLayer instanceof RhythmSpecLayerParent) {
                headIndex++;
                // if arrays run out of space, increase it twice (a-la ArrayList)
                if (headIndex >= size) {
                    int newSize = size * 2;
                    int[] newIndents = new int[newSize];
                    RhythmSpecLayerParent[] newParents = new RhythmSpecLayerParent[newSize];
                    System.arraycopy(indents, 0, newIndents, 0, size);
                    System.arraycopy(parents, 0, newParents, 0, size);
                    indents = newIndents;
                    parents = newParents;
                    size = newSize;
                }
                parents[headIndex] = (RhythmSpecLayerParent) thisLayer;
                indents[headIndex] = config.getIndent();
            }
        }
    }

    /**
     * Inflate an individual layer from raw configuration string
     *
     * @param configString configuration string to parse and feed to layer's factory
     * @return inflated layer
     */
    public RhythmSpecLayer inflateLayer(String configString) {
        return inflateLayer(parseConfig(configString));
    }

    /**
     * Inflate an individual layer from already parsed layer configuration. This method will inject display metrics into
     * the config object.
     *
     * @param config parsed layer configuration
     * @return inflated layer
     */
    public RhythmSpecLayer inflateLayer(LayerConfig config) {
        config.setDisplayMetrics(mDisplayMetrics);
        SpecLayerFactory factory = mFactories.get(config.getLayerType());
        if (factory == null) {
            throw new RhythmInflationException("No factory registered for type \"" + config.getLayerType() + "\"");
        }
        return factory.getForConfig(config);
    }

    /**
     * Parses a line with single layer configuration into a LayerConfig object
     *
     * @param configString configuration string, indented with spaces if required, starting with layer title and
     *                     containing args or key=value pairs
     * @return layer config object to feed to {@link SpecLayerFactory#getForConfig(LayerConfig)}. <b>Note:</b> does
     * not have {@link DisplayMetrics} injected into it - you have to do it yourself before querying complex dimensions
     * from this layer config object.
     */
    public static LayerConfig parseConfig(String configString) {
        // Let's just iterate over the first chars to get indent and layer type, and parse the arguments with regex
        int i = 0;
        int length = configString.length();

        // 1. indent
        while (i < length && configString.charAt(i) == ' ') {
            i++;
        }
        final int spaces = i;

        // 2. layer class name
        while (i < length && configString.charAt(i) != ' ') {
            i++;
        }
        final String specLayerType = configString.substring(spaces, i);

        final int anticipatedCapacity = (length - i) / 12 + 1;
        ArrayMap<String, String> arguments = new ArrayMap<>(anticipatedCapacity);

        // todo: instead of regex, consider parsing linearly for efficiency
        Matcher matcher = ARGUMENTS_PATTERN.matcher(configString.substring(i));
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            arguments.put(key, value);
        }

        return new LayerConfig(specLayerType, spaces, arguments);
    }

}
