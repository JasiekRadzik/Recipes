package com.example.radzik.recipes.fragment;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.radzik.recipes.R;
import com.example.radzik.recipes.database.ConstantsForRecipePartTypes;
import com.example.radzik.recipes.database.CookBook;
import com.example.radzik.recipes.database.EditTextPref;
import com.example.radzik.recipes.database.EditTextPrefManager;
import com.example.radzik.recipes.database.Recipe;
import com.example.radzik.recipes.database.RecipeManager;
import com.example.radzik.recipes.database.firebase.GeneralDataManager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Radzik on 19.09.2017.
 */

public class TestFragment extends Fragment {

    @BindView(R.id.editTextTestTitle)
    EditText mTitleEditText;

    @BindView(R.id.cookBookEditText)
    EditText mCookBookEditText;

    @BindView(R.id.buttonTestLoadRecipe)
    Button mLoadTestButton;

    @BindView(R.id.buttonTestLoadCookbookAndRecipe)
    Button mLoadTestCookBookAndRecipe;

    @BindView(R.id.buttonTestLoadCookbook)
    Button mLoadTestCookbook;

    @BindView(R.id.buttonTestRemoveRecipeFromCookbook)
    Button mRemoveTestRecipeFromCookBook;

    RecipeManager mManager;

    String mCookbookKey = "";
    String mRecipeKey = "";

    Recipe mRecipe;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_test, container, false);

        ButterKnife.bind(this, view);

        mManager = RecipeManager.getInstance();
        mRecipe = mManager.getCurrentOrCreateNewRecipe();
        GeneralDataManager.getInstance().attachListeners(GeneralDataManager.RECIPE_UPDATES_LISTENER);

        EditTextPref editTextPref = new EditTextPref();
        editTextPref.setText("Pierwszy upload TEST");
        editTextPref.setID("sfwe342dasdaw");
        editTextPref.setRecipePartType(ConstantsForRecipePartTypes.HOW_TO_COOK);
        EditTextPrefManager.getInstance().translateTypeToRecipePartName(editTextPref, getActivity());

        EditTextPref x;
        x = editTextPref;
        mManager.getCurrentOrCreateNewRecipe().getList().add(x);

        final CookBook cookBook = new CookBook();
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
        mLoadTestCookbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCookbookKey = GeneralDataManager.getInstance().uploadCookBook(getActivity(), cookBook);
            }
        });

        mRemoveTestRecipeFromCookBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneralDataManager.getInstance().removeRecipeFromCookBook(mRecipe, cookBook);
            }
        });

        mCookBookEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cookBook.setTitle(mCookBookEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mRecipe.setTitle(mTitleEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return view;
    }
}
