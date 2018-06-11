package io.agora.audiocustomization.gatherandplay.audioGather;


public interface IAudioController {
    AudioStatus init(IAudioCallback callback);
    AudioStatus start();
    AudioStatus stop();
    void destroy();
}
