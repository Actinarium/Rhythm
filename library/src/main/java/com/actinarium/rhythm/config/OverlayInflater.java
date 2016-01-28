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

import com.actinarium.rhythm.RhythmOverlay;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Inflates a {@link RhythmOverlay} from text configuration
 *
 * @author Paul Danyliuk
 */
public class OverlayInflater {

    private static final Pattern ARGUMENTS_PATTERN = Pattern.compile("([^ =]+)(?:=([^ ]+))?");

    public static Arguments parseLayerLine(String line) {
        // Let's just iterate over the first chars to get indent and layer type, and parse the arguments with regex
        int i = 0;
        int length = line.length();

        // 1. indentiation
        while (i < length && line.charAt(i) == ' ') {
            i++;
        }
        final int spaces = i;

        // 2. layer class name
        while (i < length && line.charAt(i) != ' ') {
            i++;
        }
        final String name = line.substring(spaces, i);

        final int anticipatedCapacity = (length - i) / 12 + 1;
        Arguments arguments = new Arguments(anticipatedCapacity);
        arguments.setName(name);

        // todo: instead of regex, consider parsing linearly for efficiency
        Matcher matcher = ARGUMENTS_PATTERN.matcher(line.substring(i));
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            arguments.put(key, value);
        }

        return arguments;
    }

}
