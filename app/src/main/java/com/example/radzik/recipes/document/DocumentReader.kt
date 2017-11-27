package com.example.radzik.recipes.document

/**
 * Created by Radzik on 09.09.2017.
 */

object DocumentReader {

    private var mInstance: DocumentReader? = null

    val instance: DocumentReader
        @Synchronized get() {
            if (mInstance == null) {
                mInstance = DocumentReader()
            }

            return mInstance
        }


}
