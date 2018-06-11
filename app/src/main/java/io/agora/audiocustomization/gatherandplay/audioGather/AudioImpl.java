package io.agora.audiocustomization.gatherandplay.audioGather;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioImpl implements IAudioController {
    private static final String  TAG = AudioImpl.class.getSimpleName();

    private AudioRecord mAudioRecorder = null;
    private IAudioCallback callback = null;

    private AudioStatus mStatus = AudioStatus.STOPPED;

    private int mFrameBufferSize = -1;
    private byte[] mAudioBuffer = null;

    private int sizeInBytes = 0;
    public AudioImpl(int samplingRate, int channelConfig){
        if (mStatus == AudioStatus.STOPPED) {
            int val = 0;
            if (1 == channelConfig)
                val = AudioFormat.CHANNEL_IN_MONO;
            else if(2 == channelConfig)
                val = AudioFormat.CHANNEL_IN_STEREO;

            sizeInBytes = AudioRecord.getMinBufferSize(samplingRate, val, AudioFormat.ENCODING_PCM_16BIT) * 2;

            if (mAudioRecorder != null) {
                mAudioRecorder.release();
                mAudioRecorder = null;
            }

            mAudioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    samplingRate,
                    val,
                    AudioFormat.ENCODING_PCM_16BIT,
                    sizeInBytes);
            if(mAudioRecorder == null)
                Log.e(TAG, "mAudioRecorder is null ");
            mStatus = AudioStatus.INITIALISING;
        }
    }

    @Override
    public AudioStatus init(IAudioCallback callback) {
        if (mStatus == AudioStatus.INITIALISING) {
            this.callback = callback;
        }
        return mStatus;
    }

    @Override
    public AudioStatus start() {
        if (mStatus == AudioStatus.INITIALISING) {

            if (mAudioBuffer == null)
                mAudioBuffer = new byte[sizeInBytes];

            mAudioRecorder.startRecording();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    gatherData();
                }
            }).start();
            mStatus = AudioStatus.RUNNING;
        }
        return mStatus;
    }

    @Override
    public AudioStatus stop() {
        mStatus = AudioStatus.INITIALISING;
        if (null != mAudioRecorder){
            mAudioRecorder.stop();
            mAudioRecorder.release();
            mAudioBuffer = null;
            mAudioRecorder = null;
        }
        return mStatus;
    }

    @Override
    public void destroy() {
        if (mStatus == AudioStatus.INITIALISING) {
            mStatus = AudioStatus.STOPPED;
        }
    }

    private void gatherData() {
        while (mStatus == AudioStatus.RUNNING) {
            int read = mAudioRecorder.read(mAudioBuffer, 0, mFrameBufferSize);
            if (read != mFrameBufferSize)
                continue;

            if (mAudioBuffer != null)
                callback.onAudioDataAvailable(System.currentTimeMillis(), mAudioBuffer);
        }
    }
}
