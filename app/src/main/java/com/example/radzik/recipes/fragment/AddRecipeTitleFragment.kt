package com.example.radzik.recipes.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast

import com.airnauts.toolkit.utils.KeyboardUtils
import com.example.radzik.recipes.R
import com.example.radzik.recipes.adapters.CourseTypeSpinnerAdapter
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection
import com.example.radzik.recipes.database.CookBook
import com.example.radzik.recipes.database.Recipe
import com.example.radzik.recipes.database.RecipeManager
import com.example.radzik.recipes.database.firebase.GeneralDataManager

import java.util.ArrayList
import java.util.HashMap

import kotlinx.android.synthetic.main.fragment_add_recipe_title.*

/**
 * Created by Radzik on 11.09.2017.
 */

// TODO: spinner for cookbook titles and to create a new cookbook
// todo: dialog to put a new cookbook title

// TODO: spinner for recipe course type
// TODO: spinner for style of a document

class AddRecipeTitleFragment : Fragment() {

    private var mIsCookBookSpinnerTouched = false
    private var mIsNewCookBookCreated = false
    private var mLastCookBookCreated: CookBook? = null

    internal lateinit var mRecipeManager: RecipeManager
    internal lateinit var mGeneralDataManager: GeneralDataManager

    private var mCookBooksAndRecipesMap: HashMap<CookBook, ArrayList<Recipe>>? = null
    private var mCurrentUserCookBookKeyTitleMap: HashMap<String, String>? = null
    private var mCookBooksTitles: MutableList<String>? = null
    private var mCookBookTitle = ""
    private var mCookBookUploadKey: String? = null

    private var mIsBackFromSummary = false

    private var mCourseTypeList: MutableList<String>? = null

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // used to change menu on fragment change
        setHasOptionsMenu(true)

        // checks whether a user was coming back from RecipeSummaryFragment to fill empty info
        try {
            val arguments = arguments
            mIsBackFromSummary = arguments.getBoolean("mIsBackFromSummary")
        }
        catch (e: NullPointerException) {
            e.printStackTrace()
        }

        return inflater?.inflate(R.layout.fragment_add_recipe_title, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // sets listener, so that when you click outside the edit text it will hide the keyboard
        setupUI(view)

        // sets up data managers
        mRecipeManager = RecipeManager.instance
        mGeneralDataManager = GeneralDataManager.instance

        // set currently opened fragment as CHOOSE DOC LAYOUT FRAGMENT
        mRecipeManager.setCurrentFragment(ConstantsForFragmentsSelection.TITLE_FRAGMENT)

        if(!mRecipeManager.currentOrNewRecipe.title.isNullOrEmpty()) {
            editTextTitle.setText(mRecipeManager.currentOrNewRecipe.title ?: "")
        }

        if(!mRecipeManager.currentOrNewRecipe.subheading.isNullOrEmpty()) {
            editTextSubheading.setText(mRecipeManager.currentOrNewRecipe.subheading ?: "")
        }

        // sets up cookBookTitles array
        mCookBooksTitles = ArrayList()
        mCookBooksTitles!!.add(0, "new CookBook")
        mCurrentUserCookBookKeyTitleMap = mGeneralDataManager.currentUserCookBookKeyTitleMap
        for (key in mCurrentUserCookBookKeyTitleMap!!.keys) {
            mCookBooksTitles!!.add(mCurrentUserCookBookKeyTitleMap!![key]!!)
        }

        buttonChooseStyleFragment!!.setOnClickListener {
            if (editTextTitle.text.isNotEmpty() && mGeneralDataManager.isRecipeInTheDatabase(editTextTitle.text.toString())!!) {
                Toast.makeText(context, context.resources.getString(R.string.toast_recipe_is_in_database), Toast.LENGTH_SHORT).show()
                editTextTitle.requestFocus()
                KeyboardUtils.show(editTextTitle)
            }
            else if(editTextTitle.text.isNotEmpty() && !mGeneralDataManager.isRecipeInTheDatabase(editTextTitle.text.toString())!!){
                mRecipeManager.currentOrNewRecipe.title = editTextTitle.text.toString()
                if (!editTextSubheading.text.toString().isEmpty())
                    mRecipeManager.currentOrNewRecipe.subheading = editTextSubheading.text.toString()

                if(mIsBackFromSummary) {
                    openNextFragment(RecipeSummaryFragment())
                }
                else {
                    openNextFragment(WriteRecipeFragment())
                }
            }
            else if(editTextTitle.text.isEmpty()) {
                if(mIsBackFromSummary) {
                    openNextFragment(RecipeSummaryFragment())
                }
                else {
                    openNextFragment(WriteRecipeFragment())
                }
            }
        }

        // sets up listeners updating cookbooks and recipes inside cookbooks
        mGeneralDataManager.setOnCookBookUploadedListener(cookBookTitlesUpdateListener)
        mGeneralDataManager.setOnRecipesUpdateListener(recipesUpdateListener)

        // sets up cookbooks spinner
        val cookBooksTitlesAdapter = ArrayAdapter(context,
                android.R.layout.simple_spinner_item, mCookBooksTitles!!)
        addRecipeTitleCookbookSpinner.adapter = cookBooksTitlesAdapter
        addRecipeTitleCookbookSpinner.post {
            addRecipeTitleCookbookSpinner.setOnTouchListener { v, event ->
                mIsCookBookSpinnerTouched = true
                false
            }

            addRecipeTitleCookbookSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    if (mIsCookBookSpinnerTouched) {
                        if (position == 0) {
                            val alertDialog = AlertDialog.Builder(activity)
                            alertDialog.setTitle("Create CookBook")
                            alertDialog.setMessage("Title must contain minimum 3 letters")

                            val input = EditText(activity)
                            val lp = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT)
                            input.layoutParams = lp
                            alertDialog.setView(input)

                            alertDialog.setPositiveButton("YES"
                            ) { dialog, which ->
                                mCookBookTitle = input.text.toString()
                                if (mCookBookTitle.isEmpty() || mCookBookTitle.length < 3) {
                                    Toast.makeText(context, "Title too short", Toast.LENGTH_SHORT).show()
                                    addRecipeTitleCookbookSpinner.isSelected = false
                                } else if (mCookBooksTitles!!.contains(mCookBookTitle)) {
                                    Toast.makeText(context, "There already is a CookBook with such title!", Toast.LENGTH_LONG).show()
                                    addRecipeTitleCookbookSpinner.isSelected = false
                                } else if (mCookBookTitle.length >= 3 && !mCookBooksTitles!!.contains(mCookBookTitle)) {
                                    mIsNewCookBookCreated = true
                                    mLastCookBookCreated = CookBook()
                                    mLastCookBookCreated!!.title = mCookBookTitle
                                    mCookBookUploadKey = mGeneralDataManager.uploadCookBook(activity, mLastCookBookCreated)
                                    mRecipeManager.currentCookBookKey = mCookBookUploadKey
                                    addRecipeTitleCookbookSpinner.isSelected = false
                                }
                            }

                            alertDialog.setNegativeButton("NO"
                            ) { dialog, which -> dialog.dismiss() }

                            alertDialog.show()

                        } else {
                            Log.e("POSITION: ", "selected " + position)

                            val title = mCookBooksTitles!![position]
                            mCookBookUploadKey = GeneralDataManager.instance.getCurrentUserCookBookKey(title)
                            mRecipeManager.currentCookBookKey = mCookBookUploadKey
                            mRecipeManager.currentCookBookTitle = title
                            addRecipeTitleCookbookSpinner.setSelection(position, true)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    addRecipeTitleCookbookSpinner.clearFocus()
                    addRecipeTitleCookbookSpinner.isSelected = false
                }
            }
        }


