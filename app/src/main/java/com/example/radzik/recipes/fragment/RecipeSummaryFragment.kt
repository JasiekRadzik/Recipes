package com.example.radzik.recipes.fragment

import android.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView

import com.example.radzik.recipes.R
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection
import com.example.radzik.recipes.database.Recipe
import com.example.radzik.recipes.database.RecipeManager
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.ScrollBar

import butterknife.BindView
import butterknife.ButterKnife

/**
 * Created by Radzik on 01.11.2017.
 */

class RecipeSummaryFragment : Fragment() {

    @BindView(R.id.visibleRecipeSummaryPart)
    internal var mVisibleRecipeSummaryPart: RelativeLayout? = null

    @BindView(R.id.hiddenPartShowRecipePreview)
    internal var mHiddenPartShowRecipePreview: RelativeLayout? = null

    @BindView(R.id.recipeTitleCheck)
    internal var mRecipeTitleCheckTextView: TextView? = null

    internal var mPdfView: PDFView
    internal var mScrollBar: ScrollBar

    private var mManager: RecipeManager? = null

    // variables used for text animation
    private var mTextToAnimate: CharSequence? = null
    private var mIndex: Int = 0
    private var mDelay: Long = 0

    private val mHandler = Handler()
    private val characterAdder = object : Runnable {
        override fun run() {
            mRecipeTitleCheckTextView!!.text = mTextToAnimate!!.subSequence(0, mIndex++)
            if (mIndex <= mTextToAnimate!!.length) {
                mHandler.postDelayed(this, mDelay)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        val view = inflater.inflate(R.layout.fragment_recipe_summary, container, false)
        ButterKnife.bind(this, view)

        mManager = RecipeManager.instance
        mManager!!.setCurrentFragment(ConstantsForFragmentsSelection.SUMMARY_FRAGMENT)
        val recipe = mManager!!.currentOrCreateNewRecipe
        val recipeTitle = recipe.title
        val cookBookTitle = mManager!!.currentCookBookTitle
        val isPhotoSelected = false

        mPdfView = view.findViewById(R.id.pdfViewDisplay) as PDFView
        mScrollBar = view.findViewById(R.id.pdfViewDisplayScrollBar) as ScrollBar
        mPdfView.setScrollBar(mScrollBar)

        mVisibleRecipeSummaryPart!!.visibility = View.VISIBLE
        mHiddenPartShowRecipePreview!!.visibility = View.GONE

        mRecipeTitleCheckTextView!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        setCharacterDelay(50)
        animateText("Sample String")



        return view
    }

    fun animateText(text: CharSequence) {
        mTextToAnimate = text
        mIndex = 0

        mRecipeTitleCheckTextView!!.text = ""
        mHandler.removeCallbacks(characterAdder)
        mHandler.postDelayed(characterAdder, mDelay)
    }

    fun setCharacterDelay(millis: Long) {
        mDelay = millis
    }
}
