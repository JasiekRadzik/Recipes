package com.example.radzik.recipes.database

/**
 * Created by Radzik on 04.09.2017.
 */

class RecipeImage {
    lateinit private var name: String
    lateinit private var url: String
    lateinit private var userID: String

    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    constructor() {}

    constructor(name: String, url: String, userID: String) {
        this.name = name
        this.url = url
        this.userID = userID
    }
}
