package io.agora.audiocustomization.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.agora.audiocustomization.R;
import io.agora.audiocustomization.adapter.RcvItemLogAdapter;
import io.agora.audiocustomization.adapter.RcvMusicPlayListBtnAdapter;
import io.agora.audiocustomization.adapter.RcvPrivateParameterBtnAdapter;
import io.agora.audiocustomization.manager.AgoraManager;
import io.agora.audiocustomization.player.MyMediaPlayer;
import io.agora.audiocustomization.player.PlaybackInfoListener;
import io.agora.audiocustomization.service.RecordService;
import io.agora.audiocustomization.util.AppUtil;
import io.agora.audiocustomization.util.FileUtil;
import io.agora.audiocustomization.util.SpUtils;
import io.agora.audiocustomization.util.ToastUtil;
import io.agora.audiocustomization.view.MusicPlayListWindow;
import io.agora.audiocustomization.view.MuteAudioPopupWindow;
import io.agora.audiocustomization.view.PrivateParameterButtonWindow;
import io.agora.audiocustomization.view.musiclist.GenericListItem;
import io.agora.audiocustomization.view.musiclist.MusicListPopupWindow;
import io.agora.audiocustomization.view.musiclist.MusicPanelClickHandler;
import io.agora.audiocustomization.wifidisplay.PresentationActivity;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int PEMISSION_REQUEST_CODE = 101;
    private static final int RECORD_REQUEST_CODE = 102;
    private static final int LOG_PRINT_INTERNAL_S = 6;

    private Button mBtnJoinChannel;
    private ImageView mIvBtnPlaybackMusic, mIvBtnScreenRecord, mIvBtnMute, mIvBtnAudioRecord,
            mIvBtnSpeakerPhone, mIvBtnClientRoleBroadcaster, mIvBtnPlayMixing, mIvBtnPrivateParameter;
    private MuteAudioPopupWindow mMuteAudioPopupWindow;
    private TextView mTvRawAudioRecordInfo, mTvRawAudioPlaybackInfo, mTvAudioFocus, mTvMusicProgress;
    private SeekBar mSeekBarMusic;

    private RecyclerView mRcvLog;
    private RcvItemLogAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    private MyMediaPlayer mPlayer;

    private RecordService mRecordService;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private float mRatio;

    private AudioManager mAudioManager;
    AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            addLogItems("onAudioFocusChange : focusChange = " + focusChange);
            if (mTvAudioFocus.getVisibility() != View.VISIBLE)
                mTvAudioFocus.setVisibility(View.VISIBLE);

            addLogItems("AudioManager Mode:" + mAudioManager.getMode());
            mTvAudioFocus.setText("audio focus = " + focusChange);
        }
    };

    private ScheduledExecutorService mScheduledExecutorService;
    private Runnable mLogTask;

    IRtcEngineEventHandler mRtcEngineEventHandler = new AgoraManager.RtcEventUiThreadHandler() {
        @Override
        public boolean onRecordFrame(byte[] bytes, int i, int i1, int i2, int i3) {
            if (mTvRawAudioRecordInfo.getVisibility() != View.VISIBLE)
                mTvRawAudioRecordInfo.setVisibility(View.VISIBLE);
            mTvRawAudioRecordInfo.setText("onRecordFrame: i = " + i + ", i1 = " + i1 + ", i2 = " + i2 + " i3 = " + i3);
            return true;
        }

        @Override
        public boolean onPlaybackFrame(byte[] bytes, int i, int i1, int i2, int i3) {
            if (mTvRawAudioPlaybackInfo.getVisibility() != View.VISIBLE)
                mTvRawAudioPlaybackInfo.setVisibility(View.VISIBLE);
            mTvRawAudioPlaybackInfo.setText("onPlaybackFrame: i = " + i + ", i1 = " + i1 + ", i2 = " + i2 + " i3 = " + i3);
            return true;
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            addLogItems("onJoinChannelSuccess");
            mBtnJoinChannel.setEnabled(true);
            mBtnJoinChannel.setText(R.string.leave);
            mIvBtnAudioRecord.setVisibility(View.VISIBLE);
            mIvBtnSpeakerPhone.setVisibility(View.VISIBLE);
            mIvBtnClientRoleBroadcaster.setVisibility(View.VISIBLE);

            if ((boolean)SpUtils.get(getString(R.string.pref_client_role_key), false)) {
                mIvBtnClientRoleBroadcaster.setSelected(true);
                mIvBtnClientRoleBroadcaster.setColorFilter(getResources().getColor(R.color.colorAccent));
            } else {
                mIvBtnClientRoleBroadcaster.setSelected(false);
            }

            mIvBtnMute.setVisibility(View.VISIBLE);
            checkMuteAudioStatus();

//            startLogOnSchedule();
        }

        @Override
        public void onClientRoleChanged(int oldRole, int newRole) {
            super.onClientRoleChanged(oldRole, newRole);
            addLogItems("client role changed: new role = " + newRole);
            mIvBtnClientRoleBroadcaster.setEnabled(true);
            if (newRole == Constants.CLIENT_ROLE_BROADCASTER) {
                mIvBtnClientRoleBroadcaster.setColorFilter(getResources().getColor(R.color.colorAccent));
                mIvBtnClientRoleBroadcaster.setSelected(true);
            } else {
                mIvBtnClientRoleBroadcaster.clearColorFilter();
                mIvBtnClientRoleBroadcaster.setSelected(false);
            }
        }

        @Override
        public void onAudioRouteChanged(int routing) {
            addLogItems("onAudioRouteChanged: " + routing);

            if (routing == Constants.AUDIO_ROUTE_SPEAKERPHONE) {
                mIvBtnSpeakerPhone.setColorFilter(getResources().getColor(R.color.colorAccent));
                mIvBtnSpeakerPhone.setSelected(true);
            } else {
                mIvBtnSpeakerPhone.clearColorFilter();
                mIvBtnSpeakerPhone.setSelected(false);
            }
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            addLogItems("onUserJoined: uid = " + uid + ", elapsed: " + elapsed);
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            Log.d(TAG, "onLeaveChannel: ");
            mBtnJoinChannel.setEnabled(true);
            mBtnJoinChannel.setText(R.string.join);
            mIvBtnAudioRecord.setVisibility(View.GONE);
            mIvBtnMute.setVisibility(View.GONE);
            mTvRawAudioPlaybackInfo.setVisibility(View.GONE);
            mTvRawAudioRecordInfo.setVisibility(View.GONE);
            mIvBtnSpeakerPhone.setVisibility(View.GONE);
            mIvBtnClientRoleBroadcaster.setVisibility(View.GONE);

            mMuteAudioPopupWindow = null;
            mAdapter.setData(null);

//            stopLogOnSchedule();
        }

        @Override
        public void printLog(String... strs) {
            addLogItems(strs);
        }
    };
    private MusicListPopupWindow mMusicListPopupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindView();

        initData();

        bindListener();

        AgoraManager.instance().init(this);
        AgoraManager.instance().registerOnUiThreadEventHandler(mRtcEngineEventHandler);

        String[] needPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        AppUtil.checkAndRequestAppPermission(this, needPermissions, PEMISSION_REQUEST_CODE);

        bindService(new Intent(this, RecordService.class), mConnection, BIND_AUTO_CREATE);
    }

    private void bindListener() {
        mIvBtnPlaybackMusic.setOnClickListener(this);
        mBtnJoinChannel.setOnClickListener(this);
        mIvBtnMute.setOnClickListener(this);
        mIvBtnAudioRecord.setOnClickListener(this);
        mIvBtnScreenRecord.setOnClickListener(this);
        mIvBtnSpeakerPhone.setOnClickListener(this);
        mIvBtnClientRoleBroadcaster.setOnClickListener(this);
        mIvBtnPrivateParameter.setOnClickListener(this);
        mIvBtnPlayMixing.setOnClickListener(this);
        mSeekBarMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mTvMusicProgress.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mTvMusicProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mTvMusicProgress.setVisibility(View.GONE);
                mPlayer.seekTo((int) (seekBar.getProgress() / mRatio));
            }
        });

        mPlayer.setPlaybackInfoListener(new PlaybackInfoListener() {
            @Override
            public void onDurationChanged(int duration) {
                mSeekBarMusic.setVisibility(View.VISIBLE);
                mSeekBarMusic.setMax(100);
                mRatio = 100f / duration;
            }

            @Override
            public void onPlaybackCompleted() {
                mPlayer.reset();
                playingUri = null;
                mSeekBarMusic.setVisibility(View.GONE);
            }

            @Override
            public void onStateChanged(int state) {
                switch (state) {
                    case State.PLAYING:
                        mIvBtnPlaybackMusic.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
                        break;
                    case State.PAUSED:
                    case State.RESET:
                        mIvBtnPlaybackMusic.clearColorFilter();
                        break;
                }
            }

            @Override
            public void onPositionChanged(int position) {
                mSeekBarMusic.setProgress((int) (position * mRatio));
            }
        });
    }

    private void initData() {
        mAdapter = new RcvItemLogAdapter();
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mLinearLayoutManager.setStackFromEnd(true);
        mRcvLog.setLayoutManager(mLinearLayoutManager);
        mRcvLog.setAdapter(mAdapter);

        mProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        mPlayer = new MyMediaPlayer();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AgoraManager.instance().unRegisterOnUiThreadEventHandler(mRtcEngineEventHandler);
        mPlayer.release();

        unbindService(mConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        addLogItems("onWindowFocusChanged: " + hasFocus);
    }

    @Override
    protected void onStart() {
        super.onStart();
        addLogItems("activity: on start");
    }

    @Override
    protected void onStop() {
        super.onStop();
        addLogItems("activity: on stop");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_setting:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.menu_item_wifi_display:
                startActivity(new Intent(this, PresentationActivity.class));
                break;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PEMISSION_REQUEST_CODE)
            return;

        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED)
                finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK && Build.VERSION.SDK_INT >= 21) {
            mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
            mRecordService.setMediaProject(mMediaProjection);
            mRecordService.startRecord();
            mIvBtnScreenRecord.setColorFilter(getResources().getColor(R.color.colorAccent));
        }
    }

    private void bindView() {
        mIvBtnPlaybackMusic = findViewById(R.id.iv_btn_play_music);
        mBtnJoinChannel = findViewById(R.id.btn_join_channel);
        mIvBtnMute = findViewById(R.id.iv_btn_mute_audio);
        mIvBtnAudioRecord = findViewById(R.id.iv_btn_record_audio);
        mIvBtnScreenRecord = findViewById(R.id.iv_btn_screen_record);
        mIvBtnSpeakerPhone = findViewById(R.id.iv_btn_speaker_phone);
        mIvBtnClientRoleBroadcaster = findViewById(R.id.iv_btn_client_role_broadcaster);
        mIvBtnPlayMixing = findViewById(R.id.iv_btn_play_music_mixing);
        mRcvLog = findViewById(R.id.rcv_log);
        mTvRawAudioPlaybackInfo = findViewById(R.id.tv_raw_audio_playback_info);
        mTvRawAudioRecordInfo = findViewById(R.id.tv_raw_audio_record_info);
        mTvAudioFocus = findViewById(R.id.tv_audio_focus);
        mSeekBarMusic = findViewById(R.id.seek_bar_music);
        mTvMusicProgress = findViewById(R.id.tv_music_progress);
        mIvBtnPrivateParameter = findViewById(R.id.iv_btn_private_parameter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_join_channel:
                if (mBtnJoinChannel.getText().equals(getString(R.string.join))) {
                    mBtnJoinChannel.setEnabled(false);
                    int requestResult = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    addLogItems("requestAudioFocus result = " + requestResult);

                    int result = AgoraManager.instance().joinChannel();
                    Log.d(TAG, "onClick: join channel result: " + result);
                } else {
                    mBtnJoinChannel.setEnabled(false);
                    mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
                    AgoraManager.instance().leaveChannel();
                }
                break;

            case R.id.iv_btn_play_music:
                clickBtnMusic(v);
                break;

            case R.id.iv_btn_play_music_mixing:
                clickBtnMixing(v);
                break;

            case R.id.iv_btn_screen_record:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    Snackbar.make(mIvBtnScreenRecord, "录屏功能仅支持Android 5.0以上版本手机", 800).show();
                } else if (mRecordService.isRunning()) {
                    mRecordService.stopRecord();
                    mIvBtnScreenRecord.clearColorFilter();
                } else {
                    Intent captureIntent = mProjectionManager.createScreenCaptureIntent();
                    startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
                }
                break;

            case R.id.iv_btn_mute_audio:
                showMuteAudioPopupWindow(v);
                break;

            case R.id.iv_btn_private_parameter:
                showPrivateParameterBtnPopupWindow(v);
                break;

            case R.id.iv_btn_record_audio:
                localAudioRecord();
                break;

            case R.id.iv_btn_speaker_phone:
                agoraRtcEngine().setEnableSpeakerphone(!mIvBtnSpeakerPhone.isSelected());
                break;

            case R.id.iv_btn_client_role_broadcaster:
                if (mIvBtnClientRoleBroadcaster.isSelected()) {
                    agoraRtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
                } else {
                    agoraRtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
                }
                mIvBtnClientRoleBroadcaster.setEnabled(false);
                break;
        }
    }

    private PrivateParameterButtonWindow privateParameterButtonWindow;

    private void showPrivateParameterBtnPopupWindow(View v) {
        if (privateParameterButtonWindow == null)
            privateParameterButtonWindow = new PrivateParameterButtonWindow(this, new RcvPrivateParameterBtnAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, String bean, int position) {
                    agoraRtcEngine().setParameters(bean);
                    addLogItems("Private Parameter: " + bean);
                }
            });

        privateParameterButtonWindow.show(v);
    }

    private MusicPlayListWindow musicPlayListWindow;
    private MusicPlayListWindow musicMixingListWindow;
    private String playingUri;
    private String mixingUri;
    private boolean isMixingPlaying;

    private void clickBtnMixing(View v){
        if (musicMixingListWindow == null) {
            musicMixingListWindow = new MusicPlayListWindow(this, new RcvMusicPlayListBtnAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, String bean, int position) {
                    if (!TextUtils.isEmpty(mixingUri) && mixingUri.equals(bean)) {
                        if (isMixingPlaying) {
                            agoraRtcEngine().pauseAudioMixing();
                            addLogItems("pauseAudioMixing");
                        } else {
                            agoraRtcEngine().resumeAudioMixing();
                            addLogItems("resumeAudioMixing");
                        }
                        isMixingPlaying = !isMixingPlaying;
                    } else {
                        agoraRtcEngine().startAudioMixing(bean, false, false, 1);
                        addLogItems("startAudioMixing" + bean);
                        isMixingPlaying = true;
                    }
                }
            });
        }

        musicMixingListWindow.show(v);
    }

    private void clickBtnMusic(View v) {
        if (musicPlayListWindow == null) {
            musicPlayListWindow = new MusicPlayListWindow(this, new RcvMusicPlayListBtnAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, String bean, int position) {
                    if (!TextUtils.isEmpty(playingUri) && playingUri.equals(bean)) {
                        mPlayer.toggle();
                    } else {
                        playingUri = bean;
                        mPlayer.reset();
                        mPlayer.prepare(bean);
                        mPlayer.play();
                    }
                }
            });
        }

        musicPlayListWindow.show(v);

