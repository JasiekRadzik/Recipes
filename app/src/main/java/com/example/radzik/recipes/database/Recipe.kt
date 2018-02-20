package com.example.radzik.recipes.database

import com.itextpdf.text.Document

import java.io.File
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by Radzik on 03.08.2017.
 */

class Recipe {

    private var mRecipeList: List<EditTextPref>? = null
    private var mMapForEditTextPrefs: Map<String, EditTextPref>? = null
    var photoId: String? = null
    var title: String? = null
    var subheading: String? = null
    var documentStyle = 0
    var courseType = 99
    var list: ArrayList<EditTextPref>
        get() = (mRecipeList as ArrayList<EditTextPref>?)!!
        set(list) {
            mRecipeList = list
        }
    var mapForEditTextPrefs: HashMap<String, EditTextPref>
        get() = (mMapForEditTextPrefs as HashMap<String, EditTextPref>?)!!
        set(map) {
            mMapForEditTextPrefs = map
        }

    init {
        mRecipeList = ArrayList()
        mMapForEditTextPrefs = HashMap()
    }

    companion object {

        // integers to describe if it's a dessert, main course, starter etc.
        val STARTER = 1
        val MAIN_COURSE = 2
        val DESSERT = 3
        val SOUP = 4
    }
}
