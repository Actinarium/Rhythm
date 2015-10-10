package com.actinarium.rhythm;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A core Rhythm manager that keeps hold of many Rhythm controls and communicates with the Quick Control notification
 *
 * @author Paul Danyliuk
 */
public final class RhythmManager {

    /**
     * Context is used for the sole purpose of talking to the {@link RhythmNotificationService}. I sure hope it's not
     * leaking.
     */
    private Context mContext;
    private boolean mIsShowingNotification;
    private int mNotificationId;

    private List<RhythmGroup> mControls = new ArrayList<>();

    /**
     * Create a Rhythm manager
     *
     * @param context Current context (usually the {@link Application} object where Rhythm manager setup is performed).
     *                <br />You may actually set this to <code>null</code> if you wish to suppress the quick control
     *                notification.
     */
    public RhythmManager(@Nullable Context context) {
        mContext = context;
    }

    /**
     * Make a new Rhythm control
     *
     * @param title A convenient title for this control, used to identify it in the notification
     * @return A new control instance, managed by this manager
     */
    public RhythmGroup newControl(String title) {
        final RhythmGroup control = new RhythmGroup(title);
        mControls.add(control);

        // Update the notification if it's already displayed
        if (mIsShowingNotification) {
            showNotification(mNotificationId);
        }
        return control;
    }

    /**
     * Get Rhythm control at requested index
     *
     * @param index index, which was assigned to the required control upon adding
     * @return requested Rhythm control
     */
    public RhythmGroup getControl(int index) {
        return mControls.get(index);
    }

    /**
     * Get the number of controls registered with this manager
     */
    public int getControlCount() {
        return mControls.size();
    }

    /**
     * <p>Show the "Quick Control" notification, which allows to switch configs for any Rhythm controls quickly without
     * navigating away from your app. Usually you would want to call this once during initial configuration (unless you
     * don't need the notification). If you add controls after the call to {@link #showNotification(int)}, the
     * notification will be automatically updated.</p> <p>The notification is dismissible. Upon dismiss, all Rhythm
     * overlays will be hidden. You can use this method again to bring it back.</p>
     *
     * @param notificationId ID for Rhythm notification, must be unique across the app
     */
    public void showNotification(int notificationId) {
        if (mContext == null) {
            return;
        }

        mIsShowingNotification = true;
        mNotificationId = notificationId;
        RhythmNotificationService.showNotification(mContext, notificationId);
    }

    /**
     * The {@link Application} must implement this interface so that it's possible to access Rhythm API from anywhere in
     * the app, particularly from the Quick Control
     */
    public interface Host {

        /**
         * Get the {@link RhythmManager} of this application to access any {@link RhythmGroup} and the rest of Rhythm
         * API
         *
         * @return Rhythm manager associated with this application
         */
        RhythmManager getRhythmManager();

    }

}
