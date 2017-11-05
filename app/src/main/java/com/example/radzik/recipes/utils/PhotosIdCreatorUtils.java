package com.example.radzik.recipes.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Radzik on 04.09.2017.
 */

public class PhotosIdCreatorUtils {

    private static final Integer LENGTH = 15;
    private static PhotosIdCreatorUtils mInstance;
    private List<String> mIDList = new ArrayList<>();

    private PhotosIdCreatorUtils(){
    }

    public static synchronized PhotosIdCreatorUtils getInstance() {
        if(mInstance == null)
            mInstance = new PhotosIdCreatorUtils();

        return mInstance;
    }

    private String generateId() {
        Random random = new Random();

        // 83 znaki
        char[] values = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '-', '=', '+', '[', ']', '{', '}', '<', '>', '?', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        String out = "";

        for(int i = 0; i < LENGTH; i++) {
            int x = random.nextInt(values.length);
            out = out + values[x];
        }

        return out;
    }

    public String getID() {
        return generateId();
    }

    public int dpToPx(int dp, Activity activity) {
        DisplayMetrics displayMetrics = activity.getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
