package com.example.radzik.recipes.database

/**
 * Created by Radzik on 27.08.2017.
 */

class User {

    var id: String? = null
    var name: String? = null
    var phoneNumber: String? = null
    var email: String? = null
    var password: String? = null


    constructor() {}

    constructor(id: String, name: String?, phoneNumber: String?, email: String?, password: String?) {
        this.id = id
        this.name = name
        this.phoneNumber = phoneNumber
        this.email = email
        this.password = password
    }
}


