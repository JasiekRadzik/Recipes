package com.example.radzik.recipes.database;

import android.app.Activity;

import com.example.radzik.recipes.R;

/**
 * Created by Radzik on 26.10.2017.
 */

public class EditTextPrefManager {

    private static EditTextPrefManager mInstance = null;

    public static synchronized EditTextPrefManager getInstance() {
        if(mInstance == null) {
            mInstance = new EditTextPrefManager();
        }

        return mInstance;
    }

    public void translateTypeToRecipePartName(EditTextPref editTextPref, Activity activity) {
        int recipePartType = editTextPref.getRecipePartType();
        String recipePartName = null;

        if(recipePartType == ConstantsForRecipePartTypes.SHORT_DESCRIPTION) {
            editTextPref.setRecipePartName(activity.getResources().getString(R.string.short_description));

        } else if(recipePartType == ConstantsForRecipePartTypes.INGREDIENT) {
            editTextPref.setRecipePartName(activity.getResources().getString(R.string.ingredient));

        } else if(recipePartType == ConstantsForRecipePartTypes.HOW_TO_COOK) {
            editTextPref.setRecipePartName(activity.getResources().getString(R.string.how_to_cook));

        } else if(recipePartType == ConstantsForRecipePartTypes.ADDITION_TO_EAT_WITH) {
            editTextPref.setRecipePartName(activity.getResources().getString(R.string.to_eat_with));

        }
    }
}