//        if (!mPlayer.isPrepared()) {
//            if (!(boolean) SpUtils.get(SpUtils.IS_RAW_FILES_READY, false)) {
//                ToastUtil.showShort("Please wait a moment!");
//                return;
//            }
//            mPlayer.prepare(null);
//        }
//
//        mPlayer.toggle();
    }

//    public void onDoArsRecordClicked(View view) {
//        boolean useDynamicKey = config().mUseDynamicKey;
//        log.info("onDoArsRecordClicked " + view + " " + handleRecording + " " + useDynamicKey);
//        if (!useDynamicKey) {
//            return;
//        }
//        if (handleRecording) {
//            return;
//        }
//        mRecordingButton = (ImageView) view;
//        handleRecording = true;
//        if (view.getTag() == null) {
//            worker().startARS();
//        } else if (view.getTag() instanceof Boolean && (Boolean) view.getTag()) {
//            worker().stopARS();
//        }
//    }


    @Override
    public void onBackPressed() {
        if (mIvBtnMute.getVisibility() == View.VISIBLE) {
            onClick(mBtnJoinChannel);
            Snackbar.make(mBtnJoinChannel, "leaving the channel", Snackbar.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    private void startLogOnSchedule() {
        if (mScheduledExecutorService == null)
            mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        if (mLogTask == null) {
            mLogTask = new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addLogItems("AudioManager Mode:" + mAudioManager.getMode());
                        }
                    });
                }
            };
        }

        mScheduledExecutorService.scheduleAtFixedRate(mLogTask, LOG_PRINT_INTERNAL_S,
                LOG_PRINT_INTERNAL_S, TimeUnit.SECONDS);
    }

    private void stopLogOnSchedule() {
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdown();
            mScheduledExecutorService = null;
            mLogTask = null;
        }
    }

    public void onMixMusicClicked(View view) {
        log.debug("onMixMusicClicked " + view);
        if (mMusicListPopupWindow == null || !mMusicListPopupWindow.isShowing()) {

            if (mMusicListPopupWindow == null) {
                mMusicListPopupWindow = new MusicListPopupWindow(view.getContext());
            }

            ArrayList<GenericListItem> list = new ArrayList<>();
            String[] music = getResources().getStringArray(R.array.music_list_for_mix);

            list.add(new GenericListItem(0, 1, music[0], "Normal"));
            list.add(new GenericListItem(1, 1, music[1], "Loopback"));
            list.add(new GenericListItem(2, 1, music[2], "Replace(Assets)"));
            list.add(new GenericListItem(3, 1, music[3], "Normal(Http/LAN)"));
            list.add(new GenericListItem(4, 2, music[4], "Repeat 2"));
            list.add(new GenericListItem(5, -1, music[5], "Repeat -1"));

            String path = (String) SpUtils.get(SpUtils.MUSIC_EDIT_URI_LIST, "");
            if (!TextUtils.isEmpty(path)){
                list.add(new GenericListItem(6, 1, path, "customize"));
            }

            mMusicListPopupWindow.show(view, list).addItemClickHandler(new MusicPanelClickHandler() {
                @Override
                public void onItemClicked(final int itemId, final Object data) {
                    GenericListItem item = (GenericListItem) data;

                    mMusicListPopupWindow.updateCurrentMusicTitle(item);
                }

                @Override
                public void onPlayClicked(int action, GenericListItem item) {
                    if (action == MusicPanelClickHandler.PLAYING_STATUS_PAUSE) {
                        agoraRtcEngine().pauseAudioMixing();
                    } else if (action == MusicPanelClickHandler.PLAYING_STATUS_RESUME) {
                        agoraRtcEngine().resumeAudioMixing();
                    } else if (action == MusicPanelClickHandler.PLAYING_STATUS_PLAYING) {
                        boolean loopback = "Loopback".equals(item.mDesc);
                        boolean replace = item.mDesc.startsWith("Replace");
                        boolean fromAssets = item.mDesc.contains("Assets");
                        boolean fromHttp = item.mDesc.contains("Http");
                        boolean fromCustomize = item.mDesc.equals("customize");
                        int count = item.mIconRes;
                        String mixingTargetFile;
                        if (fromAssets) {
                            mixingTargetFile = "/assets/" + item.mName;
                        } else if (fromHttp) {
                            mixingTargetFile = "http://192.168.99.149:8086/share/StrongY/" + item.mName;
                        } else if (fromCustomize) {
                            mixingTargetFile = item.mName;
                        } else {
                            mixingTargetFile = FileUtil.getMusicFilsDir() + File.separator + item.mName;
                        }
                        agoraRtcEngine().startAudioMixing(mixingTargetFile, loopback, replace, count);

                        startTimeTicker();
                    }
                }

                @Override
                public void onStopClicked() {
                    stopTimeTicker();

                    agoraRtcEngine().stopAudioMixing();
                }

                @Override
                public void onTargetVolumeChanged(boolean ifMic, int volume) {
                    if (ifMic) {
                        agoraRtcEngine().adjustRecordingSignalVolume(volume); // 0, 400
                    } else {
                        agoraRtcEngine().adjustAudioMixingVolume(volume); // 0, 100
                    }
                }

                @Override
                public void onTargetProgressChanged(int progress) {
                    log.debug("onTargetProgressChanged " + progress);
                    agoraRtcEngine().setAudioMixingPosition(progress * 1000);
                }

                @Override
                public void onTargetPitchChanged(double pitch) {
                    log.debug("onTargetPitchChanged " + pitch);
                    agoraRtcEngine().setLocalVoicePitch(pitch); // 0.5, 2
                }

                @Override
                public void onPanelDismissed() {
                    stopTimeTicker();
                }
            });

            if (mMusicListPopupWindow.isPlaying()) {
                startTimeTicker();
            }
        }
    }

    private ScheduledFuture<?> mQueryMixingFuture;

    private ScheduledThreadPoolExecutor mExecutor;

    private void ensureFixedTasksPoolCreated() {
        if (mExecutor == null) {
            mExecutor = new ScheduledThreadPoolExecutor(2);
        }
    }

    private void startTimeTicker() {
        ensureFixedTasksPoolCreated();

        if (mQueryMixingFuture == null) {
            mQueryMixingFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    final int current = agoraRtcEngine().getAudioMixingCurrentPosition();
                    final int all = agoraRtcEngine().getAudioMixingDuration();
                    log.debug("startTimeTicker " + current + " " + all);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isFinishing()) {
                                return;
                            }

                            if (mMusicListPopupWindow != null) {
                                mMusicListPopupWindow.updateCurrentMusicProgress(current, all);
                            }
                        }
                    });
                }
            }, 1, 1, TimeUnit.SECONDS);
        }
    }

    private void stopTimeTicker() {
        log.debug("stopTimeTicker " + mQueryMixingFuture);
        if (mQueryMixingFuture != null) {
            mQueryMixingFuture.cancel(true);
            mQueryMixingFuture = null;
        }
    }

    public void localAudioRecord() {
        if (mIvBtnAudioRecord.isSelected()) {
            mIvBtnAudioRecord.setSelected(false);
            mIvBtnAudioRecord.clearColorFilter();

            agoraRtcEngine().stopAudioRecording();
            addLogItems("onDoLocalAudioRecordClicked " + mIvBtnAudioRecord + " stop");
        } else {
            mIvBtnAudioRecord.setSelected(true);
            mIvBtnAudioRecord.setColorFilter(getResources().getColor(R.color.colorAccent));

            boolean even = (System.currentTimeMillis() % 1000 % 2) == 0;
            String target = FileUtil.getRecordFilsDir().getPath() + File.separator
                    + System.currentTimeMillis() + (even ? ".wav" : ".aac");
            addLogItems("onDoLocalAudioRecordClicked " + mIvBtnAudioRecord + " " + target + " start");
            int result = agoraRtcEngine().startAudioRecording(target, Constants.AUDIO_RECORDING_QUALITY_LOW);
            if (result == Constants.ERR_OK) {
                ToastUtil.showLong(getString(R.string.msg_local_audio_record_file_generated, target));
            } else {
                addLogItems("failed to record local audio file " + target);
            }
        }
    }

    private void showMuteAudioPopupWindow(View v) {

        if (mMuteAudioPopupWindow.isShowing())
            return;

        mMuteAudioPopupWindow.show(v, new MuteAudioPopupWindow.UserEventHandler() {
            @Override
            public void onPauseAudioSwitch(boolean paused) {
                if (paused) {
                    agoraRtcEngine().pauseAudio();
                } else {
                    agoraRtcEngine().resumeAudio();
                }

                checkMuteAudioStatus();
            }

            @Override
            public void onDisableAudioSwitch(boolean disabled) {
                if (disabled) {
                    agoraRtcEngine().disableAudio();
                } else {
                    agoraRtcEngine().enableAudio();
                }

                checkMuteAudioStatus();
            }

            @Override
            public void onMuteAudioSwitch(boolean local, boolean muted) {
                if (local) {
                    agoraRtcEngine().muteLocalAudioStream(muted);
                } else {
                    agoraRtcEngine().muteAllRemoteAudioStreams(muted);
                }

                checkMuteAudioStatus();
            }
        });
    }

    private void checkMuteAudioStatus() {
        if (mMuteAudioPopupWindow == null) {
            boolean audioMutedLocal = (boolean) SpUtils.get(getString(R.string.pref_mute_audio_local_key), false);
            boolean audioMutedRemote = (boolean) SpUtils.get(getString(R.string.pref_mute_audio_all_remote_key), false);
            boolean audioDisabled = (boolean) SpUtils.get(getString(R.string.pref_disable_audio_key), false);
            mMuteAudioPopupWindow = new MuteAudioPopupWindow(this,
                    audioMutedLocal,
                    audioMutedRemote,
                    false,
                    audioDisabled);
        }

        if (mMuteAudioPopupWindow.isMuteChecked())
            mIvBtnMute.setColorFilter(getResources().getColor(R.color.colorAccent));
        else
            mIvBtnMute.clearColorFilter();

    }

    private void addLogItems(String... strs) {
        if (strs == null || strs.length == 0)
            return;

        mAdapter.addItems(strs);

        if (mLinearLayoutManager.findLastVisibleItemPosition() > mAdapter.getItemCount() - strs.length - 2)
            mRcvLog.smoothScrollToPosition(mAdapter.getItemCount() - 1);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            mRecordService = binder.getRecordService();
            mRecordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
            mIvBtnScreenRecord.setEnabled(true);
            if (mRecordService.isRunning()) {
                mIvBtnScreenRecord.setColorFilter(getResources().getColor(R.color.colorAccent));
            } else {
                mIvBtnScreenRecord.clearColorFilter();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
}
