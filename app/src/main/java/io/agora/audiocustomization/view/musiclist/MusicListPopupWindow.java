package io.agora.audiocustomization.view.musiclist;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import io.agora.audiocustomization.R;


public class MusicListPopupWindow {
    private PopupWindow mView;
    private Context mCtx;

    private ClickableListAdapter mAdapter;

    private RecyclerView mListView;

    private GenericListItem mCurrentMusicToPlay;

    public MusicListPopupWindow(Context ctx) {
        mCtx = ctx;
        View contentView = LayoutInflater.from(ctx.getApplicationContext())
                .inflate(R.layout.music_list_popup_window, null);

        int height = mCtx.getResources().getDimensionPixelOffset(R.dimen.ml_popup_window_height);
        mView = new PopupWindow(contentView, WindowManager.LayoutParams.WRAP_CONTENT, height);
        mView.setBackgroundDrawable(new BitmapDrawable()); // magic code for can not close PopupWindow
        mView.setFocusable(true);
        mView.setOutsideTouchable(true);

        mListView = (RecyclerView) contentView.findViewById(R.id.popup_window_container);
    }

    public synchronized void addItemClickHandler(final MusicPanelClickHandler handler) {
        mView.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                handler.onPanelDismissed();
            }
        });

        mAdapter.addItemClickHandler(handler);
    }

    public synchronized MusicListPopupWindow show(View anchor, ArrayList<GenericListItem> items) {
        if (mAdapter == null) {
            mAdapter = new MusicListAdapter(mCtx, items);
            mAdapter.setHasStableIds(true);
            mListView.setAdapter(mAdapter);
            mListView.setLayoutManager(new LinearLayoutManager(mCtx, LinearLayoutManager.VERTICAL, false));
        } else {
            ((MusicListAdapter) mAdapter).setItems(items);
        }

        PopupWindow view = mView;

        view.getContentView().setBackgroundResource(R.drawable.rounded_corner_bg);
        int yoff = mListView.getResources().getDimensionPixelOffset(R.dimen.ml_popup_window_height) + anchor.getHeight() + 16;
        view.showAsDropDown(anchor, 0, -yoff);

        final ImageView startPausePlay = (ImageView) view.getContentView().findViewById(R.id.btn_start_pause_play);
        startPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentMusicToPlay == null) {
                    return;
                }

                MusicPanelClickHandler handler = (MusicPanelClickHandler) mAdapter.getItemClickHandler();

                int status = mPlayingStatus;
                int action;
                int res;
                if (status == MusicPanelClickHandler.PLAYING_STATUS_DEFAULT) {
                    action = MusicPanelClickHandler.PLAYING_STATUS_PLAYING;
                    res = R.drawable.icon_play_pause;
                } else if (status == MusicPanelClickHandler.PLAYING_STATUS_PAUSE) {
                    action = MusicPanelClickHandler.PLAYING_STATUS_RESUME;
                    res = R.drawable.icon_play_pause;
                } else {
                    action = MusicPanelClickHandler.PLAYING_STATUS_PAUSE;
                    res = R.drawable.icon_play_play;
                }

                handler.onPlayClicked(action, mCurrentMusicToPlay);

                mPlayingStatus = action;

                startPausePlay.setImageResource(res);
            }
        });

        ImageView stopPlay = (ImageView) view.getContentView().findViewById(R.id.btn_stop_play);
        stopPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPanelClickHandler handler = (MusicPanelClickHandler) mAdapter.getItemClickHandler();
                handler.onStopClicked();

                mPlayingStatus = MusicPanelClickHandler.PLAYING_STATUS_DEFAULT;
                startPausePlay.setImageResource(R.drawable.icon_play_play);

                updateCurrentMusicProgress(0, 0);
            }
        });

        if (mCurrentMusicToPlay != null) {
            updateCurrentMusicTitle(mCurrentMusicToPlay);
        }

        SeekBar pb = (SeekBar) view.getContentView().findViewById(R.id.music_playing_progress_bar);
        pb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MusicPanelClickHandler handler = (MusicPanelClickHandler) mAdapter.getItemClickHandler();
                if (b) {
                    handler.onTargetProgressChanged(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar micVolume = (SeekBar) view.getContentView().findViewById(R.id.set_mic_volume_seek_bar);
        micVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MusicPanelClickHandler handler = (MusicPanelClickHandler) mAdapter.getItemClickHandler();
                if (b) {
                    handler.onTargetVolumeChanged(true, i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SeekBar musicVolume = (SeekBar) view.getContentView().findViewById(R.id.set_music_volume_seek_bar);
        musicVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                MusicPanelClickHandler handler = (MusicPanelClickHandler) mAdapter.getItemClickHandler();
                if (b) {
                    handler.onTargetVolumeChanged(false, i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return this;
    }

    private int mPlayingStatus = MusicPanelClickHandler.PLAYING_STATUS_DEFAULT;

    public synchronized boolean isPlaying() {
        if (mAdapter == null) {
            return false;
        }

        return mPlayingStatus == MusicPanelClickHandler.PLAYING_STATUS_PLAYING;
    }

    public synchronized void updateCurrentMusicProgress(int current, int all) {
        if (mAdapter == null) {
            return;
        }

        PopupWindow view = mView;

        TextView tv = (TextView) view.getContentView().findViewById(R.id.progress_of_music_playing);
        SeekBar pb = (SeekBar) view.getContentView().findViewById(R.id.music_playing_progress_bar);
        current = current / 1000;
        all = all / 1000;
        tv.setText(current + "/" + all);

        pb.setMax(all);
        pb.setProgress(current);
    }

    public synchronized void updateCurrentMusicTitle(GenericListItem music) {
        mCurrentMusicToPlay = music;

        TextView tv = (TextView) mView.getContentView().findViewById(R.id.name_of_music_to_play);
        if (music.mName.contains("."))
            tv.setText(music.mName.substring(0, music.mName.indexOf(".")));
        else
            tv.setText("格式不支持");
    }

    public synchronized void resetPanel() {
        if (mAdapter == null) {
            return;
        }

        PopupWindow view = mView;

        mPlayingStatus = MusicPanelClickHandler.PLAYING_STATUS_DEFAULT;

        ImageView startPausePlay = (ImageView) view.getContentView().findViewById(R.id.btn_start_pause_play);
        startPausePlay.setImageResource(R.drawable.icon_play_play);

        updateCurrentMusicProgress(0, 0);
    }

    public synchronized boolean isShowing() {
        return mView.isShowing();
    }
}
