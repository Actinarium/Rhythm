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
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;


/**
 * An intent service backing Quick Control notification functionality. Requires {@link Application} to implement {@link
 * RhythmControl.Host} to access the application's {@link RhythmControl}
 *
 * @author Paul Danyliuk
 */
public class RhythmNotificationService extends IntentService {

    public static final String ACTION_SHOW_QUICK_CONTROL = "com.actinarium.rhythm.action.SHOW_QUICK_CONTROL";
    public static final String ACTION_NEXT_GROUP = "com.actinarium.rhythm.action.NEXT_GROUP";
    public static final String ACTION_NEXT_PATTERN = "com.actinarium.rhythm.action.NEXT_PATTERN";
    public static final String ACTION_DISMISS_QUICK_CONTROL = "com.actinarium.rhythm.action.DISMISS_QUICK_CONTROL";

    public static final String EXTRA_NOTIFICATION_ID = "com.actinarium.rhythm.extra.NOTIFICATION_ID";

    private static final int NOTIFICATION_ICON_COLOR = 0x6A50A7;
    private static final int NOTIFICATION_ERROR_COLOR = 0xEF4343;

    public RhythmNotificationService() {
        super("RhythmService");
    }

    /**
     * Show the "Quick Control" notification, which allows to switch configs for any Rhythm controls without leaving the
     * app under development. Can be used to update the notification as well.
     *
     * @param context        Used to start the service and build the notification
     * @param notificationId Notification ID, must be unique across the app
     */
    public static void showNotification(Context context, int notificationId) {
        Intent intent = new Intent(context, RhythmNotificationService.class);
        intent.setAction(ACTION_SHOW_QUICK_CONTROL);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_NEXT_PATTERN.equals(action)) {
                handleNextPattern();
            } else if (ACTION_NEXT_GROUP.equals(action)) {
                handleNextGroup();
            } else if (ACTION_SHOW_QUICK_CONTROL.equals(action)) {
                final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, Integer.MIN_VALUE);
                handleShowNotification(notificationId);
            } else if (ACTION_DISMISS_QUICK_CONTROL.equals(action)) {
                handleDismissQuickConfig();
            }
        }
    }

    /**
     * Show sticky "Quick Control" notification for Rhythm
     *
     * @param notificationId Notification ID, must be unique across the app
     */
    private void handleShowNotification(int notificationId) {
        Application application = getApplication();
        NotificationManager manager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        Notification notification;

        // If the application is not the host - show error and return.
        if (!(application instanceof RhythmControl.Host)) {
            notification = makeCommonNotification(getString(R.string.no_host_text))
                    .setColor(NOTIFICATION_ERROR_COLOR)
                    .setContentTitle(getString(R.string.no_host_title))
                    .build();
            manager.notify(notificationId, notification);
            return;
        }

        RhythmControl control = ((RhythmControl.Host) application).getRhythmControl();
        RhythmGroup currentGroup = control.getCurrentNotificationGroup();

        // If there are no groups yet - show warning and return.
        if (currentGroup == null) {
            notification = makeCommonNotification(getString(R.string.no_groups_text))
                    .setColor(NOTIFICATION_ERROR_COLOR)
                    .setContentTitle(getString(R.string.no_groups_title))
                    .build();
            manager.notify(notificationId, notification);
            return;
        }

        // Now if everything's OK:
        Intent toggleGroupAction = new Intent(this, RhythmNotificationService.class);
        toggleGroupAction.setAction(ACTION_NEXT_GROUP);
        PendingIntent piToggleGroupAction = PendingIntent.getService(this, 0, toggleGroupAction, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent togglePatternAction = new Intent(this, RhythmNotificationService.class);
        togglePatternAction.setAction(ACTION_NEXT_PATTERN);
        PendingIntent piTogglePatternAction = PendingIntent.getService(this, 0, togglePatternAction, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dismissAction = new Intent(this, RhythmNotificationService.class);
        dismissAction.setAction(ACTION_DISMISS_QUICK_CONTROL);
        PendingIntent piDismissAction = PendingIntent.getService(this, 0, dismissAction, PendingIntent.FLAG_UPDATE_CURRENT);

        // todo: another action when notification is clicked (control activity will be added in 1.0)

        // Determine what to write in notification
        RhythmPattern currentPattern = currentGroup.getCurrentPattern();
        String groupText = getString(R.string.group, currentGroup.toString());
        String patternText = currentPattern == null ?
                getString(R.string.no_pattern) : getString(R.string.pattern, currentPattern.toString());

        // Finally, build and display the notification
        notification = makeCommonNotification(patternText)
                .setColor(NOTIFICATION_ICON_COLOR)
                .setContentTitle(groupText)
                .setDeleteIntent(piDismissAction)
                .addAction(new NotificationCompat.Action(R.drawable.ic_loop, getString(R.string.next_group), piToggleGroupAction))
                .addAction(new NotificationCompat.Action(R.drawable.ic_loop, getString(R.string.next_pattern), piTogglePatternAction))
                .build();
        manager.notify(notificationId, notification);
    }

    private NotificationCompat.Builder makeCommonNotification(String text) {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_rhythm)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));
    }

    private void handleNextGroup() {
        Application application = getApplication();
        if (application instanceof RhythmControl.Host) {
            final RhythmControl rhythmControl = ((RhythmControl.Host) application).getRhythmControl();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new NextGroupRunnable(rhythmControl));
        }
    }

    private void handleNextPattern() {
        Application application = getApplication();
        if (application instanceof RhythmControl.Host) {
            final RhythmControl rhythmControl = ((RhythmControl.Host) application).getRhythmControl();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new NextPatternRunnable(rhythmControl.getCurrentNotificationGroup()));
        }
    }

    private void handleDismissQuickConfig() {
        Application application = getApplication();
        if (application instanceof RhythmControl.Host) {
            final RhythmControl rhythmControl = ((RhythmControl.Host) application).getRhythmControl();
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new HideAllPatternsRunnable(rhythmControl));
        }
    }

    /**
     * Runnable to dispatch next group call to the UI thread
     */
    private static class NextGroupRunnable implements Runnable {

        private RhythmControl mControl;

        public NextGroupRunnable(RhythmControl control) {
            mControl = control;
        }

        @Override
        public void run() {
            mControl.selectNextNotificationGroup();
        }
    }

    /**
     * Runnable to dispatch next pattern call to the UI thread
     */
    private static class NextPatternRunnable implements Runnable {

        private RhythmGroup mGroup;

        public NextPatternRunnable(RhythmGroup group) {
            mGroup = group;
        }

        @Override
        public void run() {
            mGroup.selectNextPattern();
        }
    }

    /**
     * Runnable to dispatch hide all patterns call to the UI thread
     */
    private static class HideAllPatternsRunnable implements Runnable {

        private RhythmControl mControl;

        public HideAllPatternsRunnable(RhythmControl control) {
            mControl = control;
        }

        @Override
        public void run() {
            mControl.onNotificationDismiss();
        }
    }
}
