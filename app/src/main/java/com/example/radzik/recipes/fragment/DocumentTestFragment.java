package com.example.radzik.recipes.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.radzik.recipes.R;
import com.example.radzik.recipes.database.ConstantsForRecipeDocumentStyles;
import com.example.radzik.recipes.database.ConstantsForRecipePartTypes;
import com.example.radzik.recipes.database.EditTextPref;
import com.example.radzik.recipes.database.EditTextPrefManager;
import com.example.radzik.recipes.database.RecipeManager;
import com.example.radzik.recipes.document.DocumentCreator;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.ScrollBar;

/**
 * Created by Radzik on 17.10.2017.
 */

public class DocumentTestFragment extends Fragment {

    EditTextPref mEditTextPref = null;
    RecipeManager mRecipeManager;
    DocumentCreator mDocCreator;

    RelativeLayout mMainRelativeView;
    RelativeLayout mPdfRelativeView;

    Button mStartSim;

    PDFView mPdfView;
    ScrollBar mScrollBar;

    private boolean mIsShowPreviewVisible = false;

    public DocumentTestFragment() {
       setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_document_test, container, false);
        setHasOptionsMenu(true);

        mMainRelativeView = (RelativeLayout) view.findViewById(R.id.chooseDocStyleRelativeLayoutTEST);
        mPdfRelativeView = (RelativeLayout) view.findViewById(R.id.showPreviewRelativeLayoutTEST);

        mStartSim = (Button) view.findViewById(R.id.buttonStartSimTEST);

        mPdfView = (PDFView) view.findViewById(R.id.pdfViewTEST);
        mScrollBar = (ScrollBar) view.findViewById(R.id.pdfScrollBarTEST);
        mPdfView.setScrollBar(mScrollBar);

        mMainRelativeView.setVisibility(View.VISIBLE);
        mPdfRelativeView.setVisibility(View.GONE);

        mRecipeManager = RecipeManager.getInstance();
        mRecipeManager.createNewRecipe();
        mRecipeManager.getCurrentOrCreateNewRecipe().setTitle("hehe");
        mRecipeManager.getCurrentOrCreateNewRecipe().setDocumentStyle(ConstantsForRecipeDocumentStyles.RECIPE_LIGHT_BRONZE_INGREDIENTS_LEFT);

        mDocCreator = DocumentCreator.getInstance();

        String path = "/storage/emulated/0/Pictures/cheeseCake.jpeg";
        mRecipeManager.setPicturePath(path);

        mStartSim.setEnabled(true);

        mStartSim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < 20; i++) {
                    mEditTextPref = new EditTextPref();
                    mEditTextPref.setRecipePartType(ConstantsForRecipePartTypes.HOW_TO_COOK);
                    EditTextPrefManager.getInstance().translateTypeToRecipePartName(mEditTextPref, getActivity());
                    mEditTextPref.setText("Line: " + i + " with text: " + "Hello line " + i);

                    EditTextPref x = mEditTextPref;
                    mRecipeManager.getCurrentOrCreateNewRecipe().getList().add(x);
                }

                for(int i = 0; i < mRecipeManager.getCurrentOrCreateNewRecipe().getList().size() - 1; i++) {
                    Log.e("List part: ", i + ": " + mRecipeManager.getCurrentOrCreateNewRecipe().getList().get(i).getText());
                }

                mDocCreator.prepareEnviromentAndwriteDocument(mRecipeManager.getCurrentOrCreateNewRecipe(), getActivity());
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
                if(!mIsShowPreviewVisible) {
                    // ----- TEST
                    mPdfView.fromAsset("light_brown_full_preview.pdf").load();
                    mMainRelativeView.setVisibility(View.GONE);
                    mPdfRelativeView.setVisibility(View.VISIBLE);
                    mIsShowPreviewVisible = true;

                    if(mDocCreator.getRecipeFile() == null) {
                        Toast.makeText(getContext(), "Problem with showing preview", Toast.LENGTH_SHORT).show();
                    }
                    else if(mDocCreator.getRecipeFile() != null) {
                        mPdfView.fromFile(mDocCreator.getRecipeFile()).load();
                        mMainRelativeView.setVisibility(View.GONE);
                        mPdfRelativeView.setVisibility(View.VISIBLE);
                        mIsShowPreviewVisible = true;
                    }
                }
                else if(mIsShowPreviewVisible) {
                    mMainRelativeView.setVisibility(View.VISIBLE);
                    mPdfRelativeView.setVisibility(View.GONE);
                    mIsShowPreviewVisible = false;
                }
            }
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
