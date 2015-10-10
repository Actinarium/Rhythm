package com.actinarium.rhythm;

import android.app.Application;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;


/**
 *
 * @author Paul Danyliuk
 */
public class RhythmNotificationService extends IntentService {

    public static final String ACTION_SHOW_NOTIFICATION = "com.actinarium.rhythm.action.SHOW_NOTIFICATION";
    public static final String ACTION_TOGGLE_CONFIGURATION = "com.actinarium.rhythm.action.TOGGLE_CONFIGURATION";

    public static final String EXTRA_NOTIFICATION_ID = "com.actinarium.rhythm.extra.NOTIFICATION_ID";
    public static final String EXTRA_MANAGER_ID = "com.actinarium.rhythm.extra.MANAGER_ID";

    public RhythmNotificationService() {
        super("RhythmService");
    }

    /**
     * Show the "Quick Control" notification, which allows to switch configs for any Rhythm controls without leaving
     * the app under development.
     *
     * @param context        Used to start the service and build the notification
     * @param notificationId Notification ID, must be unique across the app
     */
    public static void showNotification(Context context, int notificationId) {
        Intent intent = new Intent(context, RhythmNotificationService.class);
        intent.setAction(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TOGGLE_CONFIGURATION.equals(action)) {
                final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, Integer.MIN_VALUE);
                final int managerId = intent.getIntExtra(EXTRA_MANAGER_ID, 0);
                handleToggleConfiguration(notificationId, managerId);
            } else if (ACTION_SHOW_NOTIFICATION.equals(action)) {
                final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, Integer.MIN_VALUE);
                handleShowNotification(notificationId);
            }
        }
    }

    /**
     * Show sticky "Quick Control" notification for Rhythm
     * @param notificationId Notification ID, must be unique across the app
     */
    private void handleShowNotification(int notificationId) {
        Intent toggleAction = new Intent(this, RhythmNotificationService.class);
        toggleAction.setAction(ACTION_TOGGLE_CONFIGURATION);
        toggleAction.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent piToggleAction = PendingIntent.getService(this, 0, toggleAction, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_grid)
                .setColor(Color.BLACK)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setContentTitle("Control: Cards and such and such")
                .setContentText("Config: 4dp baseline grid")
                .addAction(new NotificationCompat.Action(R.drawable.ic_next, "Control", piToggleAction))
                .addAction(new NotificationCompat.Action(R.drawable.ic_next, "Config", piToggleAction));

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    /**
     * React to a mode toggle: update overlays associated with given manager and the notification
     * @param notificationId ID of notification to update
     * @param managerId      ID of Rhythm manager to delegate overlay change request to
     */
    private void handleToggleConfiguration(int notificationId, int managerId) {
        Application application = getApplication();
        if (application instanceof RhythmManager.Host) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new ToggleConfigRunnable(((RhythmManager.Host) application).getRhythmManager().getControl(0)));
        }
    }

    /**
     * Runnable to dispatch update configuration call to the UI thread
     */
    private static class ToggleConfigRunnable implements Runnable {

        private RhythmControl mControl;

        public ToggleConfigRunnable(RhythmControl control) {
            mControl = control;
        }

        @Override
        public void run() {
            mControl.selectNextConfig();
        }
    }
}
