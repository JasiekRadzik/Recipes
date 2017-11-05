package com.example.radzik.recipes.database.firebase;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.radzik.recipes.activity.MainActivity;
import com.example.radzik.recipes.database.CookBook;
import com.example.radzik.recipes.database.Recipe;
import com.example.radzik.recipes.database.RecipeImage;
import com.example.radzik.recipes.database.RecipeManager;
import com.example.radzik.recipes.utils.PhotosIdCreatorUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

public class GeneralDataManager {

    private static GeneralDataManager mInstance = null;
    private StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();
    private DatabaseReference mDatabasePhotosUploads = FirebaseDatabase.getInstance().getReference(ConstantsForUploads.DATABASE_PATH_PHOTOS_UPLOADS);
    private DatabaseReference mDatabaseRecipesUploads = FirebaseDatabase.getInstance().getReference(ConstantsForUploads.DATABASE_PATH_RECIPES_UPLOADS);
    private DatabaseReference mDatabaseCookBooksUploads = FirebaseDatabase.getInstance().getReference(ConstantsForUploads.DATABASE_PATH_COOKBOOKS_UPLOADS);
    private DatabaseReference mDatabaseUsersUploads = FirebaseDatabase.getInstance().getReference(ConstantsForUploads.DATABASE_PATH_USERS);

    private DatabaseReference mDatabaseRecipesUploadsBelowUser;
    private DatabaseReference mDatabaseCookBooksKeyUploadsInUserBooks = mDatabaseUsersUploads.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("books");

    // info below handles updates of RECIPES database. Handled by recipeUpdatesListener
    private List<Recipe> mCurrentUserRecipesList;
    private Map<String, String> mCurrentUserRecipeTitleKeyMap;
    private String mRecipeKey = null;

    // info below handles updates of COOKBOOKS database. Handled by cookBookKeysInUserBooksUpdatesListener
    private Map<String, String> mCurrentUserCookBookKeyTitleMap;
    private String mCookBookKey = null;
    private String mCookBookKeyToOverwrite = null;

    // info below handles updates of Recipes inside CookBooks. Handled by cookBookDatabaseListener
    private Map<CookBook, ArrayList<Recipe>> mRecipesInCookBooksMap;

    // mRecipesInCookBooksMap listener
    private OnRecipesUpdateListener mRecipesUpdateListener;

    // CookBook uploaded listener
    private OnCookBookUploadedListener mIfCookBookUploadedListener;

    // mRecipeUploadedListener
    private OnRecipeUploadedListener mRecipeUploadedListener;

    // final Strings to choose proper listener to attach
    public static final int RECIPE_UPDATES_LISTENER = 1;
    public static final int COOKBOOK_UPDATES_LISTENER = 2;
    public static final int RECIPE_IN_COOKBOOK_UPDATES_LISTENER = 3;
    public static final int DETACH_ALL_LISTENERS = 0;
    public static final int ATTACH_ALL_LISTENERS = 9;

    public static synchronized GeneralDataManager getInstance() {
        if (mInstance == null)
            mInstance = new GeneralDataManager();

        return mInstance;
    }

    private GeneralDataManager() {
        mCurrentUserCookBookKeyTitleMap = new HashMap<>();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            mDatabaseRecipesUploadsBelowUser = mDatabaseRecipesUploads.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }

