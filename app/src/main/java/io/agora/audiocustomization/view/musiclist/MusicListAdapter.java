package io.agora.audiocustomization.view.musiclist;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.agora.audiocustomization.R;


public class MusicListAdapter extends ClickableListAdapter {
    private ArrayList<GenericListItem> mMusicList;

    public MusicListAdapter(Context context, ArrayList<GenericListItem> list) {
        super(context);

        mMusicList = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.music_item, parent, false);
        return new MessageHolder(v);
    }

    public void setItems(ArrayList<GenericListItem> musicList) {
        mMusicList = musicList;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final GenericListItem msg = mMusicList.get(position);

        MessageHolder myHolder = (MessageHolder) holder;
        myHolder.mFileName.setText(msg.mName);
        myHolder.mFileDescription.setText(msg.mDesc);

        if (mHandler != null) {
            myHolder.mRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler.onItemClicked(msg.mId, msg);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mMusicList.size();
    }

    @Override
    public long getItemId(int position) {
        return mMusicList.get(position).hashCode();
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        public TextView mFileName;
        public TextView mFileDescription;

        public View mRoot;

        public MessageHolder(View v) {
            super(v);
            mRoot = v;

            mFileName = (TextView) v.findViewById(R.id.file_name);
            mFileDescription = (TextView) v.findViewById(R.id.file_description);
        }
    }

}
