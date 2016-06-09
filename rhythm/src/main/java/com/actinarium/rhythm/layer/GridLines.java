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

package com.actinarium.rhythm.layer;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.RhythmInflationException;
import com.actinarium.rhythm.ArgumentsBundle;
import com.actinarium.rhythm.RhythmSpecLayerFactory;

/**
 * A spec layer for horizontal <b>or</b> vertical grid lines (not both at once!), repeating at a fixed step. Horizontal
 * grid can float either to the top or the bottom edge of the views, whereas vertical grid can float to the left or the
 * right. You can (and should) combine multiple grid line layers to form regular grids, or you may use them alone for
 * baseline grids and incremental keylines. <b>Note:</b> RTL properties are not supported, you only have <i>left</i> and
 * <i>right</i> at your disposal.
 */
public class GridLines implements RhythmSpecLayer {

    public static final int DEFAULT_GRID_COLOR = 0x60F50057;
    public static final int DEFAULT_BASELINE_COLOR = 0x800091EA;
    /**
     * Default grid line thickness (1px)
     */
    public static final int DEFAULT_THICKNESS = 1;       // px

    @FloatRange(from = 0f, fromInclusive = false)
    protected float mStep;
    @IntRange(from = 1)
    protected int mThickness = DEFAULT_THICKNESS;
    protected int mLimit = Integer.MAX_VALUE;

    protected int mOffset;
    @ArgumentsBundle.EdgeAffinity
    protected int mEdgeAffinity;
    protected Paint mPaint;

    /**
     * Create a layer that draws horizontal or vertical grid lines. Unless offset is applied, horizontal lines are
     * always drawn <i>below</i> the delimited pixel row, and vertical lines are always drawn <i>to the right</i> of the
     * delimited column: e.g. if a child view is fully aligned to the grid on all edges, top and bottom grid lines will
     * overdraw the view, whereas bottom and right grid lines will touch the view.
     *
     * @param edgeAffinity Controls grid alignment <b>and</b> orientation. {@link Gravity#TOP} and {@link
     *                     Gravity#BOTTOM} mean horizontal lines, and {@link Gravity#LEFT} and {@link Gravity#RIGHT}
     *                     mean vertical lines, and the difference between those is from what edge of the view the steps
     *                     are counted. A good example where this can be useful is having a left-aligned and a
     *                     right-aligned layer on the left and the right half of the view when its width is not an exact
     *                     multiple of the step.
     * @param step         Grid step, in pixels. Allows for float values to properly accommodate devices with non-round
     *                     dip-to-pixel ratio (1.5x on hdpi, 2.5x on Nexus 5X etc)
     */
    public GridLines(@ArgumentsBundle.EdgeAffinity int edgeAffinity, @FloatRange(from = 0f, fromInclusive = false) float step) {
        this();
        mStep = step;
        mEdgeAffinity = edgeAffinity;
        mPaint.setColor(DEFAULT_GRID_COLOR);
    }

    /**
     * <p>Create a spec layer that displays dimensions label.</p> <p>This is a minimum constructor for the factory
     * &mdash; only paints and reusable objects are initialized. Developers extending this class are responsible for
     * setting all fields to proper argument values.</p>
     */
    protected GridLines() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * Set the step of grid lines
     *
     * @param step Grid step, in pixels. Allows for float values to properly accommodate devices with non-round
     *             dip-to-pixel ratio (1.5x on hdpi, 2.5x on Nexus 5X etc)
     * @return this for chaining
     */
    public GridLines setStep(@FloatRange(from = 0f, fromInclusive = false) float step) {
        mStep = step;
        return this;
    }

    /**
     * Set edge affinity of the grid
     *
     * @param edgeAffinity Controls grid alignment <b>and</b> orientation. Use {@link Gravity#TOP} or {@link
     *                     Gravity#BOTTOM} for horizontal lines counting from top or bottom, and {@link Gravity#LEFT} or
     *                     {@link Gravity#RIGHT} for vertical lines cointing from left or right edge of the screen
     *                     respectively.
     * @return this for chaining
     */
    public GridLines setEdgeAffinity(@ArgumentsBundle.EdgeAffinity int edgeAffinity) {
        mEdgeAffinity = edgeAffinity;
        return this;
    }

    /**
     * Set grid line color
     *
     * @param color Grid line color, in #AARRGGBB format as usual
     * @return this for chaining
     */
    public GridLines setColor(@ColorInt int color) {
        mPaint.setColor(color);
        return this;
    }

    /**
     * Set grid line thickness
     *
     * @param thickness Grid line thickness, in pixels
     * @return this for chaining
     */
    public GridLines setThickness(@IntRange(from = 1) int thickness) {
        mThickness = thickness;
        return this;
    }

