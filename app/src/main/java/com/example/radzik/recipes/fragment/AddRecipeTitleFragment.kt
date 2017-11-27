package com.example.radzik.recipes.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.app.FragmentTransaction
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast

import com.airnauts.toolkit.utils.KeyboardUtils
import com.example.radzik.recipes.R
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection
import com.example.radzik.recipes.database.CookBook
import com.example.radzik.recipes.database.Recipe
import com.example.radzik.recipes.database.RecipeManager
import com.example.radzik.recipes.database.firebase.GeneralDataManager

import java.util.ArrayList
import java.util.HashMap

import butterknife.BindView
import butterknife.ButterKnife

/**
 * Created by Radzik on 11.09.2017.
 */

// TODO: spinner for cookbook titles and to create a new cookbook
// todo: dialog to put a new cookbook title

// TODO: spinner for recipe course type
// TODO: spinner for style of a document

class AddRecipeTitleFragment : Fragment() {

    @BindView(R.id.edit_text_title)
    internal var mEditTextRecipeTitle: EditText? = null

    @BindView(R.id.edit_text_subheading)
    internal var mEditTextRecipeSubheading: EditText? = null

    @BindView(R.id.addRecipeTitleCookbookSpinner)
    internal var mCookBookSpinner: Spinner? = null

    private var mIsCookBookSpinnerTouched = false
    private var mIsNewCookBookCreated = false
    private var mLastCookBookCreated: CookBook? = null

    @BindView(R.id.addRecipeCourseTypeCookbookSpinner)
    internal var mCourseTypeSpinner: Spinner? = null

    @BindView(R.id.buttonChooseStyleFragment)
    internal var mChooseStyleFragmentButton: Button? = null

    internal var mRecipeManager: RecipeManager
    internal var mGeneralDataManager: GeneralDataManager

    private var mCookBooksAndRecipesMap: HashMap<CookBook, ArrayList<Recipe>>? = null
    private var mCurrentUserCookBookKeyTitleMap: HashMap<String, String>? = null
    private var mCookBooksTitles: MutableList<String>? = null
    private var mCookBookTitle = ""
    private var mCookBookUploadKey: String? = null

    private var mCourseTypeList: List<String>? = null

