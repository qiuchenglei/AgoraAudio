package io.agora.audiocustomization.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import io.agora.audiocustomization.R;
import io.agora.audiocustomization.adapter.RcvMusicPlayListBtnAdapter;


public class MusicPlayListWindow {
    private PopupWindow mPopupWindow;
    private Context mCtx;
    private RecyclerView rcv;
    private RcvMusicPlayListBtnAdapter adapter;

    public MusicPlayListWindow(Context ctx, RcvMusicPlayListBtnAdapter.OnItemClickListener onItemClickListener) {
        mCtx = ctx;
        View contentView = LayoutInflater.from(ctx.getApplicationContext())
                .inflate(R.layout.popup_window_private_parameter, null);

        int height = mCtx.getResources().getDimensionPixelOffset(R.dimen.mute_audio_popup_window_height);
        int width = mCtx.getResources().getDimensionPixelOffset(R.dimen.mute_audio_popup_window_width);

        mPopupWindow = new PopupWindow(contentView, width, height);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable()); // magic code for can not close PopupWindow
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);

        rcv = contentView.findViewById(R.id.rcv_private_parameter_btn_list);
        adapter = new RcvMusicPlayListBtnAdapter();
        rcv.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.VERTICAL, false));
        rcv.setAdapter(adapter);
        adapter.setOnItemClickListener(onItemClickListener);
    }

    public MusicPlayListWindow show(View anchor) {
        if (isShowing())
            return this;

        adapter.syncFromSp();
        mPopupWindow.getContentView().setBackgroundResource(R.drawable.rounded_corner_bg);

        int xoff = mPopupWindow.getContentView().getResources().getDimensionPixelOffset(R.dimen.mute_audio_popup_window_width);
        mPopupWindow.showAsDropDown(anchor, -xoff, -anchor.getHeight());

        return this;
    }

    public boolean isShowing() {
        return mPopupWindow.isShowing();
    }
}
