package io.agora.audiocustomization.view.musiclist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

public abstract class ClickableListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected ItemClickHandler mHandler;

    protected Context mContext;
    protected LayoutInflater mInflater;

    public ClickableListAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void addItemClickHandler(ItemClickHandler handler) {
        mHandler = handler;
    }

    public ItemClickHandler getItemClickHandler() {
        return mHandler;
    }
}
