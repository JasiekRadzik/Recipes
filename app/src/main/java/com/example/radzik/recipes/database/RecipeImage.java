package com.example.radzik.recipes.database;

/**
 * Created by Radzik on 04.09.2017.
 */

public class RecipeImage {
    public String mName;
    public String mUrl;
    public String mUserID;

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public RecipeImage() {
    }

    public RecipeImage(String name, String url, String userID) {
        mName = name;
        mUrl = url;
        mUserID = userID;
    }

    public void setName(String name) {
        mName = name;
    }
    public String getName() {
        return mName;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
    public String getUrl() {
        return mUrl;
    }

    public void setUserID(String userID) {
        mUserID = userID;
    }
    public String getUserID() {
        return mUserID;
    }
}
