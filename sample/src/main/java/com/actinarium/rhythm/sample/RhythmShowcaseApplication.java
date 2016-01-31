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
import com.actinarium.rhythm.config.OverlayInflater;
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

        // Initialize inflater that we'll use to inflate overlays from declarative (human-readable) config
        final OverlayInflater inflater = new OverlayInflater(getResources().getDisplayMetrics());

        // We have a custom layer type with a factory - let's register it within the inflater
        inflater.registerFactory("image-box", new ImageBox.Factory());

        // Pre-fetch and pre-calculate some values that we're going to use a lot
        final float density = getResources().getDisplayMetrics().density;
        final int i8dp = (int) (8 * density);
        final int i4dp = i8dp / 2;
        final int i16dp = i8dp * 2;
        final int i24dp = i8dp * 3;
        final int i72dp = i8dp * 9;

        // Create the groups - that's to control their overlays separately
        // There may be as many groups as you need, but one at least
        // Groups, which are attached to the control, are assigned sequential indices starting at 0
        RhythmGroup activityBgGroup = mRhythmControl.makeGroup("Activity background");               // index = 0
        RhythmGroup cardOverlayGroup = mRhythmControl.makeGroup("Card overlay");                     // index = 1
        RhythmGroup dialogOverlayGroup = mRhythmControl.makeGroup("Dialog overlay");                 // index = 2

        // Now let's create some overlays. Mix and match!

        // First, let's create an overlay with standard Material keylines
        // We're not attaching it anywhere yet, but we'll include it in other overlays
        RhythmOverlay materialKeylines = inflater.inflateOverlay(
                "guide gravity=left  distance=16dp\n" +
                "guide gravity=right distance=16dp\n" +
                "guide gravity=left  distance=72dp"
        );

        // Now make a simple 4dp baseline grid with keylines and attach it to the first group
        new RhythmOverlay()
                .setTitle("Baseline grid w/keylines")
                // todo: add support for variables and references
                .addLayer(inflater.inflateLayer("grid-lines gravity=top step=4dp color=#800091EA"))
                .addLayersFrom(materialKeylines)
                .addToGroup(activityBgGroup);

        // Another overlay: standard 8dp grid with the same keylines
        RhythmOverlay standardGrid = inflater.inflateOverlay(
                "grid-lines gravity=top  step=8dp\n" +
                "grid-lines gravity=left step=8dp"
        )
                .addLayersFrom(materialKeylines)
                .addToGroup(activityBgGroup);

        // Avatar list keylines: standard grid plus some 16dp-wide fills for margins around the avatar
        RhythmOverlay avatarList = new RhythmOverlay()
                .setTitle("Avatar list keylines")
                .addLayersFrom(standardGrid);

        inflater.inflateInto(avatarList,
                "inset no-clip left=0dp width=16dp\n" +
                "  fill color=#400091EA\n" +
                "inset no-clip left=56dp width=16dp\n" +
                "  fill color=#400091EA\n" +
                "inset no-clip right=0dp width=16dp\n" +
                "  fill color=#400091EA"
        );
        activityBgGroup.addOverlay(avatarList);         // another way to call avatarList.addToGroup(activityBgGroup);

        // Now for something more interesting

        // Both 8dp and baseline grid, but draw baselines only within the 72dp left keyline and 16dp right keyline.
        // Note that we're not drawing a baseline each 4dp, but rather each 8dp starting from 4dp offset -
        // that's because we already have a 8dp grid, so why overdraw?
        new RhythmOverlay()
                .setTitle("Standard w/ baseline")
                .addLayersFrom(standardGrid)
                .addLayer(new InsetGroup()                                            // Inset and clip grid lines
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
        new RhythmOverlay()
                .setTitle("Split-screen aligned 8dp")
                .addLayer(new GridLines(Gravity.TOP, i8dp))
                .addLayer(new GridLines(Gravity.LEFT, i8dp).setLimit(4))      // 4 lines from the left
                .addLayer(new GridLines(Gravity.RIGHT, i8dp).setLimit(4))     // 4 lines from the right
                .addLayersFrom(materialKeylines)
                .addToGroup(activityBgGroup);

        // Media card overlay, as per the spec: http://bit.ly/1PoQbHb
        RhythmOverlay cardOverlay = inflater.inflateOverlay(
                "inset right=96dp bottom=56dp\n" +
                "  inset height=24dp left=16dp\n" +
                "    fill color=#400091EA\n" +
                "  inset width=16dp\n" +
                "    fill color=#400091EA\n" +
                "  inset left=16dp top=24dp\n" +
                "    grid-lines gravity=top step=4dp\n" +
                "  guide gravity=top distance=24dp\n" +
                "inset right=0 width=96dp bottom=56dp\n" +
                "  inset height=16dp right=16dp\n" +
                "    fill color=#400091EA\n" +
                "  inset width=16dp right=0\n" +
                "    fill color=#400091EA\n" +
                "  guide gravity=top distance=16dp\n" +
                "inset bottom=0 height=56dp\n" +
                "  guide gravity=top    distance=8dp thickness=8dp color=#60F50057\n" +
                "  guide gravity=left   distance=8dp thickness=8dp color=#60F50057\n" +
                "  guide gravity=right  distance=8dp thickness=8dp color=#60F50057\n" +
                "  guide gravity=bottom distance=8dp thickness=8dp color=#60F50057\n" +
                "guide gravity=left  distance=16dp\n" +
                "guide gravity=right distance=16dp\n" +
                "image-box gravity=top|right width=80dp height=80dp distance-x=16dp distance-y=16dp");
        // Heads up: that last line was for our custom layer!

        cardOverlay
                .setTitle("Content card w/ 80dp image")
                .addToGroup(cardOverlayGroup);

        // Make a dialog overlay. Let it be a simple baseline grid with a few keylines corresponding to a bullet list
        new RhythmOverlay()
                .setTitle("Baseline w/ 24dp keylines")
                .addLayer(new GridLines(Gravity.TOP, i4dp).setColor(GridLines.DEFAULT_BASELINE_COLOR))
                .addLayer(new Guide(Gravity.LEFT, i24dp))
                .addLayer(new Guide(Gravity.RIGHT, i24dp))
                .addLayer(new Guide(Gravity.LEFT, i24dp * 2))       // Extra keyline for list inset
                .addToGroup(dialogOverlayGroup);

        mRhythmControl.showQuickControl(RHYTHM_NOTIFICATION_ID);

        // for debug purposes
        activityBgGroup.selectOverlay(2);
    }

    @Override
    public RhythmControl getRhythmControl() {
        return mRhythmControl;
    }
}
