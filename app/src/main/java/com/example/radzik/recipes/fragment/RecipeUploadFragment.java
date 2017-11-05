package com.example.radzik.recipes.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.radzik.recipes.R;
import com.example.radzik.recipes.activity.MainActivity;
import com.example.radzik.recipes.database.RecipeManager;
import com.example.radzik.recipes.database.firebase.GeneralDataManager;

/**
 * Created by Radzik on 16.10.2017.
 */

public class RecipeUploadFragment extends Fragment {

    private Handler mHandler;

    ProgressBar mRecipeProgressBar;
    ImageView mImageView;
    TextView mTextView;

    GeneralDataManager mDataManager;
    RecipeManager mRecipeManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_recipe_being_uploaded, container);

        mRecipeProgressBar = (ProgressBar) view.findViewById(R.id.recipeProgressBar);
        mImageView = (ImageView) view.findViewById(R.id.displayDocPreviewInRecipeUploadFragmentImageView);
        mTextView = (TextView) view.findViewById(R.id.textViewRecipeUploaded);
        mHandler = new Handler();

        startProgressBar();

        /*
        // set content of an ImageView
        final ChooseLayoutImagesAdapter mChooseLayoutImagesAdapter = new ChooseLayoutImagesAdapter(getContext());
        mImageView.setImageResource(mChooseLayoutImagesAdapter.mImageIds[RecipeManager.getInstance().getCurrentOrCreateNewRecipe().getDocumentStyle()]);

        // after all the recipe upload etc. etc.
        mDataManager = GeneralDataManager.getInstance();

        mDataManager.uploadAll(getActivity());

        mDataManager.setOnRecipeUploadedListener(new GeneralDataManager.OnRecipeUploadedListener() {
            @Override
            public void onRecipeUploaded(Recipe recipe) {
                startProgressBar();
            }
        });
 */

        return view;
    }

    public void startProgressBar() {
        new Thread(new Task()).start();
    }

class Task implements Runnable {
    @Override
    public void run() {
        for(int i = 0; i < 100; i++) {
            final int value = i;
            try {
                Thread.sleep(20);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mRecipeProgressBar.setProgress(value);
                }
            });

            if(value == 99) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextView.setVisibility(View.VISIBLE);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                            }
                        }, 2000);
                    }
                });

            }
        }
    }
}

}
