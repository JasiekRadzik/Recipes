package com.example.radzik.recipes.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.radzik.recipes.R;
import com.example.radzik.recipes.adapters.ChooseLayoutImagesAdapter;
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection;
import com.example.radzik.recipes.database.ConstantsForRecipeDocumentStyles;
import com.example.radzik.recipes.database.RecipeManager;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.ScrollBar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Radzik on 12.10.2017.
 */

public class ChooseDocumentLayoutFragment extends Fragment {

    @BindView(R.id.displayLayoutImgView)
    ImageView mDisplayLayoutImgView;

    @BindView(R.id.buttonOpenNextFragment)
    Button mButtonOpenNextFragment;

    PDFView mPdfView;
    ScrollBar mScrollBar;

    RelativeLayout mChooseDocStyleRelativeLayout;
    RelativeLayout mShowPreviewRelativeLayout;

    boolean mIsShowPreviewVisible = false;

    Gallery mGallery;

    public ChooseDocumentLayoutFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_doc_layout, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        mChooseDocStyleRelativeLayout = (RelativeLayout) view.findViewById(R.id.chooseDocStyleRelativeLayout);
        mShowPreviewRelativeLayout = (RelativeLayout) view.findViewById(R.id.showPreviewRelativeLayout);
        mPdfView = (PDFView) view.findViewById(R.id.pdfView);
        mScrollBar = (ScrollBar) view.findViewById(R.id.pdfScrollBar);
        mPdfView.setScrollBar(mScrollBar);

        mShowPreviewRelativeLayout.setVisibility(View.GONE);
        mChooseDocStyleRelativeLayout.setVisibility(View.VISIBLE);

        // set currently opened fragment as CHOOSE DOC LAYOUT FRAGMENT
        RecipeManager.getInstance().setCurrentFragment(ConstantsForFragmentsSelection.CHOOSE_DOCUMENT_LAYOUT_FRAGMENT);

        final Gallery gallery = (Gallery) view.findViewById(R.id.chooseDocLayoutFromList);
        mGallery = gallery;

        gallery.setSpacing(3);

        final ChooseLayoutImagesAdapter mChooseLayoutImagesAdapter = new ChooseLayoutImagesAdapter(getContext());
        gallery.setAdapter(mChooseLayoutImagesAdapter);

        // checks whether recipe has a layout chosen
        if (RecipeManager.getInstance().getCurrentOrCreateNewRecipe().getDocumentStyle() != 0) { // layout already chosen
            gallery.setSelection(RecipeManager.getInstance().getCurrentOrCreateNewRecipe().getDocumentStyle());
            mDisplayLayoutImgView.setImageResource(mChooseLayoutImagesAdapter.mImageIds[RecipeManager.getInstance().getCurrentOrCreateNewRecipe().getDocumentStyle()]);
        } else { // layout NOT chosen before
            gallery.setSelection(0);
            mDisplayLayoutImgView.setImageResource(mChooseLayoutImagesAdapter.mImageIds[0]);
        }

        mDisplayLayoutImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsShowPreviewVisible) { // opens document preview
                    mIsShowPreviewVisible = true;
                    mChooseDocStyleRelativeLayout.setVisibility(View.GONE);
                    mShowPreviewRelativeLayout.setVisibility(View.VISIBLE);

                    switch (gallery.getSelectedItemPosition()) {
                        case 0:
                            mPdfView.fromAsset("light_bronze_preview.pdf").load();
                            break;
                        case 1:
                            mPdfView.fromAsset("grey_orange_preview.pdf").load();
                            break;
                        case 2:
                            mPdfView.fromAsset("blue_red_preview.pdf").load();
                            break;
                        case 3:
                            mPdfView.fromAsset("purple_preview.pdf").load();
                            break;
                    }

                } else if (mIsShowPreviewVisible) { // closes document preview
                    mIsShowPreviewVisible = false;
                    mChooseDocStyleRelativeLayout.setVisibility(View.VISIBLE);
                    mShowPreviewRelativeLayout.setVisibility(View.GONE);
                }
            }
        });


        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDisplayLayoutImgView.setImageResource(mChooseLayoutImagesAdapter.mImageIds[position]);

                if (position == ConstantsForRecipeDocumentStyles.RECIPE_LIGHT_BRONZE_INGREDIENTS_LEFT) {
                    RecipeManager.getInstance().getCurrentOrCreateNewRecipe().setDocumentStyle(position);
                } else if (position == ConstantsForRecipeDocumentStyles.RECIPE_GREY_ORANGE_INGREDIENTS_LEFT) {
                    RecipeManager.getInstance().getCurrentOrCreateNewRecipe().setDocumentStyle(position);
                } else if (position == ConstantsForRecipeDocumentStyles.RECIPE_BLUE_RED_INGREDIENTS_LEFT) {
                    RecipeManager.getInstance().getCurrentOrCreateNewRecipe().setDocumentStyle(position);
                } else if (position == ConstantsForRecipeDocumentStyles.RECIPE_PURPLE_INGREDIENTS_LEFT) {
                    RecipeManager.getInstance().getCurrentOrCreateNewRecipe().setDocumentStyle(position);
                }
            }
        });

        mButtonOpenNextFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextFragment(new AddRecipeTitleFragment());
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_doc_preview, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.doc_preview_icon: {
                if (!mIsShowPreviewVisible) { // opens document preview
                    mIsShowPreviewVisible = true;
                    mChooseDocStyleRelativeLayout.setVisibility(View.GONE);
                    mShowPreviewRelativeLayout.setVisibility(View.VISIBLE);

                    switch (mGallery.getSelectedItemPosition()) {
                        case 0:
                            mPdfView.fromAsset("light_bronze_preview.pdf").load();
                            break;
                        case 1:
                            mPdfView.fromAsset("grey_orange_preview.pdf").load();
                            break;
                        case 2:
                            mPdfView.fromAsset("blue_red_preview.pdf").load();
                            break;
                        case 3:
                            mPdfView.fromAsset("purple_preview.pdf").load();
                            break;
                    }
                } else if (mIsShowPreviewVisible) { // closes document preview
                    mIsShowPreviewVisible = false;
                    mChooseDocStyleRelativeLayout.setVisibility(View.VISIBLE);
                    mShowPreviewRelativeLayout.setVisibility(View.GONE);
                }
            }
            default:
                return super.onOptionsItemSelected(item);
        }


    }

    private void openNextFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
