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

package com.actinarium.rhythm.common;

/**
 * <p>A runtime exception to be thrown when there is an error inflating declarative configuration.</p><p>Unlike the
 * usual exceptions, {@link RhythmInflationException} <b>must</b> either have a message or an error code, or both. The
 * error code should identify an exception cause and is used for localization. Additionally a set of objects can be
 * passed as arguments to use when formatting the localized message.</p>
 *
 * @author Paul Danyliuk
 */
public class RhythmInflationException extends RuntimeException {

    public static final int ERROR_GENERIC = 0;

    /**
     * Configuration file is malformed. Possibly there's no empty line between individual overlays or multiple headers
     * in one block.<br>Args: [0] - Offending line
     */
    public static final int ERROR_MALFORMED_LIST_SYNTAX = 1;

    /**
     * Variable declaration is malformed.<br>Args: [0] - Offending line; [1] - Actual declaration
     */
    public static final int ERROR_MALFORMED_VARIABLE_SYNTAX = 2;

    /**
     * Variable declared in illegal place.<br>Args: [0] - Offending line
     */
    public static final int ERROR_UNEXPECTED_VARIABLE_DECLARATION = 3;

    /**
     * Title declared in illegal place.<br>Args: [0] - Offending line
     */
    public static final int ERROR_UNEXPECTED_TITLE_DECLARATION = 4;

    /**
     * The layer type is unknown to inflater (invalid or not registered).<br>Args: [0] - Layer type
     */
    public static final int ERROR_UNKNOWN_LAYER_TYPE = 10;

    /**
     * A mandatory argument is missing.<br>Args: [0] - Layer type; [1] - Argument; [2] - Expected type
     */
    public static final int ERROR_ARGUMENT_MISSING = 11;

    /**
     * A mandatory argument is missing or is not one of accepted values.<br>Args: [0] - Layer type; [1] - Argument; [2]
     * - Actual value; [3] - Accepted values
     */
    public static final int ERROR_ARGUMENT_MISSING_OR_NOT_ONE_OF = 12;

    /**
     * A mandatory argument is missing or &lt;= zero.<br>Args: [0] - Layer type; [1] - Argument; [2] - Example
     */
    public static final int ERROR_ARGUMENT_MISSING_OR_NOT_POSITIVE = 13;

    /**
     * A mandatory argument is missing or not of required type.<br>Args: [0] - Layer type; [1] - Argument; [2] -
     * Expected type; [3] - Example
     */
    public static final int ERROR_ARGUMENT_MISSING_OR_NOT_EXPECTED_TYPE = 14;

    private int mErrorCode;
    private Object[] mArgs;

    public RhythmInflationException(int errorCode, Object... args) {
        super();
        mErrorCode = errorCode;
        mArgs = args;
    }

    public RhythmInflationException(int errorCode, Throwable throwable, Object... args) {
        super(throwable);
        mErrorCode = errorCode;
        mArgs = args;
    }

    public RhythmInflationException(int errorCode, String detailMessage, Object... args) {
        super(detailMessage);
        mErrorCode = errorCode;
        mArgs = args;
    }

    public RhythmInflationException(int errorCode, String detailMessage, Throwable throwable, Object... args) {
        super(detailMessage, throwable);
        mErrorCode = errorCode;
        mArgs = args;
    }

    /**
     * Get the error code of this exception. This code then can be used to render a localized message if Rhythm is used
     * as a dependency of <a href="https://play.google.com/store/apps/details?id=com.actinarium.materialcue">some other
     * apps</a>.
     *
     * @return error code integer, see <code>ERROR_*</code> constants in {@link RhythmInflationException} class.
     */
    public int getErrorCode() {
        return mErrorCode;
    }

    /**
     * Get the array of arguments carried by this exception. These objects can be used to format a localized error
     * message
     *
     * @return array of objects, possibly empty
     */
    public Object[] getArgs() {
        return mArgs;
    }
}
