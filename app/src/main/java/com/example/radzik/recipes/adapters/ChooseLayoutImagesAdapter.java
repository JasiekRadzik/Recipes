package com.example.radzik.recipes.adapters;

/**
 * Created by Radzik on 12.10.2017.
 */

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.example.radzik.recipes.R;


public class ChooseLayoutImagesAdapter extends BaseAdapter
{
    private Context mContext;



    public ChooseLayoutImagesAdapter(Context context)
    {
        mContext = context;
    }

    public int getCount() {
        return mImageIds.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }


    // Override this method according to your need
    public View getView(int index, View view, ViewGroup viewGroup)
    {
        // TODO Auto-generated method stub
        ImageView i = new ImageView(mContext);

        i.setImageResource(mImageIds[index]);
        i.setLayoutParams(new Gallery.LayoutParams(150, 250));

        i.setScaleType(ImageView.ScaleType.FIT_XY);

        return i;
    }

    public Integer[] mImageIds = {
            R.drawable.light_bronze_example,
            R.drawable.grey_orange_example,
            R.drawable.blue_red_example,
            R.drawable.purple_example
    };

}
