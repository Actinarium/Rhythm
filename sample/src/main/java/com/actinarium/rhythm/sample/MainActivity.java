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

import android.app.ActivityManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.actinarium.rhythm.RhythmControl;
import com.actinarium.rhythm.RhythmDrawable;
import com.actinarium.rhythm.RhythmGroup;
import com.actinarium.rhythm.RhythmOverlay;
import com.actinarium.rhythm.sample.util.TextViewUtils;
import com.actinarium.rhythm.spec.DimensionsLabel;
import com.actinarium.rhythm.spec.Guide;

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
        fixTextLeading();

        // Find required layouts
        LinearLayout contentView = (LinearLayout) findViewById(R.id.content);
        CardView cardView = (CardView) findViewById(R.id.card);
        LinearLayout mallowsView = (LinearLayout) findViewById(R.id.mallows);

        // Setup Rhythm
        // First let's attach the activity and the card to the groups defined in app's RhythmControl
        RhythmControl rhythmControl = ((RhythmShowcaseApplication) getApplication()).getRhythmControl();

        // Decorate the provided views' backgrounds (any views) - draw overlays UNDER content
        rhythmControl.getGroup(ACTIVITY_OVERLAY_GROUP).decorate(contentView);
        // Decorate the provided views' foregrounds (only for FrameLayouts) - draw overlays OVER content
        rhythmControl.getGroup(CARD_OVERLAY_GROUP).decorateForeground(cardView);

        // Now let's create an unlinked group (i.e. not attached to the control) and draw some dimensions over mallows
        // Note that when not using this group/overlay in notification, titles don't really matter, so leave them null
        RhythmGroup mallowsGroup = new RhythmGroup(null);
        final int accentColor = getResources().getColor(R.color.accent);
        RhythmOverlay frameAndDimensions = new RhythmOverlay(null)
                .addLayer(new Guide(Gravity.LEFT, 0).alignOutside(true).color(accentColor))
                .addLayer(new Guide(Gravity.TOP, 0).alignOutside(true).color(accentColor))
                .addLayer(new Guide(Gravity.BOTTOM, 0).alignOutside(true).color(accentColor))
                .addLayer(new DimensionsLabel(getResources().getDisplayMetrics().density))
                .addToGroup(mallowsGroup);

        // Decorate a few mallows with this group
        mallowsGroup.decorate(mallowsView.getChildAt(0), mallowsView.getChildAt(1), mallowsView.getChildAt(2));

        // Or get a Drawable and use it explicitly
        Drawable drawable = mallowsGroup.makeDrawable();
        mallowsView.getChildAt(3).setBackgroundDrawable(drawable);

        // Furthermore, you may not even need groups - for full manual transmission make RhythmDrawables explicitly
        RhythmDrawable totallyExplicitlyCreatedDrawable = new RhythmDrawable();
        RhythmOverlay lastFrameOverlay = new RhythmOverlay(null)
                .addLayersFrom(frameAndDimensions)
                .addLayer(new Guide(Gravity.RIGHT, 0).alignOutside(true).color(accentColor));
        totallyExplicitlyCreatedDrawable.setOverlay(lastFrameOverlay);
        mallowsView.getChildAt(4).setBackgroundDrawable(totallyExplicitlyCreatedDrawable);

        // Take a look at FeaturesDialogFragment and its layout for RhythmicFrameLayout example
    }

    // Methods for setup, not really relevant to showcasing Rhythm

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setElevation(getResources().getDimension(R.dimen.actionBarElevation));
        actionBar.setTitle(R.string.app_title);
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
        ((TextView) findViewById(R.id.copy_6)).setMovementMethod(LinkMovementMethod.getInstance());

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

        // Show dialog button
        findViewById(R.id.show_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeaturesDialogFragment fragment = new FeaturesDialogFragment();
                fragment.show(getSupportFragmentManager(), FeaturesDialogFragment.TAG);
            }
        });
    }

    private void fixTextLeading() {
        final int step = getResources().getDimensionPixelSize(R.dimen.baselineStep);
        final int leading20dp = getResources().getDimensionPixelSize(R.dimen.leading20);
        final int leading32dp = getResources().getDimensionPixelSize(R.dimen.leading32);
        TextViewUtils.setLeading((TextView) findViewById(R.id.content_title), step, leading32dp);
        TextViewUtils.setLeading((TextView) findViewById(R.id.copy_1), step, leading20dp);
        TextViewUtils.setLeading((TextView) findViewById(R.id.copy_2), step, leading20dp);
        TextViewUtils.setLeading((TextView) findViewById(R.id.copy_3), step, leading20dp);
        TextViewUtils.setLeading((TextView) findViewById(R.id.copy_4), step, leading20dp);
        TextViewUtils.setLeading((TextView) findViewById(R.id.copy_5), step, leading20dp);
        TextViewUtils.setLeading((TextView) findViewById(R.id.copy_6), step, leading20dp);
        TextViewUtils.setLeading((TextView) findViewById(R.id.card_title), step, leading20dp);
        TextViewUtils.setLeading((TextView) findViewById(R.id.card_copy), step, leading20dp);
    }
}
