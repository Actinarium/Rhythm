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
import com.actinarium.rhythm.control.RhythmControl;
import com.actinarium.rhythm.control.RhythmGroup;
import com.actinarium.rhythm.RhythmOverlay;
import com.actinarium.rhythm.RhythmOverlayInflater;
import com.actinarium.rhythm.layer.Inset;
import com.actinarium.rhythm.sample.customlayers.ImageBox;
import com.actinarium.rhythm.sample.customlayers.LayoutBounds;
import com.actinarium.rhythm.layer.GridLines;
import com.actinarium.rhythm.layer.Keyline;

import java.util.List;

/**
 * Application class of Rhythm sample app. For RhythmicFrameLayout and Quick Control notification support, it must
 * implement RhythmControl.Host
 *
 * @author Paul Danyliuk
 */
public class RhythmSampleApplication extends Application implements RhythmControl.Host {

    public static final int CONTENT_OVERLAY_GROUP = 0;
    public static final int CARD_OVERLAY_GROUP = 1;
    public static final int TEXT_OVERLAY_GROUP = 2;

    private RhythmControl mRhythmControl;
    private static final int RHYTHM_NOTIFICATION_ID = -2;
    private RhythmOverlayInflater mRhythmOverlayInflater;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize this application's Rhythm control. That's for the notification
        mRhythmControl = new RhythmControl(this);

        // Create the groups - that's to control their overlays separately
        // There may be as many groups as you need, but you need at least one
        // Groups attached to the control are assigned sequential indices starting at 0
        RhythmGroup contentBgGroup = mRhythmControl.makeGroup("All content background");             // index = 0
        RhythmGroup cardOverlayGroup = mRhythmControl.makeGroup("Intermission card");                // index = 1
        RhythmGroup textOverlayGroup = mRhythmControl.makeGroup("All text labels");                  // index = 2

        // Initialize inflater that we'll use to inflate overlays from declarative (human-readable) config
        mRhythmOverlayInflater = RhythmOverlayInflater.createDefault(this);

        // Register the factories for our custom layers so that we can inflate them from text config
        mRhythmOverlayInflater.registerFactory(ImageBox.Factory.LAYER_TYPE, new ImageBox.Factory());
        mRhythmOverlayInflater.registerFactory(LayoutBounds.Factory.LAYER_TYPE, new LayoutBounds.Factory());

        // Inflate everything from /res/raw/overlay_config.
        List<RhythmOverlay> overlays = mRhythmOverlayInflater.inflate(R.raw.overlay_config);

        // Overlays 0..4 are for content bg group
        contentBgGroup.addOverlays(overlays.subList(0, 5));
        // Overlay #5 goes to the card group
        cardOverlayGroup.addOverlay(overlays.get(5));
        // And the last overlay goes to text views group
        textOverlayGroup.addOverlay(overlays.get(6));

        // It's also possible to create overlays imperatively, but it's cumbersome and DISCOURAGED. Use inflater instead
        // Here's how we would build a hybrid grid identical to the one on /res/raw/overlay_config lines 25-32:
        float density = getResources().getDisplayMetrics().density;     // we need this to convert dp to px ourselves...
        RhythmOverlay unusedOverlay = new RhythmOverlay(5)
                .addLayer(new GridLines(Gravity.TOP, (int) (4 * density)).setColor(GridLines.DEFAULT_GRID_COLOR))
                .addLayer(new Inset(1)
                        .addLayer(new GridLines(Gravity.TOP, (int) (8 * density))
                                .setOffset((int) (4 * density))
                                .setColor(GridLines.DEFAULT_GRID_COLOR)))
                .addLayer(new Keyline(Gravity.LEFT, (int) (16 * density)))
                .addLayer(new Keyline(Gravity.RIGHT, (int) (16 * density)))
                .addLayer(new Keyline(Gravity.LEFT, (int) (72 * density)));

        // By default, if a group has overlays, the first one is initially selected.
        // Let's hide overlays from these two groups and let you figure out how to enable them back via the notification
        cardOverlayGroup.selectOverlay(RhythmGroup.NO_OVERLAY);
        textOverlayGroup.selectOverlay(RhythmGroup.NO_OVERLAY);

        // Show the notification, and we're all set!
        mRhythmControl.showQuickControl(RHYTHM_NOTIFICATION_ID);
    }

    @Override
    public RhythmControl getRhythmControl() {
        return mRhythmControl;
    }

    public RhythmOverlayInflater getRhythmOverlayInflater() {
        return mRhythmOverlayInflater;
    }
}
