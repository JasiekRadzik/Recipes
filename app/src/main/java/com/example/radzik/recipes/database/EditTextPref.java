package com.example.radzik.recipes.database;

/**
 * Created by Radzik on 11.08.2017.
 */

public class EditTextPref {
    private String mText;
    private String mRecipePartName;
    private String mID;
    private int mRecipePartType;

    public EditTextPref() {}

    public EditTextPref(String text) {
        mText = text;
    }

    public void setText(String text) {
        mText = text;
    }
    public String getText() {
        return mText;
    }

    public void setID(String s) {
        mID = s;
    }
    public String getID() {
        return mID;
    }

    public void setRecipePartType(int recipePartTypeConstant) {
        mRecipePartType = recipePartTypeConstant;
    }
    public int getRecipePartType() {
        return mRecipePartType;
    }

    public void setRecipePartName(String recipePartName) {
        mRecipePartName = recipePartName;
    }
    public String getRecipePartName() {
        return mRecipePartName;
    }

}
