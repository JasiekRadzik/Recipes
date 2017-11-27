package com.example.radzik.recipes.database.firebase

import android.app.Activity
import android.content.ContentResolver
import android.content.DialogInterface
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast

import com.example.radzik.recipes.activity.MainActivity
import com.example.radzik.recipes.database.CookBook
import com.example.radzik.recipes.database.Recipe
import com.example.radzik.recipes.database.RecipeImage
import com.example.radzik.recipes.database.RecipeManager
import com.example.radzik.recipes.utils.PhotosIdCreatorUtils
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

import java.util.ArrayList
import java.util.HashMap


/**
 * Created by Radzik on 04.09.2017.
 */

//1: User should have a possibility to store Recipe without attaching it to any Cookbook
// - In cookbooks/cookbook/ full Recipe object should be stored.

//2: Cookbook upload has to be finished

//3: Recipe to Cookbook upload

//TODO: 4: Delete Recipe from Recipes, which means it will be deleted from all CookBooks

// Remove Recipe from CookBook

//TODO: 5: Delete CookBook

//TODO: 6: Fragment to display arraylist with cookbooks

//TODO: 7: Fragment to display recipes from selected cookbook

//TODO: 8: DocumentReader

//TODO: 9: DocumentCreator

class GeneralDataManager private constructor() {
    private val mStorageReference = FirebaseStorage.getInstance().reference
    private val mDatabasePhotosUploads = FirebaseDatabase.getInstance().getReference(ConstantsForUploads.DATABASE_PATH_PHOTOS_UPLOADS)
    private val mDatabaseRecipesUploads = FirebaseDatabase.getInstance().getReference(ConstantsForUploads.DATABASE_PATH_RECIPES_UPLOADS)
    private val mDatabaseCookBooksUploads = FirebaseDatabase.getInstance().getReference(ConstantsForUploads.DATABASE_PATH_COOKBOOKS_UPLOADS)
    private val mDatabaseUsersUploads = FirebaseDatabase.getInstance().getReference(ConstantsForUploads.DATABASE_PATH_USERS)

    private var mDatabaseRecipesUploadsBelowUser: DatabaseReference? = null
    private val mDatabaseCookBooksKeyUploadsInUserBooks = mDatabaseUsersUploads.child(FirebaseAuth.getInstance().currentUser!!.uid).child("books")

    // info below handles updates of RECIPES database. Handled by recipeUpdatesListener
    private var mCurrentUserRecipesList: MutableList<Recipe>? = null
    private var mCurrentUserRecipeTitleKeyMap: MutableMap<String, String>? = null
    private var mRecipeKey: String? = null

    // info below handles updates of COOKBOOKS database. Handled by cookBookKeysInUserBooksUpdatesListener
    private var mCurrentUserCookBookKeyTitleMap: MutableMap<String, String>? = null
    private var mCookBookKey: String? = null
    private var mCookBookKeyToOverwrite: String? = null

    // info below handles updates of Recipes inside CookBooks. Handled by cookBookDatabaseListener
    private var mRecipesInCookBooksMap: MutableMap<CookBook, ArrayList<Recipe>>? = null

    // mRecipesInCookBooksMap listener
    private var mRecipesUpdateListener: OnRecipesUpdateListener? = null

    // CookBook uploaded listener
    private var mIfCookBookUploadedListener: OnCookBookUploadedListener? = null

    // mRecipeUploadedListener
    private var mRecipeUploadedListener: OnRecipeUploadedListener? = null

    // used to check if there is a recipe with a given title and updates users recipes list
    internal var recipeUpdatesListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            mCurrentUserRecipesList = ArrayList()
            mCurrentUserRecipeTitleKeyMap = HashMap()

            for (snapshot in dataSnapshot.children) {
                val recipe1 = snapshot.getValue(Recipe::class.java)
                mCurrentUserRecipesList!!.add(recipe1)
                mCurrentUserRecipeTitleKeyMap!!.put(recipe1!!.title, snapshot.key)
            }

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    // used to check if there is a cookbook with a given title and updates users cookbooks list
    internal var cookBookKeysInUserBooksUpdatesListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            mCurrentUserCookBookKeyTitleMap = HashMap()

            for (snapshot in dataSnapshot.children) {
                val key1 = snapshot.key
                val title1 = snapshot.getValue(String::class.java)

                mCurrentUserCookBookKeyTitleMap!!.put(key1, title1)
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {}
    }

