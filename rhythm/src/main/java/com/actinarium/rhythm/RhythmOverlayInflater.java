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

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.util.ArrayMap;
import com.actinarium.rhythm.internal.ReaderUtils;
import com.actinarium.rhythm.layer.Columns;
import com.actinarium.rhythm.layer.DimensionsLabel;
import com.actinarium.rhythm.layer.Fill;
import com.actinarium.rhythm.layer.GridLines;
import com.actinarium.rhythm.layer.InsetGroup;
import com.actinarium.rhythm.layer.Keyline;
import com.actinarium.rhythm.layer.RatioKeyline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A default inflater that creates {@linkplain RhythmOverlay}s from text configuration using registered layer
 * factories. Supports inflating multiple overlays from configuration files (see <a
 * href="https://github.com/Actinarium/Rhythm/wiki">the docs</a>) separated by newlines as well as separate overlays;
 * supports comments and variables, and supporting custom spec layers by allowing to register spec layer
 * factories.</p><p>The provided implementation is a reference one &mdash; developers are welcome to subclass this
 * inflater or any classes of the inflation pipeline to override certain aspects, or implement their own inflation
 * mechanisms (e.g. different lexers, parse-time validation, transformations, XML/JSON/YAML support etc) entirely from
 * scratch should they need something different.</p>
 *
 * @author Paul Danyliuk
 */
public class RhythmOverlayInflater {

    /**
     * Initial capacity of {layer type} -&gt; {factory} map.
     */
    private static final int INITIAL_FACTORIES_CAPACITY = 16;

    /**
     * A regex to search for arguments in configuration string by a following template: key[=value]
     */
    protected static final Pattern ARGUMENTS_PATTERN = Pattern.compile("([^\\s=]+)(?:=([^\\s]+))?");

    /**
     * A regex to validate and parse variables in configuration string by a following template: @variable=value
     */
    protected static final Pattern VARIABLES_PATTERN = Pattern.compile("(@[\\w]+)=(.*)");

    /**
     * Used internally to indicate that there's no overlay block started at the moment of evaluating current line
     */
    private static final int NOT_STARTED = -1;

    protected Context mContext;
    protected Map<String, RhythmSpecLayerFactory> mFactories;

    /**
     * <p>Create a new instance of default overlay inflater. It comes pre-configured to inflate all bundled {@link
     * RhythmSpecLayer} types, and you can add custom factories for your custom spec layers.</p><p>By default, {@link
     * GridLines}, {@link Keyline}, {@link RatioKeyline}, and {@link Fill} layer instances are cached and reused for the
     * same configuration lines &mdash; if you don't want this behavior (e.g. if you want to mutate the inflated layers
     * individually afterwards), create an empty inflater and register the factories yourself like this:</p>
     * <pre><code>
     * RhythmOverlayInflater inflater = new RhythmOverlayInflater(context);
     * inflater.registerFactory(GridLines.Factory.LAYER_TYPE, new GridLines.Factory());
     * inflater.registerFactory(Keyline.Factory.LAYER_TYPE, new Keyline.Factory());
     * inflater.registerFactory(RatioKeyline.Factory.LAYER_TYPE, new RatioKeyline.Factory());
     * inflater.registerFactory(Fill.Factory.LAYER_TYPE, new Fill.Factory());
     * inflater.registerFactory(InsetGroup.Factory.LAYER_TYPE, new InsetGroup.Factory());
     * inflater.registerFactory(Columns.Factory.LAYER_TYPE, new Columns.Factory());
     * inflater.registerFactory(DimensionsLabel.Factory.LAYER_TYPE, new DimensionsLabel.Factory());
     * </code></pre>
     *
     * @param context Context
     * @return a new overlay inflater instance configured to inflate bundled spec layers
     * @see #RhythmOverlayInflater(Context)
     */
    public static RhythmOverlayInflater createDefault(Context context) {
        final RhythmOverlayInflater inflater = new RhythmOverlayInflater(context);

        // Register bundled spec layers. Wrap keyline, fill, grid, and ratio keyline factories in caching decorators
        inflater.mFactories.put(GridLines.Factory.LAYER_TYPE, new SimpleCacheFactory<>(new GridLines.Factory()));
        inflater.mFactories.put(Keyline.Factory.LAYER_TYPE, new SimpleCacheFactory<>(new Keyline.Factory()));
        inflater.mFactories.put(RatioKeyline.Factory.LAYER_TYPE, new SimpleCacheFactory<>(new RatioKeyline.Factory()));
        inflater.mFactories.put(Fill.Factory.LAYER_TYPE, new SimpleCacheFactory<>(new Fill.Factory()));
        inflater.mFactories.put(InsetGroup.Factory.LAYER_TYPE, new InsetGroup.Factory());
        inflater.mFactories.put(Columns.Factory.LAYER_TYPE, new Columns.Factory());
        inflater.mFactories.put(DimensionsLabel.Factory.LAYER_TYPE, new DimensionsLabel.Factory());

        return inflater;
    }

