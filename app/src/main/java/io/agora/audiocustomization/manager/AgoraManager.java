package io.agora.audiocustomization.manager;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.agora.audiocustomization.R;
import io.agora.audiocustomization.gatherandplay.audioGather.AudioImpl;
import io.agora.audiocustomization.gatherandplay.audioGather.IAudioCallback;
import io.agora.audiocustomization.gatherandplay.audioPlay.AudioPlayer;
import io.agora.audiocustomization.util.FileUtil;
import io.agora.audiocustomization.util.SpUtils;
import io.agora.rtc.Constants;
import io.agora.rtc.IAudioFrameObserver;
import io.agora.rtc.IRtcEngineEventHandler;

/**
 * Created by ChengleiQiu on 2018/1/26.
 */

public class AgoraManager extends BaseAgoraManager {
    private static final double SAMPLE_INTERVAL = 0.01; //  sampleInterval >= 0.01

    private String mGatherAndPlay;

    private int mSamplingRate;
    private int mAudioChannels = 2; // 1: Mono, 2: Stereo
    private int mSamplesPerCall = 0;

    private AudioImpl mAI;
    private AudioPlayer mAudioPlayer;

    private boolean isReadRawAudioRecord;
    private boolean isReadRawAudioPlayback;

    private AgoraManager() {
    }

    private static class InstanceHolder {
        static final AgoraManager INSTANCE = new AgoraManager();
    }

    public static AgoraManager instance() {
        return InstanceHolder.INSTANCE;
    }

    public abstract static class RtcEventUiThreadHandler extends IRtcEngineEventHandler implements IAudioFrameObserver {
        public abstract void printLog(String... strs);
    }

    private void addLogItems(String... strs) {
        for (IRtcEngineEventHandler handler: mUiThreadRtcEngineEventHandlerSet) {
            if (handler instanceof RtcEventUiThreadHandler) {
                ((RtcEventUiThreadHandler) handler).printLog(strs);// because this class all method run in UI thread.
            }
        }
    }

    @Override
    protected void configRtcEngineWithSetting() {
        normalConfig();

        configAudioMuteSetting();

        channelProfileSetting();

        gatheAndPlaySetting();

        rawDataSetting();
    }

    private void configAudioMuteSetting() {
        boolean audioMutedLocal = (boolean) SpUtils.get(mAppContext.getString(R.string.pref_mute_audio_local_key), false);
        boolean audioMutedRemote = (boolean) SpUtils.get(mAppContext.getString(R.string.pref_mute_audio_all_remote_key), false);
        boolean audioDisabled = (boolean) SpUtils.get(mAppContext.getString(R.string.pref_disable_audio_key), false);
        mRtcEngine.muteLocalAudioStream(audioMutedLocal);
        mRtcEngine.muteAllRemoteAudioStreams(audioMutedRemote);
        if (audioDisabled)
            mRtcEngine.disableAudio();
        else
            mRtcEngine.enableAudio();
    }

    private void rawDataSetting() {
        //only valid in SDK2SDK
        if (!TextUtils.equals(mGatherAndPlay, SpUtils.PF_SDK_TO_SDK_VALUE))
            return;

        int samplesPerCall = Integer.parseInt((String) SpUtils.get(R.string.pref_samples_per_call_list_key, "1024"));

        isReadRawAudioRecord = (boolean) SpUtils.get(R.string.pref_raw_audio_record_key, false);
        if (isReadRawAudioRecord)
            mRtcEngine.setRecordingAudioFrameParameters(mSamplingRate, 1,
                    Constants.RAW_AUDIO_FRAME_OP_MODE_READ_ONLY, samplesPerCall);

        isReadRawAudioPlayback = (boolean) SpUtils.get(R.string.pref_raw_audio_playback_key, false);
        if (isReadRawAudioPlayback)
            mRtcEngine.setPlaybackAudioFrameParameters(mSamplingRate, 1,
                    Constants.RAW_AUDIO_FRAME_OP_MODE_READ_ONLY, samplesPerCall);

        if (!isReadRawAudioPlayback && !isReadRawAudioRecord)
            return;

        mRtcEngine.registerAudioFrameObserver(new IAudioFrameObserver() {
            @Override
            public boolean onRecordFrame(final byte[] bytes, final int i, final int i1, final int i2, final int i3) {
                if (!isReadRawAudioRecord)
                    return false;

                for (IRtcEngineEventHandler handler: mUiThreadRtcEngineEventHandlerSet) {
                    if (handler instanceof RtcEventUiThreadHandler) {
                        final RtcEventUiThreadHandler finalHandler = (RtcEventUiThreadHandler) handler;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                finalHandler.onRecordFrame(bytes, i, i1, i2, i3);
                            }
                        });
                    }
                }
                return true;
            }

