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

package com.actinarium.rhythm.spec;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.view.Gravity;
import com.actinarium.rhythm.RhythmSpecLayer;
import com.actinarium.rhythm.common.RhythmInflationException;
import com.actinarium.rhythm.ArgumentsBundle;
import com.actinarium.rhythm.config.RhythmSpecLayerFactory;

/**
 * A spec layer for horizontal <b>or</b> vertical grid lines (not both at once!), repeating at a fixed step. Horizontal
 * grid can float either to the top or the bottom of the views, whereas vertical grid can float to the left or the
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

    protected int mStep;
    protected int mThickness = DEFAULT_THICKNESS;
    protected int mLimit = Integer.MAX_VALUE;

    protected int mOffset;
    @ArgumentsBundle.EdgeAffinity
    protected int mGravity;
    protected Paint mPaint;

    /**
     * Create a layer that draws horizontal or vertical grid lines. Unless offset is applied, horizontal lines are
     * always drawn <i>below</i> the delimited pixel row, and vertical lines are always drawn <i>to the right</i> of the
     * delimited column: e.g. if a child view is fully aligned to the grid on all sides, top and bottom grid lines will
     * overdraw the view, whereas bottom and right grid lines will touch the view.
     *
     * @param gravity Control grid alignment <b>and</b> orientation. {@link Gravity#TOP} and {@link Gravity#BOTTOM} mean
     *                horizontal lines, and {@link Gravity#LEFT} and {@link Gravity#RIGHT} mean vertical lines, and the
     *                difference between those is from what side of the view the steps are counted. A good example where
     *                this can be useful is having a left-aligned and a right-aligned layer on the left and the right
     *                half of the view when its width is not an exact multiple of the step.
     * @param step    Grid step, in pixels
     */
    public GridLines(@ArgumentsBundle.EdgeAffinity int gravity, int step) {
        mStep = step;
        mGravity = gravity;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(DEFAULT_GRID_COLOR);
    }

    /**
     * Minimum constructor for the factory
     */
    protected GridLines() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
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
    public GridLines setThickness(int thickness) {
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
        int line = 0;
        if (mGravity == Gravity.TOP) {
            int curY = drawableBounds.top + mOffset;
            while (curY < drawableBounds.bottom && line <= mLimit) {
                canvas.drawRect(drawableBounds.left, curY, drawableBounds.right, curY + mThickness, mPaint);
                curY += mStep;
                line++;
            }
        } else if (mGravity == Gravity.BOTTOM) {
            int curY = drawableBounds.bottom + mOffset;
            while (curY >= drawableBounds.top && line <= mLimit) {
                canvas.drawRect(drawableBounds.left, curY, drawableBounds.right, curY + mThickness, mPaint);
                curY -= mStep;
                line++;
            }
        } else if (mGravity == Gravity.LEFT) {
            int curX = drawableBounds.left + mOffset;
            while (curX < drawableBounds.right && line <= mLimit) {
                canvas.drawRect(curX, drawableBounds.top, curX + mThickness, drawableBounds.bottom, mPaint);
                curX += mStep;
                line++;
            }
        } else if (mGravity == Gravity.RIGHT) {
            int curX = drawableBounds.right + mOffset;
            while (curX >= drawableBounds.left && line <= mLimit) {
                canvas.drawRect(curX, drawableBounds.top, curX + mThickness, drawableBounds.bottom, mPaint);
                curX -= mStep;
                line++;
            }
        }
    }

    /**
     * A factory that creates new GridLines layers from config lines like <code>grid-lines gravity=left step=4dp
     * thickness=1px color=#00FFFF</code>
     */
    public static class Factory implements RhythmSpecLayerFactory<GridLines> {

        public static final String LAYER_TYPE = "grid-lines";

        @Override
        public GridLines getForConfig(ArgumentsBundle argsBundle) {
            GridLines gridLines = new GridLines();

            gridLines.mGravity = argsBundle.getEdgeAffinity("from", Gravity.NO_GRAVITY);
            if (gridLines.mGravity == Gravity.NO_GRAVITY) {
                throw new RhythmInflationException(
                        RhythmInflationException.ERROR_ARGUMENT_MISSING_OR_NOT_ONE_OF,
                        "Error in grid-lines config: 'from' argument is mandatory and must be either 'left', 'right', 'top', 'bottom'",
                        LAYER_TYPE, "from", argsBundle.getString("from"), "left|right|top|bottom"
                );
            }

            final int step = argsBundle.getDimensionPixelOffset("step", 0);
            if (step <= 0) {
                throw new RhythmInflationException(
                        RhythmInflationException.ERROR_ARGUMENT_MISSING_OR_NOT_POSITIVE,
                        "Error in grid-lines config: 'step' argument is mandatory and must be greater than 0 (e.g. 'step=8dp')",
                        LAYER_TYPE, "step", "step=8dp"
                );
            }
            gridLines.mStep = step;

            gridLines.mPaint.setColor(argsBundle.getColor("color", DEFAULT_GRID_COLOR));
            gridLines.mThickness = argsBundle.getDimensionPixelSize("thickness", DEFAULT_THICKNESS);
            gridLines.setLimit(argsBundle.getInt("limit", Integer.MAX_VALUE));
            gridLines.mOffset = argsBundle.getDimensionPixelOffset("offset", 0);

            return gridLines;
        }
    }

}
