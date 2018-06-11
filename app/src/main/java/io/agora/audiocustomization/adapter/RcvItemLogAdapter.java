package io.agora.audiocustomization.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.agora.audiocustomization.R;

/**
 * Created by ChengleiQiu on 2018/1/20.
 */

public class RcvItemLogAdapter extends BaseRcvAdapter<String, RcvItemLogAdapter.ViewHolder> {

    public void addItems(String... strings) {
        if (strings == null || strings.length == 0)
            return;

        if (strings.length == 1)
            addItem(strings[0]);
        else
            addData(Arrays.asList(strings));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(View.inflate(parent.getContext(), R.layout.item_log, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, String bean, int position) {
        holder.tvLog.setText(bean);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLog;

        public ViewHolder(View itemView) {
            super(itemView);
            tvLog = itemView.findViewById(R.id.item_log_tv);
        }
    }
}