            @Override
            public boolean onPlaybackFrame(final byte[] bytes, final int i, final int i1, final int i2, final int i3) {
                if (!isReadRawAudioPlayback)
                    return false;

                for (IRtcEngineEventHandler handler: mUiThreadRtcEngineEventHandlerSet) {
                    if (handler instanceof RtcEventUiThreadHandler) {
                        final RtcEventUiThreadHandler finalHandler = (RtcEventUiThreadHandler) handler;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                finalHandler.onPlaybackFrame(bytes, i, i1, i2, i3);
                            }
                        });
                    }
                }
                return true;
            }
        });

    }

    private void normalConfig() {
        mRtcEngine.disableVideo();
        mRtcEngine.enableAudioVolumeIndication(800, 3); // always we set it 200 ms
        mRtcEngine.setLogFile(FileUtil.getLogFilesDir().getPath() + File.separator + "agora_rtc.log");
        mRtcEngine.setLogFilter(Constants.LOG_FILTER_DEBUG);
    }

    private void gatheAndPlaySetting() {
        mGatherAndPlay = (String) SpUtils.get(mAppContext.getString(R.string.pref_gather_and_play_list_key), SpUtils.PF_SDK_TO_SDK_VALUE);
        mSamplingRate = Integer.parseInt((String) SpUtils.get(
                mAppContext.getString(R.string.pref_sampling_rate_list_key), mAppContext.getString(R.string.pref_sampling_rate_list_default_value)));
        mSamplesPerCall = (int) (mSamplingRate * mAudioChannels * SAMPLE_INTERVAL);

        switch (mGatherAndPlay) {
            case SpUtils.PF_APP_TO_APP_VALUE:
                addLogItems("enter App2App mode!");
//                mIsNeedBtnMuteAndSpeakerphoneShow = false;
                doGatherApp();
                doPlayerApp();
                break;

            case SpUtils.PF_APP_TO_SDK_VALUE:
                addLogItems("enter App2SDK mode!");
//                mIsNeedBtnMuteAndSpeakerphoneShow = true;
                doGatherApp();
                break;

            case SpUtils.PF_SDK_TO_APP_VALUE:
                addLogItems("enter SDK2App mode!");
//                mIsNeedBtnMuteAndSpeakerphoneShow = false;
                doPlayerApp();
                break;

            case SpUtils.PF_SDK_TO_SDK_VALUE:
                addLogItems("enter SDK2SDK mode!");
//                mIsNeedBtnMuteAndSpeakerphoneShow = true;
                break;
        }
    }

    private void channelProfileSetting() {
        boolean isChannelProfileLive = (boolean) SpUtils.
                get(mAppContext.getString(R.string.pref_channel_profile_key), true);
        if (isChannelProfileLive) {
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            boolean isClientRoleBroadcaster = (boolean) SpUtils.
                    get(mAppContext.getString(R.string.pref_client_role_key), true);
            mRtcEngine.setClientRole(isClientRoleBroadcaster ?
                    Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE);
        } else {
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        }
    }

    @Override
    public int leaveChannel() {
        switch (mGatherAndPlay) {
            case SpUtils.PF_APP_TO_APP_VALUE:
                finishGatherApp();
                finishPlayerApp();
                break;

            case SpUtils.PF_APP_TO_SDK_VALUE:
                finishGatherApp();
                break;

            case SpUtils.PF_SDK_TO_APP_VALUE:
                finishPlayerApp();
                break;

            case SpUtils.PF_SDK_TO_SDK_VALUE:
//                mRtcEngine.registerAudioFrameObserver(null);
                break;
        }
        return super.leaveChannel();
    }

    private void finishPlayerApp() {
        mRtcEngine.registerAudioFrameObserver(null);
        mRtcEngine.setParameters("{\"che.audio.external_render\": false}");
        addLogItems("setParameters: {\"che.audio.external_render\": false}");
        if (mAudioPlayer != null)
            mAudioPlayer.stopPlayer();
    }

    private void finishGatherApp() {
        mRtcEngine.setExternalAudioSource(false, mSamplingRate, mAudioChannels);
        if (mAI != null) {
            mAI.stop();
            mAI.destroy();
        }
    }

    private void doGatherApp() {
        if (mAI == null)
            mAI = new AudioImpl(mSamplingRate, mAudioChannels);

        mAI.init(new IAudioCallback() {
            @Override
            public void onAudioDataAvailable(long timeStamp, byte[] audioData) {
                mRtcEngine.pushExternalAudioFrame(audioData, timeStamp);
            }
        });

        mRtcEngine.setExternalAudioSource(true, mSamplingRate, mAudioChannels);

        mAI.start();
    }

    private void doPlayerApp() {
        if (mAudioPlayer == null) {
            mAudioPlayer = new AudioPlayer(AudioManager.STREAM_VOICE_CALL, mSamplingRate,
                    mAudioChannels, AudioFormat.ENCODING_PCM_16BIT);
        }
        mAudioPlayer.startPlayer();

        mRtcEngine.registerAudioFrameObserver(new IAudioFrameObserver() {
            @Override
            public boolean onRecordFrame(byte[] bytes, int i, int i1, int i2, int i3) {
                return false;
            }

            @Override
            public boolean onPlaybackFrame(byte[] bytes, int i, int i1, int i2, int i3) {
                return false;
            }
        });

        mRtcEngine.setPlaybackAudioFrameParameters(mSamplingRate, mAudioChannels, 0, mSamplesPerCall);
        mRtcEngine.setParameters("{\"che.audio.external_render\": true}");
        addLogItems("setParameters: {\"che.audio.external_render\": true}");
    }

}
