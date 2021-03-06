package com.example.radzik.recipes.database

import android.app.Activity

import com.example.radzik.recipes.R

/**
 * Created by Radzik on 26.10.2017.
 */

class EditTextPrefManager {

    fun translateTypeToRecipePartName(editTextPref: EditTextPref, activity: Activity) {
        val recipePartType = editTextPref.recipePartType
        val recipePartName: String? = null

        when (recipePartType) {
            ConstantsForRecipePartTypes.SHORT_DESCRIPTION -> editTextPref.recipePartName = activity.resources.getString(R.string.short_description)
            ConstantsForRecipePartTypes.INGREDIENT -> editTextPref.recipePartName = activity.resources.getString(R.string.ingredient)
            ConstantsForRecipePartTypes.HOW_TO_COOK -> editTextPref.recipePartName = activity.resources.getString(R.string.how_to_cook)
            ConstantsForRecipePartTypes.ADDITION_TO_EAT_WITH -> editTextPref.recipePartName = activity.resources.getString(R.string.to_eat_with)
        }
    }

    companion object {

        private var mInstance: EditTextPrefManager? = null

        val instance: EditTextPrefManager
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = EditTextPrefManager()
                }

                return mInstance!!
            }
    }
}
