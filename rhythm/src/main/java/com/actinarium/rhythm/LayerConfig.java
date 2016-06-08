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
 * Describes spec layer configuration (arguments and metadata). Used internally by {@link RhythmOverlayInflater} to
 * describe configuration of layers to be created and their resulting hierarchy.
 *
 * @author Paul Danyliuk
 */
public class LayerConfig {

    protected String mLayerType;
    protected int mIndent;
    protected ArgumentsBundle mArgumentsBundle;

    /**
     * Create layer config object for layer of given type, with known indent, and with pre-filled arguments bag
     *
     * @param layerType       spec layer type, used for appropriate factory lookup
     * @param indent          number of leading spaces in the config line, used to resolve layer hierarchy
     * @param argumentsBundle an object describing parsed layer configuration (arguments and values)
     */
    public LayerConfig(@NonNull String layerType, int indent, @NonNull ArgumentsBundle argumentsBundle) {
        mArgumentsBundle = argumentsBundle;
        mLayerType = layerType;
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

    /**
     * Get the configuration of this layer presented by an {@link ArgumentsBundle} object
     *
     * @return layer configuration bundle
     */
    public ArgumentsBundle getArgumentsBundle() {
        return mArgumentsBundle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        LayerConfig that = (LayerConfig) o;
        return mIndent == that.mIndent
                && mLayerType.equals(that.mLayerType)
                && mArgumentsBundle.equals(that.mArgumentsBundle);
    }

    @Override
    public int hashCode() {
        int result = mLayerType.hashCode();
        result = 31 * result + mIndent;
        result = 31 * result + mArgumentsBundle.hashCode();
        return result;
    }
}
