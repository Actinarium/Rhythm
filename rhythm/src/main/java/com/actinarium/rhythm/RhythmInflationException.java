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

import android.support.annotation.IntRange;

/**
 * A runtime exception to be thrown when there is an error inflating declarative configuration, usually because of
 * syntax error or violated argument value constraints.
 *
 * @author Paul Danyliuk
 */
public class RhythmInflationException extends RuntimeException {

    private int mLineNumber = 0;

    public RhythmInflationException() {
    }

    public RhythmInflationException(String detailMessage) {
        super(detailMessage);
    }

    public RhythmInflationException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RhythmInflationException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Set the index of the line (0-based) where the error happened. If set, text "Line {x+1}: " will be prepended to
     * error message
     *
     * @param index index of the line where error happened, zero-based
     * @return this for chaining
     */
    public RhythmInflationException setLineNumber(@IntRange(from = 0) int index) {
        mLineNumber = index + 1;
        return this;
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        return mLineNumber == 0 ? message : "Line " + mLineNumber + ": " + message;
    }
}
