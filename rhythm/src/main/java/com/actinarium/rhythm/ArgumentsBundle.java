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

import android.annotation.SuppressLint;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An interface that defines an arguments container that will be used by {@link RhythmSpecLayerFactory} implementations
 * to query spec layer properties when creating new layers.
 *
 * @author Paul Danyliuk
 */
public interface ArgumentsBundle {

    int UNITS_NULL = -1;
    int UNITS_NUMBER = 0;
    int UNITS_PERCENT = 1;
    int UNITS_PX = 2;
    int UNITS_DP = 3;
    int UNITS_SP = 4;
    int UNITS_PT = 5;
    int UNITS_IN = 6;
    int UNITS_MM = 7;

    /**
     * Test if there's an argument with given key in the bundle, even if the value is <code>null</code>.
     *
     * @param key argument key
     * @return true if argument is present regardless of value
     */
    boolean hasArgument(String key);

    /**
     * Get argument value as a string.
     *
     * @param key argument key
     * @return argument value as a string, or null if the argument has null value or it cannot be retrieved
     */
    String getString(String key);

    /**
     * Get argument value as a string with fallback to default value if argument is missing.
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return argument value as raw string
     */
    String getString(String key, @Nullable String defaultValue);

    /**
     * Get argument value as integer with fallback to default value if argument is missing.
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return argument value parsed as integer
     */
    int getInt(String key, int defaultValue);

    /**
     * Get argument value as float with fallback to default value if argument is missing.
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return argument value parsed as float
     */
    float getFloat(String key, float defaultValue);

    /**
     * Get boolean argument, which can have implicit value.
     *
     * @param key          argument key
     * @param defaultValue value if argument is not present
     * @param nullValue    value if argument is present but the value is <code>null</code>
     * @return argument boolean value
     */
    boolean getBoolean(String key, boolean defaultValue, boolean nullValue);

    /**
     * Get argument value as color integer with fallback to default value if argument is missing.
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return argument value parsed as color integer
     */
    @ColorInt
    int getColor(String key, @ColorInt int defaultValue);

    /**
     * Get argument value as a gravity value (a combination of {@link Gravity} constants) with fallback to default value
     * if argument is missing.
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return gravity constant
     * @see #getEdgeAffinity(String, int)
     */
    int getGravity(String key, int defaultValue);

    /**
     * Get argument as an {@link EdgeAffinity} constant, which can be either {@link Gravity#TOP}, {@link Gravity#LEFT},
     * {@link Gravity#RIGHT}, or {@link Gravity#BOTTOM}, with fallback to default value if argument is missing or
     * invalid.
     *
     * @param key          argument key
     * @param defaultValue fallback value
     * @return gravity constant
     * @see #getGravity(String, int)
     */
    @EdgeAffinity
    int getEdgeAffinity(String key, @EdgeAffinity int defaultValue);

    /**
     * Get the units of a dimension argument.
     *
     * @param key argument key
     * @return dimension argument units, or {@link #UNITS_NULL} if the argument is null or missing
     */
    @DimensionUnits
    int getDimensionUnits(String key);

    /**
     * Get raw numeric value from dimension argument disregarding units and NOT performing any conversion to pixels.
     *
     * @param key          argument key
     * @param defaultValue fallback value in pixels
     * @return dimension argument raw value
     * @see #getDimensionPixelSize(String, int)
     * @see #getDimensionPixelOffset(String, int)
     */
    float getDimensionValue(String key, float defaultValue);

    /**
     * Get dimension argument value as pixels with possible fallback to default value if argument is missing or invalid.
     * Unlike {@link #getDimensionPixelSize(String, int)} and {@link #getDimensionPixelSize(String, int)}, this method
     * <b>doesn't</b> perform any rounding.
     *
     * @param key          argument key
     * @param defaultValue fallback value in pixels
     * @return argument value converted to pixels
     * @see #getDimensionPixelSize(String, int)
     * @see #getDimensionPixelOffset(String, int)
     */
    float getDimensionPixelExact(String key, float defaultValue);

    /**
     * Get dimension argument value as pixels with possible fallback to default value if argument is missing or invalid.
     * Unlike {@link #getDimensionPixelSize(String, int)}, this method is expected to round the raw value <b>down</b> to
     * the closest integer.
     *
     * @param key          argument key
     * @param defaultValue fallback value in pixels
     * @return argument value converted to pixels
     * @see #getDimensionPixelSize(String, int)
     * @see #getDimensionPixelExact(String, float)
     */
    int getDimensionPixelOffset(String key, int defaultValue);

    /**
     * Get dimension argument value as pixels with possible fallback to default value if argument is missing or invalid.
     * Unlike {@link #getDimensionPixelOffset(String, int)}}, this method is expected to round the raw value <b>up or
     * down</b> to the closest integer by common rules, and must ensure the result is at least 1px if original value is
     * not 0.
     *
     * @param key          argument key
     * @param defaultValue fallback value in pixels
     * @return argument value converted to pixels
     * @see #getDimensionPixelOffset(String, int)
     * @see #getDimensionPixelExact(String, float)
     */
    int getDimensionPixelSize(String key, int defaultValue);

    /**
     * Get display metrics associated with this arguments bundle.
     * <p>
     * todo: this shouldn't be part of the arguments bundle
     *
     * @return display metrics object
     */
    DisplayMetrics getDisplayMetrics();

    /**
     * Type definition for dimension argument units
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({UNITS_NULL, UNITS_NUMBER, UNITS_PERCENT, UNITS_PX, UNITS_DP, UNITS_SP, UNITS_MM, UNITS_PT, UNITS_IN})
    public @interface DimensionUnits {
    }

    /**
     * Type definition for screen edge that a keyline or pattern must be attached to. Used by some layers
     */
    @SuppressLint("RtlHardcoded")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Gravity.TOP, Gravity.BOTTOM, Gravity.LEFT, Gravity.RIGHT, Gravity.NO_GRAVITY})
    @interface EdgeAffinity {
    }
}
