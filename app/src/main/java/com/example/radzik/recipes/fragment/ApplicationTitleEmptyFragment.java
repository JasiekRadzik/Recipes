package com.example.radzik.recipes.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.radzik.recipes.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Radzik on 31.10.2017.
 */

public class ApplicationTitleEmptyFragment extends Fragment {

    @BindView(R.id.textViewSmallHint)
    TextView mSmallHintTxtView;

    @BindView(R.id.textViewWelcomeRecipe)
    TextView mWelcomeTxtView;

    @BindView(R.id.relLayoutEmptyFragment)
    RelativeLayout mLayoutEmptyFragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_application_title_empty, container, false);
        ButterKnife.bind(this, view);

        mSmallHintTxtView.setTextColor(Color.argb(75, 155, 155, 155));
        mLayoutEmptyFragment.setBackgroundColor(Color.argb(100, 246, 246, 246));
        mWelcomeTxtView.setTextColor(Color.argb(75, 155, 155, 155));

        return view;
    }
}
