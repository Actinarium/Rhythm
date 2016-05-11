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

package com.actinarium.rhythm.internal;

import java.io.IOException;

/**
 * A runtime counterpart of {@link IOException} thrown when the library cannot operate on provided data stream.
 *
 * @author Paul Danyliuk
 */
public class RuntimeIOException extends RuntimeException {

    public RuntimeIOException() {
        super();
    }

    public RuntimeIOException(String detailMessage) {
        super(detailMessage);
    }

    public RuntimeIOException(Throwable throwable) {
        super(throwable);
    }

    public RuntimeIOException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
