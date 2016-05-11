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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic implementation of {@link ArgumentsBundle}, which stores all arguments simply as a String-&gt;String key-value
 * map and parses them into required types when accessed by respective getter methods (meaning it also fails lazily).
 * Does not cache parsing results, so if the same arguments are requested multiple times, it may be a good idea to query
 * them once and store the result in a variable.
 *
 * @author Paul Danyliuk
 */
public class SimpleArgumentsBundle implements ArgumentsBundle {

    protected Map<String, String> mArguments;
    protected DisplayMetrics mMetrics;

    protected static Pattern DIMEN_VALUE_PATTERN = Pattern.compile("^-?\\d*\\.?\\d+");

    public SimpleArgumentsBundle(@NonNull Map<String, String> arguments, @NonNull DisplayMetrics metrics) {
        mArguments = arguments;
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
     * {@inheritDoc} Will return the raw string as put in the arguments map by the inflater.
     */
    @Override
    public String getString(String key) {
        return mArguments.get(key);
    }

    /**
     * {@inheritDoc} Will return the raw string as put in the arguments map by the inflater.
     */
    @Override
    public String getString(String key, @Nullable String defaultValue) {
        // todo: here and everywhere - DON'T RETRIEVE VALUE VIA GETSTRING!!! Refactor ASAP
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
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics);
            case UNITS_PX:
            case UNITS_PERCENT:
            case UNITS_NUMBER:
            case UNITS_NULL:
                return value;
            case UNITS_SP:
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, metrics);
            case UNITS_PT:
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, value, metrics);
            case UNITS_IN:
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, value, metrics);
            case UNITS_MM:
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, value, metrics);
            default:
                return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        return mArguments.equals(((SimpleArgumentsBundle) o).mArguments);
    }

    @Override
    public int hashCode() {
        return mArguments.hashCode();
    }
}
