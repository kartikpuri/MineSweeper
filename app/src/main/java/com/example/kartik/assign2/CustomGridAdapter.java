package com.example.kartik.assign2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Kartik on 10/19/2017.
 */

public class CustomGridAdapter extends BaseAdapter{

    private Context context;
    private int[] items;
    LayoutInflater inflater;


    public CustomGridAdapter(Context context, int[] items)
    {
        this.context = context;
        this.items = items;
        this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return items.length;
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            convertView =  inflater.inflate(R.layout.grid_image, null);
        }
        ImageView iv = (ImageView) convertView.findViewById(R.id.gridImage);
        return convertView;
    }

}
