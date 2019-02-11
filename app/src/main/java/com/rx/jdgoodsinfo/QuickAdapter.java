package com.rx.jdgoodsinfo;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Author:XWQ
 * Time   2019/1/7
 * Descrition: this is QuickAdapter
 */

public abstract class QuickAdapter<T> extends RecyclerView.Adapter<QuickAdapter.VH>
{
    List<T> mData;

    public QuickAdapter(List<T> data)
    {
        this.mData = data;
    }

    public abstract int getLayoutId(int viewType);

    public abstract void convert(VH holder, T data, int position);

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return VH.get(parent, getLayoutId(viewType));
    }

    @Override
    public void onBindViewHolder(VH holder, int position)
    {
        convert(holder, mData.get(position), position);
    }

    @Override
    public int getItemCount()
    {
        return mData.size();
    }

    public static class VH extends RecyclerView.ViewHolder
    {
        private SparseArray<View> mViews;
        private View mConentView;

        public VH(View itemView)
        {
            super(itemView);
            this.mConentView = itemView;
            mViews = new SparseArray<>();
        }

        public static VH get(ViewGroup parent, int layoutId)
        {
            View contentView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new VH(contentView);
        }

        public <T extends View> T getView(int id)
        {
            View view = mViews.get(id);
            if (view == null)
            {
                view = mConentView.findViewById(id);
                mViews.put(id, view);
            }
            return (T) view;
        }

        public void setText(int id, String text)
        {
            TextView textView = getView(id);
            textView.setText(text);
        }

    }
}
