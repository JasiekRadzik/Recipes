package com.example.radzik.recipes.fragment

import android.app.Fragment
import android.app.FragmentTransaction
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Gallery
import android.widget.ImageView
import android.widget.RelativeLayout

import com.example.radzik.recipes.R
import com.example.radzik.recipes.adapters.ChooseLayoutImagesAdapter
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection
import com.example.radzik.recipes.database.ConstantsForRecipeDocumentStyles
import com.example.radzik.recipes.database.RecipeManager
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.ScrollBar

import butterknife.BindView
import butterknife.ButterKnife

/**
 * Created by Radzik on 12.10.2017.
 */

class ChooseDocumentLayoutFragment : Fragment() {

    @BindView(R.id.displayLayoutImgView)
    internal var mDisplayLayoutImgView: ImageView? = null

    @BindView(R.id.buttonOpenNextFragment)
    internal var mButtonOpenNextFragment: Button? = null

    internal var mPdfView: PDFView
    internal var mScrollBar: ScrollBar

    internal var mChooseDocStyleRelativeLayout: RelativeLayout
    internal var mShowPreviewRelativeLayout: RelativeLayout

    internal var mIsShowPreviewVisible = false

    internal var mGallery: Gallery

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        val view = inflater.inflate(R.layout.fragment_choose_doc_layout, container, false)
        ButterKnife.bind(this, view)

        setHasOptionsMenu(true)

        mChooseDocStyleRelativeLayout = view.findViewById(R.id.chooseDocStyleRelativeLayout) as RelativeLayout
        mShowPreviewRelativeLayout = view.findViewById(R.id.showPreviewRelativeLayout) as RelativeLayout
        mPdfView = view.findViewById(R.id.pdfView) as PDFView
        mScrollBar = view.findViewById(R.id.pdfScrollBar) as ScrollBar
        mPdfView.setScrollBar(mScrollBar)

        mShowPreviewRelativeLayout.visibility = View.GONE
        mChooseDocStyleRelativeLayout.visibility = View.VISIBLE

        // set currently opened fragment as CHOOSE DOC LAYOUT FRAGMENT
        RecipeManager.instance.setCurrentFragment(ConstantsForFragmentsSelection.CHOOSE_DOCUMENT_LAYOUT_FRAGMENT)

        val gallery = view.findViewById(R.id.chooseDocLayoutFromList) as Gallery
        mGallery = gallery

        gallery.setSpacing(3)

        val mChooseLayoutImagesAdapter = ChooseLayoutImagesAdapter(context)
        gallery.adapter = mChooseLayoutImagesAdapter

        // checks whether recipe has a layout chosen
        if (RecipeManager.instance.currentOrCreateNewRecipe.documentStyle != 0) { // layout already chosen
            gallery.setSelection(RecipeManager.instance.currentOrCreateNewRecipe.documentStyle)
            mDisplayLayoutImgView!!.setImageResource(mChooseLayoutImagesAdapter.mImageIds[RecipeManager.instance.currentOrCreateNewRecipe.documentStyle])
        } else { // layout NOT chosen before
            gallery.setSelection(0)
            mDisplayLayoutImgView!!.setImageResource(mChooseLayoutImagesAdapter.mImageIds[0])
        }

        mDisplayLayoutImgView!!.setOnClickListener {
            if (!mIsShowPreviewVisible) { // opens document preview
                mIsShowPreviewVisible = true
                mChooseDocStyleRelativeLayout.visibility = View.GONE
                mShowPreviewRelativeLayout.visibility = View.VISIBLE

                when (gallery.selectedItemPosition) {
                    0 -> mPdfView.fromAsset("light_bronze_preview.pdf").load()
                    1 -> mPdfView.fromAsset("grey_orange_preview.pdf").load()
                    2 -> mPdfView.fromAsset("blue_red_preview.pdf").load()
                    3 -> mPdfView.fromAsset("purple_preview.pdf").load()
                }

            } else if (mIsShowPreviewVisible) { // closes document preview
                mIsShowPreviewVisible = false
                mChooseDocStyleRelativeLayout.visibility = View.VISIBLE
                mShowPreviewRelativeLayout.visibility = View.GONE
            }
        }


        gallery.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            mDisplayLayoutImgView!!.setImageResource(mChooseLayoutImagesAdapter.mImageIds[position])

            if (position == ConstantsForRecipeDocumentStyles.RECIPE_LIGHT_BRONZE_INGREDIENTS_LEFT) {
                RecipeManager.instance.currentOrCreateNewRecipe.documentStyle = position
            } else if (position == ConstantsForRecipeDocumentStyles.RECIPE_GREY_ORANGE_INGREDIENTS_LEFT) {
                RecipeManager.instance.currentOrCreateNewRecipe.documentStyle = position
            } else if (position == ConstantsForRecipeDocumentStyles.RECIPE_BLUE_RED_INGREDIENTS_LEFT) {
                RecipeManager.instance.currentOrCreateNewRecipe.documentStyle = position
            } else if (position == ConstantsForRecipeDocumentStyles.RECIPE_PURPLE_INGREDIENTS_LEFT) {
                RecipeManager.instance.currentOrCreateNewRecipe.documentStyle = position
            }
        }

        mButtonOpenNextFragment!!.setOnClickListener { openNextFragment(AddRecipeTitleFragment()) }

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
                    if (!mIsShowPreviewVisible) { // opens document preview
                        mIsShowPreviewVisible = true
                        mChooseDocStyleRelativeLayout.visibility = View.GONE
                        mShowPreviewRelativeLayout.visibility = View.VISIBLE

                        when (mGallery.selectedItemPosition) {
                            0 -> mPdfView.fromAsset("light_bronze_preview.pdf").load()
                            1 -> mPdfView.fromAsset("grey_orange_preview.pdf").load()
                            2 -> mPdfView.fromAsset("blue_red_preview.pdf").load()
                            3 -> mPdfView.fromAsset("purple_preview.pdf").load()
                        }
                    } else if (mIsShowPreviewVisible) { // closes document preview
                        mIsShowPreviewVisible = false
                        mChooseDocStyleRelativeLayout.visibility = View.VISIBLE
                        mShowPreviewRelativeLayout.visibility = View.GONE
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
