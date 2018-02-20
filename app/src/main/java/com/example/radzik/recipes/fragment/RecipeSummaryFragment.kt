package com.example.radzik.recipes.fragment

import android.app.AlertDialog
import android.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.example.radzik.recipes.R
import com.example.radzik.recipes.database.*
import com.example.radzik.recipes.document.DocumentCreator

import kotlinx.android.synthetic.main.fragment_recipe_summary.*

/**
 * Created by Radzik on 01.11.2017.
 */

class RecipeSummaryFragment : Fragment() {

    private var mManager: RecipeManager? = null

    private lateinit var mDocCreator: DocumentCreator

    // values describing a recipe
    var mRecipe: Recipe? = null
    var mRecipeTitle: String? = null
    var mRecipeSubheading: String? = null
    var mRecipeCourseType: String? = null
    var mCookBookTitle: String? = null
    var mRecipeList: List<EditTextPref>? = null
    var mIsPhotoSelected= false

    // variables used for text animation
    private var mTextToAnimate: CharSequence? = null
    private var mIndex: Int = 0
    private var mDelay: Long = 0
    private var mGivenTextView: TextView? = null

    private val mHandler = Handler()
    private val characterAdder = object : Runnable {
        override fun run() {
            try {
                mGivenTextView!!.text = mTextToAnimate!!.subSequence(0, mIndex++)
                if (mIndex <= mTextToAnimate!!.length) {
                    Log.e("Length check", "${mGivenTextView!!.windowId}, length: ${mGivenTextView!!.text.length}, mIndex: $mIndex, $mTextToAnimate length: ${mTextToAnimate!!.length} ")
                    mHandler.postDelayed(this, mDelay)
                }
            }
            catch (e: StringIndexOutOfBoundsException) {
                e.printStackTrace()
            }
        }
    }

    private var mIsShowPreviewVisible = false

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        mManager = RecipeManager.instance
        mDocCreator = DocumentCreator.instance
        mManager!!.setCurrentFragment(ConstantsForFragmentsSelection.SUMMARY_FRAGMENT)

        mRecipe = mManager!!.currentOrNewRecipe
        mRecipeTitle = mRecipe!!.title
        mRecipeSubheading = mRecipe!!.subheading ?: ""
        mRecipeCourseType = mManager!!.getCourseTypeTranslation(activity)
        mRecipeList = mRecipe!!.list
        mCookBookTitle = mManager!!.currentCookBookTitle ?: ""
        mIsPhotoSelected = !mRecipe!!.photoId.isNullOrEmpty() || !mManager!!.picturePath.isNullOrEmpty()