    /**
     * Create a new instance of overlay inflater with no factories registered. Call this constructor only if you need a
     * blank inflater that you are going to configure from scratch (i.e. by registering all the required factories with
     * {@link #registerFactory(String, RhythmSpecLayerFactory)}). If you need an inflater with all bundled spec layers
     * pre-configured, use {@link #createDefault(Context)} instead.
     *
     * @param context Context
     * @see #createDefault(Context)
     */
    public RhythmOverlayInflater(Context context) {
        mContext = context.getApplicationContext();
        mFactories = new HashMap<>(INITIAL_FACTORIES_CAPACITY);
    }

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
     * @param rawResId Raw configuration file with syntax according to the docs
     * @return A list of inflated Rhythm overlays
     * @see #inflate(List)
     */
    public List<RhythmOverlay> inflate(@RawRes int rawResId) {
        List<String> lines = ReaderUtils.readLines(mContext, rawResId);
        return inflate(lines);
    }

    /**
     * Same as {@link #inflate(int)}, but accepts a string for the whole overlay configuration file. This method may
     * come in handy if you need to bulk inflate several overlays from strings known at runtime.
     *
     * @param configString Configuration file passed in whole as a string. Must follow the same syntax rules as the
     *                     configuration file, e.g. overlays must be separated by an empty line
     * @return A list of inflated Rhythm overlays
     * @see #inflate(int)
     * @see #inflate(List)
     */
    public List<RhythmOverlay> inflate(String configString) {
        List<String> configStrings = Arrays.asList(configString.split("\\r?\\n"));
        return inflate(configStrings);
    }

    /**
     * <p>Same as {@link #inflate(int)}, but accepts the configuration file already split in lines as strings.</p>
     * <p>This method walks over the lines and determines how the config should be split into separate overlays.</p>
     *
     * @param configStrings Configuration file split as separate lines. Must follow the same syntax rules as the
     *                      configuration file, that is, no <code>null</code> strings, and overlays being separated by
     *                      an empty line
     * @return A list of inflated Rhythm overlays
     * @see #inflate(int)
     * @see #inflate(String)
     */
    public List<RhythmOverlay> inflate(List<String> configStrings) {
        List<RhythmOverlay> overlays = new ArrayList<>();
        Map<String, String> globalVars = new HashMap<>();
        final int len = configStrings.size();
        int overlayStart = NOT_STARTED;

        // Line index
        int i = 0;

        // First let's read global variables, which must be placed in the beginning of the file
        for (; i < len; i++) {
            final String line = configStrings.get(i);
            if (isEmptyOrComment(line.trim())) {
                // No-op
            } else if (line.charAt(0) == '@') {
                // Variable declaration. Let's check and parse it
                Matcher matcher = VARIABLES_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String name = matcher.group(1);
                    String value = matcher.group(2);
                    globalVars.put(name, value);
                } else {
                    // Oops, bad variable syntax
                    throw new RhythmInflationException(
                            RhythmInflationException.ERROR_MALFORMED_VARIABLE_SYNTAX,
                            "Malformed variable declaration.\nExpected syntax is @name=value where name may contain only letters, digits, and/or underscores.",
                            line
                    ).setLineNumber(i);
                }
            } else {
                // Found a non-variable-declaration, non-empty line
                break;
            }
        }

        // Now read the remaining lines, separating blocks by empty lines, and inflate the blocks as we go
        for (; i < len; i++) {
            final String line = configStrings.get(i);
            if (line.trim().length() == 0) {
                // We encountered an empty line, meaning this is the end of the previous block if the latter is present
                if (overlayStart != NOT_STARTED) {
                    // There was a block, so now it's terminated and we should inflate it.
                    final RhythmOverlay previousBlock = inflateOverlayInternal(configStrings.subList(overlayStart, i), globalVars, overlayStart);
                    overlays.add(previousBlock);
                    overlayStart = NOT_STARTED;
                }
            } else if (overlayStart == NOT_STARTED) {
                // It's not an empty line, and the block isn't started - start it at this line then
                overlayStart = i;
            }
            // todo: handle comments between overlay blocks
        }

        // If we reached the end of the file, and have a block started, inflate it
        if (overlayStart != NOT_STARTED) {
            final RhythmOverlay previousBlock = inflateOverlayInternal(configStrings.subList(overlayStart, len), globalVars, overlayStart);
            overlays.add(previousBlock);
        }

