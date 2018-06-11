package io.agora.audiocustomization.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;


import io.agora.audiocustomization.model.CheckedBean;

/**
 * RecyclerView 的checkAdapter
 */
public abstract class RcvCheckAdapter<T extends CheckedBean, Q extends RecyclerView.ViewHolder> extends BaseRcvAdapter<T, Q> {
    protected boolean isEdit;

    public RcvCheckAdapter() {
        super();
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void changeEditStatus() {
        setIsEdit(!isEdit);
    }

    public void setIsEdit(boolean isEdit) {
        if (this.isEdit != isEdit) {
            toggle();
        }
    }

    public boolean toggle() {
        this.isEdit = !this.isEdit;
        notifyDataSetChanged();
        if (!this.isEdit) {
            unSelectAll();
        }
        return this.isEdit;
    }

    public int getSelectCount() {
        if (mList == null || mList.isEmpty())
            return 0;
        int i = 0;
        for (CheckedBean bean :
                mList) {
            if (bean.isChecked)
                i++;
        }
        return i;
    }

    /**
     * 全选
     */
    public void selectAll() {
        if (mList == null || mList.isEmpty())
            return;

        for (CheckedBean bean : mList
                ) {
            bean.isChecked = true;
        }
        notifyDataSetChanged();
    }

    /**
     * 全不选
     */
    public void unSelectAll() {
        if (mList == null || mList.isEmpty())
            return;

        for (CheckedBean bean : mList
                ) {
            bean.isChecked = false;
        }
        notifyDataSetChanged();
    }

    /**
     * 反选
     */
    public void selectInvert() {
        if (mList == null || mList.isEmpty())
            return;

        for (CheckedBean bean : mList
                ) {
            bean.isChecked = !bean.isChecked;
        }
        notifyDataSetChanged();
    }

    /**
     * 删除全部
     */
    public void deleteAll() {
        setData(null);
    }

    /**
     * 删除其中一个ImageView的Id{R.id.iv_check}
     */
    public void delete(int position) {
        if (position >= mList.size())
            return;

        mList.remove(position);
        notifyDataSetChanged();
    }

    /**
     * 删除选中
     */
    public void deleteSelect() {
        if (mList == null || mList.isEmpty())
            return;

        boolean needNotify = false;
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).isChecked) {
                needNotify = true;
                mList.remove(i);
                i--;
            }
        }
        if (needNotify)
            notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final Q holder, final T bean, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bean.isChecked = !bean.isChecked;
                onItemToggleChecked(holder, bean.isChecked, position);
                if (mOnItemToggleCheckListener != null)
                    mOnItemToggleCheckListener.onToggleChecked(bean.isChecked, position);
            }
        });
    }

    private OnItemToggleCheckListener mOnItemToggleCheckListener;

    public void setOnItemToggleCheckListener(OnItemToggleCheckListener listener) {
        mOnItemToggleCheckListener = listener;
    }

    protected abstract void onItemToggleChecked(Q holder, boolean isChecked, int position);

    public interface OnItemToggleCheckListener {
        void onToggleChecked(boolean isChecked, int position);
    }

    class CheckViewHolder extends RecyclerView.ViewHolder {

        public CheckViewHolder(View itemView) {
            super(itemView);
        }
    }
}
