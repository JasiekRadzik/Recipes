package com.example.radzik.recipes.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * Created by Radzik on 02.11.2017.
 */

class ResizeWidthAnimation(private val mView: View, private val mWidth: Int) : Animation() {
    private val mStartWidth: Int

    init {
        mStartWidth = mView.width
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val newWidth = mStartWidth + ((mWidth - mStartWidth) * interpolatedTime).toInt()

        mView.layoutParams.width = newWidth
        mView.requestLayout()
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}