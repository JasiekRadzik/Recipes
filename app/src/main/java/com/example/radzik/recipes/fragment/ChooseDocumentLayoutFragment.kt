package com.example.radzik.recipes.fragment

import android.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView

import com.example.radzik.recipes.R
import com.example.radzik.recipes.adapters.ChooseLayoutImagesAdapter
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection
import com.example.radzik.recipes.database.ConstantsForRecipeDocumentStyles
import com.example.radzik.recipes.database.RecipeManager
import kotlinx.android.synthetic.main.fragment_choose_doc_layout.*

/**
 * Created by Radzik on 12.10.2017.
 */

class ChooseDocumentLayoutFragment : Fragment() {

    internal var mIsShowPreviewVisible = false

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        return inflater?.inflate(R.layout.fragment_choose_doc_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfView.setScrollBar(pdfScrollBar)

        showPreviewRelativeLayout.visibility = View.GONE
        chooseDocStyleRelativeLayout.visibility = View.VISIBLE

        // set currently opened fragment as CHOOSE DOC LAYOUT FRAGMENT
        RecipeManager.instance.setCurrentFragment(ConstantsForFragmentsSelection.CHOOSE_DOCUMENT_LAYOUT_FRAGMENT)

        chooseDocLayoutFromList.setSpacing(3)

        val mChooseLayoutImagesAdapter = ChooseLayoutImagesAdapter(context)
        chooseDocLayoutFromList.adapter = mChooseLayoutImagesAdapter

        // checks whether recipe has a layout chosen
        if (RecipeManager.instance.currentOrNewRecipe.documentStyle != 0) { // layout already chosen
            chooseDocLayoutFromList.setSelection(RecipeManager.instance.currentOrNewRecipe.documentStyle)
            displayLayoutImgView.setImageResource(mChooseLayoutImagesAdapter.mImageIds[RecipeManager.instance.currentOrNewRecipe.documentStyle])
        } else { // layout NOT chosen before
            chooseDocLayoutFromList.setSelection(0)
            displayLayoutImgView.setImageResource(mChooseLayoutImagesAdapter.mImageIds[0])
        }

        displayLayoutImgView.setOnClickListener {
            if (!mIsShowPreviewVisible) { // opens document preview
                mIsShowPreviewVisible = true
                chooseDocStyleRelativeLayout.visibility = View.GONE
                showPreviewRelativeLayout.visibility = View.VISIBLE

                when (chooseDocLayoutFromList.selectedItemPosition) {
                    0 -> pdfView.fromAsset("light_bronze_preview.pdf").load()
                    1 -> pdfView.fromAsset("grey_orange_preview.pdf").load()
                    2 -> pdfView.fromAsset("blue_red_preview.pdf").load()
                    3 -> pdfView.fromAsset("purple_preview.pdf").load()
                }

            } else if (mIsShowPreviewVisible) { // closes document preview
                mIsShowPreviewVisible = false
                chooseDocStyleRelativeLayout.visibility = View.VISIBLE
                showPreviewRelativeLayout.visibility = View.GONE
            }
        }


        chooseDocLayoutFromList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            displayLayoutImgView.setImageResource(mChooseLayoutImagesAdapter.mImageIds[position])

            if (position == ConstantsForRecipeDocumentStyles.RECIPE_LIGHT_BRONZE_INGREDIENTS_LEFT) {
                RecipeManager.instance.currentOrNewRecipe.documentStyle = position
            } else if (position == ConstantsForRecipeDocumentStyles.RECIPE_GREY_ORANGE_INGREDIENTS_LEFT) {
                RecipeManager.instance.currentOrNewRecipe.documentStyle = position
            } else if (position == ConstantsForRecipeDocumentStyles.RECIPE_BLUE_RED_INGREDIENTS_LEFT) {
                RecipeManager.instance.currentOrNewRecipe.documentStyle = position
            } else if (position == ConstantsForRecipeDocumentStyles.RECIPE_PURPLE_INGREDIENTS_LEFT) {
                RecipeManager.instance.currentOrNewRecipe.documentStyle = position
            }
        }

        buttonOpenNextFragment.setOnClickListener { openNextFragment(AddRecipeTitleFragment()) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_doc_preview, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.doc_preview_icon -> {
                run {
                    if (!mIsShowPreviewVisible) { // opens document preview
                        mIsShowPreviewVisible = true
                        chooseDocStyleRelativeLayout.visibility = View.GONE
                        showPreviewRelativeLayout.visibility = View.VISIBLE

                        when (chooseDocLayoutFromList.selectedItemPosition) {
                            0 -> pdfView.fromAsset("light_bronze_preview.pdf").load()
                            1 -> pdfView.fromAsset("grey_orange_preview.pdf").load()
                            2 -> pdfView.fromAsset("blue_red_preview.pdf").load()
                            3 -> pdfView.fromAsset("purple_preview.pdf").load()
                        }
                    } else if (mIsShowPreviewVisible) { // closes document preview
                        mIsShowPreviewVisible = false
                        chooseDocStyleRelativeLayout.visibility = View.VISIBLE
                        showPreviewRelativeLayout.visibility = View.GONE
                    }
                }
                return super.onOptionsItemSelected(item)
            }
            else -> return super.onOptionsItemSelected(item)
        }


    }

    private fun openNextFragment(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
