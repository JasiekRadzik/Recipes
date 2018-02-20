package com.example.radzik.recipes.utils

import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.RelativeLayout

/**
 * Created by Radzik on 02.11.2017.
 */

class ResizeWidthAnimation(private val mView: RelativeLayout?, private val mWidth: Int) : Animation() {
    private var mStartWidth: Int = 0

    init {
        if (mView != null) {
            mStartWidth = mView.width
        }
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val newWidth = mStartWidth + ((mWidth - mStartWidth) * interpolatedTime).toInt()

        if (mView != null) {
            mView.layoutParams.width = newWidth
        }
        mView?.requestLayout()
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
    }

    override fun willChangeBounds(): Boolean {
        return true
    }
}