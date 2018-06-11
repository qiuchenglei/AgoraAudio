package io.agora.audiocustomization.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ChengleiQiu on 2018/1/19.
 */

public abstract class BaseRcvAdapter<T, Q extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<Q> {
    protected List<T> mList = new ArrayList<>();

    public BaseRcvAdapter() {
    }

    public List<T> getData() {
        return mList;
    }

    public T getItem(int position) {
        return mList.get(position);
    }

    public void setData(List<T> list) {
        if (list == null) {
            mList.clear();
        } else {
            mList = list;
        }
        notifyDataSetChanged();
    }

    public void addData(List<T> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void addItem(T bean) {
        mList.add(bean);
        notifyDataSetChanged();
    }

    public void insertItem(int position, T bean) {
        if (position >= mList.size() - 1) {
            mList.add(bean);
        }

        mList.add(position, bean);
        notifyDataSetChanged();
    }

    public void insertData(int position, List<T> list) {
        if (position >= mList.size() - 1) {
            mList.addAll(list);
        }

        mList.addAll(position, list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    @Override
    public void onBindViewHolder(Q holder, int position) {
        onBindViewHolder(holder, mList.get(position), position);
    }

    public abstract void onBindViewHolder(Q holder, T bean, int position);

}
