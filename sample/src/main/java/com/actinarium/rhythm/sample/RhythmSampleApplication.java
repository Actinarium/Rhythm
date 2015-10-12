package com.actinarium.rhythm.sample;

import android.app.Application;
import android.graphics.Color;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmPattern;
import com.actinarium.rhythm.RhythmGroup;
import com.actinarium.rhythm.RhythmControl;
import com.actinarium.rhythm.layers.DimensionsLabelLayer;
import com.actinarium.rhythm.layers.GridLinesLayer;

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
                .addPattern(RhythmPattern.make8DipAndBaselineGrid("Both 8dp and baseline", density));

        final RhythmGroup secondaryControl = mRhythmControl.makeGroup("Card overlays");
        final int gridStep8dp = (int) (8 * density);
        new RhythmPattern("Test config")
                .addLayer(new GridLinesLayer(Gravity.LEFT, gridStep8dp).margins(true, 0, 0, 60, 0)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new GridLinesLayer(Gravity.RIGHT, gridStep8dp).margins(true, 60, 0, 0, 0)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new GridLinesLayer(Gravity.TOP, gridStep8dp).margins(true, 0, 0, 0, 60)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new GridLinesLayer(Gravity.BOTTOM, gridStep8dp).margins(true, 0, 60, 0, 0)
                        .color(Color.BLACK)
                        .limit(4))
                .addLayer(new DimensionsLabelLayer(density))
                .addToControl(secondaryControl);

        mRhythmControl.showQuickControl(-2);
    }

    @Override
    public RhythmControl getRhythmControl() {
        return mRhythmControl;
    }
}
