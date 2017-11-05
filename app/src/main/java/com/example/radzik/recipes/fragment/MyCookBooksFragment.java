package com.example.radzik.recipes.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.radzik.recipes.database.CookBook;
import com.example.radzik.recipes.database.Recipe;
import com.example.radzik.recipes.database.firebase.GeneralDataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Radzik on 11.09.2017.
 */

// todo: Recycler View layout to display list

public class MyCookBooksFragment extends Fragment {

    private Map<CookBook, ArrayList<Recipe>> mCookBooksMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        // sets listener for MAP
        GeneralDataManager.OnRecipesUpdateListener listener = new GeneralDataManager.OnRecipesUpdateListener() {
            public void onMapChanged(HashMap<CookBook, ArrayList<Recipe>> map) {
                mCookBooksMap = map;
            }
        };

        GeneralDataManager.getInstance().setOnRecipesUpdateListener(listener);

        return super.onCreateView(inflater, container, savedInstanceState);

    }
}
