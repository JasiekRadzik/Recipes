package com.example.radzik.recipes.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * Created by Radzik on 03.11.2017.
 */

class ResizeHeightAnimation(private val mView: View, private val mHeight: Int) : Animation() {
    private val mStartHeight: Int = mView.height

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val newHeight = mStartHeight + ((mHeight - mStartHeight) * interpolatedTime).toInt()

        mView.layoutParams.height = newHeight
        mView.requestLayout()
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}
