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

package com.actinarium.rhythm.sample.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Parcel;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.LeadingMarginSpan;

/**
 * Bullet list span, copied from SDK but made more customizable
 *
 * @author Paul Danyliuk
 */
public class MyBulletSpan implements LeadingMarginSpan {
    private final int mBulletRadius;
    private final int mBulletLeftPadding;
    private final int mBulletRightPadding;
    private final boolean mWantColor;
    private final int mColor;

    private static Path sBulletPath = null;
    private static final int STANDARD_BULLET_RADIUS = 4;
    private static final int STANDARD_GAP_WIDTH = 8;

    public MyBulletSpan() {
        mBulletRadius = STANDARD_BULLET_RADIUS;
        mBulletLeftPadding = STANDARD_GAP_WIDTH;
        mBulletRightPadding = STANDARD_GAP_WIDTH;
        mWantColor = false;
        mColor = 0;
    }

    public MyBulletSpan(int bulletRadius, int bulletLeftPadding, int bulletRightPadding) {
        mBulletRadius = bulletRadius;
        mBulletLeftPadding = bulletLeftPadding;
        mBulletRightPadding = bulletRightPadding;
        mWantColor = false;
        mColor = 0;
    }

    public MyBulletSpan(Parcel src) {
        mBulletRadius = src.readInt();
        mBulletLeftPadding = src.readInt();
        mBulletRightPadding = src.readInt();
        mWantColor = src.readInt() != 0;
        mColor = src.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mBulletRadius);
        dest.writeInt(mBulletLeftPadding);
        dest.writeInt(mBulletRightPadding);
        dest.writeInt(mWantColor ? 1 : 0);
        dest.writeInt(mColor);
    }

    public int getLeadingMargin(boolean first) {
        return 2 * mBulletRadius + mBulletLeftPadding + mBulletRightPadding;
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout l) {
        if (((Spanned) text).getSpanStart(this) == start) {
            Paint.Style style = p.getStyle();
            int oldcolor = 0;

            if (mWantColor) {
                oldcolor = p.getColor();
                p.setColor(mColor);
            }

            p.setStyle(Paint.Style.FILL);

            if (Build.VERSION.SDK_INT >= 11 && c.isHardwareAccelerated()) {
                if (sBulletPath == null) {
                    sBulletPath = new Path();
                    sBulletPath.addCircle(0.0f, 0.0f, mBulletRadius, Path.Direction.CW);
                }

                c.save();
                c.translate(x + dir * mBulletRadius + mBulletLeftPadding, (top + bottom) / 2.0f);
                c.drawPath(sBulletPath, p);
                c.restore();
            } else {
                c.drawCircle(x + dir * mBulletRadius + mBulletLeftPadding, (top + bottom) / 2.0f, mBulletRadius, p);
            }

            if (mWantColor) {
                p.setColor(oldcolor);
            }

            p.setStyle(style);
        }
    }
}
