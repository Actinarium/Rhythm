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
    public static final String ACTION_HIDE_NOTIFICATION = "com.actinarium.rhythm.action.HIDE_NOTIFICATION";
    public static final String ACTION_TOGGLE_CONFIGURATION = "com.actinarium.rhythm.action.TOGGLE_CONFIGURATION";

    public static final String EXTRA_NOTIFICATION_ID = "com.actinarium.rhythm.extra.NOTIFICATION_ID";
    public static final String EXTRA_MANAGER_ID = "com.actinarium.rhythm.extra.MANAGER_ID";

    public RhythmNotificationService() {
        super("RhythmService");
    }

    public static void showNotification(Context context, int notificationId, int managerId) {
        Intent intent = new Intent(context, RhythmNotificationService.class);
        intent.setAction(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        intent.putExtra(EXTRA_MANAGER_ID, managerId);
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
                final int managerId = intent.getIntExtra(EXTRA_MANAGER_ID, 0);
                handleShowNotification(notificationId, managerId);
            } else if (ACTION_HIDE_NOTIFICATION.equals(action)) {
                final int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, Integer.MIN_VALUE);
                handleHideNotification(notificationId);
            }
        }
    }

    /**
     * Show sticky notification for controlling a Rhythm manager
     * @param notificationId Notification ID, must be unique across the app where Rhythm library is used
     * @param managerId      ID of Rhythm manager to associate this notification with
     */
    private void handleShowNotification(int notificationId, int managerId) {
        Intent toggleAction = new Intent(this, RhythmNotificationService.class);
        toggleAction.setAction(ACTION_TOGGLE_CONFIGURATION);
        toggleAction.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        toggleAction.putExtra(EXTRA_MANAGER_ID, managerId);
        PendingIntent piToggleAction = PendingIntent.getService(this, 0, toggleAction, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_grid_on_white_24dp)
                .setColor(Color.BLACK)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setContentTitle("Rhythm notification")
                .setContentText("Click to select next mode")
                .addAction(new NotificationCompat.Action(R.drawable.ic_grid_on_white_24dp, "Toggle config", piToggleAction));
//                .setContentIntent(piToggleAction);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    /**
     * H
     * @param notificationId
     */
    private void handleHideNotification(int notificationId) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }

    /**
     * React to a mode toggle: update overlays associated with given manager and the notification
     * @param notificationId ID of notification to update
     * @param managerId      ID of Rhythm manager to delegate overlay change request to
     */
    private void handleToggleConfiguration(int notificationId, int managerId) {
        Application application = getApplication();
        if (application instanceof Rhythmic) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new ToggleConfigRunnable(((Rhythmic) application).getRhythmManager()));
        }
    }

    /**
     * Runnable to dispatch update configuration call to the UI thread
     */
    private static class ToggleConfigRunnable implements Runnable {

        private RhythmManager mManager;

        public ToggleConfigRunnable(RhythmManager manager) {
            mManager = manager;
        }

        @Override
        public void run() {
            mManager.updateConfiguration();
        }
    }
}
