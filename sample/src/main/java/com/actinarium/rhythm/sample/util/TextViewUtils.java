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

import android.graphics.Paint;
import android.widget.TextView;

/**
 * @author Paul Danyliuk
 */
public class TextViewUtils {

    public static void setLeading(TextView view, int step, int leading) {
        // This is to make the behavior more deterministic: remove extra top/bottom padding
        view.setIncludeFontPadding(false);

        // Get font metrics and calculate required inter-line extra
        Paint.FontMetricsInt metrics = view.getPaint().getFontMetricsInt();
        final int extra = leading - metrics.descent + metrics.ascent;
        view.setLineSpacing(extra, 1);

        // Determine minimum required top extra so that the view lands on the grid
        final int alignTopExtra = (step + metrics.ascent % step) % step;
        // Determine minimum required bottom extra so that view bounds are aligned with the grid
        final int alignBottomExtra = (step - metrics.descent % step) % step;

        view.setPadding(view.getPaddingLeft(), view.getPaddingTop() + alignTopExtra, view.getPaddingRight(), view.getPaddingBottom() + alignBottomExtra);
    }

}
