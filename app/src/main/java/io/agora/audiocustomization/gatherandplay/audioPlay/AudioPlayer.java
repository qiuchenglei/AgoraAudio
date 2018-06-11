package io.agora.audiocustomization.gatherandplay.audioPlay;

/**
 * Created by wubingshuai on 21/11/2017.
 */

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import io.agora.audiocustomization.gatherandplay.audioGather.AudioStatus;


public class AudioPlayer {
    private static final String  TAG = AudioPlayer.class.getSimpleName();
    private static final int DEFAULT_PLAY_MODE = AudioTrack.MODE_STREAM;

    private AudioTrack mAudioTrack;
    private AudioStatus mAudioStatus = AudioStatus.STOPPED;

    public  AudioPlayer(int streamType, int sampleRateInHz, int channelConfig, int audioFormat){
        if(mAudioStatus == AudioStatus.STOPPED) {
            int Val = 0;
            if(1 == channelConfig)
                Val = AudioFormat.CHANNEL_OUT_MONO;
            else if(2 == channelConfig)
                Val = AudioFormat.CHANNEL_OUT_STEREO;

            int mMinBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, Val, audioFormat);
            if (mMinBufferSize == AudioTrack.ERROR_BAD_VALUE) {
                Log.e(TAG,"AudioTrack.ERROR_BAD_VALUE : " + AudioTrack.ERROR_BAD_VALUE) ;
            }

            mAudioTrack = new AudioTrack(streamType, sampleRateInHz, Val, audioFormat, mMinBufferSize, DEFAULT_PLAY_MODE);
            if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
                throw new RuntimeException("Error on AudioTrack created");
            }
            mAudioStatus = AudioStatus.INITIALISING;
        }
    }

    public boolean startPlayer() {
        if(mAudioStatus == AudioStatus.INITIALISING) {
            mAudioTrack.play();
            mAudioStatus = AudioStatus.RUNNING;
        }
        return true;
    }

    public void stopPlayer() {
        if(null != mAudioTrack){
            mAudioStatus = AudioStatus.STOPPED;
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
        }
    }

    public boolean play(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        if(mAudioStatus == AudioStatus.RUNNING) {
            mAudioTrack.write(audioData, offsetInBytes, sizeInBytes);
        }
        return true;
    }
}