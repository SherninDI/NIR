package com.example.nir;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EptAdapter extends RecyclerView.Adapter<EptAdapter.ViewHolder>{
    private final List<String> subGroups;
    private final LayoutInflater mInflater;
    private static EptClickListener eptClickListener;

    // data is passed into the constructor
    EptAdapter(Context context, List<String> subGroups) {
        this.mInflater = LayoutInflater.from(context);
        this.subGroups = subGroups;
    }

    // inflates the row layout from xml when needed
    @Override
    public EptAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_ept, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final String itemList = subGroups.get(position);
        holder.tvSubGroupName.setText(itemList);
//        holder.tvSubGroupValue.setText(itemList.getSubGroupValue());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return subGroups.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tvSubGroupName;
//        public TextView tvSubGroupValue;
        public ViewHolder(View itemView) {
            super(itemView);
            tvSubGroupName = itemView.findViewById(R.id.tvEptName);
//            tvSubGroupValue = itemView.findViewById(R.id.tvSubGroupValue);
            itemView.setOnClickListener(this::onClick);
        }

        @Override
        public void onClick(View itemView) {

            eptClickListener.onEptClick(getAdapterPosition(),itemView);
        }
    }

    public void setOnSubGroupClickListener(EptAdapter.EptClickListener subgroupClickListener) {
        EptAdapter.eptClickListener = eptClickListener;
    }

    public interface EptClickListener {
        void onEptClick(int position, View itemView);

    }
}
