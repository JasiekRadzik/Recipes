package com.example.radzik.recipes.utils

import android.app.Activity
import android.util.DisplayMetrics

import java.util.ArrayList
import java.util.Random

/**
 * Created by Radzik on 04.09.2017.
 */

class PhotosIdCreatorUtils private constructor() {

    val id: String
        get() = generateId()

    private fun generateId(): String {
        val random = Random()
        val values = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '-', '=', '+', '[', ']', '{', '}', '<', '>', '?', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        var out = ""

        for (i in 0 until LENGTH) {
            val x = random.nextInt(values.size)
            out += values[x]
        }

        return out
    }

    fun dpToPx(dp: Int, activity: Activity): Int {
        val displayMetrics = activity.applicationContext.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    companion object {
        private val LENGTH = 15
        private var mInstance: PhotosIdCreatorUtils? = null

        val instance: PhotosIdCreatorUtils
            @Synchronized get() {
                if (mInstance == null)
                    mInstance = PhotosIdCreatorUtils()

                return mInstance as PhotosIdCreatorUtils
            }
    }
}
