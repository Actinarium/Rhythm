package com.actinarium.rhythm;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.view.Gravity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * <p>Draws grids, keylines etc.</p> <p>Made final intentionally. If you need to perform custom drawing, consider
 * creating a custom </p>
 *
 * @author Paul Danyliuk
 */
public final class RhythmDrawable extends Drawable {

    private RhythmConfig mConfig;

    @Override
    public void draw(Canvas canvas) {
        if (mConfig == null) {
            // Overlay is disabled, drawing nothing
            return;
        }

        Rect bounds = getBounds();
        final List<RhythmDrawableLayer> layers = mConfig.mLayers;
        // Draw each layer
        for (int i = 0, size = layers.size(); i < size; i++) {
            layers.get(i).draw(canvas, bounds);
        }
    }

    public void setConfig(@Nullable RhythmConfig config) {
        mConfig = config;
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        // no-op
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        // no-op
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }


    /**
     * An interface describing a lightweight reusable drawable, which can be drawn multiple times using provided bounds
     * (unlike {@link Drawable} subclasses). Implement this interface to create custom drawables for Rhythm overlays.
     */
    public interface RhythmDrawableLayer {
        void draw(Canvas canvas, Rect drawableBounds);
    }

    /**
     * A Rhythm for horizontal or vertical grid lines, repeating at a fixed step. Horizontal grid can be float either to
     * the top or the bottom of the views, whereas vertical grid can float to the left or the right. You can (and
     * should) combine grid lines to form regular grids, or you may use them alone for baseline grids and incremental
     * keylines. <b>Note:</b> RTL properties are not supported, you only have <i>left</i> and <i>right</i> at your
     * disposal.
     */
    public static class GridLines implements RhythmDrawableLayer {

        protected boolean mIsVertical;
        protected int mStep;
        protected int mThickness = 1;
        protected boolean mMarginIsPercent;
        protected int mMarginLeft;
        protected int mMarginTop;
        protected int mMarginRight;
        protected int mMarginBottom;
        protected int mOffset;
        protected int mGravity;
        protected Paint mPaint;

        /**
         * Create a layer that draws horizontal or vertical grid lines. The lines are always placed <i>after</i> the
         * delimited pixel row, e.g. if a horizontal grid is drawn from the top, grid lines will be drawn <i>below</i>
         * each n-th pixel, or if a vertical grid is drawn from the left, the lines are drawn <i>to the right</i> of
         * each n-th pixel.
         *
         * @param gravity Grid alignment (gravity). Controls on what side grid lines will touch the view - this may be
         *                useful e.g. to have two separate grids on the left and the right aligned respectively.<br>Also
         *                determines whether the grid is horizontal ({@link Gravity#TOP} or {@link Gravity#BOTTOM}) or
         *                vertical ({@link Gravity#LEFT} or {@link Gravity#RIGHT})
         * @param step    Grid step, in pixels
         * @param color   Grid line color, in #AARRGGBB format as usual
         */
        public GridLines(@GridGravityConstraint int gravity, int step, int color) {
            mStep = step;
            mGravity = gravity;
            // If gravity pulls the grid left or right (horizontal pull), then we're talking about vertical lines
            mIsVertical = Gravity.isHorizontal(gravity);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(color);
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
         * Set layer margins, in either pixels or percent of the view. This can be useful if you need to display e.g. a
         * separate grid in the bottom of the view and a grid in the top, so that they don't overlap. By default the
         * margins are 0.
         *
         * @param isPercent false to treat parameters as pixels, true to treat parameters as percent in range 0..100.
         *                  Percent values outside this range will be treated as 0.
         * @param left      left margin (px or %)
         * @param top       top margin (px or %)
         * @param right     right margin (px or %)
         * @param bottom    bottom margin (px or %)
         * @return this for chaining
         */
        public GridLines setMargins(boolean isPercent, int left, int top, int right, int bottom) {
            mMarginIsPercent = isPercent;
            mMarginLeft = left;
            mMarginTop = top;
            mMarginRight = right;
            mMarginBottom = bottom;
            return this;
        }

        /**
         * Set additional grid offset. Might be useful if you need to tweak the position of the grid just a few pixels
         * up or down, or prevent overdraw when combining a few interleaving grids (e.g. to draw a 4dp baseline grid
         * over a 8dp regular grid you may use a 8dp step and a 4dp offset).
         *
         * @param offset Grid offset in pixels. Regardless of gravity, positive offset means down, negative means up
         * @return this for chaining
         */
        public GridLines setOffset(int offset) {
            mOffset = offset;
            return this;
        }

        @Override
        public void draw(Canvas canvas, Rect drawableBounds) {
            // Calculate real left/right/top/bottom bounds based on drawable bounds and margins
            final int left, top, right, bottom;
            if (mMarginIsPercent) {
                final int width = drawableBounds.width();
                final int height = drawableBounds.height();
                left = drawableBounds.left + width * mMarginLeft / 100;
                top = drawableBounds.top + height * mMarginTop / 100;
                right = drawableBounds.right - width * mMarginRight / 100;
                bottom = drawableBounds.bottom - height * mMarginBottom / 100;
            } else {
                left = drawableBounds.left + mMarginLeft;
                top = drawableBounds.top + mMarginTop;
                right = drawableBounds.right - mMarginRight;
                bottom = drawableBounds.bottom - mMarginBottom;
            }

            // Calculate final width and height
            final int width = right - left;
            final int height = bottom - top;
            if (width <= 0 || height <= 0) {
                // Nothing to draw
                return;
            }

            if (Gravity.isVertical(mGravity)) {
                // We're drawing horizontal lines
                // We'll be drawing top to bottom either way, so let's determine starting point (line top Y)
                int curY = mGravity == Gravity.TOP
                        ? top + mOffset
                        : top + height % mStep + mOffset - mThickness;
                while (curY < bottom) {
                    canvas.drawRect(left, curY, right, curY + mThickness, mPaint);
                    curY += mStep;
                }
            } else {
                // Then we're drawing vertical lines
                // Same with vertical lines
                int curX = mGravity == Gravity.LEFT
                        ? left + mOffset
                        : left + width % mStep + mOffset - mThickness;
                while (curX < right) {
                    canvas.drawRect(curX, top, curX + mThickness, bottom, mPaint);
                    curX += mStep;
                }
            }
        }
    }

    @SuppressLint("RtlHardcoded")
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Gravity.TOP, Gravity.BOTTOM, Gravity.LEFT, Gravity.RIGHT})
    @interface GridGravityConstraint {
    }
}
