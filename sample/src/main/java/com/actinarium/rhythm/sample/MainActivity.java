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

import android.app.ActivityManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.actinarium.rhythm.RhythmControl;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting up toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setElevation(getResources().getDimension(R.dimen.actionBarElevation));
        actionBar.setTitle(R.string.app_name);

        // Setting up Recents card icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            int color = typedValue.data;

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_rhythm);
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(null, bm, color);

            setTaskDescription(td);
            bm.recycle();
        }

        View view = findViewById(R.id.scrollViewContent);
        FrameLayout subView = (FrameLayout) findViewById(R.id.subframe);
        final RhythmControl rhythmControl = ((RhythmSampleApplication) getApplication()).getRhythmControl();

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.line);
        rhythmControl.getGroup(0).decorate(view);
        rhythmControl.getGroup(1).decorate(linearLayout.getChildAt(0), linearLayout.getChildAt(1), linearLayout.getChildAt(2), linearLayout.getChildAt(3), linearLayout.getChildAt(4));
        rhythmControl.getGroup(1).decorateForeground(subView);
    }
}
