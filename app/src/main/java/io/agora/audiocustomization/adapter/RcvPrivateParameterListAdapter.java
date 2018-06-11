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
import io.agora.audiocustomization.model.PrivateParameterBean;
import io.agora.audiocustomization.util.SpUtils;

/**
 * Created by ChengleiQiu on 2018/2/13.
 */

public class RcvPrivateParameterListAdapter extends RcvCheckAdapter<PrivateParameterBean, RcvPrivateParameterListAdapter.PrivateParameterViewHolder> {

    private Context context;

    public RcvPrivateParameterListAdapter() {
        super();
        isEdit = true;
    }

    @Override
    public void onBindViewHolder(PrivateParameterViewHolder holder, final PrivateParameterBean bean, int position) {
        super.onBindViewHolder(holder, bean, position);
        if (position < 1) {
            holder.ivBtnChecked.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            holder.edtPrivateParameter.setEnabled(false);
        }
        holder.edtPrivateParameter.setText(bean.privateParameter);
        holder.edtPrivateParameter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                bean.privateParameter = s.toString();
//                syncToSp();
            }
        });
    }

    @Override
    protected void onItemToggleChecked(PrivateParameterViewHolder holder, boolean isChecked, int position) {
        if (isChecked) {
            holder.ivBtnChecked.setColorFilter(context.getResources().getColor(R.color.colorAccent));
        } else {
            holder.ivBtnChecked.clearColorFilter();
        }

    }

    public void syncFromSp() {

        List<String> uriSet = SpUtils.getStrListValue(SpUtils.PRIVATE_PARAMETER_LIST);
        if (uriSet != null && !uriSet.isEmpty()) {
            ArrayList<PrivateParameterBean> musicBeans = new ArrayList<>();
            for (String uriStr :
                    uriSet) {
                musicBeans.add(new PrivateParameterBean(uriStr));
            }
            setData(musicBeans);
        }
    }

    public void syncToSp() {
        ArrayList<String> value = new ArrayList<>();
        for (PrivateParameterBean bean : mList) {
            value.add(bean.privateParameter);
        }
        SpUtils.putStrListValue(SpUtils.PRIVATE_PARAMETER_LIST, value);
    }

    @Override
    public void deleteSelect() {
        super.deleteSelect();

        syncToSp();
    }

    @Override
    public PrivateParameterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new PrivateParameterViewHolder(View.inflate(parent.getContext(), R.layout.item_private_parameter_edit, null));
    }

    class PrivateParameterViewHolder extends RecyclerView.ViewHolder {

        ImageView ivBtnChecked;
        EditText edtPrivateParameter;

        public PrivateParameterViewHolder(View itemView) {
            super(itemView);
            ivBtnChecked = itemView.findViewById(R.id.iv_btn_check);
            edtPrivateParameter = itemView.findViewById(R.id.edt_private_parameter_str);
        }
    }
}
