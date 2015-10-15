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

        RhythmGroup activityBgGroup = mRhythmControl.makeGroup("Activity background");
        RhythmGroup cardOverlaysGroup = mRhythmControl.makeGroup("Card overlays");

        RhythmOverlay base8dpGrid = new RhythmOverlay("8dp grid")
                .addLayer(new GridLines(Gravity.TOP, step))
                .addLayer(new GridLines(Gravity.LEFT, step))
                .addToGroup(activityBgGroup)
                .addToGroup(cardOverlaysGroup);

        RhythmOverlay baselineGrid = new RhythmOverlay("Baseline grid")
                .addLayer(new GridLines(Gravity.TOP, step / 2).color(GridLines.DEFAULT_BASELINE_COLOR))
                .addToGroup(activityBgGroup);

        new RhythmOverlay("Both 8dp and baseline")
                .addLayersFrom(base8dpGrid)
                .addLayer(new GridLines(Gravity.TOP, step).offset(step / 2).color(GridLines.DEFAULT_BASELINE_COLOR))
                .addToGroup(activityBgGroup);

        new RhythmOverlay("Avatar keylines with baseline")
                .addLayersFrom(baselineGrid)
                .addLayer(new Guide(Gravity.LEFT, step * 2).thickness(step * 2).color(Guide.DEFAULT_HIGHLIGHT_COLOR))
                .addLayer(new Guide(Gravity.LEFT, step * 9).thickness(step * 2).color(Guide.DEFAULT_HIGHLIGHT_COLOR))
                .addLayer(new Guide(Gravity.LEFT, step * 2))
                .addLayer(new Guide(Gravity.LEFT, step * 7).alignOutside(true))
                .addLayer(new Guide(Gravity.LEFT, step * 9))
                .addToGroup(activityBgGroup);

        new RhythmOverlay("Test config")
                .addLayer(new GridLines(Gravity.LEFT, step).margins(true, 0, 0, 60, 0).color(Color.BLACK).limit(4))
                .addLayer(new GridLines(Gravity.RIGHT, step).margins(true, 60, 0, 0, 0).color(Color.BLACK).limit(4))
                .addLayer(new GridLines(Gravity.TOP, step).margins(true, 0, 0, 0, 60).color(Color.BLACK).limit(4))
                .addLayer(new GridLines(Gravity.BOTTOM, step).margins(true, 0, 60, 0, 0).color(Color.BLACK).limit(4))
                .addLayer(new DimensionsLabel(density))
                .addToGroup(cardOverlaysGroup);

        mRhythmControl.showQuickControl(-2);
    }

    @Override
    public RhythmControl getRhythmControl() {
        return mRhythmControl;
    }
}
