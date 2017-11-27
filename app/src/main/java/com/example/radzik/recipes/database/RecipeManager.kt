package com.example.radzik.recipes.database

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.net.Uri

import com.example.radzik.recipes.R

import java.lang.reflect.Array
import java.util.ArrayList

/**
 * Created by Radzik on 03.08.2017.
 */

class RecipeManager {
    private var mRecipe: Recipe? = null
    var isRecipeHeldInManager = false
        private set
    var photoUri: Uri? = null
    var picturePath: String? = null
    private var mCourseTypesArray: MutableList<String>? = null
    var currentCookBookKey: String? = null
    var currentCookBookTitle: String? = null
    var currentFragmentID = 0
        private set

    val currentOrCreateNewRecipe: Recipe
        get() {
            if (mRecipe == null) {
                mRecipe = Recipe()
            }

            isRecipeHeldInManager = true
            return mRecipe
        }

    fun resetManagerInstance() {
        mInstance = RecipeManager()
    }

    fun createNewRecipe() {
        mRecipe = Recipe()
    }

    fun getCourseTypeTranslation(activity: Activity): String {
        return if (mRecipe!!.courseType == mRecipe!!.STARTER) {
            activity.resources.getString(R.string.course_type_starter)
        } else if (mRecipe!!.courseType == mRecipe!!.MAIN_COURSE) {
            activity.resources.getString(R.string.course_type_main_course)
        } else if (mRecipe!!.courseType == Recipe.DESSERT) {
            activity.resources.getString(R.string.course_type_dessert)
        } else if (mRecipe!!.courseType == mRecipe!!.SOUP) {
            activity.resources.getString(R.string.course_type_soup)
        } else {
            ""
        }
    }

    fun getSelectedCourseTypeTranslation(number: Int?, activity: Activity): String {
        return if (number == mRecipe!!.STARTER) {
            activity.resources.getString(R.string.course_type_starter)
        } else if (number == mRecipe!!.MAIN_COURSE) {
            activity.resources.getString(R.string.course_type_main_course)
        } else if (number == mRecipe!!.DESSERT) {
            activity.resources.getString(R.string.course_type_dessert)
        } else if (number == mRecipe!!.SOUP) {
            activity.resources.getString(R.string.course_type_soup)
        } else {
            ""
        }
    }

    fun getAllCourseTypesArray(activity: Activity): ArrayList<String> {
        mCourseTypesArray = ArrayList()
        mCourseTypesArray!!.add(getSelectedCourseTypeTranslation(Recipe.STARTER, activity))
        mCourseTypesArray!!.add(getSelectedCourseTypeTranslation(Recipe.MAIN_COURSE, activity))
        mCourseTypesArray!!.add(getSelectedCourseTypeTranslation(Recipe.DESSERT, activity))
        mCourseTypesArray!!.add(getSelectedCourseTypeTranslation(Recipe.SOUP, activity))

        return mCourseTypesArray as ArrayList<String>?
    }

    fun setCurrentFragment(currentFragmentConstant: Int) {
        currentFragmentID = currentFragmentConstant
    }

    fun resetRecipeInstance() {
        mRecipe = null
        isRecipeHeldInManager = false
    }

    fun putSelectedRecipeToManager(recipe: Recipe) {
        mRecipe = recipe
        isRecipeHeldInManager = true
    }

    companion object {

        private var mInstance: RecipeManager? = null

        val instance: RecipeManager
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = RecipeManager()
                }

                return mInstance
            }
    }
}
