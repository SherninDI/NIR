package com.example.nir;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EptNameAdapter extends RecyclerView.Adapter<EptNameAdapter.ViewHolder>{
    private final ArrayList<ItemEpt> ept;
    int selected_position = -1;
    private final LayoutInflater mInflater;
    private static EptNameClickListener eptNameClickListener;

    // data is passed into the constructor
    EptNameAdapter(Context context, ArrayList<ItemEpt> ept) {
        this.mInflater = LayoutInflater.from(context);
        this.ept = ept;
    }

    // inflates the row layout from xml when needed
    @Override
    public EptNameAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_ept_name, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ItemEpt itemList = ept.get(position);
        String pos = (position + 1) + ".";
        holder.tvEptNumber.setText(pos);
        holder.tvEptName.setText(itemList.getEptNameText());
        holder.tvEptValue.setText(itemList.getEptValueText());
        holder.itemView.setBackgroundColor(selected_position == position ? Color.RED : Color.TRANSPARENT);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return ept.size();
    }


    public ItemEpt getItem(int position) {
        return ept.get(position);
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView tvEptNumber;
        public TextView tvEptName;
        public TextView tvEptValue;
        public ViewHolder(View itemView) {
            super(itemView);
            tvEptNumber = itemView.findViewById(R.id.tvEptNumber1);
            tvEptName = itemView.findViewById(R.id.tvEptName1);
            tvEptValue = itemView.findViewById(R.id.tvEptValue1);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View itemView) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
            notifyItemChanged(selected_position);
            selected_position = getAdapterPosition();
            notifyItemChanged(selected_position);
            eptNameClickListener.onEptNameClick(selected_position,itemView);
        }
    }

    public void setOnEptNameClickListener(EptNameAdapter.EptNameClickListener eptNameClickListener) {
        EptNameAdapter.eptNameClickListener = eptNameClickListener;
    }

    public interface EptNameClickListener {
        void onEptNameClick(int position, View itemView);

    }
}
