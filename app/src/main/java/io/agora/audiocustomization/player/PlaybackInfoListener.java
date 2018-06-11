package io.agora.audiocustomization.player;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Allows {MyMediaPlayer} to report media playback duration
 * and progress update to.
 */

public abstract class PlaybackInfoListener {

    @IntDef({State.INVALID, State.PREPARED, State.PLAYING, State.PAUSED, State.RESET, State.COMPLETED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
        int INVALID = -1;
        int PREPARED = 0;
        int PLAYING = 1;
        int PAUSED = 2;
        int RESET = 3;
        int COMPLETED = 4;
    }

    public static String converStateToString(@State int state) {
        String stateString;
        switch (state) {
            case State.INVALID:
                stateString = "INVALID";
                break;
            case State.PREPARED:
                stateString = "PREPARED";
                break;
            case State.PLAYING:
                stateString = "PLAYING";
                break;
            case State.PAUSED:
                stateString = "PAUSE";
                break;
            case State.COMPLETED:
                stateString = "COMPLETED";
                break;
            case State.RESET:
                stateString = "RESET";
                break;
            default:
                stateString = "N/A";
        }
        return stateString;
    }

    public void onLogUpdated(String formattedMessage) {
    }

    public void onDurationChanged(int duration) {
    }

    public void onPositionChanged(int position) {
    }

    public void onStateChanged(@State int state) {
    }

    public void onPlaybackCompleted() {
    }
}
