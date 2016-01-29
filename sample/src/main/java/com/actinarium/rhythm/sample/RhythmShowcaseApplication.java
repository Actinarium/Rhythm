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

package com.actinarium.rhythm.sample;

import android.app.Application;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmControl;
import com.actinarium.rhythm.RhythmGroup;
import com.actinarium.rhythm.RhythmOverlay;
import com.actinarium.rhythm.spec.Fill;
import com.actinarium.rhythm.spec.GridLines;
import com.actinarium.rhythm.spec.Guide;
import com.actinarium.rhythm.spec.InsetGroup;

/**
 * Application class of Rhythm sample app. For RhythmicFrameLayout and Quick Control notification support, it must
 * implement RhythmControl.Host
 *
 * @author Paul Danyliuk
 */
public class RhythmShowcaseApplication extends Application implements RhythmControl.Host {

    public static final int ACTIVITY_OVERLAY_GROUP = 0;
    public static final int CARD_OVERLAY_GROUP = 1;

    private RhythmControl mRhythmControl;
    private static final int RHYTHM_NOTIFICATION_ID = -2;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize this application's Rhythm control
        mRhythmControl = new RhythmControl(this);

        // Pre-fetch and pre-calculate some values that we're going to use a lot
        final float density = getResources().getDisplayMetrics().density;
        final int i8dp = (int) (8 * density);
        final int i4dp = i8dp / 2;
        final int i16dp = i8dp * 2;
        final int i24dp = i8dp * 3;
        final int i56dp = i8dp * 7;
        final int i72dp = i8dp * 9;
        final int i80dp = i8dp * 10;

        // Create the groups - that's to control their overlays separately
        // There may be as many groups as you need, but one at least
        // Groups, which are attached to the control, are assigned sequential indices starting at 0
        RhythmGroup activityBgGroup = mRhythmControl.makeGroup("Activity background");               // index = 0
        RhythmGroup cardOverlayGroup = mRhythmControl.makeGroup("Card overlay");                     // index = 1
        RhythmGroup dialogOverlayGroup = mRhythmControl.makeGroup("Dialog overlay");                 // index = 2

        // Now let's create some overlays. Mix and match!

        // First, let's create an overlay with standard Material keylines
        // We're not attaching it anywhere yet, but we'll include it in other overlays
        RhythmOverlay materialKeylines = new RhythmOverlay("Material keylines")
                .addLayer(new Guide(Gravity.LEFT, i16dp))                                  // 16 dp from the left
                .addLayer(new Guide(Gravity.RIGHT, i16dp))                                 // 16 dp from the right
                .addLayer(new Guide(Gravity.LEFT, i72dp));                                 // 72 dp from the left

        // Now make a simple 4dp baseline grid with keylines and attach it to the first group
        new RhythmOverlay("Baseline grid w/keylines")
                .addLayer(new GridLines(Gravity.TOP, i4dp).setColor(GridLines.DEFAULT_BASELINE_COLOR))
                .addLayersFrom(materialKeylines)
                .addToGroup(activityBgGroup);

        // Another overlay: standard 8dp grid with the same keylines
        RhythmOverlay standardGrid = new RhythmOverlay("Standard grid")
                .addLayer(new GridLines(Gravity.TOP, i8dp))
                .addLayer(new GridLines(Gravity.LEFT, i8dp))
                .addLayersFrom(materialKeylines)
                .addToGroup(activityBgGroup);

        // Avatar list keylines: standard grid plus some 16dp-wide highlights for margins around the avatar
        // When gravity is LEFT, thickness controls how the guide expands towards the LEFT side of the screen
        new RhythmOverlay("Avatar list keylines")
                .addLayersFrom(standardGrid)
                .addLayer(new Guide(Gravity.LEFT, i16dp).setThickness(i16dp).setColor(Fill.DEFAULT_FILL_COLOR))
                .addLayer(new Guide(Gravity.LEFT, i72dp).setThickness(i16dp).setColor(Fill.DEFAULT_FILL_COLOR))
                .addLayer(new Guide(Gravity.RIGHT, i16dp).setThickness(i16dp).setColor(Fill.DEFAULT_FILL_COLOR))
                .addToGroup(activityBgGroup);

        // Now for something more interesting

