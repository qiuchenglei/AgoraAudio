package io.agora.audiocustomization.player;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.agora.audiocustomization.MyApplication;
import io.agora.audiocustomization.util.FileUtil;
import io.agora.audiocustomization.util.SpUtils;
import io.agora.audiocustomization.util.ToastUtil;

/**
 * Exposes the functionality of the {@link android.media.MediaPlayer} and implements the
 * {@link IPlayer}, so that the controller can control music playback.
 */

public final class MyMediaPlayer implements IPlayer {
    public static final String TAG = "MyMediaPlayer";
    public static final int PLAYBACK_POSITION_REFRESH_INTERVAL_MS = 1000;

    private MediaPlayer mMediaPlayer;
    private PlaybackInfoListener mPlaybackInfoListener;
    private ScheduledExecutorService mExecutor;
    private Runnable mSeekbarProgressUpdateTask;
    private boolean isPrepared;
    private String mPath;

    public MyMediaPlayer() {
    }

    public void setPlaybackInfoListener(PlaybackInfoListener listener) {
        mPlaybackInfoListener = listener;
    }

    /**
     * Syncs the mMediaPlayer position via recurring task.
     */
    private void startUpdateProgressTask() {
        if (mExecutor == null)
            mExecutor = Executors.newSingleThreadScheduledExecutor();

        if (mSeekbarProgressUpdateTask == null) {
            mSeekbarProgressUpdateTask = new Runnable() {
                @Override
                public void run() {
                    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                        int currentPosition = mMediaPlayer.getCurrentPosition();
                        if (mPlaybackInfoListener != null)
                            mPlaybackInfoListener.onPositionChanged(currentPosition);
                    }
                }
            };
        }

        mExecutor.scheduleAtFixedRate(mSeekbarProgressUpdateTask, 0,
                PLAYBACK_POSITION_REFRESH_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void stopUpdateProgressTask(boolean resetUIPlaybackPosition) {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
            mExecutor = null;
            mSeekbarProgressUpdateTask = null;
            if (resetUIPlaybackPosition && mPlaybackInfoListener != null)
                mPlaybackInfoListener.onPositionChanged(0);
        }
    }

    /**
     * Once the {@link MediaPlayer} is released, it can't be used again, and another one has
     * to be created. In the onStop() method of the {@link Activity} the {@link MediaPlayer}
     * is released. Then in the onStart() of the {@link Activity} a new {@link MediaPlayer}
     * object has to be created. That's why this method is private, and called by loadMedia(int)
     * and not the constructor.
     */
    @Override
    public void prepare(String path) {
        mPath = path;

//        if (path == null) {
//            path = (String) SpUtils.get(SpUtils.MUSIC_EDIT_URI_LIST, "");
//            if (TextUtils.isEmpty(path)) {
//                File[] mp3FilePaths = FileUtil.getAppMp3Files();
//                if (mp3FilePaths == null || mp3FilePaths.length == 0 || mp3FilePaths[0] == null)
//                    return;
//
//                path = mp3FilePaths[0].getAbsolutePath();
//            }
//        }

        if (TextUtils.isEmpty(path)) {
            ToastUtil.showShort("播放地址为空");
            return;
        }

        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(MyApplication.getInstance(), Uri.parse(path));
            if (mMediaPlayer == null) {
                ToastUtil.showShort("无法播放");
                return;
            }
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopUpdateProgressTask(true);
                    Log.d(TAG, "onCompletion: MediaPlayer playback completed.");
                    if (mPlaybackInfoListener != null) {
                        mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.COMPLETED);
                        mPlaybackInfoListener.onPlaybackCompleted();
                    }
                }
            });
            Log.d(TAG, "initializeMediaPlayer: mMediaPlayer = new MediaPlayer()");
            isPrepared = true;
        } else {
            Log.d(TAG, "loadMedia: mediaplayer.setDataSource" + path);
            try {
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepare();
//                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mp) {
//
//                    }
//                });
                isPrepared = true;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "loadMedia: mediaplayer.setDataSource or prepare fail");
                isPrepared = false;
                return;
            }
        }
        if (mPlaybackInfoListener != null)
            mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PREPARED);

        initializeProgressCallback();
        Log.d(TAG, "loadMedia: initializeProgressCallback");
    }

    @Override
    public void play() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
//            Log.d(TAG, "play: playbackStart: " + mContext.getResources().getResourceEntryName(R.raw.music));
            Log.d(TAG, "play: playbackStart: ");
            mMediaPlayer.start();
            if (mPlaybackInfoListener != null)
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PLAYING);

            startUpdateProgressTask();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            if (mPlaybackInfoListener != null)
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PAUSED);

            Log.d(TAG, "pause: playbackPause()");
        }
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            if (mPlaybackInfoListener != null)
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.RESET);

            stopUpdateProgressTask(true);
            Log.d(TAG, "reset: playbackReset");
        }
    }

    @Override
    public boolean toggle() {
        boolean isToPlay = false;
        if (mMediaPlayer != null) {
            isToPlay = !mMediaPlayer.isPlaying();
            if (isToPlay) {
                play();
            } else {
                pause();
            }
        }
        return isToPlay;
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            isPrepared = false;
            Log.d(TAG, "release: release() and mMediaPlayer = null");
        }
    }

    @Override
    public void initializeProgressCallback() {
        final int duration = mMediaPlayer.getDuration();
        if (mPlaybackInfoListener != null) {
            mPlaybackInfoListener.onDurationChanged(duration);
            mPlaybackInfoListener.onPositionChanged(0);
            Log.d(TAG, "initializeProgressCallback: setPlaybackDuration: "
                    + TimeUnit.MILLISECONDS.toSeconds(duration) + "s.");
        }
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer == null)
            return false;

        return mMediaPlayer.isPlaying();
    }

    @Override
    public boolean isPrepared() {
        return isPrepared;
    }

    @Override
    public void seekTo(int position) {
        if (mMediaPlayer != null) {
            if (position > mMediaPlayer.getDuration())
                position = mMediaPlayer.getDuration();
            mMediaPlayer.seekTo(position);
            Log.d(TAG, "seekTo: " + position + "ms");
        }
    }
}