        // sets up Course Type Spinner data
        mCourseTypeList = ArrayList()
        mCourseTypeList!!.add("Select course type...")
        RecipeManager.instance.getAllCourseTypesArray(activity)!!.forEach { mCourseTypeList!!.add(it) }

        val courseTypeAdapter = CourseTypeSpinnerAdapter(context,
                android.R.layout.simple_spinner_item, mCourseTypeList!!)

        addRecipeCourseTypeCookbookSpinner!!.adapter = courseTypeAdapter

        if (RecipeManager.instance.currentOrNewRecipe.courseType != 99) {
            addRecipeCourseTypeCookbookSpinner!!.setSelection(RecipeManager.instance.currentOrNewRecipe.courseType)
        }

        // sets up course type spinner
        addRecipeCourseTypeCookbookSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if(position > 0) {
                    mRecipeManager.currentOrNewRecipe.courseType = position
                    addRecipeCourseTypeCookbookSpinner!!.setSelection(position, true)
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                addRecipeCourseTypeCookbookSpinner!!.clearFocus()
            }
        }




    }

    internal var cookBookTitlesUpdateListener: GeneralDataManager.OnCookBookUploadedListener = object : GeneralDataManager.OnCookBookUploadedListener {
        override fun OnCookBookUploaded(cookBooksMap: HashMap<String, String>?, cookBookTitle: String?) {
            mCurrentUserCookBookKeyTitleMap = cookBooksMap
            mCookBooksTitles = ArrayList()
            mCookBooksTitles!!.add(0, "new CookBook")

            mCurrentUserCookBookKeyTitleMap = mGeneralDataManager.currentUserCookBookKeyTitleMap
            for (key in mCurrentUserCookBookKeyTitleMap!!.keys) {
                mCookBooksTitles!!.add(mCurrentUserCookBookKeyTitleMap!![key]!!)
            }

            val cookBooksTitlesAdapter = ArrayAdapter(context,
                    android.R.layout.simple_spinner_item, mCookBooksTitles!!)
            addRecipeTitleCookbookSpinner.adapter = cookBooksTitlesAdapter

            if (mIsNewCookBookCreated) {
                for (i in 0 until mCookBooksTitles!!.size - 1) {
                    if (mCookBooksTitles!![i] == mLastCookBookCreated!!.title) {
                        addRecipeTitleCookbookSpinner.setSelection(i)
                        mIsNewCookBookCreated = false
                    }
                }
            }
        }
    }

    // sets listener for MAP
    internal var recipesUpdateListener: GeneralDataManager.OnRecipesUpdateListener = object : GeneralDataManager.OnRecipesUpdateListener {
        override fun onMapChanged(map: HashMap<CookBook, ArrayList<Recipe>>?) {
            mCookBooksAndRecipesMap = map

            mCookBooksAndRecipesMap = mGeneralDataManager.recipesInCookBooksMap
            for (cookBook in mCookBooksAndRecipesMap!!.keys) {
                cookBook.title?.let { mCookBooksTitles!!.add(it) }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_question, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun setupUI(view: View?) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view?.setOnTouchListener { v, event ->
                hideSoftKeyboard(activity)
                editTextTitle.clearFocus()
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            (0 until view.childCount)
                    .map { view.getChildAt(it) }
                    .forEach { setupUI(it) }
        }
    }

    private fun openNextFragment(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {

        fun hideSoftKeyboard(activity: Activity) {
            val inputMethodManager = activity.getSystemService(
                    Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken, 0)
        }
    }
}
