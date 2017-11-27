package com.example.radzik.recipes.utils

import android.app.Activity
import android.content.Context
import android.util.TypedValue

/**
 * Created by Radzik on 06.11.2017.
 */

class PixelsToDensityConverter {

    fun convertToInt(pixels: Int, activity: Activity): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels.toFloat(), activity.resources.displayMetrics).toInt()
    }

    companion object {

        private var mInstance: PixelsToDensityConverter? = null

        val instance: PixelsToDensityConverter
            @Synchronized get() {
                if (mInstance == null)
                    mInstance = PixelsToDensityConverter()

                return mInstance as PixelsToDensityConverter
            }
    }
}
