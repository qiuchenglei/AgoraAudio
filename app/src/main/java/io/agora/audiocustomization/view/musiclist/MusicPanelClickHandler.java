package io.agora.audiocustomization.view.musiclist;

public interface MusicPanelClickHandler extends ItemClickHandler {
    void onPlayClicked(int action, GenericListItem music);

    void onStopClicked();

    void onTargetVolumeChanged(boolean ifMic, int volume);

    void onTargetProgressChanged(int progress);

    void onTargetPitchChanged(double pitch);

    void onPanelDismissed();

    int PLAYING_STATUS_DEFAULT = 0;
    int PLAYING_STATUS_PLAYING = 1;
    int PLAYING_STATUS_PAUSE = 2;
    int PLAYING_STATUS_RESUME = 3;
}
