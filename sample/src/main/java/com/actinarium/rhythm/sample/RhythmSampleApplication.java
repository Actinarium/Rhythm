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
import com.actinarium.rhythm.RhythmPattern;
import com.actinarium.rhythm.RhythmGroup;
import com.actinarium.rhythm.RhythmControl;
import com.actinarium.rhythm.layers.DimensionsLabel;
import com.actinarium.rhythm.layers.GridLines;
import com.actinarium.rhythm.layers.Guide;

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

        mRhythmControl.makeGroup("Activity background")
                .addPattern(RhythmPattern.make8DipGrid("8dp grid", density))
                .addPattern(RhythmPattern.makeBaselineGrid("Baseline grid", density))
                .addPattern(RhythmPattern.make8DipAndBaselineGrid("Both 8dp and baseline", density))
                .addPattern(
                        RhythmPattern.makeBaselineGrid("Baseline grid with keylines", density)
                                .addLayer(new Guide(Gravity.LEFT, (int) (24 * density)))
                                .addLayer(new Guide(Gravity.RIGHT, (int) (24 * density)))
                                .addLayer(new Guide(Gravity.LEFT, (int) (24 * density)).thickness((int) (8 * density)).color(Guide.DEFAULT_HIGHLIGHT_COLOR).alignOutside(false))
                                .addLayer(new Guide(Gravity.RIGHT, (int) (24 * density)).thickness((int) (8 * density))
                                        .color(Guide.DEFAULT_HIGHLIGHT_COLOR)
                                        .alignOutside(false))
                                .addLayer(new Guide(Gravity.TOP, (int) (24 * density)))
                                .addLayer(new Guide(Gravity.BOTTOM, (int) (24 * density)))
                                .addLayer(new Guide(Gravity.TOP, (int) (24 * density)).thickness((int) (8 * density)).color(Guide.DEFAULT_HIGHLIGHT_COLOR).alignOutside(false))
                                .addLayer(new Guide(Gravity.BOTTOM, (int) (24 * density)).thickness((int) (8 * density)).color(Guide.DEFAULT_HIGHLIGHT_COLOR).alignOutside(false))
                );

        final RhythmGroup secondaryControl = mRhythmControl.makeGroup("Card overlays");
        final int gridStep8dp = (int) (8 * density);
        new RhythmPattern("Test config")
                .addLayer(new GridLines(Gravity.LEFT, gridStep8dp).margins(true, 0, 0, 60, 0)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new GridLines(Gravity.RIGHT, gridStep8dp).margins(true, 60, 0, 0, 0)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new GridLines(Gravity.TOP, gridStep8dp).margins(true, 0, 0, 0, 60)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new GridLines(Gravity.BOTTOM, gridStep8dp).margins(true, 0, 60, 0, 0)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new DimensionsLabel(density))
                .addToControl(secondaryControl);

        mRhythmControl.showQuickControl(-2);
    }

    @Override
    public RhythmControl getRhythmControl() {
        return mRhythmControl;
    }
}
