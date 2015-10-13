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

package com.actinarium.rhythm;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An entry point to Rhythm API, used to control Rhythm groups and communicate with the Quick Control notification
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
    private Context mContext;
    private int mCurrentNotificationGroupIndex = NOTIFICATION_OFF;
    private int mNotificationId;

    /**
     * A list of Rhythm groups registered in this control
     */
    private List<RhythmGroup> mRhythmGroups;

    /**
     * Create a Rhythm manager
     *
     * @param context Current context (usually the {@link Application} object where Rhythm manager setup is performed).
     *                <br>You may actually set this to <code>null</code> if you wish to suppress the quick control
     *                notification.
     */
    public RhythmControl(@Nullable Context context) {
        mContext = context;
        mRhythmGroups = new ArrayList<>();
    }

    /**
     * Make a new Rhythm group that will be controlled by this Rhythm control
     *
     * @param title A convenient title for this group, used to identify it in the notification. Can be <code>null</code>
     *              &mdash; in this case the notification will show group index.
     * @return The created Rhythm group instance, managed by this control
     */
    public RhythmGroup makeGroup(String title) {
        final RhythmGroup group = new RhythmGroup(title);
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
     * @param index index, which was assigned to the required group upon adding
     * @return requested Rhythm group
     */
    public RhythmGroup getGroup(int index) {
        return mRhythmGroups.get(index);
    }

    /**
     * @return the number of groups registered with this control
     */
    public int getGroupCount() {
        return mRhythmGroups.size();
    }

    /**
     * <p>Show the &ldquo;Quick Control&rdquo; notification, which allows to switch configs for any Rhythm controls
     * quickly without navigating away from your app. Usually you would want to call this once during initial
     * configuration (unless you don’t need the notification). If you add controls after the call to {@link
     * #showQuickControl(int)}, the notification will be automatically updated.</p> <p>The notification is dismissible.
     * You can use this method again to bring it back. Upon dismiss, all Rhythm overlays will be hidden.</p>
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
     * Sets all registered drawables in all managed groups to display no Rhythm pattern; sets notification state to
     * hidden
     */
    void onNotificationDismiss() {
        mCurrentNotificationGroupIndex = NOTIFICATION_OFF;
        for (int i = 0, size = mRhythmGroups.size(); i < size; i++) {
            mRhythmGroups.get(i).selectPattern(RhythmGroup.NO_PATTERN);
        }
    }

    /**
     * Should be called whenever notification state is changed (e.g. current group is changed, its pattern is changed)
     */
    void requestNotificationUpdate() {
        if (mCurrentNotificationGroupIndex != NOTIFICATION_OFF) {
            RhythmNotificationService.showNotification(mContext, mNotificationId);
        }
    }

    /**
     * Notification service uses this to obtain information about currently controlled group
     *
     * @return Currently controlled group, or null if no groups attached to this control yet
     */
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
     * The {@link Application} must implement this interface so that it’s possible to access Rhythm API from anywhere in
     * the app, particularly from the Quick Control notification
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