    /**
     * Set the maximum number of steps to outline, respecting layer’s gravity (i.e. if gravity is set to {@link
     * Gravity#BOTTOM} and the limit is 4, this layer will draw four lines enclosing 4 cells. Default is no limit.
     *
     * @param limit Number of lines to draw. Setting zero or less means no limit.
     * @return this for chaining
     */
    public GridLines setLimit(int limit) {
        mLimit = limit > 0 ? limit : Integer.MAX_VALUE;
        return this;
    }

    /**
     * Set additional grid offset. Might be useful if you need to tweak the position of the grid just a few pixels up or
     * down, or prevent overdraw when combining a few interleaving grids (e.g. to add a 4dp baseline grid to a 8dp
     * regular grid you only need to draw each second baseline, which is done with a 8dp step and a 4dp offset).
     *
     * @param offset Grid offset in pixels. Regardless of gravity, positive offset means right/down, negative means
     *               left/up
     * @return this for chaining
     */
    public GridLines setOffset(int offset) {
        mOffset = offset;
        return this;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void draw(Canvas canvas, Rect drawableBounds) {
        // Depending on gravity the orientation, the order of drawing, and the starting point are different
        if (mEdgeAffinity == Gravity.TOP) {
            final float top = drawableBounds.top + mOffset + 0.5f;
            for (int i = 0; i <= mLimit; i++) {
                int y = (int) (top + mStep * i);
                if (y >= drawableBounds.bottom) {
                    return;
                }
                canvas.drawRect(drawableBounds.left, y, drawableBounds.right, y + mThickness, mPaint);
            }
        } else if (mEdgeAffinity == Gravity.BOTTOM) {
            final float bottom = drawableBounds.bottom + mOffset + 0.5f;
            for (int i = 0; i <= mLimit; i++) {
                int y = (int) (bottom - mStep * i);
                if (y < drawableBounds.top) {
                    return;
                }
                canvas.drawRect(drawableBounds.left, y, drawableBounds.right, y + mThickness, mPaint);
            }
        } else if (mEdgeAffinity == Gravity.LEFT) {
            final float left = drawableBounds.left + mOffset + 0.5f;
            for (int i = 0; i <= mLimit; i++) {
                int x = (int) (left + mStep * i);
                if (x >= drawableBounds.right) {
                    return;
                }
                canvas.drawRect(x, drawableBounds.top, x + mThickness, drawableBounds.bottom, mPaint);
            }
        } else if (mEdgeAffinity == Gravity.RIGHT) {
            final float right = drawableBounds.right + mOffset + 0.5f;
            for (int i = 0; i <= mLimit; i++) {
                int x = (int) (right - mStep * i);
                if (x < drawableBounds.left) {
                    return;
                }
                canvas.drawRect(x, drawableBounds.top, x + mThickness, drawableBounds.bottom, mPaint);
            }
        }
    }

    /**
     * A default factory that creates new {@link GridLines} layers from config lines according to <a
     * href="https://github.com/Actinarium/Rhythm/wiki/Declarative-configuration#grid-lines">the docs</a>
     */
    public static class Factory implements RhythmSpecLayerFactory<GridLines> {

        public static final String LAYER_TYPE = "grid-lines";
        public static final String ARG_EDGE = "from";
        public static final String ARG_STEP = "step";
        public static final String ARG_COLOR = "color";
        public static final String ARG_THICKNESS = "thickness";
        public static final String ARG_LIMIT = "limit";
        public static final String ARG_OFFSET = "offset";

        @Override
        public GridLines getForArguments(ArgumentsBundle argsBundle) {
            GridLines gridLines = new GridLines();

            gridLines.mEdgeAffinity = argsBundle.getEdgeAffinity(ARG_EDGE, Gravity.NO_GRAVITY);
            if (gridLines.mEdgeAffinity == Gravity.NO_GRAVITY) {
                throw new RhythmInflationException(
                        "Error in grid-lines config: 'from' argument is mandatory and must be either 'left', 'right', 'top', 'bottom'"
                );
            }

            final float step = argsBundle.getDimensionPixelExact(ARG_STEP, 0f);
            if (step <= 0) {
                throw new RhythmInflationException(
                        "Error in grid-lines config: 'step' argument is mandatory and must be greater than 0"
                );
            }
            gridLines.mStep = step;

            gridLines.mPaint.setColor(argsBundle.getColor(ARG_COLOR, DEFAULT_GRID_COLOR));
            gridLines.mThickness = argsBundle.getDimensionPixelSize(ARG_THICKNESS, DEFAULT_THICKNESS);
            gridLines.setLimit(argsBundle.getInt(ARG_LIMIT, Integer.MAX_VALUE));
            gridLines.mOffset = argsBundle.getDimensionPixelOffset(ARG_OFFSET, 0);

            return gridLines;
        }
    }

}
