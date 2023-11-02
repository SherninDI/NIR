package com.example.nir;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final int textViewResourceId;
    private final List<String> objects;
//    public static boolean flag = false;
    public SpinnerAdapter(Context context, int textViewResourceId,
                          List<String> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        this.objects = objects;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = View.inflate(context, textViewResourceId, null);

        TextView tv = (TextView) convertView;
        tv.setText(objects.get(position));

//        if (flag) {
//
//        }
        return convertView;
    }
}
