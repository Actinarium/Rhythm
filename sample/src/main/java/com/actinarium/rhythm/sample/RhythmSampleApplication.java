package com.actinarium.rhythm.sample;

import android.app.Application;
import com.actinarium.rhythm.RhythmManager;
import com.actinarium.rhythm.Rhythmic;

/**
 * <p></p>
 *
 * @author Paul Danyliuk
 * @version $Id$
 */
public class RhythmSampleApplication extends Application implements Rhythmic {

    private RhythmManager mRhythmManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mRhythmManager = new RhythmManager(this, 0).displayNotification(0);
    }

    @Override
    public RhythmManager getRhythmManager() {
        return mRhythmManager;
    }
}
