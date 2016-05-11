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

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import com.actinarium.rhythm.ArgumentsBundle;
import com.actinarium.rhythm.RhythmSpecLayer;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A basic implementation of {@link ArgumentsBundle} used by {@link RhythmOverlayInflater}. For simplicity, it stores
 * all arguments as a String-&gt;String key-value map and parses them into required types when accessed by respective
 * getter methods (also meaning it fails lazily). Does not cache parsing results.</p><p>Additionally to the arguments
 * bag, objects of this class store extra metadata about the layer being inflated, such as the layer type and its indent
 * (leading spaces in the config) to properly nest it within the overlay being inflated.</p>
 *
 * @author Paul Danyliuk
 */
public class SimpleLayerConfig implements ArgumentsBundle {

    protected String mLayerType;
    protected int mIndent;
    protected Map<String, String> mArguments;
    protected Map<String, String> mVariables;
    protected DisplayMetrics mMetrics;

    protected static Pattern DIMEN_VALUE_PATTERN = Pattern.compile("^-?\\d*\\.?\\d+");

    /**
     * Create layer config object for layer of given type, with known indent, and with pre-filled arguments bag
     *
     * @param layerType spec layer type, used for appropriate factory lookup
     * @param indent    number of leading spaces in the config line, used to resolve layer hierarchy
     * @param arguments bag of raw arguments parsed from configuration string
     * @param variables bag of variables to be used in arguments. Keys must be prefixed with <code>@</code>.
     */
    public SimpleLayerConfig(@NonNull String layerType, int indent, @NonNull Map<String, String> arguments, @NonNull Map<String, String> variables) {
        mLayerType = layerType;
        mIndent = indent;
        mArguments = arguments;
        mVariables = variables;
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
     * Inject display metrics so that complex dimensions (dp, sp etc) can be resolved
     *
     * @param metrics display metrics object
     */
    public void setDisplayMetrics(DisplayMetrics metrics) {
        mMetrics = metrics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DisplayMetrics getDisplayMetrics() {
        return mMetrics;
    }

    /**
     * Put a key-value pair into the arguments bag
     *
     * @param key   Key
     * @param value Value
     */
    public void put(String key, String value) {
        mArguments.put(key, value);
    }

    /**
     * Put a key without value (<code>null</code> value) into the bag
     *
     * @param key Key
     * @see #getBoolean(String, boolean, boolean)
     */
    public void put(String key) {
        mArguments.put(key, null);
    }

    @Override
    public boolean hasArgument(String key) {
        return mArguments.containsKey(key);
    }

    /**
     * {@inheritDoc} Will return the raw string as put in the arguments map by the inflater. If the stored value is a
     * variable (starting with <code>@</code>), returns the value of the variable by provided reference name.
     */
    @Override
    public String getString(String key) {
        String value = mArguments.get(key);
        if (value != null && value.length() > 0 && value.charAt(0) == '@') {
            value = mVariables.get(value);
        }
        return value;
    }

    /**
     * {@inheritDoc} Will return the raw string as put in the arguments map by the inflater.
     */
    @Override
    public String getString(String key, @Nullable String defaultValue) {
        String rawValue = getString(key);
        return rawValue != null ? rawValue : defaultValue;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String rawValue = getString(key);
        return rawValue != null ? Integer.parseInt(rawValue) : defaultValue;
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        String rawValue = getString(key);
        return rawValue != null ? Float.parseFloat(rawValue) : defaultValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue, boolean nullValue) {
        if (mArguments.containsKey(key)) {
            String rawValue = getString(key);
            return rawValue == null ? nullValue : Boolean.parseBoolean(rawValue);
        } else {
            return defaultValue;
        }
    }

    @Override
    @ColorInt
    public int getColor(String key, @ColorInt int defaultValue) {
        String rawValue = getString(key);
        return rawValue != null ? Color.parseColor(rawValue) : defaultValue;
    }

    /**
     * {@inheritDoc} Does a quick and rough parsing of the raw string for containing constant words like
     * <code>top</code> or <code>center_vertical</code>
     */
    @Override
    @SuppressLint("RtlHardcoded")
    public int getGravity(String key, int defaultValue) {
        String gravityArg = getString(key);
        if (gravityArg == null) {
            return defaultValue;
        } else if (gravityArg.equals("center")) {
            return Gravity.CENTER;
        } else if (gravityArg.equals("fill")) {
            return Gravity.FILL;
        } else {
            // supported options
            int gravity = 0;
            if (gravityArg.contains("top")) {
                gravity |= Gravity.TOP;
            }
            if (gravityArg.contains("bottom")) {
                gravity |= Gravity.BOTTOM;
            }
            if (gravityArg.contains("center_vertical")) {
                gravity |= Gravity.CENTER_VERTICAL;
            }
            if (gravityArg.contains("fill_vertical")) {
                gravity |= Gravity.FILL_VERTICAL;
            }
            if (gravityArg.contains("left")) {
                gravity |= Gravity.LEFT;
            }
            if (gravityArg.contains("right")) {
                gravity |= Gravity.RIGHT;
            }
            if (gravityArg.contains("center_horizontal")) {
                gravity |= Gravity.CENTER_HORIZONTAL;
            }
            if (gravityArg.contains("fill_horizontal")) {
                gravity |= Gravity.FILL_HORIZONTAL;
            }
            return gravity;
        }
    }

    @Override
    @SuppressLint("RtlHardcoded")
    @EdgeAffinity
    public int getEdgeAffinity(String key, @EdgeAffinity int defaultValue) {
        String gravityArg = getString(key);
        if ("top".equals(gravityArg)) {
            return Gravity.TOP;
        } else if ("left".equals(gravityArg)) {
            return Gravity.LEFT;
        } else if ("right".equals(gravityArg)) {
            return Gravity.RIGHT;
        } else if ("bottom".equals(gravityArg)) {
            return Gravity.BOTTOM;
        } else {
            return defaultValue;
        }
    }

    /**
     * {@inheritDoc} <b>Note:</b> this is a very crude implementation relying only on the check of trailing string
     * characters (i.e. whether the string ends with "dp", "px", "%" etc). However, for development-time library and
     * assuming that developers are not their own enemies, that should be fine.
     */
    @Override
    @DimensionUnits
    public int getDimensionUnits(String key) {
        String value = getString(key);
        if (value == null) {
            return UNITS_NULL;
        } else if (value.endsWith("dp") || value.endsWith("dip")) {
            return UNITS_DP;
        } else if (value.endsWith("px")) {
            return UNITS_PX;
        } else if (value.endsWith("%")) {
            return UNITS_PERCENT;
        } else if (value.endsWith("sp")) {
            return UNITS_SP;
        } else if (value.endsWith("pt")) {
            return UNITS_PT;
        } else if (value.endsWith("in")) {
            return UNITS_IN;
        } else if (value.endsWith("mm")) {
            return UNITS_MM;
        } else {
            // assume raw number, try to parse as float
            return UNITS_NUMBER;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see #getDimensionPixelRaw(float, int, DisplayMetrics)
     */
    @Override
    public float getDimensionValue(String key, float defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        Matcher matcher = DIMEN_VALUE_PATTERN.matcher(value);
        if (matcher.find()) {
            return Float.parseFloat(matcher.group());
        } else {
            return defaultValue;
        }
    }

    /**
     * {@inheritDoc} <b>Note:</b> this method requires that {@link DisplayMetrics} object is injected in this config.
     *
     * @see #getDimensionPixelRaw(float, int, DisplayMetrics)
     */
    @Override
    public float getDimensionPixelExact(String key, float defaultValue) {
        @DimensionUnits int units = getDimensionUnits(key);
        if (units == UNITS_NULL) {
            return defaultValue;
        }
        float rawValue = getDimensionValue(key, defaultValue);
        return getDimensionPixelRaw(rawValue, units, mMetrics);
    }

    /**
     * {@inheritDoc} <b>Note:</b> this method requires that {@link DisplayMetrics} object is injected in this config.
     *
     * @see #getDimensionPixelRaw(float, int, DisplayMetrics)
     */
    @Override
    public int getDimensionPixelOffset(String key, int defaultValue) {
        @DimensionUnits int units = getDimensionUnits(key);
        if (units == UNITS_NULL) {
            return defaultValue;
        }
        float rawValue = getDimensionValue(key, defaultValue);
        return (int) getDimensionPixelRaw(rawValue, units, mMetrics);
    }

    /**
     * {@inheritDoc} <b>Note:</b> this method requires that {@link DisplayMetrics} object is injected in this config.
     *
     * @see #getDimensionPixelRaw(float, int, DisplayMetrics)
     */
    @Override
    public int getDimensionPixelSize(String key, int defaultValue) {
        @DimensionUnits int units = getDimensionUnits(key);
        float rawValue = getDimensionValue(key, defaultValue);
        float result = getDimensionPixelRaw(rawValue, units, mMetrics);

        final int res = (int) (result + 0.5f);
        if (res != 0) { return res; }
        if (rawValue == 0) { return 0; }
        if (rawValue > 0) { return 1; }
        return defaultValue;
    }

    /**
     * Convert complex dimension value of provided units into pixels.
     *
     * @param value   raw dimension value, e.g. <code>24f</code>
     * @param units   dimension units, one of {@link #UNITS_PX}, {@link #UNITS_DP}, {@link #UNITS_SP}, {@link
     *                #UNITS_PT}, {@link #UNITS_IN}, {@link #UNITS_MM}, {@link #UNITS_NUMBER}, {@link #UNITS_NULL}, or
     *                {@link #UNITS_PERCENT}
     * @param metrics display metrics to convert complex dimension types that depend on density (dp, sp etc) into
     *                pixels, can be null if type is one of {@link #UNITS_PX}, {@link #UNITS_PERCENT}, {@link
     *                #UNITS_NUMBER}, or {@link #UNITS_NULL}
     * @return dimension value in pixels
     */
    public static float getDimensionPixelRaw(float value, @DimensionUnits int units, DisplayMetrics metrics) {
        switch (units) {
            case UNITS_DP:
                if (metrics == null) {
                    throw new IllegalArgumentException("Need metrics for dp->px conversion");
                }
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics);
            case UNITS_PX:
            case UNITS_PERCENT:
            case UNITS_NUMBER:
            case UNITS_NULL:
                return value;
            case UNITS_SP:
                if (metrics == null) {
                    throw new IllegalArgumentException("Need metrics for sp->px conversion");
                }
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, metrics);
            case UNITS_PT:
                if (metrics == null) {
                    throw new IllegalArgumentException("Need metrics for pt->px conversion");
                }
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, value, metrics);
            case UNITS_IN:
                if (metrics == null) {
                    throw new IllegalArgumentException("Need metrics for in->px conversion");
                }
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, value, metrics);
            case UNITS_MM:
                if (metrics == null) {
                    throw new IllegalArgumentException("Need metrics for mm->px conversion");
                }
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, value, metrics);
            default:
                return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        SimpleLayerConfig that = (SimpleLayerConfig) o;
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