        return overlays;
    }

    /**
     * Inflate a single overlay from overlay configuration string according to the syntax spec.
     *
     * @param configString layer configuration string, following the syntax rules
     * @return inflated Rhythm overlay
     */
    @SuppressWarnings("unchecked")
    public RhythmOverlay inflateOverlay(String configString) {
        List<String> configStrings = Arrays.asList(configString.split("\\r?\\n"));
        return inflateOverlayInternal(configStrings, Collections.EMPTY_MAP, 0);
    }

    /**
     * Inflate a single overlay from overlay configuration string according to the syntax spec.
     *
     * @param configString layer configuration string, following the syntax rules
     * @param vars         the @key-&gt;value map of the values that can be referenced within this overlay (see the
     *                     docs)
     * @return inflated Rhythm overlay
     */
    public RhythmOverlay inflateOverlay(String configString, @NonNull Map<String, String> vars) {
        List<String> configStrings = Arrays.asList(configString.split("\\r?\\n"));
        return inflateOverlayInternal(configStrings, vars, 0);
    }

    /**
     * Inflate a single overlay from overlay configuration already presented as separate lines.
     *
     * @param configStrings layer configuration split in lines
     * @return inflated Rhythm overlay
     */
    @SuppressWarnings("unchecked")
    public RhythmOverlay inflateOverlay(List<String> configStrings) {
        return inflateOverlayInternal(configStrings, Collections.EMPTY_MAP, 0);
    }

    /**
     * Inflate a single overlay from overlay configuration already presented as separate lines.
     *
     * @param configStrings layer configuration split in lines
     * @param vars          the @key-&gt;value map of the values that can be referenced within this overlay (see the
     *                      docs)
     * @return inflated Rhythm overlay
     */
    public RhythmOverlay inflateOverlay(List<String> configStrings, @NonNull Map<String, String> vars) {
        return inflateOverlayInternal(configStrings, vars, 0);
    }

    /**
     * Internal method for inflating an overlay from separate config lines, with provided global variables map, and
     * possibly as a part of an overlay config file.
     *
     * @param configStrings layer configuration split in lines
     * @param globalVars    map of global variables
     * @param offset        index of the line where this overlay starts in the context of an outer config. Pass 0 if
     *                      inflating this overlay on its own
     * @return inflated Rhythm overlay
     */
    protected RhythmOverlay inflateOverlayInternal(List<String> configStrings, @NonNull Map<String, String> globalVars, int offset) {
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

        // At first assume there are no local overrides, so reusing global vars map for now
        Map<String, String> localVars = globalVars;
        boolean hasLocalVars = false;

        // Read line by line, evaluate line types, parse and nest
        for (int i = 0, lines = configStrings.size(); i < lines; i++) {
            String line = configStrings.get(i);
            if (isEmptyOrComment(line.trim())) {
                // Empty or comment line, no-op (btw there should be no empty lines here if inflating the whole file)
            } else if (line.charAt(0) == '@') {
                // This is a local variable. And all variables must be declared before any overlay lines.
                if (overlay.size() != 0) {
                    throw new RhythmInflationException(
                            RhythmInflationException.ERROR_UNEXPECTED_VARIABLE_DECLARATION,
                            "Unexpected variable declaration.\nVariables must be declared before spec layers."
                    ).setLineNumber(i + offset);
                }

                // If it's the first local var, copy the global vars map where we'll be adding/overwriting values
                if (!hasLocalVars) {
                    localVars = new HashMap<>(globalVars);
                    hasLocalVars = true;
                }

                // Let's check and parse
                Matcher matcher = VARIABLES_PATTERN.matcher(line);
                if (matcher.matches()) {
                    String name = matcher.group(1);
                    String value = matcher.group(2);
                    localVars.put(name, value);
                } else {
                    // Oops, bad variable syntax
                    throw new RhythmInflationException(
                            RhythmInflationException.ERROR_MALFORMED_VARIABLE_SYNTAX,
                            "Malformed variable declaration: \"" + line + "\".\nExpected syntax is @name=value where name may contain only letters, digits, and/or underscores.",
                            line
                    ).setLineNumber(i + offset);
                }
            } else if (line.charAt(0) == '#') {
                // Looks like a title. A title should be the first non-empty line, and there should be no multiple titles per block
                if (overlay.getTitle() != null || hasLocalVars || overlay.size() != 0) {
                    throw new RhythmInflationException(
                            RhythmInflationException.ERROR_UNEXPECTED_TITLE_DECLARATION,
                            "Unexpected overlay title.\nThere can be only one title per overlay, and it must be the first line. Did you forget an empty newline before starting a new overlay?"
                    ).setLineNumber(i + offset);
                }

                // Otherwise OK, we probably have a title
                String title = line.substring(1).trim();
                if (title.length() != 0) {
                    overlay.setTitle(title);
                }
            } else {
                // Otherwise assume the line is a spec layer, try parsing and inflating it as a separate layer
                LayerConfig config = parseConfig(line, localVars);

                // If indent is <= indent of parent layer, then go up the hierarchy. Won't underflow b/c indents[0] is -1
                while (config.getIndent() <= indents[headIndex]) {
                    headIndex--;
                    // we could clean up the stacks but there's really no need
                }

                RhythmSpecLayer thisLayer = inflateLayerInternal(config, i + offset);
                parents[headIndex].addLayer(thisLayer);

                // if this is a layer group, add it to the stack
                if (thisLayer instanceof RhythmSpecLayerParent) {
                    headIndex++;
                    // if arrays run out of space, grow it twice (a-la ArrayList)
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

        // If there are only variables and nothing else, seems like the user tried to declare global variables between overlay blocks
        if (hasLocalVars && overlay.size() == 0 && overlay.getTitle() == null) {
            throw new RhythmInflationException(
                    RhythmInflationException.ERROR_UNEXPECTED_VARIABLE_DECLARATION,
                    "Unexpected variable declaration.\nGlobal variables must be declared before all overlay blocks."
            ).setLineNumber(offset);
        }

        return overlay;
    }

    /**
     * Inflate an individual layer from raw configuration string
     *
     * @param configString configuration string to parse and feed to layer's factory
     * @return inflated layer
     */
    @SuppressWarnings("unchecked")
    public RhythmSpecLayer inflateLayer(String configString) {
        return inflateLayerInternal(parseConfig(configString, Collections.EMPTY_MAP), 0);
    }

    /**
     * Inflate an individual layer from raw configuration string
     *
     * @param configString configuration string to parse and feed to layer's factory
     * @param vars         map of @key-&gt;value mappings used to resolve argument references (e.g.
     *                     <code>@primary=#FF0000</code> to use in <code>color=@primary</code>)
     * @return inflated layer
     */
    public RhythmSpecLayer inflateLayer(String configString, @NonNull Map<String, String> vars) {
        return inflateLayerInternal(parseConfig(configString, vars), 0);
    }

    /**
     * Inflate an individual layer from already parsed layer configuration.
     *
     * @param config     parsed layer configuration
     * @param lineNumber number of the configuration line we're inflating. Required for error reporting.
     * @return inflated layer
     */
    protected RhythmSpecLayer inflateLayerInternal(LayerConfig config, int lineNumber) {
        RhythmSpecLayerFactory factory = mFactories.get(config.getLayerType());
        if (factory == null) {
            Object[] knownLayers = mFactories.keySet().toArray();
            throw new RhythmInflationException(
                    RhythmInflationException.ERROR_UNKNOWN_LAYER_TYPE,
                    "Unknown layer type \"" + config.getLayerType() + "\".\nAvailable types are: " + Arrays.toString(knownLayers),
                    config.getLayerType(), knownLayers
            ).setLineNumber(lineNumber);
        }
        try {
            return factory.getForConfig(config);
        } catch (RhythmInflationException e) {
            // Set line number and rethrow
            throw e.setLineNumber(lineNumber);
        } catch (Exception e) {
            // Catch all other exceptions (e.g. IllegalArgument etc) and wrap'em in RhythmInflationException
            throw new RhythmInflationException(
                    RhythmInflationException.ERROR_INFLATING_LAYER_GENERIC,
                    "Error inflating layer: " + e.getMessage(), e
            ).setLineNumber(lineNumber);
        }
    }

    /**
     * Parses a line with single layer configuration into an ArgumentsBundle object.
     *
     * @param configString configuration string, indented with spaces if required, starting with layer title and
     *                     containing args or key=value pairs
     * @param vars         map of @key-&gt;value mappings used to resolve argument references (e.g.
     *                     <code>@primary=#FF0000</code> to use in <code>color=@primary</code>)
     * @return layer config object to feed to {@link RhythmSpecLayerFactory#getForConfig(ArgumentsBundle)}.
     */
    protected LayerConfig parseConfig(String configString, @NonNull Map<String, String> vars) {
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
        Map<String, String> arguments;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            arguments = new ArrayMap<>(anticipatedCapacity);
        } else {
            arguments = new HashMap<>(anticipatedCapacity);
        }

        Matcher matcher = ARGUMENTS_PATTERN.matcher(configString.substring(i));
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            arguments.put(key, value);
        }

        return new LayerConfig(specLayerType, spaces, arguments, vars, mContext.getResources().getDisplayMetrics());
    }

    /**
     * Determines whether the line is empty or a comment one (starts with <code>//</code>) and thus should be ignored.
     *
     * @param line line to test, should be pre-trimmed
     * @return true if empty or comment
     */
    private static boolean isEmptyOrComment(String line) {
        return line.length() == 0 || (line.charAt(0) == '/' && line.length() >= 2 && line.charAt(1) == '/');
    }
}
