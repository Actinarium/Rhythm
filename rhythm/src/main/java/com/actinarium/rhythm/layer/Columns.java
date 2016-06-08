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

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.IntRange;
import com.actinarium.rhythm.AbstractSpecLayerGroup;
import com.actinarium.rhythm.RhythmInflationException;
import com.actinarium.rhythm.ArgumentsBundle;
import com.actinarium.rhythm.RhythmSpecLayerFactory;

/**
 * A layer that divides provided bounds into arbitrary number of equally wide columns and then draws nested layers in
 * each column.
 *
 * @author Paul Danyliuk
 */
public class Columns extends AbstractSpecLayerGroup<Columns> {

    @IntRange(from = 1)
    protected int mColumnCount;

    private Rect mTemp = new Rect();

    /**
     * Create spec layer that will evenly divide current bounds in given number of columns and then draw all child
     * layers in each
     *
     * @param columnCount number of columns, must be a positive integer
     */
    public Columns(@IntRange(from = 1) int columnCount) {
        super();
        mColumnCount = columnCount;
    }

    /**
     * Create spec layer that will evenly divide current bounds in given number of columns and then draw all child
     * layers in each
     *
     * @param columnCount     number of columns, must be a positive integer
     * @param initialCapacity anticipated number of child layers
     */
    public Columns(@IntRange(from = 1) int columnCount, int initialCapacity) {
        super(initialCapacity);
        mColumnCount = columnCount;
    }

    /**
     * <p>Create spec layer that will evenly divide current bounds in given number of columns and then draw all child
     * layers in each.</p> <p>This is a minimum constructor for the factory &mdash; only paints and reusable objects are
     * initialized. Developers extending this class are responsible for setting all fields to proper argument
     * values.</p>
     */
    protected Columns() {
        super();
    }

    /**
     * Set the number of columns
     *
     * @param columnCount number of columns, must be a positive integer
     * @return this for chaining
     */
    public Columns setColumnCount(@IntRange(from = 1) int columnCount) {
        mColumnCount = columnCount;
        return this;
    }

    @Override
    public void draw(Canvas canvas, Rect drawableBounds) {
        mTemp.set(drawableBounds);
        final int left = drawableBounds.left;
        final float width = drawableBounds.width();
        for (int i = 1; i <= mColumnCount; i++) {
            // Always adding rounded i/count fraction of width to the fixed left to ensure symmetry
            // and that the bounds don't overflow overall width
            mTemp.right = left + (int) Math.floor(width * i / mColumnCount + 0.5f);

            // Draw all children into the column
            super.draw(canvas, mTemp);

            // Offset the temporary rect
            mTemp.left = mTemp.right;
        }
    }

    /**
     * A default factory that creates new {@link Columns} layers from config lines according to <a
     * href="https://github.com/Actinarium/Rhythm/wiki/Declarative-configuration#columns">the docs</a>
     */
    public static class Factory implements RhythmSpecLayerFactory<Columns> {

        public static final String LAYER_TYPE = "columns";
        public static final String ARG_COUNT = "count";

        @Override
        public Columns getForArguments(ArgumentsBundle argsBundle) {
            Columns columns = new Columns();

            columns.mColumnCount = argsBundle.getInt(ARG_COUNT, 0);
            if (columns.mColumnCount <= 0) {
                throw new RhythmInflationException(
                        RhythmInflationException.ERROR_ARGUMENT_MISSING_OR_NOT_POSITIVE,
                        "Error in columns config: 'count' argument is mandatory and must be greater than 0 (e.g. 'count=4')",
                        LAYER_TYPE, "count", "count=4"
                );
            }

            return columns;
        }
    }
}
