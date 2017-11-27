package com.example.radzik.recipes.fragment

import android.app.Fragment
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText

import com.example.radzik.recipes.R
import com.example.radzik.recipes.database.ConstantsForRecipePartTypes
import com.example.radzik.recipes.database.CookBook
import com.example.radzik.recipes.database.EditTextPref
import com.example.radzik.recipes.database.EditTextPrefManager
import com.example.radzik.recipes.database.Recipe
import com.example.radzik.recipes.database.RecipeManager
import com.example.radzik.recipes.database.firebase.GeneralDataManager

import butterknife.BindView
import butterknife.ButterKnife

/**
 * Created by Radzik on 19.09.2017.
 */

class TestFragment : Fragment() {

    @BindView(R.id.editTextTestTitle)
    internal var mTitleEditText: EditText? = null

    @BindView(R.id.cookBookEditText)
    internal var mCookBookEditText: EditText? = null

    @BindView(R.id.buttonTestLoadRecipe)
    internal var mLoadTestButton: Button? = null

    @BindView(R.id.buttonTestLoadCookbookAndRecipe)
    internal var mLoadTestCookBookAndRecipe: Button? = null

    @BindView(R.id.buttonTestLoadCookbook)
    internal var mLoadTestCookbook: Button? = null

    @BindView(R.id.buttonTestRemoveRecipeFromCookbook)
    internal var mRemoveTestRecipeFromCookBook: Button? = null

    internal var mManager: RecipeManager

    internal var mCookbookKey = ""
    internal var mRecipeKey = ""

    internal var mRecipe: Recipe

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)

        ButterKnife.bind(this, view)

        mManager = RecipeManager.instance
        mRecipe = mManager.currentOrCreateNewRecipe
        GeneralDataManager.getInstance().attachListeners(GeneralDataManager.RECIPE_UPDATES_LISTENER)

        val editTextPref = EditTextPref()
        editTextPref.text = "Pierwszy upload TEST"
        editTextPref.id = "sfwe342dasdaw"
        editTextPref.recipePartType = ConstantsForRecipePartTypes.HOW_TO_COOK
        EditTextPrefManager.instance.translateTypeToRecipePartName(editTextPref, activity)

        val x: EditTextPref
        x = editTextPref
        mManager.currentOrCreateNewRecipe.list.add(x)

        val cookBook = CookBook()
        /*
        mLoadTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRecipe != null) {
                    mRecipeKey = GeneralDataManager.getInstance().uploadRecipeFile(getActivity());
                }
            }
        });

        mLoadTestCookBookAndRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mRecipeKey.equals("") && !mCookbookKey.equals("")) {
                    GeneralDataManager.getInstance().putRecipeToCookBook(mRecipeKey, mCookbookKey);
                    Log.e("Recipe KEY", "" + mRecipeKey);
                    Log.e("CookBook KEY", "" + mCookbookKey);
                }
            }
        });
 */
        mLoadTestCookbook!!.setOnClickListener { mCookbookKey = GeneralDataManager.getInstance().uploadCookBook(activity, cookBook) }

        mRemoveTestRecipeFromCookBook!!.setOnClickListener { GeneralDataManager.getInstance().removeRecipeFromCookBook(mRecipe, cookBook) }

        mCookBookEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                cookBook.title = mCookBookEditText!!.text.toString()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        mTitleEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                mRecipe.title = mTitleEditText!!.text.toString()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })


        return view
    }
}
