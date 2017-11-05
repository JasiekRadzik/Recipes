package com.example.radzik.recipes.database;

import com.itextpdf.text.Document;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Radzik on 03.08.2017.
 */

public class Recipe {

    // integers to describe if it's a dessert, main course, starter etc.
    public static final int STARTER = 1;
    public static final int MAIN_COURSE = 2;
    public static final int DESSERT = 3;
    public static final int SOUP = 4;

    private List<EditTextPref> mRecipeList;
    private Map<String, EditTextPref> mMapForEditTextPrefs;
    private String mPhotoId;
    private String mTitle;
    private int mDocumentStyle = 0;
    private int mCourseType = 0;


    public Recipe() {
        mRecipeList = new ArrayList<>();
        mMapForEditTextPrefs = new HashMap<>();

    }

    public void setPhotoId (String photoId) {
        mPhotoId = photoId;
    }
    public String getPhotoId() {
        return mPhotoId;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
    public String getTitle() {
        return mTitle;
    }

    public void setList(ArrayList<EditTextPref> list) {
        mRecipeList = list;
    }
    public ArrayList<EditTextPref> getList() {
        return (ArrayList<EditTextPref>) mRecipeList;
    }

    public void setMapForEditTextPrefs(HashMap<String, EditTextPref> map) {
        mMapForEditTextPrefs = map;
    }
    public HashMap<String, EditTextPref> getMapForEditTextPrefs() {
        return (HashMap<String, EditTextPref>) mMapForEditTextPrefs;
    }

    public void setDocumentStyle(int constantForDocStyle) {
        mDocumentStyle = constantForDocStyle;
    }
    public int getDocumentStyle() {
        return mDocumentStyle;
    }

    public void setCourseType(int courseType) {
        mCourseType = courseType;
    }
    public int getCourseType() {
        return mCourseType;
    }
}
