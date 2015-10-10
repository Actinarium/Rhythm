package com.actinarium.rhythm;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;

import java.util.ArrayList;
import java.util.List;

/**
 * A configuration is a set of rules, which grid lines, keylines etc to draw in all {@link RhythmDrawable}s attached to
 * the {@link RhythmControl} where this config is used at the moment.
 *
 * @author Paul Danyliuk
 */
public class RhythmConfig {

    protected static final int ESTIMATED_AVG_CAPACITY = 6;

    protected String mTitle;
    protected List<RhythmDrawable.RhythmDrawableLayer> mLayers;

    /**
     * Create a new config
     *
     * @param title A convenient title for this config, used to identify it in the notification
     */
    public RhythmConfig(@Nullable String title) {
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
    public RhythmConfig addLayer(@NonNull RhythmDrawable.RhythmDrawableLayer layer) {
        mLayers.add(layer);
        return this;
    }

    /**
     * A shorthand for {@link RhythmControl#addConfig(RhythmConfig)}
     *
     * @param control Rhythm control to add this config to
     * @return this for chaining (e.g. for adding this config to other controls as well)
     */
    public RhythmConfig addToControl(RhythmControl control) {
        control.addConfig(this);
        return this;
    }

    /*
     *  A few rather popular configs
     */

    /**
     * Make a simple square 8dp grid. You can then add layers atop this config
     *
     * @param title       Config title
     * @param scaleFactor px to dp ratio, obtained through {@link DisplayMetrics#density}
     * @param color       Color of the grid, in the usual #AARRGGBB format
     * @return Rhythm config with pre-configured grid line layers
     */
    @SuppressLint("RtlHardcoded")
    public static RhythmConfig make8DipGrid(String title, float scaleFactor, int color) {
        final int step = (int) (scaleFactor * 8);
        return new RhythmConfig(title)
                .addLayer(new RhythmDrawable.GridLines(Gravity.TOP, step, color))
                .addLayer(new RhythmDrawable.GridLines(Gravity.LEFT, step, color));
    }

    /**
     * Make a simple 4dp baseline grid. You can then add layers atop this config
     *
     * @param title       Config title
     * @param scaleFactor px to dp ratio, obtained through {@link DisplayMetrics#density}
     * @param color       Color of the baseline grid, in the usual #AARRGGBB format
     * @return Rhythm config with pre-configured grid line layer
     */
    public static RhythmConfig makeBaselineGrid(String title, float scaleFactor, int color) {
        return new RhythmConfig(title)
                .addLayer(new RhythmDrawable.GridLines(Gravity.TOP, (int) (scaleFactor * 4), color));
    }

    /**
     * Make a square 8dp grid combined with differently colored baselines between horizontal grid lines. You can add
     * layers atop this config
     *
     * @param title         Config title
     * @param scaleFactor   px to dp ratio, obtained through {@link DisplayMetrics#density}
     * @param gridColor     Color of the 8dp grid, in the usual #AARRGGBB format
     * @param baselineColor Color of additional baselines, in the usual #AARRGGBB format
     * @return Rhythm config with pre-configured grid line layers
     */
    public static RhythmConfig make8DipAndBaselineGrid(String title, float scaleFactor, int gridColor, int baselineColor) {
        final int step = (int) (scaleFactor * 8);
        return make8DipGrid(title, scaleFactor, gridColor)
                .addLayer(new RhythmDrawable.GridLines(Gravity.TOP, step, baselineColor).setOffset(step / 2));
    }
}