    public void uploadAll(Activity activity) {
        String recipeKey = null;
        if(RecipeManager.getInstance().getPhotoUri() != null) {
            uploadPhoto(RecipeManager.getInstance().getPhotoUri());
            recipeKey = uploadRecipeFile(activity);
            if(RecipeManager.getInstance().getCurrentCookBookKey() != null) {
                putRecipeToCookBook(recipeKey, RecipeManager.getInstance().getCurrentCookBookKey());
            }
        }
        else {
            recipeKey = uploadRecipeFile(activity);
            if(RecipeManager.getInstance().getCurrentCookBookKey() != null) {
                putRecipeToCookBook(recipeKey, RecipeManager.getInstance().getCurrentCookBookKey());
            }

        }
    }
    private void uploadPhoto(Uri filePath) {

        // ----- ----- ----- ----- 1. Puts photo to data storage ----- ----- ----- ----- \\
        // ----- ----- ----- ----- 2. Puts photo id to database ----- ----- ----- ----- \\
        // ----- ----- ----- ----- 3. Puts recipe to data storage ----- ----- ----- ----- \\
        // ----- ----- ----- ----- 4. Attaches listeners that watch upload status ----- ----- ----- ----- \\

        //checking if file is available
        if (filePath != null) {

            //getting the storage reference
            StorageReference sRef = mStorageReference.child(ConstantsForUploads.STORAGE_PATH_PHOTOS_UPLOADS + System.currentTimeMillis() + "." + getFileExtension(filePath));

            //adding the file to reference
            sRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //displaying success toast
                            Log.e("Photo", "Photo uploaded");

                            //creating the upload object to store uploaded image details
                            RecipeImage upload = new RecipeImage(PhotosIdCreatorUtils.getInstance().getID(),
                                    taskSnapshot.getDownloadUrl().toString(), FirebaseAuth.getInstance().getCurrentUser().getUid());

                            //adding an upload to firebase database
                            String uploadId = mDatabasePhotosUploads.push().getKey();
                            RecipeManager.getInstance().getCurrentOrCreateNewRecipe().setPhotoId(uploadId);
                            mDatabasePhotosUploads.child(uploadId).setValue(upload);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e("Photo", "Photo NOT uploaded");
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });
        } else {
            //display an error if no file is selected
        }
    }

    private String uploadRecipeFile(final Activity activity) {
        final Recipe recipe = RecipeManager.getInstance().getCurrentOrCreateNewRecipe();
        final String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // checks if recipe is not null (IT SHOULD NOT BE NULL)
        if (recipe != null) {

            // check if in mCurrentUserRecipesList is a recipe with same title.
            // if so, ask user if he wants to replace it or leave the old one.
            Boolean recipeHasSameTitle = false;
            if (mCurrentUserRecipesList.size() != 0) {
                for (int i = 0; i < mCurrentUserRecipesList.size() - 1; i++) {
                    if (mCurrentUserRecipesList.get(i).getTitle().equals(recipe.getTitle())) {
                        recipeHasSameTitle = true;
                    }
                }
            }


            if (recipeHasSameTitle) {
                // asks user whether he would like to overwrite his old recipe or rename it and create a new one
                final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setTitle("");
                alertDialog.setMessage("There is a Recipe with this title already. Do you want to overwrite it?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String keyToOverwrite = mCurrentUserRecipeTitleKeyMap.get(recipe.getTitle());
                        mDatabaseRecipesUploads.child(userUid).child(keyToOverwrite).setValue(recipe);
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(activity, "Change the title of your Recipe", Toast.LENGTH_LONG).show();
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();

            } else {
                // checks if user has any recipes. If not, it creates a child inside recipes folder named by Uid of the current user
                if (mDatabaseRecipesUploads.child(userUid) == null) {  // user doesn't have any recipes
                    mDatabaseRecipesUploads.push().child(userUid);
                    Log.e("recipes/userUID", "The reference by user UID created");

                    // there is none with that title, because user didn't even have his folder inside recipes,  so it creates new recipe
                    mRecipeKey = mDatabaseRecipesUploads.child(userUid).push().getKey();
                    mDatabaseRecipesUploads.child(userUid).child(mRecipeKey).setValue(recipe).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e("recipes/userUID/recipe", "NEW Recipe uploaded" + aVoid.toString());
                            // OnRecipeUploadedListener notification
                            mRecipeUploadedListener.onRecipeUploaded(recipe);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("recipes/userUID/recipe", "NEW Recipe NOT uploaded" + e);
                        }
                    });


                } else { // user has its folder inside recipes folder, so it just adds new recipe to this folder
                    Log.e("recipes/userUID", "User had his reference before");

                    mRecipeKey = mDatabaseRecipesUploads.child(userUid).push().getKey();
                    mDatabaseRecipesUploads.child(userUid).child(mRecipeKey).setValue(recipe).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e("recipes/userUID/recipe", "Recipe uploaded with new KEY");
                            mCurrentUserRecipesList.add(recipe);
                            if (mCurrentUserRecipesList.size() > 0) {
                                for (int i = 0; i < mCurrentUserRecipesList.size() - 1; i++) {
                                    Log.e("Recipes list", "Recipe " + i + ": " + mCurrentUserRecipesList.get(i).getTitle());
                                }
                            }

                            // OnRecipeUploadedListener notification
                            mRecipeUploadedListener.onRecipeUploaded(recipe);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("recipes/userUID/recipe", "Recipe NOT uploaded" + e);
                        }
                    });
                }
            }
        }

        return mRecipeKey;
    }

    public String uploadCookBook(final Activity activity, final CookBook cookBook) {
        if (cookBook != null) {

            Boolean cookBookHasSameTitle = false;
            if (mCurrentUserCookBookKeyTitleMap != null) {
                if (!mCurrentUserCookBookKeyTitleMap.isEmpty()) {
                    for (String key : mCurrentUserCookBookKeyTitleMap.keySet()) {
                        if (mCurrentUserCookBookKeyTitleMap.get(key).equals(cookBook.getTitle())) {
                            cookBookHasSameTitle = true;
                            mCookBookKeyToOverwrite = key;
                        }
                    }
                }

            }

            if (cookBookHasSameTitle) {
                // asks user whether he would like to overwrite his old recipe or rename it and create a new one
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("There is a CookBook with this title already. Do you want to overwrite it?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseCookBooksUploads.child(mCookBookKeyToOverwrite).setValue(cookBook);
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(activity, "Change the title of your Cookbook", Toast.LENGTH_LONG).show();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                mCookBookKey = mDatabaseCookBooksUploads.push().getKey();

                // Uploads a cookbook to "cookbooks"
                mDatabaseCookBooksUploads.child(mCookBookKey).setValue(cookBook).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.e("Cookbook", "Cookbook uploaded to cookbooks/");

                        // Uploads key to this cookbook to "users" >> "user" >> "books"
                        mDatabaseUsersUploads.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("books").child(mCookBookKey).setValue(cookBook.getTitle()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mCurrentUserCookBookKeyTitleMap.put(mCookBookKey, cookBook.getTitle());

                                mIfCookBookUploadedListener.OnCookBookUploaded((HashMap<String, String>) mCurrentUserCookBookKeyTitleMap, cookBook.getTitle());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("Cookbook KEY", "Cookbook KEY NOT uploaded to users/user/books/");
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Cookbook", "Cookbook NOT uploaded to cookbooks/");
                    }
                });
            }
        }

        return mCookBookKey;
    }

    private void putRecipeToCookBook(String recipeKey, String cookBookKey) {

        int recipePositionOnTheList = 99999;

        for (String key : mCurrentUserRecipeTitleKeyMap.keySet()) {
            if (mCurrentUserRecipeTitleKeyMap.get(key).equals(recipeKey)) {
                for (int i = 0; i < mCurrentUserRecipesList.size() - 1; i++) {
                    if (mCurrentUserRecipesList.get(i).getTitle().equals(key)) {
                        recipePositionOnTheList = i;
                    }
                }
            }
        }

        Recipe recipe = null;
        if (recipePositionOnTheList != 99999) {
            recipe = mCurrentUserRecipesList.get(recipePositionOnTheList);
        }


        mDatabaseCookBooksUploads.child(cookBookKey).child(recipeKey).setValue(recipe).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.e("Cookbook update", "Recipe put to Cookbook");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Cookbook update", "Recipe not put to cookbook");
            }
        });
    }

    public void removeRecipeFromCookBook(Recipe recipeToBeRemoved, CookBook cookBookHoldingRecipe) {
        String recipeTitle = recipeToBeRemoved.getTitle();
        String recipeKey = mCurrentUserRecipeTitleKeyMap.get(recipeTitle);
        String cookBookTitle = cookBookHoldingRecipe.getTitle();
        String cookBookKey = "";

        for (String key : mCurrentUserCookBookKeyTitleMap.keySet()) {
            if (mCurrentUserCookBookKeyTitleMap.get(key).equals(cookBookTitle)) {
                cookBookKey = key;
            }
        }
        mDatabaseCookBooksUploads.child(cookBookKey).child(recipeKey).setValue(null);

        Log.e("Recipe removed", "Recipe with key: " + recipeKey + " removed from cookBook with key: " + cookBookKey);

    }

    // remove Recipe from recipes directory and from all cookbooks it was inside
    public void removeRecipeFromAllRecipes(Recipe recipeToBeRemoved, Activity activity) {

        String recipeTitle = recipeToBeRemoved.getTitle();
        String recipeKey = mCurrentUserRecipeTitleKeyMap.get(recipeTitle);

        // array holding cookbook keys which gold a recipe to be deleted
        List<String> mCookBookHoldersKeysList = new ArrayList<>();

        // deletes from all recipes
        mDatabaseRecipesUploads.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(recipeKey).setValue(null);

        // finds and puts in an array cookbook keys in the database
        for (CookBook cookBook1 : mRecipesInCookBooksMap.keySet()) {
            for (Recipe recipe1 : mRecipesInCookBooksMap.get(cookBook1)) {
                if (recipeToBeRemoved.getTitle().equals(recipe1.getTitle())) {
                    for (String cookBookKey : mCurrentUserCookBookKeyTitleMap.keySet()) {
                        if (mCurrentUserCookBookKeyTitleMap.get(cookBookKey).equals(cookBook1.getTitle())) {
                            mCookBookHoldersKeysList.add(cookBookKey);
                        }
                    }
                }
            }
        }

        // deletes a recipe from inside all cookbooks holding it
        for (String cookBookKey : mCookBookHoldersKeysList) {
            mDatabaseCookBooksUploads.child(cookBookKey).child(recipeKey).setValue(null);
        }
    }

    // used to check if there is a recipe with a given title and updates users recipes list
    ValueEventListener recipeUpdatesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mCurrentUserRecipesList = new ArrayList<>();
            mCurrentUserRecipeTitleKeyMap = new HashMap<>();

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                Recipe recipe1 = snapshot.getValue(Recipe.class);
                mCurrentUserRecipesList.add(recipe1);
                mCurrentUserRecipeTitleKeyMap.put(recipe1.getTitle(), snapshot.getKey());
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    // used to check if there is a cookbook with a given title and updates users cookbooks list
    ValueEventListener cookBookKeysInUserBooksUpdatesListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mCurrentUserCookBookKeyTitleMap = new HashMap<>();

            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                String key1 = snapshot.getKey();
                String title1 = snapshot.getValue(String.class);

                mCurrentUserCookBookKeyTitleMap.put(key1, title1);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    // updates cookbooks lists used to display users cookbooks in MyCookBooksFragment
    ValueEventListener cookBookDatabaseListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mRecipesInCookBooksMap = new HashMap<>();

            for (DataSnapshot snapshotOfCookBooks : dataSnapshot.getChildren()) {
                String cookBookKey1 = snapshotOfCookBooks.getKey();

                if (mCurrentUserCookBookKeyTitleMap.containsKey(cookBookKey1)) {

                    CookBook cookbook1 = snapshotOfCookBooks.getValue(CookBook.class);
                    for (DataSnapshot snapshotOfRecipesInsideCookBooks : snapshotOfCookBooks.getChildren()) {

                        if (!snapshotOfRecipesInsideCookBooks.getKey().equals("title")) {
                            Recipe recipe1 = snapshotOfRecipesInsideCookBooks.getValue(Recipe.class);
                            if (!mRecipesInCookBooksMap.containsKey(cookbook1)) {

                                mRecipesInCookBooksMap.put(cookbook1, new ArrayList<Recipe>());
                                mRecipesInCookBooksMap.get(cookbook1).add(recipe1);
                            } else {

                                mRecipesInCookBooksMap.get(cookbook1).add(recipe1);
                            }
                        }
                    }
                }
            }

            for (CookBook logCookBook : mRecipesInCookBooksMap.keySet()) {
                for (Recipe logRecipe : mRecipesInCookBooksMap.get(logCookBook)) {
                    Log.e("R inside C", "CookBook: " + "[" + logCookBook.getTitle() + "]" +" contains Recipe: " + "[" + logRecipe.getTitle() + "]");
                }
            }

            // attaches listener so that fragment displaying cookbooks receives arraylist containing cookbooks
            if (mRecipesInCookBooksMap != null && mRecipesUpdateListener != null) {
                mRecipesUpdateListener.onMapChanged((HashMap<CookBook, ArrayList<Recipe>>) mRecipesInCookBooksMap);
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private String getFileExtension(Uri uri) {
        ContentResolver cR = MainActivity.getContextOfApplication().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void attachListeners(int staticIntFromGeneralUploader) {
        // attaches and detaches SingleValueEventListeners so it can download data while opening an app
        mDatabaseRecipesUploadsBelowUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentUserRecipesList = new ArrayList<>();
                mCurrentUserRecipeTitleKeyMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe1 = snapshot.getValue(Recipe.class);
                    mCurrentUserRecipesList.add(recipe1);
                    mCurrentUserRecipeTitleKeyMap.put(recipe1.getTitle(), snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseCookBooksUploads.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mRecipesInCookBooksMap = new HashMap<>();

                for (DataSnapshot snapshotOfCookBooks : dataSnapshot.getChildren()) {
                    String cookBookKey1 = snapshotOfCookBooks.getKey();

                    if (mCurrentUserCookBookKeyTitleMap.containsKey(cookBookKey1)) {
                        CookBook cookbook1 = snapshotOfCookBooks.getValue(CookBook.class);
                        for (DataSnapshot snapshotOfRecipesInsideCookBooks : snapshotOfCookBooks.getChildren()) {
                            if (snapshotOfRecipesInsideCookBooks.getValue() == Recipe.class) {
                                Recipe recipe1 = snapshotOfRecipesInsideCookBooks.getValue(Recipe.class);
                                if (!mRecipesInCookBooksMap.containsKey(cookbook1)) {
                                    mRecipesInCookBooksMap.put(cookbook1, new ArrayList<Recipe>());
                                    mRecipesInCookBooksMap.get(cookbook1).add(recipe1);
                                } else {
                                    mRecipesInCookBooksMap.get(cookbook1).add(recipe1);
                                }
                            }

                        }
                    }
                }

                for (CookBook logCookBook : mRecipesInCookBooksMap.keySet()) {
                    for (Recipe logRecipe : mRecipesInCookBooksMap.get(logCookBook)) {
                        Log.e("R inside C", "CookBook " + logCookBook.getTitle() + " containts Recipe: " + logRecipe.getTitle());
                    }
                }

                // attaches listener so that fragment displaying cookbooks receives arraylist containing cookbooks
                if (mRecipesInCookBooksMap != null && mRecipesUpdateListener != null) {
                    mRecipesUpdateListener.onMapChanged((HashMap<CookBook, ArrayList<Recipe>>) mRecipesInCookBooksMap);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseCookBooksKeyUploadsInUserBooks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentUserCookBookKeyTitleMap = new HashMap<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key1 = snapshot.getKey();
                    String title1 = snapshot.getValue(String.class);

                    mCurrentUserCookBookKeyTitleMap.put(key1, title1);
                }
                Log.e("CurrentUserCookBook", "Updated" + mCurrentUserCookBookKeyTitleMap.keySet());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (staticIntFromGeneralUploader == GeneralDataManager.RECIPE_UPDATES_LISTENER) {
            mDatabaseRecipesUploadsBelowUser.addValueEventListener(recipeUpdatesListener);

        } else if (staticIntFromGeneralUploader == GeneralDataManager.COOKBOOK_UPDATES_LISTENER) {
            mDatabaseCookBooksKeyUploadsInUserBooks.addValueEventListener(cookBookKeysInUserBooksUpdatesListener);

        } else if (staticIntFromGeneralUploader == GeneralDataManager.RECIPE_IN_COOKBOOK_UPDATES_LISTENER) {
            mDatabaseCookBooksUploads.addValueEventListener(cookBookDatabaseListener);

        } else if (staticIntFromGeneralUploader == GeneralDataManager.ATTACH_ALL_LISTENERS) {
            mDatabaseRecipesUploadsBelowUser.addValueEventListener(recipeUpdatesListener);
            mDatabaseCookBooksKeyUploadsInUserBooks.addValueEventListener(cookBookKeysInUserBooksUpdatesListener);
            mDatabaseCookBooksUploads.addValueEventListener(cookBookDatabaseListener);
        }

    }

    public void detachListeners(int staticIntFromGeneralUploader) {
        if (staticIntFromGeneralUploader == GeneralDataManager.RECIPE_UPDATES_LISTENER) {
            mDatabaseRecipesUploadsBelowUser.addValueEventListener(null);

        } else if (staticIntFromGeneralUploader == GeneralDataManager.COOKBOOK_UPDATES_LISTENER) {
            mDatabaseCookBooksKeyUploadsInUserBooks.addValueEventListener(null);

        } else if (staticIntFromGeneralUploader == GeneralDataManager.RECIPE_IN_COOKBOOK_UPDATES_LISTENER) {
            mDatabaseCookBooksUploads.addValueEventListener(null);

        } else if (staticIntFromGeneralUploader == GeneralDataManager.DETACH_ALL_LISTENERS) {
            mDatabaseRecipesUploadsBelowUser.addValueEventListener(null);
            mDatabaseCookBooksKeyUploadsInUserBooks.addValueEventListener(null);
            mDatabaseCookBooksUploads.addValueEventListener(null);
        }
    }

    public void keepSynced() {
        mDatabasePhotosUploads.keepSynced(true);
        mDatabaseRecipesUploads.keepSynced(true);
        mDatabaseCookBooksUploads.keepSynced(true);
        mDatabaseUsersUploads.keepSynced(true);
        mDatabaseCookBooksKeyUploadsInUserBooks.keepSynced(true);
        mDatabaseRecipesUploadsBelowUser.keepSynced(true);
    }

    public String getCurrentUserCookBookKey(String title) {
        String returnKey = "";

        if(mCurrentUserCookBookKeyTitleMap != null || !mCurrentUserCookBookKeyTitleMap.isEmpty()) {
            for(String key : mCurrentUserCookBookKeyTitleMap.keySet()) {
                if(mCurrentUserCookBookKeyTitleMap.get(key).equals(title)) {
                    returnKey = key;
                }
            }
        }

        return returnKey;
    }

    public Boolean isRecipeInTheDatabase(String title) {
        Boolean isInTheDatatabase = false;

        for(Recipe userRecipe : mCurrentUserRecipesList) {
            if(title != null) {
                if(title.equals(userRecipe.getTitle())) {
                    isInTheDatatabase = true;
                }
            }
        }

        return isInTheDatatabase;
    }

    public HashMap<String, String> getCurrentUserCookBookKeyTitleMap() {
        return (HashMap<String, String>) mCurrentUserCookBookKeyTitleMap;
    }


    public HashMap<CookBook, ArrayList<Recipe>> getRecipesInCookBooksMap() {
        return (HashMap<CookBook, ArrayList<Recipe>>) mRecipesInCookBooksMap;
    }

    // mRecipesInCookBooksMap listener
    public interface OnRecipesUpdateListener {
        void onMapChanged(HashMap<CookBook, ArrayList<Recipe>> map);
    }

    public void setOnRecipesUpdateListener(OnRecipesUpdateListener listener) {
        mRecipesUpdateListener = listener;
    }

    // OnCookBookUploaded listener
    public interface OnCookBookUploadedListener {
        void OnCookBookUploaded(HashMap<String, String> cookBooksMap, String cookBookTitle);
    }

    public void setOnCookBookUploadedListener(OnCookBookUploadedListener listener) {
        mIfCookBookUploadedListener = listener;
    }

    // OnRecipeUploaded listener
    public interface OnRecipeUploadedListener {
        void onRecipeUploaded(Recipe recipe);
    }

    public void setOnRecipeUploadedListener(OnRecipeUploadedListener listener) {
        mRecipeUploadedListener = listener;
    }
}
