package io.agora.audiocustomization.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import io.agora.audiocustomization.MyApplication;
import io.agora.audiocustomization.R;
import io.agora.audiocustomization.constant.Constant;
import io.agora.audiocustomization.util.SpUtils;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

/**
 * Created by ChengleiQiu on 2018/1/17.
 */

public abstract class BaseAgoraManager {
    private final String TAG = getClass().getSimpleName();

    protected RtcEngine mRtcEngine;

    protected Context mAppContext;
    protected Handler mHandler;

    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    protected Set<IRtcEngineEventHandler> mRtcEngineEventHandlerSet = new HashSet<>();
    protected Set<IRtcEngineEventHandler> mUiThreadRtcEngineEventHandlerSet = new HashSet<>();

    private IRtcEngineEventHandler mRtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(final String channel, final int uid, final int elapsed) {
            SpUtils.put(SpUtils.USER_ID, uid);

            if (!mRtcEngineEventHandlerSet.isEmpty()) {
                for (IRtcEngineEventHandler rtcHandler : mRtcEngineEventHandlerSet) {
                    if (rtcHandler != null)
                        rtcHandler.onJoinChannelSuccess(channel, uid, elapsed);
                }
            }

            if (!mUiThreadRtcEngineEventHandlerSet.isEmpty()) {
                for (final IRtcEngineEventHandler rtcHandler : mUiThreadRtcEngineEventHandlerSet) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (rtcHandler != null)
                                rtcHandler.onJoinChannelSuccess(channel, uid, elapsed);
                        }
                    });
                }
            }
        }

        @Override
        public void onClientRoleChanged(final int oldRole, final int newRole) {

            if (!mRtcEngineEventHandlerSet.isEmpty()) {
                for (IRtcEngineEventHandler rtcHandler : mRtcEngineEventHandlerSet) {
                    if (rtcHandler != null)
                        rtcHandler.onClientRoleChanged(oldRole, newRole);
                }
            }

            if (!mUiThreadRtcEngineEventHandlerSet.isEmpty()) {
                for (final IRtcEngineEventHandler rtcHandler : mUiThreadRtcEngineEventHandlerSet) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (rtcHandler != null)
                                rtcHandler.onClientRoleChanged(oldRole, newRole);
                        }
                    });
                }
            }
        }

        @Override
        public void onAudioRouteChanged(final int routing) {

            if (!mRtcEngineEventHandlerSet.isEmpty()) {
                for (IRtcEngineEventHandler rtcHandler : mRtcEngineEventHandlerSet) {
                    if (rtcHandler != null)
                        rtcHandler.onAudioRouteChanged(routing);
                }
            }

            if (!mUiThreadRtcEngineEventHandlerSet.isEmpty()) {
                for (final IRtcEngineEventHandler rtcHandler : mUiThreadRtcEngineEventHandlerSet) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (rtcHandler != null)
                                rtcHandler.onAudioRouteChanged(routing);
                        }
                    });
                }
            }
        }

        @Override
        public void onUserJoined(final int uid, final int elapsed) {

            if (!mRtcEngineEventHandlerSet.isEmpty()) {
                for (IRtcEngineEventHandler rtcHandler : mRtcEngineEventHandlerSet) {
                    if (rtcHandler != null)
                        rtcHandler.onUserJoined(uid, elapsed);
                }
            }

            if (!mUiThreadRtcEngineEventHandlerSet.isEmpty()) {
                for (final IRtcEngineEventHandler rtcHandler : mUiThreadRtcEngineEventHandlerSet) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (rtcHandler != null)
                                rtcHandler.onUserJoined(uid, elapsed);
                        }
                    });
                }
            }
        }

        @Override
        public void onLeaveChannel(final RtcStats stats) {

            if (!mRtcEngineEventHandlerSet.isEmpty()) {
                for (IRtcEngineEventHandler rtcHandler : mRtcEngineEventHandlerSet) {
                    if (rtcHandler != null)
                        rtcHandler.onLeaveChannel(stats);
                }
            }

            if (!mUiThreadRtcEngineEventHandlerSet.isEmpty()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (IRtcEngineEventHandler rtcHandler : mUiThreadRtcEngineEventHandlerSet) {
                            if (rtcHandler != null)
                                rtcHandler.onLeaveChannel(stats);
                        }
                    }
                });
            }
        }
    };

    public void registerOnUiThreadEventHandler(IRtcEngineEventHandler handler) {
        mUiThreadRtcEngineEventHandlerSet.add(handler);
    }

    public void unRegisterOnUiThreadEventHandler(IRtcEngineEventHandler handler) {
        mUiThreadRtcEngineEventHandlerSet.remove(handler);
    }

    public void registerOnEngineThreadEventHandler(IRtcEngineEventHandler handler) {
        mRtcEngineEventHandlerSet.add(handler);
    }

    public void unRegisterOnEngineThreadEventHandler(IRtcEngineEventHandler handler) {
        mRtcEngineEventHandlerSet.remove(handler);
    }

    public int joinChannel() {
        if (mHandler == null)
            mHandler = new Handler(Looper.getMainLooper());

        configRtcEngineWithSetting();

        int uid = (int) SpUtils.get(SpUtils.USER_ID, 0);
        return mRtcEngine.joinChannel(null, Constant.AGORA_CHANNEL_NAME, Constant.AGORA_OPTIONAL_INFO, uid);
    }

    protected abstract void configRtcEngineWithSetting();

    public int leaveChannel() {
        return mRtcEngine.leaveChannel();
    }

    public void init(Context context) {
        mAppContext = context.getApplicationContext();
        try {
            mRtcEngine = RtcEngine.create(MyApplication.getInstance(),
                    MyApplication.getInstance().getString(R.string.private_app_id),
                    mRtcEngineEventHandler);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "ensureRtcEngineReady: RtcEngine create fail", e);
        }
    }
}
