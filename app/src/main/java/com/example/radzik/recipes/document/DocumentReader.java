package com.example.radzik.recipes.document;

/**
 * Created by Radzik on 09.09.2017.
 */

public class DocumentReader {

    private static DocumentReader mInstance = null;

    public static synchronized DocumentReader getInstance() {
        if(mInstance == null) {
            mInstance = new DocumentReader();
        }

        return mInstance;
    }

    private DocumentReader() {

    }


}
