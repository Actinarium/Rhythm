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

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.actinarium.rhythm.RhythmControl;
import com.actinarium.rhythm.RhythmGroup;
import com.actinarium.rhythm.config.RhythmOverlayInflater;

import static com.actinarium.rhythm.sample.RhythmSampleApplication.CARD_OVERLAY_GROUP;
import static com.actinarium.rhythm.sample.RhythmSampleApplication.CONTENT_OVERLAY_GROUP;
import static com.actinarium.rhythm.sample.RhythmSampleApplication.TEXT_OVERLAY_GROUP;

public class MainActivity extends AppCompatActivity {

    private RhythmSandbox mSandbox;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Rhythm-unrelated init routines
        setupToolbar();
        setupInteractivity(savedInstanceState);

        // Find required layouts
        final LinearLayout contentView = (LinearLayout) findViewById(R.id.content);
        final CardView cardView = (CardView) findViewById(R.id.card);

        // Get the groups from the app's RhythmControl.
        // These groups are already configured with respectable overlays in RhythmSampleApplication#onCreate
        // All that's left is to link these groups to appropriate views
        final RhythmControl rhythmControl = ((RhythmSampleApplication) getApplication()).getRhythmControl();
        final RhythmGroup contentOverlayGroup = rhythmControl.getGroup(CONTENT_OVERLAY_GROUP);
        final RhythmGroup cardOverlayGroup = rhythmControl.getGroup(CARD_OVERLAY_GROUP);
        final RhythmGroup textOverlayGroup = rhythmControl.getGroup(TEXT_OVERLAY_GROUP);

        // Decorate the background of our topmost scrollable layout (LinearLayout) to draw overlays from the 1st group
        // The decorate() method works with all views and draws overlay UNDER content (over existing background if any)
        contentOverlayGroup.decorate(contentView /*, view2, view3... */);

        // Decorate the foreground of our intermission card with the overlay from the 2nd group
        // If you have FrameLayout or its child classes, you can use decorateForeground() to draw overlays OVER content
        cardOverlayGroup.decorateForeground(cardView /*, frameLayout2, frameLayout3... */);

        // Decorate all text views with overlays attached to the 3rd group
        for (int i = 0, count = contentView.getChildCount(); i < count; i++) {
            final View child = contentView.getChildAt(i);
            if (child instanceof com.actinarium.aligned.TextView) {
                textOverlayGroup.decorate(child);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSandbox.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSandbox.onRestoreInstanceState(savedInstanceState);
    }

    // Methods for setup, not really relevant to teaching you how to set up Rhythm
    // If you're interested how Rhythm Sandbox works, feel free to check out the eponymous class

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setElevation(getResources().getDimension(R.dimen.actionBarElevation));
        actionBar.setTitle(R.string.app_title);
    }

    private void setupInteractivity(Bundle savedInstanceState) {
        final RhythmSampleApplication application = (RhythmSampleApplication) getApplication();

        // Make links clickable
        ((TextView) findViewById(R.id.copy_2)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.copy_6)).setMovementMethod(LinkMovementMethod.getInstance());

        // Setup the sandbox presenter - the piece of UI where you can play around with overlay config at runtime.
        // It needs an inflater - we could build a new one, but we already have the one prepared in RhythmSampleApplication
        final RhythmOverlayInflater inflater = application.getRhythmOverlayInflater();
        final View sandboxRootView = findViewById(R.id.sandbox);
        mSandbox = new RhythmSandbox(this, sandboxRootView, inflater);
        if (savedInstanceState == null) {
            // Call this so that the presenter prepares its initial state
            mSandbox.onRestoreInstanceState(null);
        }

        // Set up "Toggle overlay" button in Intermission card
        // Usually you'll switch overlays via a notification, but here's the demo of how to do it programmatically
        final RhythmGroup cardOverlayGroup = application.getRhythmControl().getGroup(CARD_OVERLAY_GROUP);
        findViewById(R.id.toggle_card_overlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Since there is only one card overlay, selectNextOverlay() will cycle through that one and NO_OVERLAY
                cardOverlayGroup.selectNextOverlay();
            }
        });
    }
}
