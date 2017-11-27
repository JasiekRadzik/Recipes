package com.example.radzik.recipes.fragment

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast

import com.example.radzik.recipes.R
import com.example.radzik.recipes.database.ConstantsForRecipeDocumentStyles
import com.example.radzik.recipes.database.ConstantsForRecipePartTypes
import com.example.radzik.recipes.database.EditTextPref
import com.example.radzik.recipes.database.EditTextPrefManager
import com.example.radzik.recipes.database.RecipeManager
import com.example.radzik.recipes.document.DocumentCreator
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.ScrollBar

/**
 * Created by Radzik on 17.10.2017.
 */

class DocumentTestFragment : Fragment() {

    internal var mEditTextPref: EditTextPref? = null
    internal var mRecipeManager: RecipeManager
    internal var mDocCreator: DocumentCreator

    internal var mMainRelativeView: RelativeLayout
    internal var mPdfRelativeView: RelativeLayout

    internal var mStartSim: Button

    internal var mPdfView: PDFView
    internal var mScrollBar: ScrollBar

    private var mIsShowPreviewVisible = false

    init {
        setHasOptionsMenu(true)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        val view = inflater.inflate(R.layout.fragment_document_test, container, false)
        setHasOptionsMenu(true)

        mMainRelativeView = view.findViewById(R.id.chooseDocStyleRelativeLayoutTEST) as RelativeLayout
        mPdfRelativeView = view.findViewById(R.id.showPreviewRelativeLayoutTEST) as RelativeLayout

        mStartSim = view.findViewById(R.id.buttonStartSimTEST) as Button

        mPdfView = view.findViewById(R.id.pdfViewTEST) as PDFView
        mScrollBar = view.findViewById(R.id.pdfScrollBarTEST) as ScrollBar
        mPdfView.setScrollBar(mScrollBar)

        mMainRelativeView.visibility = View.VISIBLE
        mPdfRelativeView.visibility = View.GONE

        mRecipeManager = RecipeManager.instance
        mRecipeManager.createNewRecipe()
        mRecipeManager.currentOrCreateNewRecipe.title = "hehe"
        mRecipeManager.currentOrCreateNewRecipe.documentStyle = ConstantsForRecipeDocumentStyles.RECIPE_LIGHT_BRONZE_INGREDIENTS_LEFT

        mDocCreator = DocumentCreator.getInstance()

        val path = "/storage/emulated/0/Pictures/cheeseCake.jpeg"
        mRecipeManager.picturePath = path

        mStartSim.isEnabled = true

        mStartSim.setOnClickListener {
            for (i in 0..19) {
                mEditTextPref = EditTextPref()
                mEditTextPref!!.recipePartType = ConstantsForRecipePartTypes.HOW_TO_COOK
                EditTextPrefManager.instance.translateTypeToRecipePartName(mEditTextPref!!, activity)
                mEditTextPref!!.text = "Line: $i with text: Hello line $i"

                val x = mEditTextPref
                mRecipeManager.currentOrCreateNewRecipe.list.add(x)
            }

            for (i in 0 until mRecipeManager.currentOrCreateNewRecipe.list.size - 1) {
                Log.e("List part: ", i.toString() + ": " + mRecipeManager.currentOrCreateNewRecipe.list[i].text)
            }

            mDocCreator.prepareEnviromentAndwriteDocument(mRecipeManager.currentOrCreateNewRecipe, activity)
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_doc_preview, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.doc_preview_icon -> {
                run {
                    if (!mIsShowPreviewVisible) {
                        // ----- TEST
                        mPdfView.fromAsset("light_brown_full_preview.pdf").load()
                        mMainRelativeView.visibility = View.GONE
                        mPdfRelativeView.visibility = View.VISIBLE
                        mIsShowPreviewVisible = true

                        if (mDocCreator.recipeFile == null) {
                            Toast.makeText(context, "Problem with showing preview", Toast.LENGTH_SHORT).show()
                        } else if (mDocCreator.recipeFile != null) {
                            mPdfView.fromFile(mDocCreator.recipeFile).load()
                            mMainRelativeView.visibility = View.GONE
                            mPdfRelativeView.visibility = View.VISIBLE
                            mIsShowPreviewVisible = true
                        }
                    } else if (mIsShowPreviewVisible) {
                        mMainRelativeView.visibility = View.VISIBLE
                        mPdfRelativeView.visibility = View.GONE
                        mIsShowPreviewVisible = false
                    }
                }
                return super.onOptionsItemSelected(item)
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }
}
