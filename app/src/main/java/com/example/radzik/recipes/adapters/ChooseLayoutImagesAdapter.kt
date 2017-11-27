package com.example.radzik.recipes.adapters

/**
 * Created by Radzik on 12.10.2017.
 */

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Gallery
import android.widget.ImageView

import com.example.radzik.recipes.R


class ChooseLayoutImagesAdapter(private val mContext: Context) : BaseAdapter() {

    var mImageIds = arrayOf(R.drawable.light_bronze_example, R.drawable.grey_orange_example, R.drawable.blue_red_example, R.drawable.purple_example)

    override fun getCount(): Int {
        return mImageIds.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    // Override this method according to your need
    override fun getView(index: Int, view: View, viewGroup: ViewGroup): View {
        // TODO Auto-generated method stub
        val i = ImageView(mContext)

        i.setImageResource(mImageIds[index])
        i.layoutParams = Gallery.LayoutParams(150, 250)

        i.scaleType = ImageView.ScaleType.FIT_XY

        return i
    }

}
