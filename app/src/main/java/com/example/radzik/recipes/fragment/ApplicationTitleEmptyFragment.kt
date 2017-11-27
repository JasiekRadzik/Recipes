package com.example.radzik.recipes.fragment

import android.app.Fragment
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView

import com.example.radzik.recipes.R

import butterknife.BindView
import butterknife.ButterKnife

/**
 * Created by Radzik on 31.10.2017.
 */

class ApplicationTitleEmptyFragment : Fragment() {

    @BindView(R.id.textViewSmallHint)
    internal var mSmallHintTxtView: TextView? = null

    @BindView(R.id.textViewWelcomeRecipe)
    internal var mWelcomeTxtView: TextView? = null

    @BindView(R.id.relLayoutEmptyFragment)
    internal var mLayoutEmptyFragment: RelativeLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        val view = inflater.inflate(R.layout.fragment_application_title_empty, container, false)
        ButterKnife.bind(this, view)

        mSmallHintTxtView!!.setTextColor(Color.argb(75, 155, 155, 155))
        mLayoutEmptyFragment!!.setBackgroundColor(Color.argb(100, 246, 246, 246))
        mWelcomeTxtView!!.setTextColor(Color.argb(75, 155, 155, 155))

        return view
    }
}
