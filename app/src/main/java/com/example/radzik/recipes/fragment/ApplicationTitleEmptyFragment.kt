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
import kotlinx.android.synthetic.main.fragment_application_title_empty.*

/**
 * Created by Radzik on 31.10.2017.
 */

class ApplicationTitleEmptyFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_application_title_empty, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textViewSmallHint.setTextColor(Color.argb(75, 155, 155, 155))
        relLayoutEmptyFragment.setBackgroundColor(Color.argb(100, 246, 246, 246))
        textViewWelcomeRecipe.setTextColor(Color.argb(75, 155, 155, 155))
    }
}
