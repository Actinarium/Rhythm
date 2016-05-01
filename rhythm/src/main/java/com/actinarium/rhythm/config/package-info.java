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

/**
 * The {@link com.actinarium.rhythm.config} package contains the basic inflation mechanism that converts declarative
 * configuration as documented <a href="https://github.com/Actinarium/Rhythm/wiki/Declarative-configuration">here</a>
 * into a hierarchy of Java objects, namely {@link com.actinarium.rhythm.RhythmOverlay} classes that are root objects
 * for rendering overlays. The provided implementation is a reference one &mdash; developers are welcome to implement
 * their own inflation mechanisms (e.g. different lexers, parse-time validation, transformations, XML/JSON/YAML support
 * etc) should they need something different.
 */
package com.actinarium.rhythm.config;