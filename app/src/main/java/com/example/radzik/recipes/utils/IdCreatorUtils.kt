package com.example.radzik.recipes.utils

import java.util.ArrayList
import java.util.Random

/**
 * Created by Radzik on 21.08.2017.
 */

class IdCreatorUtils private constructor() {
    private val mIDList = ArrayList<String>()

    val id: String
        get() {
            var isOriginalID: Boolean? = false
            var id = ""

            while (isOriginalID == false) {
                id = generateId()
                if (!mIDList.contains(id))
                    isOriginalID = true

            }
            return id
        }

    private fun generateId(): String {
        val random = Random()

        // 83 znaki
        val values = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '-', '=', '+', '[', ']', '{', '}', '<', '>', '?', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        var out = ""

        for (i in 0 until LENGTH) {
            val x = random.nextInt(values.size)
            out += values[x]
        }

        return out
    }

    companion object {

        private var mInstance: IdCreatorUtils? = null
        private val LENGTH = 10

        val instance: IdCreatorUtils
            @Synchronized get() {
                if (mInstance == null)
                    mInstance = IdCreatorUtils()

                return mInstance!!
            }
    }
}
