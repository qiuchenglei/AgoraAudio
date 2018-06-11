package io.agora.audiocustomization.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.agora.audiocustomization.R;
import io.agora.audiocustomization.util.SpUtils;

/**
 * Created by ChengleiQiu on 2018/1/20.
 */

public class RcvMusicPlayListBtnAdapter extends BaseRcvAdapter<String, RcvMusicPlayListBtnAdapter.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(View.inflate(parent.getContext(), R.layout.item_private_parameter_btn, null));
    }

    public interface OnItemClickListener{
        public void onItemClick(View v, String bean, int position);
    }

    private OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    public void syncFromSp() {

        List<String> uriSet = SpUtils.getStrListValue(SpUtils.MUSIC_EDIT_URI_LIST);
        if (uriSet != null && !uriSet.isEmpty()) {
            setData(uriSet);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final String bean, final int position) {
        holder.tvParameter.setText(bean);
        holder.tvParameter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null)
                    itemClickListener.onItemClick(holder.tvParameter, bean, position);
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvParameter;

        public ViewHolder(View itemView) {
            super(itemView);
            tvParameter = itemView.findViewById(R.id.item_private_parameter);
        }
    }
}
