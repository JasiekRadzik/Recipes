package com.example.radzik.recipes.fragment

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView

import com.example.radzik.recipes.R
import com.example.radzik.recipes.activity.MainActivity
import com.example.radzik.recipes.database.RecipeManager
import com.example.radzik.recipes.database.firebase.GeneralDataManager

/**
 * Created by Radzik on 16.10.2017.
 */

class RecipeUploadFragment : Fragment() {

    private var mHandler: Handler? = null

    private lateinit var mRecipeProgressBar: ProgressBar
    private lateinit var mImageView: ImageView
    private lateinit var mTextView: TextView

    internal var mDataManager: GeneralDataManager? = null
    internal var mRecipeManager: RecipeManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {

        val view = inflater.inflate(R.layout.fragment_recipe_being_uploaded, container)

        mRecipeProgressBar = view.findViewById(R.id.recipeProgressBar) as ProgressBar
        mImageView = view.findViewById(R.id.displayDocPreviewInRecipeUploadFragmentImageView) as ImageView
        mTextView = view.findViewById(R.id.textViewRecipeUploaded) as TextView
        mHandler = Handler()

        startProgressBar()

        /*
        // set content of an ImageView
        final ChooseLayoutImagesAdapter mChooseLayoutImagesAdapter = new ChooseLayoutImagesAdapter(getContext());
        mImageView.setImageResource(mChooseLayoutImagesAdapter.mImageIds[RecipeManager.getInstance().getCurrentOrNewRecipe().getDocumentStyle()]);

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

        return view
    }

    fun startProgressBar() {
        Thread(Task()).start()
    }

    internal inner class Task : Runnable {
        override fun run() {
            for (i in 0..99) {
                try {
                    Thread.sleep(20)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                mHandler!!.post { mRecipeProgressBar.progress = i }

                if (i == 99) {
                    activity.runOnUiThread {
                        mTextView.visibility = View.VISIBLE
                        val handler = Handler()
                        handler.postDelayed({ }, 2000)
                    }

                }
            }
        }
    }

}
