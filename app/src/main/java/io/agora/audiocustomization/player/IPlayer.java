package io.agora.audiocustomization.player;

/**
 * The media player implements this to be controlled.
 */

public interface IPlayer {

    void prepare(String path);

    void play();

    void pause();

    void reset();

    boolean toggle();//return isToPlay

    void release();

    void initializeProgressCallback();

    boolean isPlaying();

    boolean isPrepared();

    void seekTo(int position);
}
