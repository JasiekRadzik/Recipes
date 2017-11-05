package com.example.radzik.recipes.database;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;

import com.example.radzik.recipes.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Radzik on 03.08.2017.
 */

public class RecipeManager {

    private static RecipeManager mInstance;
    private Recipe mRecipe;
    private boolean mIsRecipeHeldInManager = false;
    private Uri mPhotoUri = null;
    private String mPicturePath = null;
    private List<String> mCourseTypesArray;
    private String mCurrentCookBookKey = null;
    private String mCurrentCookBookTitle = null;
    private int mCurrentFragment = 0;

    public static synchronized RecipeManager getInstance() {
        if(mInstance == null) {
            mInstance = new RecipeManager();
        }

        return mInstance;
    }

    public RecipeManager() {

    }

    public void resetManagerInstance() {
        mInstance = new RecipeManager();
    }

    public Recipe getCurrentOrCreateNewRecipe() {
        if(mRecipe == null) {
            mRecipe = new Recipe();
        }

        mIsRecipeHeldInManager = true;
        return mRecipe;
    }

    public void createNewRecipe() {
        mRecipe = new Recipe();
    }

    public void setPhotoUri(Uri uri) {
        mPhotoUri = uri;
    }

    public Uri getPhotoUri() {
        return mPhotoUri;
    }

    public String getCourseTypeTranslation(Activity activity) {
        if(mRecipe.getCourseType() == mRecipe.STARTER) {
            return activity.getResources().getString(R.string.course_type_starter);
        } else if(mRecipe.getCourseType() == mRecipe.MAIN_COURSE) {
            return activity.getResources().getString(R.string.course_type_main_course);
        } else if(mRecipe.getCourseType() == Recipe.DESSERT) {
            return activity.getResources().getString(R.string.course_type_dessert);
        } else if (mRecipe.getCourseType() == mRecipe.SOUP) {
            return activity.getResources().getString(R.string.course_type_soup);
        } else {
            return "";
        }
    }

    public String getSelectedCourseTypeTranslation(Integer number, Activity activity) {
        if(number == mRecipe.STARTER) {
            return activity.getResources().getString(R.string.course_type_starter);
        } else if (number == mRecipe.MAIN_COURSE) {
            return activity.getResources().getString(R.string.course_type_main_course);
        } else if (number == mRecipe.DESSERT) {
            return activity.getResources().getString(R.string.course_type_dessert);
        } else if(number == mRecipe.SOUP) {
            return activity.getResources().getString(R.string.course_type_soup);
        } else {
            return "";
        }
    }

    public ArrayList<String> getAllCourseTypesArray(Activity activity) {
        mCourseTypesArray = new ArrayList<>();
        mCourseTypesArray.add(getSelectedCourseTypeTranslation(Recipe.STARTER, activity));
        mCourseTypesArray.add(getSelectedCourseTypeTranslation(Recipe.MAIN_COURSE, activity));
        mCourseTypesArray.add(getSelectedCourseTypeTranslation(Recipe.DESSERT, activity));
        mCourseTypesArray.add(getSelectedCourseTypeTranslation(Recipe.SOUP, activity));

        return (ArrayList<String>) mCourseTypesArray;
    }

    public void setCurrentCookBookKey(String key) {
        mCurrentCookBookKey = key;
    }

    public String getCurrentCookBookKey() {
        return mCurrentCookBookKey;
    }

    public void setCurrentCookBookTitle(String title) {
        mCurrentCookBookTitle = title;
    }

    public String getCurrentCookBookTitle() {
        return mCurrentCookBookTitle;
    }

    public void setPicturePath(String picturePath) {
        mPicturePath = picturePath;
    }

    public String getPicturePath() {
        return mPicturePath;
    }

    public void setCurrentFragment(int currentFragmentConstant) {
        mCurrentFragment = currentFragmentConstant;
    }
    public int getCurrentFragmentID() {
        return mCurrentFragment;
    }

    public void resetRecipeInstance() {
        mRecipe = null;
        mIsRecipeHeldInManager = false;
    }

    public boolean isRecipeHeldInManager() {
        return mIsRecipeHeldInManager;
    }

    public void putSelectedRecipeToManager(Recipe recipe) {
        mRecipe = recipe;
        mIsRecipeHeldInManager = true;
    }
}
