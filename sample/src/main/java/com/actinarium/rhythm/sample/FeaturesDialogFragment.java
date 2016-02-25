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

package com.actinarium.rhythm.sample;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.actinarium.rhythm.sample.util.ViewUtils;

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

        // Build a bullet list
        String[] bulletPoints = getResources().getStringArray(R.array.bullet_points);
        final int bulletRadius = getResources().getDimensionPixelSize(R.dimen.bulletRadius);
        final int bulletCenterX = getResources().getDimensionPixelSize(R.dimen.bulletCenterX);
        final int leadingMargin = getResources().getDimensionPixelSize(R.dimen.bulletLeadingMargin);
        CharSequence bulletList = ViewUtils.makeBulletList(bulletRadius, bulletCenterX, leadingMargin, bulletPoints);
        TextView textView = (TextView) view.findViewById(R.id.bullet_list);
        textView.setText(bulletList);

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.layer_types)
                .setView(view)
                .setPositiveButton(R.string.got_it, null)
                .create();

        // - Wait, that's it? Where's the getRhythmControl()? Where's decorate()?
        // - You don't need it if you use RhythmicFrameLayout to wrap your views.
        // Take a look at /res/layout/dialog_features.xml
    }
}
