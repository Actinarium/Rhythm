package com.actinarium.rhythm;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * <p></p>
 *
 * @author Paul Danyliuk
 * @version $Id$
 */
public class RhythmDrawable extends Drawable {

    private static final int DEFAULT_INCREMENT = 4 * 3;

    protected Paint mPaint;
    private int mIncrement;

    public RhythmDrawable() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mPaint.setAlpha(127);

        mIncrement = DEFAULT_INCREMENT;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        int curY = bounds.top;
        while (curY <= bounds.bottom) {
            canvas.drawLine(bounds.left, curY, bounds.right, curY, mPaint);
            curY += mIncrement;
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public void setConfiguration(RhythmConfiguration configuration) {
        mIncrement = configuration.mIncrement;
        invalidateSelf();
    }
}
