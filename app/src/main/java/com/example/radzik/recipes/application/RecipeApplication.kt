package com.example.radzik.recipes.application

import android.app.Application
import android.support.multidex.MultiDexApplication

import com.airnauts.toolkit.data.DataManager
import com.example.radzik.recipes.R
import com.example.radzik.recipes.database.firebase.GeneralDataManager
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

import uk.co.chrisjenx.calligraphy.CalligraphyConfig

/**
 * Created by Radzik on 26.07.2017.
 */

class RecipeApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        CalligraphyConfig.initDefault(CalligraphyConfig.Builder().setDefaultFontPath(getString(R.string.light)).setFontAttrId(R.attr.fontPath).build())

        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)

        DataManager.getInstance().initialize(applicationContext)
    }
}
