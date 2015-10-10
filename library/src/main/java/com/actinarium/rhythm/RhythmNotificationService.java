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
 *
 * @author Paul Danyliuk
 */
public class RhythmNotificationService extends IntentService {

    public static final String ACTION_SHOW_NOTIFICATION = "com.actinarium.rhythm.action.SHOW_NOTIFICATION";
    public static final String ACTION_NEXT_GROUP = "com.actinarium.rhythm.action.NEXT_GROUP";
    public static final String ACTION_NEXT_CONFIG = "com.actinarium.rhythm.action.NEXT_CONFIG";

    public static final String EXTRA_NOTIFICATION_ID = "com.actinarium.rhythm.extra.NOTIFICATION_ID";
    public static final String EXTRA_MANAGER_ID = "com.actinarium.rhythm.extra.MANAGER_ID";

    private static final int NOTIFICATION_ICON_COLOR = 0xEF43A7;
    private static final int NOTIFICATION_ERROR_COLOR = 0xEF4343;

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
            if (ACTION_NEXT_CONFIG.equals(action)) {
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
        Application application = getApplication();
        Notification notification;
        if (application instanceof RhythmManager.Host) {
            notification = makeWarningNotification();
        } else {

        }


        Intent toggleAction = new Intent(this, RhythmNotificationService.class);
        toggleAction.setAction(ACTION_NEXT_CONFIG);
        toggleAction.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent piToggleAction = PendingIntent.getService(this, 0, toggleAction, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_grid)
                .setColor(NOTIFICATION_ICON_COLOR)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setContentTitle("Control: Cards and such and such")
                .setContentText("Config: 4dp baseline grid")
                .addAction(new NotificationCompat.Action(R.drawable.ic_next, "Next config", piToggleAction))
                .addAction(new NotificationCompat.Action(R.drawable.ic_next, "Next control", piToggleAction));

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    private Notification makeWarningNotification() {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_grid)
                .setColor(NOTIFICATION_ERROR_COLOR)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setContentTitle(getString(R.string.notif_error_title))
                .setContentText(getString(R.string.notif_error_text))
                .setStyle(new NotificationCompat.BigTextStyle())
                .build();
    }

    private Notification makeControlNotification(String currentControl, String currentConfig) {
        Intent nextConfigIntent = new Intent(this, RhythmNotificationService.class);

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_grid)
                .setColor(NOTIFICATION_ICON_COLOR)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setContentTitle(getString(R.string.notif_control, currentControl))
                .setContentText(getString(R.string.notif_config, currentConfig))
                .build();
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

        private RhythmGroup mControl;

        public ToggleConfigRunnable(RhythmGroup control) {
            mControl = control;
        }

        @Override
        public void run() {
            mControl.selectNextConfig();
        }
    }
}
