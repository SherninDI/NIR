package com.example.nir;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

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
        String pos = (position + 1) + ".";
        holder.tvEptNumber.setText(pos);
//        holder.tvEptName.setText(itemList.getEptNameText());
        holder.tvEptAmpl.setText(String.valueOf(itemList.getEptAmpl()));
        holder.tvEptTime.setText(String.valueOf(itemList.getEptTime()));
        holder.tvEptValue.setText(itemList.getEptValueText());
        holder.itemView.setBackgroundColor(selected_position == position ? Color.rgb(187,134,252) : Color.TRANSPARENT);
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
        public TextView tvEptAmpl;
        public TextView tvEptTime;
//        public TextView tvEptName;
        public TextView tvEptValue;
        public ViewHolder(View itemView) {
            super(itemView);
            tvEptAmpl = itemView.findViewById(R.id.tvEptAmpl);
            tvEptTime = itemView.findViewById(R.id.tvEptTime);
            tvEptNumber = itemView.findViewById(R.id.tvEptNumber);
//            tvEptName = itemView.findViewById(R.id.tvEptName);
            tvEptValue = itemView.findViewById(R.id.tvEptValue);
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
