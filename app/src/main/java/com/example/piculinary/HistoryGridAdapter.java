package com.example.piculinary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryGridAdapter extends BaseAdapter {

    Context context;
    String[] cuisineName;
    int[] image;

    LayoutInflater inflater;

    public HistoryGridAdapter(Context context, String[] cuisineName, int[] image) {
        this.context = context;
        this.cuisineName = cuisineName;
        this.image = image;
    }

    @Override
    public int getCount() {
        return cuisineName.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (view == null) {
            view = inflater.inflate(R.layout.history_grid_item, null);
        }

        ImageView imageView = view.findViewById(R.id.history_picture);
        TextView textView = view.findViewById(R.id.cuisine_name);

        imageView.setImageResource((image[i]));
        textView.setText(cuisineName[i]);

        return view;
    }
}
