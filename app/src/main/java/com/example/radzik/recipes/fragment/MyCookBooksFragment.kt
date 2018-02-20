package com.example.radzik.recipes.fragment

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.radzik.recipes.database.CookBook
import com.example.radzik.recipes.database.Recipe
import com.example.radzik.recipes.database.firebase.GeneralDataManager

import java.util.ArrayList
import java.util.HashMap

/**
 * Created by Radzik on 11.09.2017.
 */

// todo: Recycler View layout to display list

class MyCookBooksFragment : Fragment() {

    private var mCookBooksMap: Map<CookBook, ArrayList<Recipe>>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {

        // sets listener for MAP
        val listener = object : GeneralDataManager.OnRecipesUpdateListener {
            override fun onMapChanged(map: HashMap<CookBook, ArrayList<Recipe>>?) {
                mCookBooksMap = map
            }
        }

        GeneralDataManager.instance.setOnRecipesUpdateListener(listener)

        return super.onCreateView(inflater, container, savedInstanceState)

    }
}
