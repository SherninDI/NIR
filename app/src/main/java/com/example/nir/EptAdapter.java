package com.example.nir;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EptAdapter extends RecyclerView.Adapter<EptAdapter.ViewHolder>{
    private final ArrayList<ItemEpt> ept;
    int selected_position = -1;
    private final LayoutInflater mInflater;
    private static EptClickListener eptClickListener;

    // data is passed into the constructor
    EptAdapter(Context context, ArrayList<ItemEpt> ept) {
        this.mInflater = LayoutInflater.from(context);
        this.ept = ept;
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
        final ItemEpt itemList = ept.get(position);
        holder.tvSubGroupName.setText(itemList.getEptNameText());
        holder.tvSubGroupValue.setText(itemList.getEptValueText());
        holder.itemView.setBackgroundColor(selected_position == position ? Color.RED : Color.TRANSPARENT);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return ept.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tvSubGroupName;
        public TextView tvSubGroupValue;
        public ViewHolder(View itemView) {
            super(itemView);
            tvSubGroupName = itemView.findViewById(R.id.tvEptName);
            tvSubGroupValue = itemView.findViewById(R.id.tvEptValue);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View itemView) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
            notifyItemChanged(selected_position);
            selected_position = getAdapterPosition();
            notifyItemChanged(selected_position);
            eptClickListener.onEptClick(selected_position,itemView);
        }
    }

    public void setOnEptClickListener(EptAdapter.EptClickListener eptClickListener) {
        EptAdapter.eptClickListener = eptClickListener;
    }

    public interface EptClickListener {
        void onEptClick(int position, View itemView);

    }
}
