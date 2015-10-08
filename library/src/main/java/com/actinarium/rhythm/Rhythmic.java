package com.actinarium.rhythm;

import android.app.Application;

/**
 * An {@link Application} must implement this interface so that it's possible to control the grids via notification(s)
 *
 * @author Paul Danyliuk
 */
public interface Rhythmic {

    /**
     * Get main {@link RhythmManager} of this application to control all linked grid overlays
     *
     * @return Main Rhythm manager
     */
    RhythmManager getRhythmManager();

}