        return inflater?.inflate(R.layout.fragment_recipe_summary, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfViewDisplay.setScrollBar(pdfViewDisplayScrollBar)
        visibleRecipeSummaryPart!!.visibility = View.VISIBLE
        hiddenPartShowRecipePreview!!.visibility = View.GONE

        buttonSaveRecipe.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                checkRecipeForUpload(mRecipe!!)
            }
        })
        // handles display recipe parts animation
        startAnimatingAll()
    }

    private fun animateText(text: CharSequence, givenTextView: TextView) {
        mTextToAnimate = text
        mIndex = 0
        mGivenTextView = givenTextView

        mHandler.removeCallbacks(characterAdder)
        mHandler.postDelayed(characterAdder, mDelay)
    }

    private fun setCharacterDelay(millis: Long) {
        mDelay = millis
    }

    private fun startAnimatingAll() {
        setCharacterDelay(100)

        recipeTitleCheck.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (recipeTitleCheck.text.length == mRecipeTitle!!.length) {
                    // shows check drawable
                    recipeTitleCheckDraw.visibility = View.VISIBLE

                    if(!mRecipeSubheading!!.isEmpty()) {
                        animateText(mRecipeSubheading!!, recipeSubheadingCheck)
                    }
                    else {
                        // shows cross drawable for recipe Subheading
                        recipeSubheadingCrossDraw.visibility = View.VISIBLE

                        if (!mRecipeCourseType.isNullOrEmpty()) {
                            animateText(mRecipeCourseType!!, recipeCourseTypeCheck)
                        }
                        else {
                            recipeCourseTypeCrossDraw.visibility = View.VISIBLE

                            if(!mCookBookTitle!!.isEmpty()) {
                                animateText(mCookBookTitle!!, cookbookTitleCheck)
                            }
                            else {
                                cookbookTitleCrossDraw.visibility = View.VISIBLE

                                if(mIsPhotoSelected) {
                                    photoSelectedCheckDraw.visibility = View.VISIBLE
                                }
                                else {
                                    photoSelectedCrossDraw.visibility = View.VISIBLE
                                }

                                if (mRecipe!!.list.isEmpty()) {
                                    recipeWrittenCrossDraw.visibility = View.VISIBLE

                                    attachOnClickListenerToViews(
                                            recipeTitleTextView,
                                            recipeTitleCheck,
                                            recipeSubheadingTextView,
                                            recipeSubheadingCheck,
                                            recipeCourseTypeTextView,
                                            recipeCourseTypeCheck,
                                            cookBookTitleTextView,
                                            cookbookTitleCheck,
                                            photoTextView,
                                            recipeWrittenTextView)
                                }
                                else {
                                    recipeWrittenCheckDraw.visibility = View.VISIBLE

                                    attachOnClickListenerToViews(
                                            recipeTitleTextView,
                                            recipeTitleCheck,
                                            recipeSubheadingTextView,
                                            recipeSubheadingCheck,
                                            recipeCourseTypeTextView,
                                            recipeCourseTypeCheck,
                                            cookBookTitleTextView,
                                            cookbookTitleCheck,
                                            photoTextView,
                                            recipeWrittenTextView)
                                }
                            }
                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        recipeSubheadingCheck.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (recipeSubheadingCheck.text.length == mRecipeSubheading!!.length) {
                    // shows check drawable for recipe subheading
                    recipeSubheadingCheckDraw.visibility = View.VISIBLE

                    if (!mRecipeCourseType.isNullOrEmpty()) {
                        animateText(mRecipeCourseType!!, recipeCourseTypeCheck)
                    }
                    else {
                        recipeCourseTypeCrossDraw.visibility = View.VISIBLE

                        if(!mCookBookTitle!!.isEmpty()) {
                            animateText(mCookBookTitle!!, cookbookTitleCheck)
                        }
                        else {
                            cookbookTitleCrossDraw.visibility = View.VISIBLE

                            if(mIsPhotoSelected) {
                                photoSelectedCheckDraw.visibility = View.VISIBLE
                            }
                            else {
                                photoSelectedCrossDraw.visibility = View.VISIBLE
                            }

                            if (mRecipe!!.list.isEmpty()) {
                                recipeWrittenCrossDraw.visibility = View.VISIBLE

                                attachOnClickListenerToViews(
                                        recipeTitleTextView,
                                        recipeTitleCheck,
                                        recipeSubheadingTextView,
                                        recipeSubheadingCheck,
                                        recipeCourseTypeTextView,
                                        recipeCourseTypeCheck,
                                        cookBookTitleTextView,
                                        cookbookTitleCheck,
                                        photoTextView,
                                        recipeWrittenTextView)
                            }
                            else {
                                recipeWrittenCheckDraw.visibility = View.VISIBLE

                                attachOnClickListenerToViews(
                                        recipeTitleTextView,
                                        recipeTitleCheck,
                                        recipeSubheadingTextView,
                                        recipeSubheadingCheck,
                                        recipeCourseTypeTextView,
                                        recipeCourseTypeCheck,
                                        cookBookTitleTextView,
                                        cookbookTitleCheck,
                                        photoTextView,
                                        recipeWrittenTextView)
                            }

                        }
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        recipeCourseTypeCheck.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(recipeCourseTypeCheck.text.length == mRecipeCourseType!!.length) {
                    // shows check drawable for recipe course type
                    recipeCourseTypeCheckDraw.visibility = View.VISIBLE

                    if(!mCookBookTitle!!.isEmpty()) {
                        animateText(mCookBookTitle!!, cookbookTitleCheck)
                    }
                    else {
                        cookbookTitleCrossDraw.visibility = View.VISIBLE

                        if(mIsPhotoSelected) {
                            photoSelectedCheckDraw.visibility = View.VISIBLE
                        }
                        else {
                            photoSelectedCrossDraw.visibility = View.VISIBLE
                        }

                        if (mRecipe!!.list.isEmpty()) {
                            recipeWrittenCrossDraw.visibility = View.VISIBLE

                            attachOnClickListenerToViews(
                                    recipeTitleTextView,
                                    recipeTitleCheck,
                                    recipeSubheadingTextView,
                                    recipeSubheadingCheck,
                                    recipeCourseTypeTextView,
                                    recipeCourseTypeCheck,
                                    cookBookTitleTextView,
                                    cookbookTitleCheck,
                                    photoTextView,
                                    recipeWrittenTextView)
                        }
                        else {
                            recipeWrittenCheckDraw.visibility = View.VISIBLE

                            attachOnClickListenerToViews(
                                    recipeTitleTextView,
                                    recipeTitleCheck,
                                    recipeSubheadingTextView,
                                    recipeSubheadingCheck,
                                    recipeCourseTypeTextView,
                                    recipeCourseTypeCheck,
                                    cookBookTitleTextView,
                                    cookbookTitleCheck,
                                    photoTextView,
                                    recipeWrittenTextView)
                        }
                    }


                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cookbookTitleCheck.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(cookbookTitleCheck.text.length == mCookBookTitle!!.length) {
                    // shows check drawable for cookbook title
                    cookbookTitleCheckDraw.visibility = View.VISIBLE

                    if(mIsPhotoSelected) {
                        photoSelectedCheckDraw.visibility = View.VISIBLE
                    }
                    else {
                        photoSelectedCrossDraw.visibility = View.VISIBLE
                    }

                    if (mRecipe!!.list.isEmpty()) {
                        recipeWrittenCrossDraw.visibility = View.VISIBLE

                        attachOnClickListenerToViews(
                                recipeTitleTextView,
                                recipeTitleCheck,
                                recipeSubheadingTextView,
                                recipeSubheadingCheck,
                                recipeCourseTypeTextView,
                                recipeCourseTypeCheck,
                                cookBookTitleTextView,
                                cookbookTitleCheck,
                                photoTextView,
                                recipeWrittenTextView)
                    }
                    else {
                        recipeWrittenCheckDraw.visibility = View.VISIBLE

                        attachOnClickListenerToViews(
                                recipeTitleTextView,
                                recipeTitleCheck,
                                recipeSubheadingTextView,
                                recipeSubheadingCheck,
                                recipeCourseTypeTextView,
                                recipeCourseTypeCheck,
                                cookBookTitleTextView,
                                cookbookTitleCheck,
                                photoTextView,
                                recipeWrittenTextView)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        if(!mRecipeTitle!!.isEmpty()) {
            animateText(mRecipeTitle!!, recipeTitleCheck) // beginning of animation
        }
        else {
            recipeTitleCheckCrossDraw.visibility = View.VISIBLE

            if(!mRecipeSubheading!!.isEmpty()) {
                animateText(mRecipeSubheading!!, recipeSubheadingCheck)
            }
            else {
                // shows cross drawable for recipe Subheading
                recipeSubheadingCrossDraw.visibility = View.VISIBLE

                if (!mRecipeCourseType.isNullOrEmpty()) {
                    animateText(mRecipeCourseType!!, recipeCourseTypeCheck)
                }
                else {
                    recipeCourseTypeCrossDraw.visibility = View.VISIBLE

                    if(!mCookBookTitle!!.isEmpty()) {
                        animateText(mCookBookTitle!!, cookbookTitleCheck)
                    }
                    else {
                        cookbookTitleCrossDraw.visibility = View.VISIBLE

                        if(mIsPhotoSelected) {
                            photoSelectedCheckDraw.visibility = View.VISIBLE
                        }
                        else {
                            photoSelectedCrossDraw.visibility = View.VISIBLE
                        }

                        if (mRecipe!!.list.isEmpty()) {
                            recipeWrittenCrossDraw.visibility = View.VISIBLE

                            attachOnClickListenerToViews(
                                    recipeTitleTextView,
                                    recipeTitleCheck,
                                    recipeSubheadingTextView,
                                    recipeSubheadingCheck,
                                    recipeCourseTypeTextView,
                                    recipeCourseTypeCheck,
                                    cookBookTitleTextView,
                                    cookbookTitleCheck,
                                    photoTextView,
                                    recipeWrittenTextView)
                        }
                        else {
                            recipeWrittenCheckDraw.visibility = View.VISIBLE

                            attachOnClickListenerToViews(
                                    recipeTitleTextView,
                                    recipeTitleCheck,
                                    recipeSubheadingTextView,
                                    recipeSubheadingCheck,
                                    recipeCourseTypeTextView,
                                    recipeCourseTypeCheck,
                                    cookBookTitleTextView,
                                    cookbookTitleCheck,
                                    photoTextView,
                                    recipeWrittenTextView)
                        }
                    }
                }
            }
        }
    }

    val onTxtViewClickListener = View.OnClickListener { v ->
        when(v!!.id) {
            R.id.recipeTitleTextView -> openNextFragmentWithBoolean(AddRecipeTitleFragment())
            R.id.recipeTitleCheck -> openNextFragmentWithBoolean(AddRecipeTitleFragment())

            R.id.recipeSubheadingTextView -> openNextFragmentWithBoolean(AddRecipeTitleFragment())
            R.id.recipeSubheadingCheck -> openNextFragmentWithBoolean(AddRecipeTitleFragment())

            R.id.recipeCourseTypeTextView -> openNextFragmentWithBoolean(AddRecipeTitleFragment())
            R.id.recipeCourseTypeCheck -> openNextFragmentWithBoolean(AddRecipeTitleFragment())

            R.id.cookBookTitleTextView -> openNextFragmentWithBoolean(AddRecipeTitleFragment())
            R.id.cookbookTitleCheck -> openNextFragmentWithBoolean(AddRecipeTitleFragment())

            R.id.photoTextView -> openNextFragmentWithBoolean(ChoosePhotoFragment())

            R.id.recipeWrittenTextView -> openNextFragmentWithBoolean(WriteRecipeFragment())
        }
    }

    private fun attachOnClickListenerToViews(vararg views: View) {
        views.forEach { it.setOnClickListener(onTxtViewClickListener) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_doc_preview, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.doc_preview_icon -> {
                run {
                    when {
                        !mIsShowPreviewVisible -> // ----- TEST
                            /*pdfViewDisplay.fromAsset("light_brown_full_preview.pdf").load()
                            visibleRecipeSummaryPart.visibility = View.GONE
                            hiddenPartShowRecipePreview.visibility = View.VISIBLE
                            mIsShowPreviewVisible = true*/
                            when {
                                mDocCreator.recipeFile == null -> Toast.makeText(context, "Problem with showing preview", Toast.LENGTH_SHORT).show()
                                mDocCreator.recipeFile != null -> {
                                    pdfViewDisplay.fromFile(mDocCreator.recipeFile).load()
                                    visibleRecipeSummaryPart.visibility = View.GONE
                                    hiddenPartShowRecipePreview.visibility = View.VISIBLE
                                    mIsShowPreviewVisible = true
                                }
                            }
                        mIsShowPreviewVisible -> {
                            visibleRecipeSummaryPart.visibility = View.VISIBLE
                            hiddenPartShowRecipePreview.visibility = View.GONE
                            mIsShowPreviewVisible = false
                        }
                    }
                }
                return super.onOptionsItemSelected(item)
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    private fun checkRecipeForUpload(recipe: Recipe) {
        if(!mRecipeTitle.isNullOrEmpty() && mRecipeList!!.isNotEmpty()) {
            if(mRecipeSubheading.isNullOrEmpty() || mRecipeCourseType.isNullOrEmpty() || mCookBookTitle.isNullOrEmpty() || !mIsPhotoSelected) {
                val alertDialog = AlertDialog.Builder(activity)
                alertDialog.setTitle("Recipe creator")
                alertDialog.setMessage("Do you want to save your recipe with missing fields?")

                val input = EditText(activity)
                val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT)
                input.layoutParams = lp
                alertDialog.setView(input)

                alertDialog.setPositiveButton("YES"
                ) { dialog, which ->

                    // UPLOAD

                    dialog.dismiss()
                }

                alertDialog.setNegativeButton("NO"
                ) { dialog, which -> dialog.dismiss() }

                alertDialog.show()
            }
            else if(!mRecipeSubheading.isNullOrEmpty() && !mRecipeCourseType.isNullOrEmpty() && !mCookBookTitle.isNullOrEmpty() && mIsPhotoSelected) {

                // UPLOAD RECIPE

            }
        } else if(mRecipeTitle.isNullOrEmpty()) {

            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setTitle("Recipe title can't be empty!")
            alertDialog.setMessage("Do you want to change it now?")

            val input = EditText(activity)
            val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
            input.layoutParams = lp
            alertDialog.setView(input)

            alertDialog.setPositiveButton("YES"
            ) { dialog, which ->

                openNextFragmentWithBoolean(AddRecipeTitleFragment())
                dialog.dismiss()
            }

            alertDialog.setNegativeButton("NO"
            ) { dialog, which -> dialog.dismiss() }

            alertDialog.show()

        } else if(mRecipeList!!.isEmpty()) {
            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setTitle("Recipe can't be empty!")
            alertDialog.setMessage("Do you want to write it now?")

            val input = EditText(activity)
            val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
            input.layoutParams = lp
            alertDialog.setView(input)

            alertDialog.setPositiveButton("YES"
            ) { dialog, which ->

                openNextFragmentWithBoolean(WriteRecipeFragment())
                dialog.dismiss()
            }

            alertDialog.setNegativeButton("NO"
            ) { dialog, which -> dialog.dismiss() }

            alertDialog.show()
        }


    }

    private fun openNextFragmentWithBoolean(fragment: Fragment) {
        val arguments = Bundle()
        arguments.putBoolean("mIsBackFromSummary", true)
        fragment.arguments = arguments

        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }
}
