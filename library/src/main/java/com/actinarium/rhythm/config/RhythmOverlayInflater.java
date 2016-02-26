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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An inflater that creates {@link RhythmOverlay RhythmOverlays} from text configuration
 *
 * @author Paul Danyliuk
 */
public class RhythmOverlayInflater {

    private static final int INITIAL_FACTORIES_CAPACITY = 8;
    private static final Pattern ARGUMENTS_PATTERN = Pattern.compile("([^\\s=]+)(?:=([^\\s]+))?");
    private static final int NOT_STARTED = -1;

    private Context mContext;
    private Map<String, RhythmSpecLayerFactory> mFactories;

    /**
     * <p>Create a new instance of default overlay inflater. It comes pre-configured to inflate all bundled {@link
     * RhythmSpecLayer} types, and you can add custom factories for your custom spec layers.</p><p>By default, {@link
     * GridLines}, {@link Guide}, and {@link Fill} layer instances are cached and reused for the same configuration
     * lines &mdash; if you don't want this behavior (e.g. if you want to mutate the inflated layers individually
     * afterwards), create an empty inflater and register the factories yourself like this:
     * <pre><code>
     * RhythmOverlayInflater inflater = createEmpty(context);
     * inflater.registerFactory(GridLines.Factory.LAYER_TYPE, new GridLines.Factory());
     * inflater.registerFactory(Guide.Factory.LAYER_TYPE, new Guide.Factory());
     * inflater.registerFactory(Fill.Factory.LAYER_TYPE, new Fill.Factory());
     * inflater.registerFactory(InsetGroup.Factory.LAYER_TYPE, new InsetGroup.Factory());
     * inflater.registerFactory(DimensionsLabel.Factory.LAYER_TYPE, new DimensionsLabel.Factory());
     * </code></pre></p>
     *
     * @param context Context
     * @return a new overlay inflater instance configured to inflate bundled spec layers
     * @see #createEmpty(Context)
     */
    public static RhythmOverlayInflater createDefault(Context context) {
        final RhythmOverlayInflater inflater = createEmpty(context);

        // Register bundled spec layers. Wrap guide, fill, and grid line factory in a caching decorator
        inflater.mFactories.put(GridLines.Factory.LAYER_TYPE, new SimpleCacheFactory<>(new GridLines.Factory()));
        inflater.mFactories.put(Guide.Factory.LAYER_TYPE, new SimpleCacheFactory<>(new Guide.Factory()));
        inflater.mFactories.put(Fill.Factory.LAYER_TYPE, new SimpleCacheFactory<>(new Fill.Factory()));
        inflater.mFactories.put(InsetGroup.Factory.LAYER_TYPE, new InsetGroup.Factory());
        inflater.mFactories.put(DimensionsLabel.Factory.LAYER_TYPE, new DimensionsLabel.Factory());

        return inflater;
    }

    /**
     * Create a new instance of overlay inflater with no factories registered. You should register all the required
     * factories by calling {@link #registerFactory(String, RhythmSpecLayerFactory)} method.
     *
     * @param context Context
     * @return a new overlay inflater instance that is not configured to inflate any spec layer types
     */
    public static RhythmOverlayInflater createEmpty(Context context) {
        final RhythmOverlayInflater inflater = new RhythmOverlayInflater();
        inflater.mContext = context;
        inflater.mFactories = new HashMap<>(INITIAL_FACTORIES_CAPACITY);
        return inflater;
    }

    /**
     * Private constructor. Use {@link #createDefault(Context)} or {@link #createEmpty(Context)} instead
     */
    private RhythmOverlayInflater() {}

    /**
     * Register a factory for provided layer type. Use this method to register factories for your custom spec layers or
     * override default behavior. You can add the same factory for multiple layer types, e.g. for aliasing.
     *
     * @param layerType string that identifies a specific spec layer class; the first argument in each config line
     * @param factory   a factory object that will inflate config line into a layer
     * @return this for chaining
     */
    public RhythmOverlayInflater registerFactory(@NonNull String layerType, @NonNull RhythmSpecLayerFactory factory) {
        mFactories.put(layerType, factory);
        return this;
    }

    /**
     * Add an alias for arbitrary layer type. This will make multiple layer type strings map to the same factory. For
     * custom layers, a slightly more efficient way would be to simply call {@link #registerFactory(String,
     * RhythmSpecLayerFactory)} multiple times with different strings and the same factory objects to avoid lookups.
     *
     * @param existingLayerType layer type string for layer to alias (used for lookup)
     * @param aliasLayerType    layer type string to map to the same factory
     * @return this for chaining
     */
    public RhythmOverlayInflater addAlias(@NonNull String existingLayerType, @NonNull String aliasLayerType) {
        RhythmSpecLayerFactory factory = mFactories.get(existingLayerType);
        if (factory != null) {
            mFactories.put(aliasLayerType, factory);
        } else {
            throw new IllegalArgumentException("No factory registered for type \"" + existingLayerType + "\"");
        }
        return this;
    }

