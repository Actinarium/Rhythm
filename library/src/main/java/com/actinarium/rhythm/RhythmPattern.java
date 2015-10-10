package com.actinarium.rhythm;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import com.actinarium.rhythm.layers.GridLinesLayer;
import com.actinarium.rhythm.layers.RhythmDrawableLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * A configuration is a set of rules, which grid lines, keylines etc to draw in all {@link RhythmDrawable}s attached to
 * the {@link RhythmGroup} where this config is used at the moment.
 *
 * @author Paul Danyliuk
 */
public class RhythmPattern {

    protected static final int ESTIMATED_AVG_CAPACITY = 6;

    protected String mTitle;
    protected List<RhythmDrawableLayer> mLayers;

    /**
     * Create a new config
     *
     * @param title A convenient title for this config, used to identify it in the notification
     */
    public RhythmPattern(@Nullable String title) {
        mTitle = title;
        mLayers = new ArrayList<>(ESTIMATED_AVG_CAPACITY);
    }

    /**
     * Add a layer to this config. <b>Note:</b> Doing this when config is in use will not automatically update all
     * Rhythm drawables where it's in use
     *
     * @param layer A Rhythm drawable layer implementation
     * @return this for chaining
     */
    public RhythmPattern addLayer(@NonNull RhythmDrawableLayer layer) {
        mLayers.add(layer);
        return this;
    }

    /**
     * A shorthand for {@link RhythmGroup#addPattern(RhythmPattern)}
     *
     * @param control Rhythm control to add this config to
     * @return this for chaining (e.g. for adding this config to other controls as well)
     */
    public RhythmPattern addToControl(RhythmGroup control) {
        control.addPattern(this);
        return this;
    }

    @Override
    public String toString() {
        return mTitle != null ? mTitle : "Untitled pattern";
    }

    /*
     *  A few rather popular configs
     */

    /**
     * Make a simple square 8dp grid. You can then add layers atop this config
     *
     * @param title       Config title
     * @param scaleFactor px to dp ratio, obtained through {@link DisplayMetrics#density}
     * @return Rhythm config with pre-configured grid line layers
     */
    @SuppressLint("RtlHardcoded")
    public static RhythmPattern make8DipGrid(String title, float scaleFactor) {
        final int step = (int) (scaleFactor * 8);
        return new RhythmPattern(title)
                .addLayer(new GridLinesLayer(Gravity.TOP, step))
                .addLayer(new GridLinesLayer(Gravity.LEFT, step));
    }

    /**
     * Make a simple 4dp baseline grid. You can then add layers atop this config
     *
     * @param title       Config title
     * @param scaleFactor px to dp ratio, obtained through {@link DisplayMetrics#density}
     * @return Rhythm config with pre-configured grid line layer
     */
    public static RhythmPattern makeBaselineGrid(String title, float scaleFactor) {
        return new RhythmPattern(title)
                .addLayer(new GridLinesLayer(Gravity.TOP, (int) (scaleFactor * 4)).color(GridLinesLayer.DEFAULT_BASELINE_COLOR));
    }

    /**
     * Make a square 8dp grid combined with differently colored baselines between horizontal grid lines. You can add
     * layers atop this config
     *
     * @param title       Config title
     * @param scaleFactor px to dp ratio, obtained through {@link DisplayMetrics#density}
     * @return Rhythm config with pre-configured grid line layers
     */
    public static RhythmPattern make8DipAndBaselineGrid(String title, float scaleFactor) {
        final int step = (int) (scaleFactor * 8);
        return make8DipGrid(title, scaleFactor)
                .addLayer(new GridLinesLayer(Gravity.TOP, step).offset(step / 2).color(GridLinesLayer.DEFAULT_BASELINE_COLOR));
    }
}