    // updates cookbooks lists used to display users cookbooks in MyCookBooksFragment
    internal var cookBookDatabaseListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            mRecipesInCookBooksMap = HashMap()

            for (snapshotOfCookBooks in dataSnapshot.children) {
                val cookBookKey1 = snapshotOfCookBooks.key

                if (mCurrentUserCookBookKeyTitleMap!!.containsKey(cookBookKey1)) {

                    val cookbook1 = snapshotOfCookBooks.getValue(CookBook::class.java)
                    for (snapshotOfRecipesInsideCookBooks in snapshotOfCookBooks.children) {

                        if (snapshotOfRecipesInsideCookBooks.key != "title") {
                            val recipe1 = snapshotOfRecipesInsideCookBooks.getValue(Recipe::class.java)
                            if (!mRecipesInCookBooksMap!!.containsKey(cookbook1)) {

                                mRecipesInCookBooksMap!!.put(cookbook1, ArrayList())
                                mRecipesInCookBooksMap!![cookbook1].add(recipe1)
                            } else {

                                mRecipesInCookBooksMap!![cookbook1].add(recipe1)
                            }
                        }
                    }
                }
            }

            for (logCookBook in mRecipesInCookBooksMap!!.keys) {
                for (logRecipe in mRecipesInCookBooksMap!![logCookBook]) {
                    Log.e("R inside C", "CookBook: " + "[" + logCookBook.title + "]" + " contains Recipe: " + "[" + logRecipe.title + "]")
                }
            }

            // attaches listener so that fragment displaying cookbooks receives arraylist containing cookbooks
            if (mRecipesInCookBooksMap != null && mRecipesUpdateListener != null) {
                mRecipesUpdateListener!!.onMapChanged(mRecipesInCookBooksMap as HashMap<CookBook, ArrayList<Recipe>>?)
            }

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    val currentUserCookBookKeyTitleMap: HashMap<String, String>
        get() = mCurrentUserCookBookKeyTitleMap as HashMap<String, String>?


    val recipesInCookBooksMap: HashMap<CookBook, ArrayList<Recipe>>
        get() = mRecipesInCookBooksMap as HashMap<CookBook, ArrayList<Recipe>>?

    init {
        mCurrentUserCookBookKeyTitleMap = HashMap()
        if (FirebaseAuth.getInstance().currentUser != null) {
            mDatabaseRecipesUploadsBelowUser = mDatabaseRecipesUploads.child(FirebaseAuth.getInstance().currentUser!!.uid)
        }
    }

    fun uploadAll(activity: Activity) {
        var recipeKey: String? = null
        if (RecipeManager.instance.photoUri != null) {
            uploadPhoto(RecipeManager.instance.photoUri)
            recipeKey = uploadRecipeFile(activity)
            if (RecipeManager.instance.currentCookBookKey != null) {
                putRecipeToCookBook(recipeKey, RecipeManager.instance.currentCookBookKey)
            }
        } else {
            recipeKey = uploadRecipeFile(activity)
            if (RecipeManager.instance.currentCookBookKey != null) {
                putRecipeToCookBook(recipeKey, RecipeManager.instance.currentCookBookKey)
            }

        }
    }

    private fun uploadPhoto(filePath: Uri?) {

        // ----- ----- ----- ----- 1. Puts photo to data storage ----- ----- ----- ----- \\
        // ----- ----- ----- ----- 2. Puts photo id to database ----- ----- ----- ----- \\
        // ----- ----- ----- ----- 3. Puts recipe to data storage ----- ----- ----- ----- \\
        // ----- ----- ----- ----- 4. Attaches listeners that watch upload status ----- ----- ----- ----- \\

        //checking if file is available
        if (filePath != null) {

            //getting the storage reference
            val sRef = mStorageReference.child(ConstantsForUploads.STORAGE_PATH_PHOTOS_UPLOADS + System.currentTimeMillis() + "." + getFileExtension(filePath))

            //adding the file to reference
            sRef.putFile(filePath)
                    .addOnSuccessListener { taskSnapshot ->
                        //displaying success toast
                        Log.e("Photo", "Photo uploaded")

                        //creating the upload object to store uploaded image details
                        val upload = RecipeImage(PhotosIdCreatorUtils.instance.id,
                                taskSnapshot.downloadUrl!!.toString(), FirebaseAuth.getInstance().currentUser!!.uid)

                        //adding an upload to firebase database
                        val uploadId = mDatabasePhotosUploads.push().key
                        RecipeManager.instance.currentOrCreateNewRecipe.photoId = uploadId
                        mDatabasePhotosUploads.child(uploadId).setValue(upload)
                    }
                    .addOnFailureListener { Log.e("Photo", "Photo NOT uploaded") }
                    .addOnProgressListener { }
        } else {
            //display an error if no file is selected
        }
    }

