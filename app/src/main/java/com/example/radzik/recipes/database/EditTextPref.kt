package com.example.radzik.recipes.database

/**
 * Created by Radzik on 11.08.2017.
 */

class EditTextPref {
    var text: String? = null
    var recipePartName: String? = null
    var id: String? = null
    var recipePartType: Int = 0
    var ingredientAmount = ""

    constructor() {}

    constructor(text: String) {
        this.text = text
    }
}
