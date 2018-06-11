package io.agora.audiocustomization.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.GravityCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupWindow;

import io.agora.audiocustomization.R;


public class MuteAudioPopupWindow {
    private PopupWindow mView;
    private Context mCtx;
    private CheckBox muteLSwitch, muteRSwitch, pauseSwitch, disableSwitch;

    private UserEventHandler mUserEventHandler;

    public boolean isMuteChecked() {
        return muteLSwitch.isChecked() || muteRSwitch.isChecked() ||
                pauseSwitch.isChecked() || disableSwitch.isChecked();
    }

    public interface UserEventHandler {
        void onPauseAudioSwitch(boolean paused);

        void onDisableAudioSwitch(boolean disabled);

        void onMuteAudioSwitch(boolean local, boolean muted);
    }

    public MuteAudioPopupWindow(Context ctx, boolean muteLocal, boolean muteRemote, boolean pauseAudio, boolean disableAudio) {
        mCtx = ctx;
        View contentView = LayoutInflater.from(ctx.getApplicationContext())
                .inflate(R.layout.mute_audio_popup_window, null);

        int height = mCtx.getResources().getDimensionPixelOffset(R.dimen.mute_audio_popup_window_height);
        int width = mCtx.getResources().getDimensionPixelOffset(R.dimen.mute_audio_popup_window_width);

        mView = new PopupWindow(contentView, width, height);
        mView.setBackgroundDrawable(new BitmapDrawable()); // magic code for can not close PopupWindow
        mView.setFocusable(true);
        mView.setOutsideTouchable(true);

        muteLSwitch = (CheckBox) contentView.findViewById(R.id.mute_local_audio_switch);
        muteLSwitch.setChecked(muteLocal);
        muteLSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mUserEventHandler != null) {
                    mUserEventHandler.onMuteAudioSwitch(true, isChecked);
                }
            }
        });

        muteRSwitch = (CheckBox) contentView.findViewById(R.id.mute_remote_audio_switch);
        muteRSwitch.setChecked(muteRemote);
        muteRSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mUserEventHandler != null) {
                    mUserEventHandler.onMuteAudioSwitch(false, isChecked);
                }
            }
        });

        pauseSwitch = (CheckBox) contentView.findViewById(R.id.pause_audio_switch);
        pauseSwitch.setChecked(pauseAudio);
        pauseSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mUserEventHandler != null) {
                    mUserEventHandler.onPauseAudioSwitch(isChecked);
                }
            }
        });

        disableSwitch = (CheckBox) contentView.findViewById(R.id.disable_audio_switch);
        disableSwitch.setChecked(disableAudio);
        disableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mUserEventHandler != null) {
                    mUserEventHandler.onDisableAudioSwitch(isChecked);
                }
            }
        });
    }

    public synchronized MuteAudioPopupWindow show(View anchor, UserEventHandler handler) {
        PopupWindow view = mView;

        view.getContentView().setBackgroundResource(R.drawable.rounded_corner_bg);

        int yoff = view.getContentView().getResources().getDimensionPixelOffset(R.dimen.mute_audio_popup_window_height) + anchor.getHeight() + 16;
        int xoff = (view.getContentView().getResources().getDimensionPixelOffset(R.dimen.mute_audio_popup_window_width) - anchor.getWidth()) / 2;
        view.showAsDropDown(anchor, -xoff, -yoff);

        mUserEventHandler = handler;

        return this;
    }

    public synchronized boolean isShowing() {
        return mView.isShowing();
    }
}
