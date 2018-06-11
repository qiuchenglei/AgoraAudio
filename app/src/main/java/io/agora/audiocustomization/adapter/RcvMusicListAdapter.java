package io.agora.audiocustomization.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.agora.audiocustomization.R;
import io.agora.audiocustomization.model.MusicBean;
import io.agora.audiocustomization.util.SpUtils;

/**
 * Created by ChengleiQiu on 2018/2/13.
 */

public class RcvMusicListAdapter extends RcvCheckAdapter<MusicBean, RcvMusicListAdapter.MusicViewHolder> {

    private Context context;

    @Override
    public void onBindViewHolder(MusicViewHolder holder, MusicBean bean, final int position) {
        super.onBindViewHolder(holder, bean, position);
        holder.edtUri.setText(bean.uri);
        if (position < 2) {
            holder.ivBtnChecked.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            holder.edtUri.setEnabled(false);
        }
        holder.edtUri.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mList.get(position).uri = s.toString();
//                syncToSp();
            }
        });
    }

    @Override
    protected void onItemToggleChecked(MusicViewHolder holder, boolean isChecked, int position) {
        if (isChecked) {
            holder.ivBtnChecked.setColorFilter(context.getResources().getColor(R.color.colorAccent));
        } else {
            holder.ivBtnChecked.clearColorFilter();
        }
    }

    public void syncFromSp() {

        List<String> uriSet = SpUtils.getStrListValue(SpUtils.MUSIC_EDIT_URI_LIST);
        if (uriSet != null && !uriSet.isEmpty()) {
            ArrayList<MusicBean> musicBeans = new ArrayList<>();
            for (String uriStr :
                    uriSet) {
                musicBeans.add(new MusicBean(uriStr));
            }
            setData(musicBeans);
        }
    }

    public void syncToSp() {
        ArrayList<String> value = new ArrayList<>();
        for (MusicBean musicBean : mList) {
            value.add(musicBean.uri);
        }
        SpUtils.putStrListValue(SpUtils.MUSIC_EDIT_URI_LIST, value);
    }

    @Override
    public void deleteSelect() {
        super.deleteSelect();

        syncToSp();
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new MusicViewHolder(View.inflate(parent.getContext(), R.layout.item_music_edit, null));
    }

    class MusicViewHolder extends RecyclerView.ViewHolder {

        ImageView ivBtnChecked;
        EditText edtUri;

        public MusicViewHolder(View itemView) {
            super(itemView);
            ivBtnChecked = itemView.findViewById(R.id.iv_btn_check);
            edtUri = itemView.findViewById(R.id.edt_music_uri);
        }
    }
}
