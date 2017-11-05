package com.example.radzik.recipes.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.radzik.recipes.R;
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection;
import com.example.radzik.recipes.database.Recipe;
import com.example.radzik.recipes.database.RecipeManager;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.ScrollBar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Radzik on 01.11.2017.
 */

public class RecipeSummaryFragment extends Fragment {

    @BindView(R.id.visibleRecipeSummaryPart)
    RelativeLayout mVisibleRecipeSummaryPart;

    @BindView(R.id.hiddenPartShowRecipePreview)
    RelativeLayout mHiddenPartShowRecipePreview;

    @BindView(R.id.recipeTitleCheck)
    TextView mRecipeTitleCheckTextView;

    PDFView mPdfView;
    ScrollBar mScrollBar;

    private RecipeManager mManager;

    // variables used for text animation
    private CharSequence mTextToAnimate;
    private int mIndex;
    private long mDelay;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_summary, container, false);
        ButterKnife.bind(this, view);

        mManager = RecipeManager.getInstance();
        mManager.setCurrentFragment(ConstantsForFragmentsSelection.SUMMARY_FRAGMENT);
        Recipe recipe = mManager.getCurrentOrCreateNewRecipe();
        String recipeTitle = recipe.getTitle();
        String cookBookTitle = mManager.getCurrentCookBookTitle();
        boolean isPhotoSelected = false;

        mPdfView = (PDFView) view.findViewById(R.id.pdfViewDisplay);
        mScrollBar = (ScrollBar) view.findViewById(R.id.pdfViewDisplayScrollBar);
        mPdfView.setScrollBar(mScrollBar);

        mVisibleRecipeSummaryPart.setVisibility(View.VISIBLE);
        mHiddenPartShowRecipePreview.setVisibility(View.GONE);

        mRecipeTitleCheckTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setCharacterDelay(50);
        animateText("Sample String");



        return view;
    }

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            mRecipeTitleCheckTextView.setText(mTextToAnimate.subSequence(0, mIndex++));
            if(mIndex <= mTextToAnimate.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            }
        }
    };

    public void animateText(CharSequence text) {
        mTextToAnimate = text;
        mIndex = 0;

        mRecipeTitleCheckTextView.setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
}