    internal var cookBookTitlesUpdateListener: GeneralDataManager.OnCookBookUploadedListener = object : GeneralDataManager.OnCookBookUploadedListener {
        override fun OnCookBookUploaded(cookBooksMap: HashMap<String, String>?, cookBookTitle: String?) {
            mCurrentUserCookBookKeyTitleMap = cookBooksMap
            mCookBooksTitles = ArrayList()
            mCookBooksTitles!!.add(0, "new CookBook")

            mCurrentUserCookBookKeyTitleMap = mGeneralDataManager.currentUserCookBookKeyTitleMap
            for (key in mCurrentUserCookBookKeyTitleMap!!.keys) {
                mCookBooksTitles!!.add(mCurrentUserCookBookKeyTitleMap!![key])
            }

            val cookBooksTitlesAdapter = ArrayAdapter(context,
                    android.R.layout.simple_spinner_item, mCookBooksTitles!!)
            mCookBookSpinner!!.adapter = cookBooksTitlesAdapter

            if (mIsNewCookBookCreated) {
                for (i in 0 until mCookBooksTitles!!.size - 1) {
                    if (mCookBooksTitles!![i] == mLastCookBookCreated!!.title) {
                        mCookBookSpinner!!.setSelection(i)
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
                mCookBooksTitles!!.add(cookBook.title)
            }
        }
    }

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        val view = inflater.inflate(R.layout.fragment_add_recipe_title, container, false)
        ButterKnife.bind(this, view)

        // used to change menu on fragment change
        setHasOptionsMenu(true)

        // sets listener, so that when you click outside the edit text it will hide the keyboard
        setupUI(view)

        // set currently opened fragment as CHOOSE DOC LAYOUT FRAGMENT
        RecipeManager.instance.setCurrentFragment(ConstantsForFragmentsSelection.TITLE_FRAGMENT)

        // sets up data managers
        mRecipeManager = RecipeManager.instance
        mGeneralDataManager = GeneralDataManager.getInstance()

        // sets up cookBookTitles array
        mCookBooksTitles = ArrayList()
        mCookBooksTitles!!.add(0, "new CookBook")
        mCurrentUserCookBookKeyTitleMap = mGeneralDataManager.currentUserCookBookKeyTitleMap
        for (key in mCurrentUserCookBookKeyTitleMap!!.keys) {
            mCookBooksTitles!!.add(mCurrentUserCookBookKeyTitleMap!![key])
        }

        mChooseStyleFragmentButton!!.setOnClickListener {
            if (mEditTextRecipeTitle!!.text.toString().length == 0) {
                Toast.makeText(context, context.resources.getString(R.string.toast_no_recipe_title), Toast.LENGTH_SHORT).show()
                mEditTextRecipeTitle!!.requestFocus()
                KeyboardUtils.show(mEditTextRecipeTitle)
            } else if (mEditTextRecipeTitle!!.text.toString().length < 3) {
                Toast.makeText(context, context.resources.getString(R.string.toast_recipe_title_too_short), Toast.LENGTH_SHORT).show()
                mEditTextRecipeTitle!!.requestFocus()
                KeyboardUtils.show(mEditTextRecipeTitle)
            } else if (mEditTextRecipeTitle!!.text.toString().length >= 3 && mGeneralDataManager.isRecipeInTheDatabase(mEditTextRecipeTitle!!.text.toString())!!) {
                Toast.makeText(context, context.resources.getString(R.string.toast_recipe_is_in_database), Toast.LENGTH_SHORT).show()
                mEditTextRecipeTitle!!.requestFocus()
                KeyboardUtils.show(mEditTextRecipeTitle)
            } else if (mEditTextRecipeTitle!!.text.toString().length >= 3 && (!mGeneralDataManager.isRecipeInTheDatabase(mEditTextRecipeTitle!!.text.toString()))!!) {
                mRecipeManager.currentOrCreateNewRecipe.title = mEditTextRecipeTitle!!.text.toString()
                if (mEditTextRecipeSubheading!!.text.toString().length != 0)
                    mRecipeManager.currentOrCreateNewRecipe.subheading = mEditTextRecipeSubheading!!.text.toString()

                openNextFragment(WriteRecipeFragment())
            }
        }

        // sets up listeners updating cookbooks and recipes inside cookbooks
        GeneralDataManager.getInstance().setOnCookBookUploadedListener(cookBookTitlesUpdateListener)
        GeneralDataManager.getInstance().setOnRecipesUpdateListener(recipesUpdateListener)

        // sets up cookbooks spinner
        val cookBooksTitlesAdapter = ArrayAdapter(context,
                android.R.layout.simple_spinner_item, mCookBooksTitles!!)
        mCookBookSpinner!!.adapter = cookBooksTitlesAdapter
        mCookBookSpinner!!.post {
            mCookBookSpinner!!.setOnTouchListener { v, event ->
                mIsCookBookSpinnerTouched = true
                false
            }

            mCookBookSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
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
                                    mCookBookSpinner!!.isSelected = false
                                } else if (mCookBooksTitles!!.contains(mCookBookTitle)) {
                                    Toast.makeText(context, "There already is a CookBook with such title!", Toast.LENGTH_LONG).show()
                                    mCookBookSpinner!!.isSelected = false
                                } else if (mCookBookTitle.length >= 3 && !mCookBooksTitles!!.contains(mCookBookTitle)) {
                                    mIsNewCookBookCreated = true
                                    mLastCookBookCreated = CookBook()
                                    mLastCookBookCreated!!.title = mCookBookTitle
                                    mCookBookUploadKey = mGeneralDataManager.uploadCookBook(activity, mLastCookBookCreated)
                                    mRecipeManager.currentCookBookKey = mCookBookUploadKey
                                    mCookBookSpinner!!.isSelected = false
                                }
                            }

                            alertDialog.setNegativeButton("NO"
                            ) { dialog, which -> dialog.dismiss() }

                            alertDialog.show()

                        } else {
                            Log.e("POSITION: ", "selected " + position)

                            val title = mCookBooksTitles!![position]
                            mCookBookUploadKey = GeneralDataManager.getInstance().getCurrentUserCookBookKey(title)
                            mRecipeManager.currentCookBookKey = mCookBookUploadKey
                            mRecipeManager.currentCookBookTitle = title
                            mCookBookSpinner!!.setSelection(position, true)
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    mCookBookSpinner!!.clearFocus()
                    mCookBookSpinner!!.isSelected = false
                }
            }
        }


        // sets up Course Type Spinner data
        mCourseTypeList = RecipeManager.instance.getAllCourseTypesArray(activity)

        val courseTypeAdapter = ArrayAdapter(context,
                android.R.layout.simple_spinner_item, mCourseTypeList!!)
        mCourseTypeSpinner!!.adapter = courseTypeAdapter

        if (RecipeManager.instance.currentOrCreateNewRecipe.courseType != 99) {
            mCourseTypeSpinner!!.setSelection(RecipeManager.instance.currentOrCreateNewRecipe.courseType)
        }

        // sets up course type spinner
        mCourseTypeSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mRecipeManager.currentOrCreateNewRecipe.courseType = position
                mCourseTypeSpinner!!.setSelection(position, true)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                mCourseTypeSpinner!!.clearFocus()
            }
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_question, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun setupUI(view: View) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                hideSoftKeyboard(activity)
                mEditTextRecipeTitle!!.clearFocus()
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setupUI(innerView)
            }
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
