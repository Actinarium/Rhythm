/*
 * Copyright (C) 2015 Actinarium
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

package com.actinarium.rhythm.sample;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.actinarium.rhythm.sample.util.MyBulletSpan;
import com.actinarium.rhythm.sample.util.TextViewUtils;

/**
 * A dialog fragment showcasing how Rhythm works with dialogs as well
 *
 * @author Paul Danyliuk
 */
public class FeaturesDialogFragment extends DialogFragment {

    public static final String TAG = "FeaturesDialogFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_features, null);

        // Build a bullet list (I still have no idea why it has to be so hard)
        String[] bulletPoints = getResources().getStringArray(R.array.bullet_points);
        // Add an emoji to the last point
        bulletPoints[bulletPoints.length - 1] += " \uD83D\uDE09";
        final int bulletRadius = getResources().getDimensionPixelSize(R.dimen.bulletRadius);
        final int bulletLeftPadding = getResources().getDimensionPixelSize(R.dimen.bulletLeftPadding);
        final int bulletRightPadding = getResources().getDimensionPixelSize(R.dimen.bulletRightPadding);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int spanStart = 0;
        int spanEnd;
        for (String bulletPoint : bulletPoints) {
            builder.append(bulletPoint);
            spanEnd = builder.length();
            builder.setSpan(new MyBulletSpan(bulletRadius, bulletLeftPadding, bulletRightPadding), spanStart, spanEnd, 0);
            spanStart = spanEnd;
        }
        TextView textView = (TextView) view.findViewById(R.id.bullet_list);
        textView.setText(builder);

        // Fix text leading
        final int step = getResources().getDimensionPixelSize(R.dimen.baselineStep);
        final int leading20dp = getResources().getDimensionPixelSize(R.dimen.leading20);
        TextViewUtils.setLeading(textView, step, leading20dp);

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.layer_types)
                .setView(view)
                .setPositiveButton(R.string.got_it, null)
                .create();

        // - Wait, that's it? Where's the getRhythmControl()? Where's decorate()?
        // - You don't need it if you use RhythmicFrameLayout to wrap your views. Take a look at dialog_features.xml
    }
}
