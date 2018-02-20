package com.example.radzik.recipes.adapters

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView



/**
 * Created by Radzik on 22.11.2017.
 */

class CourseTypeSpinnerAdapter(context: Context, resourceId: Int, val listOfCourses: List<String>) : ArrayAdapter<String>(context, resourceId, listOfCourses) {

    override fun isEnabled(position: Int): Boolean {
        return position != 0
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getDropDownView(position, convertView, parent)
        val tv = view as TextView
        when (position) {
            0 -> // Set the hint text color gray
                tv.setTextColor(Color.GRAY)
            else -> tv.setTextColor(Color.BLACK)
        }
        return view
    }


}