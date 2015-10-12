package com.actinarium.rhythm.layers;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Gravity;

/**
 * A layer that draws a small box with dimensions of the current view. Used to inspect the dimensions of your views at
 * glance and notice the issues. By default, the box is placed in the bottom right corner, but you can control its
 * gravity.
 *
 * @author Paul Danyliuk
 */
public class DimensionsLabelLayer implements RhythmDrawableLayer {

    public static final int DEFAULT_BACKGROUND = 0x80000000;
    public static final int DEFAULT_TEXT_COLOR = 0xA0FFFFFF;
    public static final int DEFAULT_TEXT_SIZE = 12;          // dp

    private static final char ONE_HALF = '\u00bd';
    private static final char ONE_FOURTH = '\u00bc';
    private static final char THREE_FOURTHS = '\u00be';
    private static final char ONE_THIRD = '\u2153';
    private static final char TWO_THIRDS = '\u2154';
    private static final char MULTIPLY = '\u00d7';

    protected float mScaleFactor;
    protected int mGravity = Gravity.BOTTOM | Gravity.END;
    protected Paint mBackgroundPaint;
    protected TextPaint mTextPaint;
    private Rect mTempRect;

    /**
     * Create a layer that displays dimensions label
     *
     * @param scaleFactor Scale factor to divide pixels by. Provide {@link DisplayMetrics#density} here to get thy
     *                    dimensions in dips, or set to 1f to get pixels.
     */
    public DimensionsLabelLayer(float scaleFactor) {
        mScaleFactor = scaleFactor;

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(DEFAULT_BACKGROUND);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(DEFAULT_TEXT_COLOR);
        mTextPaint.setTextSize(DEFAULT_TEXT_SIZE * mScaleFactor);

        mTempRect = new Rect();
    }

    /**
     * Set label gravity. Default is bottom right.
     *
     * @param gravity Desired gravity. Can be combinations, e.g. <code>{@link Gravity#BOTTOM} | {@link
     *                Gravity#LEFT}</code>
     * @return this for chaining
     */
    public DimensionsLabelLayer gravity(int gravity) {
        mGravity = gravity;
        return this;
    }

    /**
     * Set the color of text box background
     *
     * @param color Text box background color, in #AARRGGBB format as usual
     * @return this for chaining
     */
    public DimensionsLabelLayer boxColor(int color) {
        mBackgroundPaint.setColor(color);
        return this;
    }

    /**
     * Set text color of the label
     *
     * @param color Text color, in #AARRGGBB format as usual
     * @return this for chaining
     */
    public DimensionsLabelLayer textColor(int color) {
        mTextPaint.setColor(color);
        return this;
    }

    /**
     * Set text size
     *
     * @param size Text size, in pixels
     * @return this for chaining
     */
    public DimensionsLabelLayer textSize(float size) {
        mTextPaint.setTextSize(size);
        return this;
    }

    @Override
    public void draw(Canvas canvas, Rect drawableBounds) {
        final int intWidth = drawableBounds.width();
        // Make the label text based on width, height, and scale factor
        String text = pxToDipVulgar(intWidth, mScaleFactor)
                + ' ' + MULTIPLY + ' '
                + pxToDipVulgar(drawableBounds.height(), mScaleFactor);

        // Use StaticLayout, which will calculate text dimensions nicely, then position the box using Gravity.apply
        // (although that's one instantiation per draw call...)
        // This is what happens if you're obsessed with perfection like me
        StaticLayout layout = new StaticLayout(text, mTextPaint, intWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
        Gravity.apply(mGravity, (int) (layout.getLineMax(0) + 0.5), layout.getHeight(), drawableBounds, mTempRect);

        // Draw background
        canvas.drawRect(mTempRect, mBackgroundPaint);

        // We have to translate the canvas ourselves, since layout can only draw itself at (0, 0)
        canvas.save();
        canvas.translate(mTempRect.left, mTempRect.top);
        layout.draw(canvas);
        canvas.restore();
    }

    /**
     * Sophisticated conversion of pixels to dips with the use of vulgar fractions (to save screen space)
     *
     * @param px          Pixels to convert to dips
     * @param scaleFactor Scale factor, equal to {@link DisplayMetrics#density}
     * @return String formatted with vulgar fraction if required
     */
    public static String pxToDipVulgar(int px, float scaleFactor) {
        String dip;
        if (scaleFactor == 1) {
            dip = String.valueOf(px);
        } else if (scaleFactor == 2) {
            dip = String.valueOf(px / 2);
            if (px % 2 == 1) {
                dip += ONE_HALF;
            }
        } else if (scaleFactor == 3) {
            dip = String.valueOf(px / 3);
            if (px % 3 == 1) {
                dip += ONE_THIRD;
            } else if (px % 3 == 2) {
                dip += TWO_THIRDS;
            }
        } else if (scaleFactor == 4) {
            dip = String.valueOf(px / 4);
            if (px % 4 == 1) {
                dip += ONE_FOURTH;
            } else if (px % 4 == 2) {
                dip += ONE_HALF;
            } else if (px % 4 == 3) {
                dip += THREE_FOURTHS;
            }
        } else {
            float result = px / scaleFactor;
            float remainder = result - (int) result;
            if (remainder == 0) {
                dip = String.valueOf(px);
            } else if (remainder == 1.0 / 3) {
                dip = String.valueOf(px) + ONE_THIRD;
            } else if (remainder == 2.0 / 3) {
                dip = String.valueOf(px) + TWO_THIRDS;
            } else {
                dip = String.format("%.2f", result);
            }
        }
        return dip;
    }
}
