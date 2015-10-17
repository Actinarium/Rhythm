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
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.actinarium.rhythm.RhythmControl;
import com.actinarium.rhythm.sample.util.MyBulletSpan;

import static com.actinarium.rhythm.sample.RhythmShowcaseApplication.ACTIVITY_OVERLAY_GROUP;
import static com.actinarium.rhythm.sample.RhythmShowcaseApplication.CARD_OVERLAY_GROUP;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Rhythm-unrelated init routines
        setupToolbar();
        setupRecentsIcon();
        setupInteractivity();

        // Find required layouts
        LinearLayout contentView = (LinearLayout) findViewById(R.id.content);
        CardView cardView = (CardView) findViewById(R.id.card);

        // Setup Rhythm
        RhythmControl rhythmControl = ((RhythmShowcaseApplication) getApplication()).getRhythmControl();
        rhythmControl.getGroup(ACTIVITY_OVERLAY_GROUP).decorate(contentView);
        rhythmControl.getGroup(CARD_OVERLAY_GROUP).decorateForeground(cardView);

    }

    // Methods for setup, not really relevant to showcasing Rhythm

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setElevation(getResources().getDimension(R.dimen.actionBarElevation));
        actionBar.setTitle(R.string.app_name);
    }

    private void setupRecentsIcon() {
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
    }

    private void setupInteractivity() {
        // Make links clickable
        ((TextView) findViewById(R.id.copy_1)).setMovementMethod(LinkMovementMethod.getInstance());

        // Make card buttons responsible
        findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, R.string.agree_msg, Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.disagree).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, R.string.disagree_msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Build a bullet list (I still have no idea why it has to be so hard)
        String[] bulletPoints = getResources().getStringArray(R.array.bullet_points);
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
        TextView textView = (TextView) findViewById(R.id.bullet_list);
        textView.setText(builder);

    }
}