        // Both 8dp and baseline grid, but draw baselines only within the 72dp left keyline and 16dp right keyline.
        // Note that we're not drawing a baseline each 4dp, but rather each 8dp starting from 4dp offset -
        // that's because we already have a 8dp grid, so why overdraw?
        new RhythmOverlay("Standard w/ baseline")
                .addLayersFrom(standardGrid)
                .addLayer(new InsetGroup(InsetGroup.MODE_DEFAULT)                     // Inset and clip grid lines
                        .setLeft(i72dp, InsetGroup.UNITS_PX)
                        .setRight(i16dp, InsetGroup.UNITS_PX)
                        .addLayer(new GridLines(Gravity.TOP, i8dp)                    // Draw a grid line each 8 dips...
                                .setOffset(i4dp)                                      // ...starting from 4dp...
                                .setColor(GridLines.DEFAULT_BASELINE_COLOR)
                        )
                )
                .addToGroup(activityBgGroup);

        // Since some devices are not exactly 8x dip wide (e.g. Nexus 5 is not 8x dip wide in landscape),
        // let's draw a right-aligned grid on the right half of the screen
        new RhythmOverlay("Split-screen aligned 8dp")
                .addLayer(new GridLines(Gravity.TOP, i8dp))
                .addLayer(new GridLines(Gravity.LEFT, i8dp).setLimit(4))      // 4 lines from the left
                .addLayer(new GridLines(Gravity.RIGHT, i8dp).setLimit(4))     // 4 lines from the right
                .addLayersFrom(materialKeylines)
                .addToGroup(activityBgGroup);

        // Media card overlay, as per the spec: http://bit.ly/1PoQbHb
        new RhythmOverlay("Content card w/ 80dp image")
                .addLayer(new InsetGroup(InsetGroup.MODE_DEFAULT)
                        .setLeft(i16dp, InsetGroup.UNITS_PX)
                        .setTop(i24dp, InsetGroup.UNITS_PX)
                        .setRight(i16dp + i80dp, InsetGroup.UNITS_PX)
                        .setBottom(i56dp, InsetGroup.UNITS_PX)
                        .addLayer(new GridLines(Gravity.TOP, i4dp))
                )
                .addLayer(new Guide(Gravity.LEFT, i16dp).setColor(Fill.DEFAULT_FILL_COLOR).setThickness(i16dp))
                .addLayer(new Guide(Gravity.RIGHT, i16dp).setColor(Fill.DEFAULT_FILL_COLOR).setThickness(i16dp))
                .addLayer(new Guide(Gravity.TOP, i24dp).setColor(Fill.DEFAULT_FILL_COLOR).setThickness(i24dp))
                .addLayer(new Guide(Gravity.LEFT, i16dp))
                .addLayer(new Guide(Gravity.RIGHT, i16dp))
                .addLayer(new Guide(Gravity.TOP, i24dp))
                .addLayer(new Guide(Gravity.LEFT, i8dp).setThickness(i8dp).setColor(Fill.DEFAULT_FILL_COLOR))
                .addLayer(new Guide(Gravity.RIGHT, i8dp).setThickness(i8dp).setColor(Fill.DEFAULT_FILL_COLOR))
                .addLayer(new Guide(Gravity.BOTTOM, i8dp).setThickness(i8dp).setColor(Fill.DEFAULT_FILL_COLOR))
                .addLayer(new Guide(Gravity.BOTTOM, i56dp).setThickness(i8dp).setColor(Fill.DEFAULT_FILL_COLOR))
                .addLayer(new ImageBox(i80dp, i80dp, i16dp, i16dp, Gravity.TOP | Gravity.RIGHT, density))
                .addToGroup(cardOverlayGroup);

        // Make a dialog overlay. Let it be a simple baseline grid with a few keylines corresponding to a bullet list
        new RhythmOverlay("Baseline w/ 24dp keylines")
                .addLayer(new GridLines(Gravity.TOP, i4dp).setColor(GridLines.DEFAULT_BASELINE_COLOR))
                .addLayer(new Guide(Gravity.LEFT, i24dp))
                .addLayer(new Guide(Gravity.RIGHT, i24dp))
                .addLayer(new Guide(Gravity.LEFT, i24dp * 2))       // Extra keyline for list inset
                .addToGroup(dialogOverlayGroup);

        mRhythmControl.showQuickControl(RHYTHM_NOTIFICATION_ID);
    }

    @Override
    public RhythmControl getRhythmControl() {
        return mRhythmControl;
    }
}