    /**
     * Inflate a Rhythm configuration file into a list of {@link RhythmOverlay RhythmOverlays}, which you can then
     * assign to a group, or make sub-lists of and assign to different groups.
     *
     * @param configFile Raw configuration file with syntax according to the docs
     * @return A list of inflated RhythmOverlays
     * @see #inflate(List)
     */
    public List<RhythmOverlay> inflate(@RawRes int configFile) {
        // Read our config
        final InputStream inputStream = mContext.getResources().openRawResource(configFile);
        List<String> lines = readFrom(inputStream);
        return inflate(lines);
    }

    /**
     * Same as {@link #inflate(int)}, but instead of raw resource file it accepts a list of strings. This method may
     * come in handy if you need to bulk inflate several overlays at runtime.
     *
     * @param config List of configuration lines, which must follow the same syntax rules as the configuration file,
     *               that is, no <code>null</code> strings, and overlays being separated by an empty line
     * @return A list of inflated RhythmOverlays
     * @see #inflate(int)
     */
    public List<RhythmOverlay> inflate(List<String> config) {
        List<RhythmOverlay> overlays = new ArrayList<>();
        final int len = config.size();
        String overlayTitle = null;
        int overlayStart = NOT_STARTED;

        // Now read the lines, determine block bounds and inflate each
        for (int i = 0; i < len; i++) {
            final String line = config.get(i);
            if (line.trim().length() == 0) {
                // We encountered an empty line, meaning this is probably the end of the previous block
                if (overlayStart != NOT_STARTED) {
                    // We deliberately ignore the case when sublist is empty (i.e. only title specified)
                    final RhythmOverlay previousBlock = inflateOverlay(config.subList(overlayStart, i));
                    previousBlock.setTitle(overlayTitle);
                    overlays.add(previousBlock);
                    overlayStart = NOT_STARTED;
                    overlayTitle = null;
                }
            } else if (line.charAt(0) == '#') {
                // We found an overlay title!
                // It must be the first line of the block, so if the latter is already started, we have a problem
                if (overlayStart != NOT_STARTED) {
                    throw new RhythmInflationException("Malformed Rhythm configuration at line " + (i + 1)
                            + ". Did you forget an empty newline before starting a new overlay with a #Header?");
                }

                // Otherwise we're probably fine
                // Trim the leading # and extract the title
                overlayTitle = line.substring(1).trim();
                if (overlayTitle.length() == 0) {
                    overlayTitle = null;
                }

                // Start the overlay at the next line.
                overlayStart = i + 1;
            } else if (overlayStart == NOT_STARTED) {
                // Found a non-empty line. If the block isn't started yet, start a block
                overlayStart = i;
            }
        }

        // If we reached the end of the file, and have a block started, inflate it
        if (overlayStart != NOT_STARTED) {
            final RhythmOverlay previousBlock = inflateOverlay(config.subList(overlayStart, len));
            previousBlock.setTitle(overlayTitle);
            overlays.add(previousBlock);
        }

        return overlays;
    }

    /**
     * Inflate the whole overlay from overlay configuration string. The string must have layer configs on separate
     * lines, nested layers being properly indented with spaces.
     *
     * @param configString layer configuration string, following the syntax rules
     * @return inflated rhythm overlay
     */
    public RhythmOverlay inflateOverlay(String configString) {
        List<String> configStrings = Arrays.asList(configString.split("\\r?\\n"));
        return inflateOverlay(configStrings);
    }

    private RhythmOverlay inflateOverlay(List<String> lines) {
        // initialize stacks for parents and indents. Since there's no adequate stack implementations out there for API 8+, make own.
        // Assume there rarely will be more than 4-deep hierarchy
        int size = 4;
        int[] indents = new int[size];
        RhythmSpecLayerParent[] parents = new RhythmSpecLayerParent[size];
        int headIndex = 0;

        // at the bottom of the stack we have the new RhythmOverlay object
        final RhythmOverlay overlay = new RhythmOverlay();
        parents[0] = overlay;
        indents[0] = -1;

        // parse line by line, nest as required
        for (int i = 0, linesSize = lines.size(); i < linesSize; i++) {
            LayerConfig config = parseConfig(lines.get(i));

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

        return overlay;
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
        config.setDisplayMetrics(mContext.getResources().getDisplayMetrics());
        RhythmSpecLayerFactory factory = mFactories.get(config.getLayerType());
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
     * @return layer config object to feed to {@link RhythmSpecLayerFactory#getForConfig(LayerConfig)}. <b>Note:</b>
     * does not have {@link DisplayMetrics} injected into it - you have to do it yourself before querying complex
     * dimensions from this layer config object.
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

    /**
     * Reads lines from stream and closes it
     *
     * @param inputStream Resource input stream
     * @return Lines read
     */
    private static ArrayList<String> readFrom(InputStream inputStream) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
        ArrayList<String> readLines = new ArrayList<>();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                readLines.add(line);
            }
            inputStream.close();
        } catch (IOException e) {
            throw new RhythmInflationException("Error when inflating the file: " + e.getMessage(), e);
        }
        return readLines;
    }

}
