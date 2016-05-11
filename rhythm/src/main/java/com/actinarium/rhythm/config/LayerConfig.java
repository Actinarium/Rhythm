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
import android.util.DisplayMetrics;
import com.actinarium.rhythm.RhythmSpecLayer;

import java.util.Map;

/**
 * A {@link SimpleArgumentsBundle} subclass used by {@link RhythmOverlayInflater} to describe configuration of layers
 * that should be created. In addition to the arguments map, objects of this class store extra spec layer metadata such
 * as the layer type and its indent (leading spaces in the config) to properly nest it within the parent overlay.
 *
 * @author Paul Danyliuk
 */
public class LayerConfig extends SimpleArgumentsBundle {

    protected String mLayerType;
    protected int mIndent;

    protected Map<String, String> mVariables;

    /**
     * Create layer config object for layer of given type, with known indent, and with pre-filled arguments bag
     *
     * @param layerType spec layer type, used for appropriate factory lookup
     * @param indent    number of leading spaces in the config line, used to resolve layer hierarchy
     * @param arguments bag of raw arguments parsed from configuration string
     * @param variables bag of variables to be used in arguments. Keys must be prefixed with <code>@</code>.
     * @param metrics   display metrics so that complex dimensions (dp, sp etc) can be resolved into pixels.
     */
    public LayerConfig(@NonNull String layerType, int indent, @NonNull Map<String, String> arguments, @NonNull Map<String, String> variables, DisplayMetrics metrics) {
        super(arguments, metrics);
        mLayerType = layerType;
        mVariables = variables;
        mIndent = indent;
    }

    /**
     * Get the name of {@link RhythmSpecLayer spec layer} to inflate with these arguments
     *
     * @return spec layer type
     */
    public String getLayerType() {
        return mLayerType;
    }

    /**
     * Get the number of spaces this config line was indented with. Used internally to resolve grouping
     *
     * @return number of spaces
     */
    public int getIndent() {
        return mIndent;
    }

    @Override
    public String getString(String key) {
        String value = super.getString(key);

        // If the value is a reference to a variable, return variable value
        // todo: it's really bad to rely on other methods calling this one - refactor
        if (value != null && value.length() > 0 && value.charAt(0) == '@') {
            value = mVariables.get(value);
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        LayerConfig that = (LayerConfig) o;
        return mArguments.equals(that.mArguments) && mVariables.equals(that.mVariables) && mLayerType.equals(that.mLayerType);
    }

    @Override
    public int hashCode() {
        int result = mLayerType.hashCode();
        result = 31 * result + mArguments.hashCode();
        result = 31 * result + mVariables.hashCode();
        return result;
    }

}
