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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.RhythmSpecLayer.Edge;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A container for arguments parsed from layer configuration line. The arguments are stored as a String-&gt;String
 * key-value map and parsed as required types when requested by <code>get*()</code> methods, so if you need to request
 * the same parameter multiple times, storing it in a variable may be a good idea.
 *
 * @author Paul Danyliuk
 */
public class LayerConfig {

    public static final int UNITS_NULL = -1;
    public static final int UNITS_NUMBER = 0;
    public static final int UNITS_PERCENT = 1;
    public static final int UNITS_PX = 2;
    public static final int UNITS_DP = 3;
    public static final int UNITS_SP = 4;
    public static final int UNITS_PT = 5;
    public static final int UNITS_IN = 6;
    public static final int UNITS_MM = 7;

    protected String mLayerType;
    protected int mIndent;
    protected Map<String, String> mArguments;
    protected DisplayMetrics mMetrics;

    protected static Pattern DIMEN_VALUE_PATTERN = Pattern.compile("^-?\\d*\\.?\\d+");

    /**
     * Create layer config object for layer of given type, with known indent, and with pre-filled arguments bag
     *
     * @param layerType spec layer type, used for appropriate factory lookup
     * @param indent    number of leading spaces in the config line, used to resolve layer hierarchy
     * @param arguments bag of raw arguments parsed from configuration string
     */
    public LayerConfig(@NonNull String layerType, int indent, @NonNull Map<String, String> arguments) {
        mLayerType = layerType;
        mIndent = indent;
        mArguments = arguments;
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
     * Get display metrics injected in this config
     *
     * @return display metrics object
     */
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
     * Put a key without value into the bag
     *
     * @param key Key
     */
    public void put(String key) {
        mArguments.put(key, null);
    }

    /**
     * Test if there's an argument with given key in the bag
     *
     * @param key argument key
     * @return true if argument is present regardless of value
     */
    public boolean hasArgument(String key) {
        return mArguments.containsKey(key);
    }

    /**
     * Get argument value as raw string
     *
     * @param key argument key
     * @return argument value as raw string
     */
    public String getString(String key) {
        return mArguments.get(key);
    }

    /**
     * Get argument value as raw string with possible fallback to default value
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return argument value as raw string
     */
    public String getString(String key, @Nullable String defaultValue) {
        return mArguments.containsKey(key) ? mArguments.get(key) : defaultValue;
    }

    /**
     * Get argument value as integer with possible fallback to default value
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return argument value parsed as integer
     */
    public int getInt(String key, int defaultValue) {
        return mArguments.containsKey(key) ? Integer.parseInt(mArguments.get(key)) : defaultValue;
    }

    /**
     * Get argument value as float with possible fallback to default value
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return argument value parsed as float
     */
    public float getFloat(String key, float defaultValue) {
        return mArguments.containsKey(key) ? Float.parseFloat(mArguments.get(key)) : defaultValue;
    }

    /**
     * Get boolean argument, which can have implicit value
     *
     * @param key          argument key
     * @param defaultValue value if argument is not present
     * @param nullValue    value if argument is present but doesn't contain value (i.e. not <code>arg=true</code> but
     *                     simply <code>arg</code>)
     * @return argument boolean value
     */
    public boolean getBoolean(String key, boolean defaultValue, boolean nullValue) {
        if (mArguments.containsKey(key)) {
            final String value = mArguments.get(key);
            return value == null ? nullValue : Boolean.parseBoolean(value);
        } else {
            return defaultValue;
        }
    }

    /**
     * Get argument value as color integer with possible fallback to default value
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return argument value parsed as color integer
     */
    @ColorInt
    public int getColor(String key, @ColorInt int defaultValue) {
        return mArguments.containsKey(key) ? Color.parseColor(mArguments.get(key)) : defaultValue;
    }

    /**
     * Determine gravity constant (a combination of {@link Gravity} constants) from a string like <code>top|left</code>,
     * with fallback to default value.
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return gravity constant
     * @see #getLayerGravity(String, int)
     */
    @SuppressLint("RtlHardcoded")
    public int getGravity(String key, int defaultValue) {
        String gravityArg = mArguments.get(key);
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

    /**
     * Get argument as layer gravity constant, which is either <code>top</code>, <code>bottom</code>, <code>left</code>,
     * or <code>right</code>, with fallback to default value if argument is missing or invalid.
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return gravity constant
     * @see #getGravity(String, int)
     */
    @SuppressLint("RtlHardcoded")
    @Edge
    public int getLayerGravity(String key, @Edge int defaultValue) {
        String gravityArg = mArguments.get(key);
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
     * Determine units of dimension argument. <b>Note:</b> this is a very crude implementation relying only on the check
     * of trailing string characters (i.e. whether the string ends with "dp", "px", "%" etc). However, for
     * development-time library and assuming that developers are not their own enemies, that should be fine.
     *
     * @param key argument key
     * @return dimension argument units
     */
    @DimensionUnits
    public int getDimensionUnits(String key) {
        String value = mArguments.get(key);
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
     * Extract numeric value from dimension argument disregarding units and NOT performing any conversion to pixels.
     * Normally you should use one of the "pixel" methods instead.
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return dimension argument raw value
     * @see #getDimensionPixelSize(String, int)
     * @see #getDimensionPixelOffset(String, int)
     * @see #getDimensionPixelRaw(float, int, DisplayMetrics)
     */
    public float getDimensionValueRaw(String key, float defaultValue) {
        String value = mArguments.get(key);
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
     * Get dimension argument value as pixels with possible fallback to default value. Unlike {@link
     * #getDimensionPixelSize(String, int)} and {@link #getDimensionPixelSize(String, int)}, this method <b>doesn't</b>
     * perform any rounding. <b>Note:</b> for resolving complex dimension types, {@link DisplayMetrics} must be injected
     * into this config
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return argument value converted to pixels
     * @see #getDimensionPixelSize(String, int)
     * @see #getDimensionPixelOffset(String, int)
     * @see #getDimensionPixelRaw(float, int, DisplayMetrics)
     */
    public float getDimensionPixelExact(String key, float defaultValue) {
        @DimensionUnits int units = getDimensionUnits(key);
        if (units == UNITS_NULL) {
            return defaultValue;
        }
        float rawValue = getDimensionValueRaw(key, defaultValue);
        return getDimensionPixelRaw(rawValue, units, mMetrics);
    }

    /**
     * Get dimension argument value as pixels with possible fallback to default value. Unlike {@link
     * #getDimensionPixelSize(String, int)}, this method rounds converted value <b>down</b> to the closest integer.
     * <b>Note:</b> for resolving complex dimension types, {@link DisplayMetrics} must be injected into this config
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return argument value converted to pixels
     * @see #getDimensionPixelSize(String, int)
     * @see #getDimensionPixelExact(String, float)
     * @see #getDimensionPixelRaw(float, int, DisplayMetrics)
     */
    public int getDimensionPixelOffset(String key, int defaultValue) {
        @DimensionUnits int units = getDimensionUnits(key);
        if (units == UNITS_NULL) {
            return defaultValue;
        }
        float rawValue = getDimensionValueRaw(key, defaultValue);
        return (int) getDimensionPixelRaw(rawValue, units, mMetrics);
    }

    /**
     * Get dimension argument value as pixels with possible fallback to default value. Unlike {@link
     * #getDimensionPixelOffset(String, int)}}, this method rounds converted value <b>up or down</b> to the closest
     * integer by common rules, and ensures the result is at least 1px if original value is not 0. <b>Note:</b> for
     * resolving complex dimension types, {@link DisplayMetrics} must be injected into this config
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return argument value converted to pixels
     * @see #getDimensionPixelOffset(String, int)
     * @see #getDimensionPixelExact(String, float)
     * @see #getDimensionPixelRaw(float, int, DisplayMetrics)
     */
    public int getDimensionPixelSize(String key, int defaultValue) {
        @DimensionUnits int units = getDimensionUnits(key);
        float rawValue = getDimensionValueRaw(key, defaultValue);
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
        LayerConfig that = (LayerConfig) o;
        return mArguments.equals(that.mArguments) && mLayerType.equals(that.mLayerType);
    }

    @Override
    public int hashCode() {
        int result = mLayerType.hashCode();
        result = 31 * result + mArguments.hashCode();
        return result;
    }

    /**
     * Dimension argument units
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UNITS_NULL, UNITS_NUMBER, UNITS_PERCENT, UNITS_PX, UNITS_DP, UNITS_SP, UNITS_MM, UNITS_PT, UNITS_IN})
    public @interface DimensionUnits {
    }

}
