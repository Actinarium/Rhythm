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
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A container for arguments parsed from layer configuration line
 *
 * @author Paul Danyliuk
 */
public class Arguments {

    public static final int TYPE_NULL = -1;
    public static final int TYPE_INTEGER = 0;
    public static final int TYPE_PERCENT = 1;
    public static final int TYPE_PX = 2;
    public static final int TYPE_DP = 3;
    public static final int TYPE_SP = 4;
    public static final int TYPE_PT = 5;
    public static final int TYPE_IN = 6;
    public static final int TYPE_MM = 7;

    private String mName;
    private Map<String, String> mArguments;
    private DisplayMetrics mMetrics;

    private static Pattern DIMEN_VALUE_PATTERN = Pattern.compile("^\\d*\\.?\\d+");

    public Arguments() {
        mArguments = new ArrayMap<>();
    }

    public Arguments(int initialCapacity) {
        mArguments = new ArrayMap<>(initialCapacity);
    }

    public void setDisplayMetrics(DisplayMetrics metrics) {
        mMetrics = metrics;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    /**
     * Put a key-value pair into the arguments map
     *
     * @param key   Key
     * @param value Value
     */
    public void put(String key, String value) {
        mArguments.put(key, value);
    }

    /**
     * Put a key without value into the map
     *
     * @param key Key
     */
    public void put(String key) {
        mArguments.put(key, null);
    }

    public boolean hasArgument(String key) {
        return mArguments.containsKey(key);
    }

    public String getString(String key) {
        return mArguments.get(key);
    }

    public String getString(String key, @Nullable String defaultValue) {
        return mArguments.containsKey(key) ? mArguments.get(key) : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        return mArguments.containsKey(key) ? Integer.parseInt(mArguments.get(key)) : defaultValue;
    }

    public float getFloat(String key, float defaultValue) {
        return mArguments.containsKey(key) ? Float.parseFloat(mArguments.get(key)) : defaultValue;
    }

    /**
     * Get boolean argument, which can have implicit value
     *
     * @param key          argument key
     * @param defaultValue value if argument is not present
     * @param nullValue    value if argument is present but doesn't contain value (HTTP query string style)
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

    public int getColor(String key, int defaultValue) {
        return mArguments.containsKey(key) ? Color.parseColor(mArguments.get(key)) : defaultValue;
    }

    /**
     * Very crude way to determine dimension type, checking whether the string ends with dimension characters. However,
     * for development-time library assuming developers are not their own enemies, that should be fine.
     *
     * @param key argument key
     * @return argument dimension type
     */
    public
    @ValueType
    int getDimensionType(String key) {
        String value = mArguments.get(key);
        if (value == null) {
            return TYPE_NULL;
        } else if (value.endsWith("dp") || value.endsWith("dip")) {
            return TYPE_DP;
        } else if (value.endsWith("px")) {
            return TYPE_PX;
        } else if (value.endsWith("%")) {
            return TYPE_PERCENT;
        } else if (value.endsWith("sp")) {
            return TYPE_SP;
        } else if (value.endsWith("pt")) {
            return TYPE_PT;
        } else if (value.endsWith("in")) {
            return TYPE_IN;
        } else if (value.endsWith("mm")) {
            return TYPE_MM;
        } else {
            // assume integer, try to parse as integer
            return TYPE_INTEGER;
        }
    }

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

    public int getDimensionPixelSize(String key, int defaultValue) {
        @ValueType int type = getDimensionType(key);
        if (type == TYPE_NULL) {
            return defaultValue;
        }
        float rawValue = getDimensionValueRaw(key, defaultValue);
        float result = getDimensionPixelRaw(type, rawValue, mMetrics);

        final int res = (int) (result + 0.5f);
        if (res != 0) { return res; }
        if (rawValue == 0) { return 0; }
        if (rawValue > 0) { return 1; }
        return -1;
    }

    public int getDimensionPixelOffset(String key, int defaultValue) {
        @ValueType int type = getDimensionType(key);
        if (type == TYPE_NULL) {
            return defaultValue;
        }
        float rawValue = getDimensionValueRaw(key, defaultValue);
        return (int) getDimensionPixelRaw(type, rawValue, mMetrics);
    }

    @SuppressLint("SwitchIntDef")
    public static float getDimensionPixelRaw(@ValueType int type, float value, DisplayMetrics metrics) {
        switch (type) {
            case TYPE_DP:
                if (metrics == null) {
                    throw new IllegalArgumentException("Need metrics for dp->px conversion");
                }
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics);
            case TYPE_PX:
            case TYPE_PERCENT:
            case TYPE_INTEGER:
                return value;
            case TYPE_SP:
                if (metrics == null) {
                    throw new IllegalArgumentException("Need metrics for sp->px conversion");
                }
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, metrics);
            case TYPE_PT:
                if (metrics == null) {
                    throw new IllegalArgumentException("Need metrics for pt->px conversion");
                }
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, value, metrics);
            case TYPE_IN:
                if (metrics == null) {
                    throw new IllegalArgumentException("Need metrics for in->px conversion");
                }
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, value, metrics);
            case TYPE_MM:
                if (metrics == null) {
                    throw new IllegalArgumentException("Need metrics for mm->px conversion");
                }
                return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, value, metrics);
            default:
                return 0;
        }
    }

    /**
     * Types of dimension arguments
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_NULL, TYPE_INTEGER, TYPE_PERCENT, TYPE_PX, TYPE_DP, TYPE_SP, TYPE_MM, TYPE_PT, TYPE_IN})
    @interface ValueType {
    }


}
