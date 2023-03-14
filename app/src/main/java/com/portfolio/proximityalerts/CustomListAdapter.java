package com.portfolio.proximityalerts;

import android.content.Context;
import android.content.res.Resources;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.Inflater;

public class CustomListAdapter extends BaseAdapter {
    
    Context context;
    LayoutInflater inflater;
    TextView itemName, itemMmsi, itemDescription;
    HashMap<String, String> targetHash;
    ArrayList<String> targetList;


    public CustomListAdapter(Context applicationContext, HashMap<String, String> targetArray){
        this.context = context;
        this.targetList= new ArrayList<>();
        this.targetList.addAll(targetArray.values());
        inflater = (LayoutInflater.from(applicationContext));

    }

    @Override
    public int getCount() {
        return targetList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.layout_target_list_item, null);

        TextView name = (TextView) view.findViewById(R.id.textView_item_name);
        TextView mmsi = (TextView) view.findViewById(R.id.textView_item_mmsi);
        TextView description = (TextView) view.findViewById(R.id.textView_item_description);
        ImageView color = (ImageView) view.findViewById(R.id.imgView_item_color);
        
        String [] parsedData = targetList.get(i).split(";");

        name.setText(parsedData[0]);
        mmsi.setText(parsedData[1]);
        description.setText(parsedData[2]);
        color.setImageResource(R.drawable.ic_baseline_circle_24);

        int intColor = view.getResources().getColor(R.color.black);
        switch (Integer.parseInt(parsedData[3])){
            case 1:
                intColor = view.getResources().getColor(R.color.green);
                break;
            case 2:
                intColor = view.getResources().getColor(R.color.yellow);
                break;
            case 3:
                intColor = view.getResources().getColor(R.color.red);
                break;
            default:
                break;
        }
        color.setColorFilter(intColor);
        return view;
    }

    public void removeItem(int index){
        this.targetList.remove(index);
    }
}
