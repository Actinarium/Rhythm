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

package com.actinarium.rhythm.control;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A controller that interconnects {@link RhythmGroup}s, {@link RhythmFrameLayout}s, and the Quick Control
 * notification, and should be used as an entry point to accessing Rhythm library programmatically. For proper function
 * a singleton Rhythm control must be accessible from ApplicationContext (i.e. the app’s {@link Application} object must
 * implement {@link Host}).</p><p><b>Note:</b> if you don’t need the notification or <code>RhythmicFrameLayouts</code>,
 * you might actually not need a Rhythm control in your project &mdash; just instantiate and use {@link RhythmGroup}s
 * directly.</p>
 *
 * @author Paul Danyliuk
 */
public final class RhythmControl {

    public static int NOTIFICATION_OFF = -2;
    public static int NOTIFICATION_NO_GROUPS = -1;

    /**
     * Context is used for the sole purpose of talking to the {@link RhythmNotificationService}. I sure hope it’s not
     * leaking.
     */
    @Nullable
    private Context mContext;
    private int mCurrentNotificationGroupIndex = NOTIFICATION_OFF;
    private int mNotificationId;

    /**
     * A list of Rhythm groups registered in this control
     */
    private List<RhythmGroup> mRhythmGroups;

    /**
     * Create a Rhythm control. Normally you shouldn’t create more than one Rhythm control in your application.
     *
     * @param context Current context (usually the {@link Application} object where Rhythm control setup is performed).
     *                <br>You may actually set this to <code>null</code> if you don’t need the Quick Control
     *                notification.
     */
    public RhythmControl(@Nullable Context context) {
        mContext = context;
        mRhythmGroups = new ArrayList<>();
    }

    /**
     * Make a new Rhythm group, registered in this Rhythm control
     *
     * @param title A convenient title for this group to identify it in the notification. Not mandatory (can be
     *              <code>null</code>) but recommended.
     * @return The created Rhythm group instance, managed by this control
     */
    public RhythmGroup makeGroup(String title) {
        final RhythmGroup group = new RhythmGroup();
        group.mTitle = title;
        group.mIndex = mRhythmGroups.size();
        group.mControl = this;
        mRhythmGroups.add(group);

        // If this was the first group, and the notification is already shown, set it to display the first group
        if (mCurrentNotificationGroupIndex == NOTIFICATION_NO_GROUPS) {
            mCurrentNotificationGroupIndex = 0;
            requestNotificationUpdate();
        }
        return group;
    }

    /**
     * Get Rhythm group at requested index
     *
     * @param index index of the group (0, 1, 2... in order of adding)
     * @return requested Rhythm group
     */
    public RhythmGroup getGroup(int index) {
        return mRhythmGroups.get(index);
    }

    /**
     * @return the number of groups registered in this control
     */
    public int getGroupCount() {
        return mRhythmGroups.size();
    }

    /**
     * <p>Show the &ldquo;Quick Control&rdquo; notification, which allows to switch overlays for all registered Rhythm
     * groups quickly without navigating away from your app. Usually you would want to call this once during initial
     * configuration (unless you don’t need the notification).</p> <p><b>Note:</b> Quick Control notification is
     * dismissible.  Upon dismiss, all Rhythm overlays will be hidden. There’s no way to bring it back other than kill
     * and restart the application, unless you explicitly create a button, a menu option etc in your application that
     * would conjure the notification again by calling this method.</p>
     *
     * @param notificationId ID for Rhythm notification, must be unique across the app
     */
    public void showQuickControl(int notificationId) {
        if (mContext == null) {
            return;
        }

        // Remember the notification ID for reuse on update
        mNotificationId = notificationId;

        // If notification isn't displayed already, display the first group (or no groups notice)
        if (mCurrentNotificationGroupIndex == NOTIFICATION_OFF) {
            mCurrentNotificationGroupIndex = mRhythmGroups.isEmpty() ? NOTIFICATION_NO_GROUPS : 0;
        }
        requestNotificationUpdate();
    }

    /**
     * Sets all registered drawables in all managed groups to display no Rhythm overlays; sets notification state to
     * hidden
     */
    void onNotificationDismiss() {
        mCurrentNotificationGroupIndex = NOTIFICATION_OFF;
        for (int i = 0, size = mRhythmGroups.size(); i < size; i++) {
            mRhythmGroups.get(i).selectOverlay(RhythmGroup.NO_OVERLAY);
        }
    }

    /**
     * Should be called whenever notification state is changed (e.g. when cycling through the groups or overlays)
     */
    void requestNotificationUpdate() {
        if (mCurrentNotificationGroupIndex != NOTIFICATION_OFF) {
            RhythmNotificationService.showNotification(mContext, mNotificationId);
        }
    }

    RhythmGroup getCurrentNotificationGroup() {
        return mCurrentNotificationGroupIndex < 0 ? null : mRhythmGroups.get(mCurrentNotificationGroupIndex);
    }

    void selectNextNotificationGroup() {
        // Assume that this method can be called only when valid notification is displayed for a group with index >= 0

        // Increment by 1 and wrap if that was the last one. And request notification update
        mCurrentNotificationGroupIndex = ++mCurrentNotificationGroupIndex % mRhythmGroups.size();
        requestNotificationUpdate();
    }

    /**
     * The {@link Application} must implement this interface to provide the singleton {@link RhythmControl} instance
     * through its method {@link #getRhythmControl()} to {@link RhythmFrameLayout}s and the Quick Control notification
     */
    public interface Host {

        /**
         * Get the {@link RhythmControl} of this application to access any {@link RhythmGroup} and the rest of Rhythm
         * API
         *
         * @return Rhythm control associated with this application
         */
        RhythmControl getRhythmControl();

    }

}