    private fun uploadRecipeFile(activity: Activity): String {
        val recipe = RecipeManager.instance.currentOrCreateNewRecipe
        val userUid = FirebaseAuth.getInstance().currentUser!!.uid

        // checks if recipe is not null (IT SHOULD NOT BE NULL)
        if (recipe != null) {

            // check if in mCurrentUserRecipesList is a recipe with same title.
            // if so, ask user if he wants to replace it or leave the old one.
            var recipeHasSameTitle: Boolean? = false
            if (mCurrentUserRecipesList!!.size != 0) {
                for (i in 0 until mCurrentUserRecipesList!!.size - 1) {
                    if (mCurrentUserRecipesList!![i].title == recipe.title) {
                        recipeHasSameTitle = true
                    }
                }
            }


            if (recipeHasSameTitle!!) {
                // asks user whether he would like to overwrite his old recipe or rename it and create a new one
                val alertDialog = AlertDialog.Builder(activity).create()
                alertDialog.setTitle("")
                alertDialog.setMessage("There is a Recipe with this title already. Do you want to overwrite it?")
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, which ->
                    val keyToOverwrite = mCurrentUserRecipeTitleKeyMap!![recipe.title]
                    mDatabaseRecipesUploads.child(userUid).child(keyToOverwrite).setValue(recipe)
                    alertDialog.dismiss()
                }
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, which ->
                    Toast.makeText(activity, "Change the title of your Recipe", Toast.LENGTH_LONG).show()
                    alertDialog.dismiss()
                }

                alertDialog.show()

            } else {
                // checks if user has any recipes. If not, it creates a child inside recipes folder named by Uid of the current user
                if (mDatabaseRecipesUploads.child(userUid) == null) {  // user doesn't have any recipes
                    mDatabaseRecipesUploads.push().child(userUid)
                    Log.e("recipes/userUID", "The reference by user UID created")

                    // there is none with that title, because user didn't even have his folder inside recipes,  so it creates new recipe
                    mRecipeKey = mDatabaseRecipesUploads.child(userUid).push().key
                    mDatabaseRecipesUploads.child(userUid).child(mRecipeKey!!).setValue(recipe).addOnSuccessListener { aVoid ->
                        Log.e("recipes/userUID/recipe", "NEW Recipe uploaded" + aVoid.toString())
                        // OnRecipeUploadedListener notification
                        mRecipeUploadedListener!!.onRecipeUploaded(recipe)
                    }.addOnFailureListener { e -> Log.e("recipes/userUID/recipe", "NEW Recipe NOT uploaded" + e) }


                } else { // user has its folder inside recipes folder, so it just adds new recipe to this folder
                    Log.e("recipes/userUID", "User had his reference before")

                    mRecipeKey = mDatabaseRecipesUploads.child(userUid).push().key
                    mDatabaseRecipesUploads.child(userUid).child(mRecipeKey!!).setValue(recipe).addOnSuccessListener {
                        Log.e("recipes/userUID/recipe", "Recipe uploaded with new KEY")
                        mCurrentUserRecipesList!!.add(recipe)
                        if (mCurrentUserRecipesList!!.size > 0) {
                            for (i in 0 until mCurrentUserRecipesList!!.size - 1) {
                                Log.e("Recipes list", "Recipe " + i + ": " + mCurrentUserRecipesList!![i].title)
                            }
                        }

                        // OnRecipeUploadedListener notification
                        mRecipeUploadedListener!!.onRecipeUploaded(recipe)
                    }.addOnFailureListener { e -> Log.e("recipes/userUID/recipe", "Recipe NOT uploaded" + e) }
                }
            }
        }

        return mRecipeKey
    }

    fun uploadCookBook(activity: Activity, cookBook: CookBook?): String {
        if (cookBook != null) {

            var cookBookHasSameTitle: Boolean? = false
            if (mCurrentUserCookBookKeyTitleMap != null) {
                if (!mCurrentUserCookBookKeyTitleMap!!.isEmpty()) {
                    for (key in mCurrentUserCookBookKeyTitleMap!!.keys) {
                        if (mCurrentUserCookBookKeyTitleMap!![key] == cookBook.title) {
                            cookBookHasSameTitle = true
                            mCookBookKeyToOverwrite = key
                        }
                    }
                }

            }

            if (cookBookHasSameTitle!!) {
                // asks user whether he would like to overwrite his old recipe or rename it and create a new one
                val builder = AlertDialog.Builder(activity)
                builder.setTitle("There is a CookBook with this title already. Do you want to overwrite it?")

                builder.setPositiveButton("Yes") { dialog, which -> mDatabaseCookBooksUploads.child(mCookBookKeyToOverwrite!!).setValue(cookBook) }

                builder.setNegativeButton("No") { dialog, which -> Toast.makeText(activity, "Change the title of your Cookbook", Toast.LENGTH_LONG).show() }

                val alertDialog = builder.create()
                alertDialog.show()
            } else {
                mCookBookKey = mDatabaseCookBooksUploads.push().key

                // Uploads a cookbook to "cookbooks"
                mDatabaseCookBooksUploads.child(mCookBookKey!!).setValue(cookBook).addOnSuccessListener {
                    Log.e("Cookbook", "Cookbook uploaded to cookbooks/")

                    // Uploads key to this cookbook to "users" >> "user" >> "books"
                    mDatabaseUsersUploads.child(FirebaseAuth.getInstance().currentUser!!.uid).child("books").child(mCookBookKey!!).setValue(cookBook.title).addOnSuccessListener {
                        mCurrentUserCookBookKeyTitleMap!!.put(mCookBookKey, cookBook.title)

                        mIfCookBookUploadedListener!!.OnCookBookUploaded(mCurrentUserCookBookKeyTitleMap as HashMap<String, String>?, cookBook.title)
                    }.addOnFailureListener { Log.e("Cookbook KEY", "Cookbook KEY NOT uploaded to users/user/books/") }
                }.addOnFailureListener { Log.e("Cookbook", "Cookbook NOT uploaded to cookbooks/") }
            }
        }

        return mCookBookKey
    }

    private fun putRecipeToCookBook(recipeKey: String?, cookBookKey: String?) {

        var recipePositionOnTheList = 99999

        for (key in mCurrentUserRecipeTitleKeyMap!!.keys) {
            if (mCurrentUserRecipeTitleKeyMap!![key] == recipeKey) {
                for (i in 0 until mCurrentUserRecipesList!!.size - 1) {
                    if (mCurrentUserRecipesList!![i].title == key) {
                        recipePositionOnTheList = i
                    }
                }
            }
        }

        var recipe: Recipe? = null
        if (recipePositionOnTheList != 99999) {
            recipe = mCurrentUserRecipesList!![recipePositionOnTheList]
        }


        mDatabaseCookBooksUploads.child(cookBookKey!!).child(recipeKey!!).setValue(recipe).addOnSuccessListener { Log.e("Cookbook update", "Recipe put to Cookbook") }.addOnFailureListener { Log.e("Cookbook update", "Recipe not put to cookbook") }
    }

    fun removeRecipeFromCookBook(recipeToBeRemoved: Recipe, cookBookHoldingRecipe: CookBook) {
        val recipeTitle = recipeToBeRemoved.title
        val recipeKey = mCurrentUserRecipeTitleKeyMap!![recipeTitle]
        val cookBookTitle = cookBookHoldingRecipe.title
        var cookBookKey = ""

        for (key in mCurrentUserCookBookKeyTitleMap!!.keys) {
            if (mCurrentUserCookBookKeyTitleMap!![key] == cookBookTitle) {
                cookBookKey = key
            }
        }
        mDatabaseCookBooksUploads.child(cookBookKey).child(recipeKey).setValue(null)

        Log.e("Recipe removed", "Recipe with key: $recipeKey removed from cookBook with key: $cookBookKey")

    }

    // remove Recipe from recipes directory and from all cookbooks it was inside
    fun removeRecipeFromAllRecipes(recipeToBeRemoved: Recipe, activity: Activity) {

        val recipeTitle = recipeToBeRemoved.title
        val recipeKey = mCurrentUserRecipeTitleKeyMap!![recipeTitle]

        // array holding cookbook keys which gold a recipe to be deleted
        val mCookBookHoldersKeysList = ArrayList<String>()

        // deletes from all recipes
        mDatabaseRecipesUploads.child(FirebaseAuth.getInstance().currentUser!!.uid).child(recipeKey).setValue(null)

        // finds and puts in an array cookbook keys in the database
        for (cookBook1 in mRecipesInCookBooksMap!!.keys) {
            for (recipe1 in mRecipesInCookBooksMap!![cookBook1]) {
                if (recipeToBeRemoved.title == recipe1.title) {
                    for (cookBookKey in mCurrentUserCookBookKeyTitleMap!!.keys) {
                        if (mCurrentUserCookBookKeyTitleMap!![cookBookKey] == cookBook1.title) {
                            mCookBookHoldersKeysList.add(cookBookKey)
                        }
                    }
                }
            }
        }

        // deletes a recipe from inside all cookbooks holding it
        for (cookBookKey in mCookBookHoldersKeysList) {
            mDatabaseCookBooksUploads.child(cookBookKey).child(recipeKey).setValue(null)
        }
    }

    private fun getFileExtension(uri: Uri): String {
        val cR = MainActivity.contextOfApplication.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }

    fun attachListeners(staticIntFromGeneralUploader: Int) {
        // attaches and detaches SingleValueEventListeners so it can download data while opening an app
        mDatabaseRecipesUploadsBelowUser!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mCurrentUserRecipesList = ArrayList()
                mCurrentUserRecipeTitleKeyMap = HashMap()

                for (snapshot in dataSnapshot.children) {
                    val recipe1 = snapshot.getValue(Recipe::class.java)
                    mCurrentUserRecipesList!!.add(recipe1)
                    mCurrentUserRecipeTitleKeyMap!!.put(recipe1!!.title, snapshot.key)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        mDatabaseCookBooksUploads.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mRecipesInCookBooksMap = HashMap()

                for (snapshotOfCookBooks in dataSnapshot.children) {
                    val cookBookKey1 = snapshotOfCookBooks.key

                    if (mCurrentUserCookBookKeyTitleMap!!.containsKey(cookBookKey1)) {
                        val cookbook1 = snapshotOfCookBooks.getValue(CookBook::class.java)
                        for (snapshotOfRecipesInsideCookBooks in snapshotOfCookBooks.children) {
                            if (snapshotOfRecipesInsideCookBooks.value === Recipe::class.java) {
                                val recipe1 = snapshotOfRecipesInsideCookBooks.getValue(Recipe::class.java)
                                if (!mRecipesInCookBooksMap!!.containsKey(cookbook1)) {
                                    mRecipesInCookBooksMap!!.put(cookbook1, ArrayList())
                                    mRecipesInCookBooksMap!![cookbook1].add(recipe1)
                                } else {
                                    mRecipesInCookBooksMap!![cookbook1].add(recipe1)
                                }
                            }

                        }
                    }
                }

                for (logCookBook in mRecipesInCookBooksMap!!.keys) {
                    for (logRecipe in mRecipesInCookBooksMap!![logCookBook]) {
                        Log.e("R inside C", "CookBook " + logCookBook.title + " containts Recipe: " + logRecipe.title)
                    }
                }

                // attaches listener so that fragment displaying cookbooks receives arraylist containing cookbooks
                if (mRecipesInCookBooksMap != null && mRecipesUpdateListener != null) {
                    mRecipesUpdateListener!!.onMapChanged(mRecipesInCookBooksMap as HashMap<CookBook, ArrayList<Recipe>>?)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        mDatabaseCookBooksKeyUploadsInUserBooks.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mCurrentUserCookBookKeyTitleMap = HashMap()

                for (snapshot in dataSnapshot.children) {
                    val key1 = snapshot.key
                    val title1 = snapshot.getValue(String::class.java)

                    mCurrentUserCookBookKeyTitleMap!!.put(key1, title1)
                }
                Log.e("CurrentUserCookBook", "Updated" + mCurrentUserCookBookKeyTitleMap!!.keys)
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

        if (staticIntFromGeneralUploader == GeneralDataManager.RECIPE_UPDATES_LISTENER) {
            mDatabaseRecipesUploadsBelowUser!!.addValueEventListener(recipeUpdatesListener)

        } else if (staticIntFromGeneralUploader == GeneralDataManager.COOKBOOK_UPDATES_LISTENER) {
            mDatabaseCookBooksKeyUploadsInUserBooks.addValueEventListener(cookBookKeysInUserBooksUpdatesListener)

        } else if (staticIntFromGeneralUploader == GeneralDataManager.RECIPE_IN_COOKBOOK_UPDATES_LISTENER) {
            mDatabaseCookBooksUploads.addValueEventListener(cookBookDatabaseListener)

        } else if (staticIntFromGeneralUploader == GeneralDataManager.ATTACH_ALL_LISTENERS) {
            mDatabaseRecipesUploadsBelowUser!!.addValueEventListener(recipeUpdatesListener)
            mDatabaseCookBooksKeyUploadsInUserBooks.addValueEventListener(cookBookKeysInUserBooksUpdatesListener)
            mDatabaseCookBooksUploads.addValueEventListener(cookBookDatabaseListener)
        }

    }

    fun detachListeners(staticIntFromGeneralUploader: Int) {
        if (staticIntFromGeneralUploader == GeneralDataManager.RECIPE_UPDATES_LISTENER) {
            mDatabaseRecipesUploadsBelowUser!!.addValueEventListener(null)

        } else if (staticIntFromGeneralUploader == GeneralDataManager.COOKBOOK_UPDATES_LISTENER) {
            mDatabaseCookBooksKeyUploadsInUserBooks.addValueEventListener(null)

        } else if (staticIntFromGeneralUploader == GeneralDataManager.RECIPE_IN_COOKBOOK_UPDATES_LISTENER) {
            mDatabaseCookBooksUploads.addValueEventListener(null)

        } else if (staticIntFromGeneralUploader == GeneralDataManager.DETACH_ALL_LISTENERS) {
            mDatabaseRecipesUploadsBelowUser!!.addValueEventListener(null)
            mDatabaseCookBooksKeyUploadsInUserBooks.addValueEventListener(null)
            mDatabaseCookBooksUploads.addValueEventListener(null)
        }
    }

    fun keepSynced() {
        mDatabasePhotosUploads.keepSynced(true)
        mDatabaseRecipesUploads.keepSynced(true)
        mDatabaseCookBooksUploads.keepSynced(true)
        mDatabaseUsersUploads.keepSynced(true)
        mDatabaseCookBooksKeyUploadsInUserBooks.keepSynced(true)
        mDatabaseRecipesUploadsBelowUser!!.keepSynced(true)
    }

    fun getCurrentUserCookBookKey(title: String): String {
        var returnKey = ""

        if (mCurrentUserCookBookKeyTitleMap != null || !mCurrentUserCookBookKeyTitleMap!!.isEmpty()) {
            for (key in mCurrentUserCookBookKeyTitleMap!!.keys) {
                if (mCurrentUserCookBookKeyTitleMap!![key] == title) {
                    returnKey = key
                }
            }
        }

        return returnKey
    }

    fun isRecipeInTheDatabase(title: String?): Boolean? {
        var isInTheDatatabase: Boolean? = false

        for (userRecipe in mCurrentUserRecipesList!!) {
            if (title != null) {
                if (title == userRecipe.title) {
                    isInTheDatatabase = true
                }
            }
        }

        return isInTheDatatabase
    }

    // mRecipesInCookBooksMap listener
    interface OnRecipesUpdateListener {
        fun onMapChanged(map: HashMap<CookBook, ArrayList<Recipe>>)
    }

    fun setOnRecipesUpdateListener(listener: OnRecipesUpdateListener) {
        mRecipesUpdateListener = listener
    }

    // OnCookBookUploaded listener
    interface OnCookBookUploadedListener {
        fun OnCookBookUploaded(cookBooksMap: HashMap<String, String>, cookBookTitle: String?)
    }

    fun setOnCookBookUploadedListener(listener: OnCookBookUploadedListener) {
        mIfCookBookUploadedListener = listener
    }

    // OnRecipeUploaded listener
    interface OnRecipeUploadedListener {
        fun onRecipeUploaded(recipe: Recipe)
    }

    fun setOnRecipeUploadedListener(listener: OnRecipeUploadedListener) {
        mRecipeUploadedListener = listener
    }

    companion object {

        private var mInstance: GeneralDataManager? = null

        // final Strings to choose proper listener to attach
        val RECIPE_UPDATES_LISTENER = 1
        val COOKBOOK_UPDATES_LISTENER = 2
        val RECIPE_IN_COOKBOOK_UPDATES_LISTENER = 3
        val DETACH_ALL_LISTENERS = 0
        val ATTACH_ALL_LISTENERS = 9

        val instance: GeneralDataManager
            @Synchronized get() {
                if (mInstance == null)
                    mInstance = GeneralDataManager()

                return mInstance
            }
    }
}
