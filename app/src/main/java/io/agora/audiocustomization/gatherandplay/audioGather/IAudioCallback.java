package io.agora.audiocustomization.gatherandplay.audioGather;

public interface IAudioCallback {
    void onAudioDataAvailable(long timeStamp, byte[] audioData);
}
