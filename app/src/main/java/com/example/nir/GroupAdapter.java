package com.example.nir;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private final List<String> groups;
    private final LayoutInflater mInflater;
    private static GroupClickListener groupClickListener;

    // data is passed into the constructor
    GroupAdapter(Context context, List<String> groups) {
        this.mInflater = LayoutInflater.from(context);
        this.groups = groups;

    }

    // inflates the row layout from xml when needed
    @Override
    public GroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_group, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final String itemList = groups.get(position);
        holder.tvGroupName.setText(itemList);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return groups.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tvGroupName;
        public ViewHolder(View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            itemView.setOnClickListener(this::onClick);
        }

        @Override
        public void onClick(View itemView) {
            groupClickListener.onGroupClick(getAdapterPosition(),itemView);
        }

    }

    public void setOnGroupClickListener(GroupAdapter.GroupClickListener groupClickListener) {
        GroupAdapter.groupClickListener = groupClickListener;
    }

    public interface GroupClickListener {
        void onGroupClick(int position, View itemView);
    }
}
