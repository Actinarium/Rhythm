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
public class BulletSpan implements LeadingMarginSpan {
    private final int mBulletRadius;
    private final int mBulletCenterX;
    private final int mLeadingMargin;
    private final boolean mWantColor;
    private final int mColor;

    private static Path sBulletPath = null;

    public BulletSpan(int bulletRadius, int bulletCenterX, int leadingMargin) {
        mBulletRadius = bulletRadius;
        mBulletCenterX = bulletCenterX;
        mLeadingMargin = leadingMargin;
        mWantColor = false;
        mColor = 0;
    }

    public BulletSpan(Parcel src) {
        mBulletRadius = src.readInt();
        mBulletCenterX = src.readInt();
        mLeadingMargin = src.readInt();
        mWantColor = src.readInt() != 0;
        mColor = src.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mBulletRadius);
        dest.writeInt(mBulletCenterX);
        dest.writeInt(mLeadingMargin);
        dest.writeInt(mWantColor ? 1 : 0);
        dest.writeInt(mColor);
    }

    public int getLeadingMargin(boolean first) {
        return mLeadingMargin;
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end,
                                  boolean first, Layout l) {
        if (((Spanned) text).getSpanStart(this) == start) {
            Paint.Style style = p.getStyle();
            int oldColor = 0;

            if (mWantColor) {
                oldColor = p.getColor();
                p.setColor(mColor);
            }

            p.setStyle(Paint.Style.FILL);

            if (Build.VERSION.SDK_INT >= 11 && c.isHardwareAccelerated()) {
                if (sBulletPath == null) {
                    sBulletPath = new Path();
                    sBulletPath.addCircle(0.0f, 0.0f, mBulletRadius, Path.Direction.CW);
                }

                c.save();
                c.translate(x + dir * mBulletCenterX, (top + bottom) / 2.0f);
                c.drawPath(sBulletPath, p);
                c.restore();
            } else {
                c.drawCircle(x + dir * mBulletCenterX, (top + bottom) / 2.0f, mBulletRadius, p);
            }

            if (mWantColor) {
                p.setColor(oldColor);
            }

            p.setStyle(style);
        }
    }
}
