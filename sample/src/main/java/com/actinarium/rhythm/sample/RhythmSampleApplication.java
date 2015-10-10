package com.actinarium.rhythm.sample;

import android.app.Application;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmConfig;
import com.actinarium.rhythm.RhythmGroup;
import com.actinarium.rhythm.RhythmDrawable;
import com.actinarium.rhythm.RhythmManager;

/**
 * <p></p>
 *
 * @author Paul Danyliuk
 * @version $Id$
 */
public class RhythmSampleApplication extends Application implements RhythmManager.Host {

    private RhythmManager mRhythmManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mRhythmManager = new RhythmManager(this);
        final float density = getResources().getDisplayMetrics().density;

        mRhythmManager.newControl("Main control")
                .addConfig(RhythmConfig.make8DipGrid("8dp grid", density))
                .addConfig(RhythmConfig.makeBaselineGrid("Baseline grid", density))
                .addConfig(RhythmConfig.make8DipAndBaselineGrid("Baseline grid", density));

        final RhythmGroup secondaryControl = mRhythmManager.newControl("Secondary control");
        final int gridStep8dp = (int) (8 * density);
        new RhythmConfig("Test config")
                .addLayer(new RhythmDrawable.GridLines(Gravity.LEFT, gridStep8dp).setMargins(true, 0, 0, 60, 0))
                .addLayer(new RhythmDrawable.GridLines(Gravity.RIGHT, gridStep8dp).setMargins(true, 60, 0, 0, 0))
                .addLayer(new RhythmDrawable.GridLines(Gravity.TOP, gridStep8dp).setMargins(true, 0, 0, 0, 60))
                .addLayer(new RhythmDrawable.GridLines(Gravity.BOTTOM, gridStep8dp).setMargins(true, 0, 60, 0, 0))
                .addToControl(secondaryControl);

        mRhythmManager.showNotification(-2);
    }

    @Override
    public RhythmManager getRhythmManager() {
        return mRhythmManager;
    }
}
