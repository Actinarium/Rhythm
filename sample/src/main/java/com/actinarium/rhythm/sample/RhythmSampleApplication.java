/*
 * Copyright (C) 2015 Actinarium
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

package com.actinarium.rhythm.sample;

import android.app.Application;
import android.graphics.Color;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmOverlay;
import com.actinarium.rhythm.RhythmGroup;
import com.actinarium.rhythm.RhythmControl;
import com.actinarium.rhythm.spec.DimensionsLabel;
import com.actinarium.rhythm.spec.GridLines;
import com.actinarium.rhythm.spec.Guide;

/**
 * <p></p>
 *
 * @author Paul Danyliuk
 * @version $Id$
 */
public class RhythmSampleApplication extends Application implements RhythmControl.Host {

    private RhythmControl mRhythmControl;

    @Override
    public void onCreate() {
        super.onCreate();

        mRhythmControl = new RhythmControl(this);
        final float density = getResources().getDisplayMetrics().density;

        final int step = (int) (density * 8);
        mRhythmControl.makeGroup("Activity background")
                .addOverlay(new RhythmOverlay("8dp grid")
                        .addLayer(new GridLines(Gravity.TOP, step))
                        .addLayer(new GridLines(Gravity.LEFT, step)))
                .addOverlay(new RhythmOverlay("Baseline grid")
                        .addLayer(new GridLines(Gravity.TOP, step / 2).color(GridLines.DEFAULT_BASELINE_COLOR)))
                .addOverlay(new RhythmOverlay("Both 8dp and baseline")
                        .addLayer(new GridLines(Gravity.TOP, step))
                        .addLayer(new GridLines(Gravity.LEFT, step))
                        .addLayer(new GridLines(Gravity.TOP, step).offset(step / 2)
                                .color(GridLines.DEFAULT_BASELINE_COLOR)))
                .addOverlay(
                        new RhythmOverlay("Baseline grid with keylines")
                                .addLayer(new GridLines(Gravity.TOP, step / 2).color(GridLines.DEFAULT_BASELINE_COLOR))
                                .addLayer(new Guide(Gravity.LEFT, (int) (24 * density)))
                                .addLayer(new Guide(Gravity.RIGHT, (int) (24 * density)))
                                .addLayer(new Guide(Gravity.LEFT, (int) (24 * density)).thickness(step)
                                        .color(Guide.DEFAULT_HIGHLIGHT_COLOR)
                                        .alignOutside(false))
                                .addLayer(new Guide(Gravity.RIGHT, (int) (24 * density)).thickness(step)
                                        .color(Guide.DEFAULT_HIGHLIGHT_COLOR)
                                        .alignOutside(false))
                                .addLayer(new Guide(Gravity.TOP, (int) (24 * density)))
                                .addLayer(new Guide(Gravity.BOTTOM, (int) (24 * density)))
                                .addLayer(new Guide(Gravity.TOP, (int) (24 * density)).thickness(step)
                                        .color(Guide.DEFAULT_HIGHLIGHT_COLOR)
                                        .alignOutside(false))
                                .addLayer(new Guide(Gravity.BOTTOM, (int) (24 * density)).thickness(step)
                                        .color(Guide.DEFAULT_HIGHLIGHT_COLOR)
                                        .alignOutside(false))
                );

        final RhythmGroup secondaryControl = mRhythmControl.makeGroup("Card overlays");
        new RhythmOverlay("Test config")
                .addLayer(new GridLines(Gravity.LEFT, step).margins(true, 0, 0, 60, 0)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new GridLines(Gravity.RIGHT, step).margins(true, 60, 0, 0, 0)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new GridLines(Gravity.TOP, step).margins(true, 0, 0, 0, 60)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new GridLines(Gravity.BOTTOM, step).margins(true, 0, 60, 0, 0)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new DimensionsLabel(density))
                .addToGroup(secondaryControl);

        mRhythmControl.showQuickControl(-2);
    }

    @Override
    public RhythmControl getRhythmControl() {
        return mRhythmControl;
    }
}
