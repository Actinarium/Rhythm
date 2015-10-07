package com.actinarium.rhythm.sample;

import android.app.Application;
import com.actinarium.rhythm.RhythmService;

/**
 * <p></p>
 *
 * @author Paul Danyliuk
 * @version $Id$
 */
public class RhythmSampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RhythmService.showNotification(this, 0, 0);
    }
}
