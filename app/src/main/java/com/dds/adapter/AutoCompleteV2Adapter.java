package com.dds.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.dds.webrtc.R;
import com.skt.Tmap.TMapAutoCompleteV2;

import java.util.ArrayList;

public class AutoCompleteV2Adapter extends BaseAdapter {

    private Context context;
    private ArrayList<TMapAutoCompleteV2> itemList;

    public AutoCompleteV2Adapter(Context context) {
        this.context = context;
        itemList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View cView = convertView;
        if (cView == null) {
            cView = LayoutInflater.from(context).inflate(R.layout.list_layout, parent, false);
        }

        TMapAutoCompleteV2 item = itemList.get(position);

        TextView tv = cView.findViewById(R.id.TextView01);
        tv.setText(item.keyword);

        return cView;
    }

    public void setItemList(ArrayList<TMapAutoCompleteV2> itemList) {
        this.itemList = itemList;

    }

    public void clear() {
        this.itemList.clear();
        notifyDataSetChanged();
    }
}
