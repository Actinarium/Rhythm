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

package com.actinarium.rhythm.sample.util;

import android.text.SpannableStringBuilder;

/**
 * @author Paul Danyliuk
 */
public final class ViewUtils {

    private ViewUtils() {}

    public static CharSequence makeBulletList(int bulletRadius, int bulletCenterX, int leadingMargin, CharSequence... items) {
        final SpannableStringBuilder builder = new SpannableStringBuilder();
        int spanStart = 0;
        int spanEnd;
        for (int i = 0, itemsLength = items.length; i < itemsLength; i++) {
            CharSequence item = items[i];
            builder.append(item);
            if (i != itemsLength - 1) {
                builder.append("\n");
            }
            spanEnd = builder.length();
            builder.setSpan(new BulletSpan(bulletRadius, bulletCenterX, leadingMargin), spanStart, spanEnd, 0);
            spanStart = spanEnd;
        }
        return builder;
    }

}
