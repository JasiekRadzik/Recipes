package com.example.radzik.recipes.activity;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.radzik.recipes.R;
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection;
import com.example.radzik.recipes.database.RecipeManager;
import com.example.radzik.recipes.database.firebase.GeneralDataManager;
import com.example.radzik.recipes.fragment.AddRecipeTitleFragment;
import com.example.radzik.recipes.fragment.ApplicationTitleEmptyFragment;
import com.example.radzik.recipes.fragment.ChooseDocumentLayoutFragment;
import com.example.radzik.recipes.fragment.ChoosePhotoFragment;
import com.example.radzik.recipes.fragment.DocumentTestFragment;
import com.example.radzik.recipes.fragment.WriteRecipeFragment;
import com.facebook.login.LoginManager;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final int PROFILE_SETTING = 100000;
    private AccountHeader mHeaderResult = null;
    private Drawer mDrawer = null;

    private static final String TAG = "AndroidBash";
    private Firebase myFirebaseRef;
    private FirebaseAuth mAuth;

    private String mImageUrl;
    private String mUid;

    private boolean mWasRecipeUploaded = false;

    RecipeManager mManager;

    private int mCurrentFragmentID = 0;

    public static Context mContextOfApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mManager = RecipeManager.getInstance();

        mContextOfApplication = getApplicationContext();

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myFirebaseRef.setAndroidContext(getApplicationContext());
        myFirebaseRef = new Firebase("https://recipes-309da.firebaseio.com/users/");
        mAuth = FirebaseAuth.getInstance();

        GeneralDataManager.getInstance().keepSynced();

        openNextFragment(new ApplicationTitleEmptyFragment());

        //Get the uid for the currently logged in User from intent data passed to this activity
            mUid = getIntent().getExtras().getString("user_id");

        //Get the imageUrl  for the currently logged in User from intent data passed to this activity
            mImageUrl = getIntent().getExtras().getString("profile_picture");

        // Get the boolean telling MainActivity if recipe was previously uploaded
            mWasRecipeUploaded = getIntent().getExtras().getBoolean("recipe_uploaded");

        final PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.create_recipe).withIcon(R.drawable.icon_chefs_hat);
        final PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("Cookbooks").withIcon(R.drawable.icon_book_shelf);
        final PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.my_recipes).withIcon(R.drawable.icon_recipe_list);
        final PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName("Import recipe").withIcon(R.drawable.icon_document);
        final SecondaryDrawerItem item5 = new SecondaryDrawerItem().withIdentifier(5).withName("Settings").withIcon(R.drawable.icon_settings);
        final SecondaryDrawerItem item6 = new SecondaryDrawerItem().withIdentifier(6).withName("Log out").withIcon(R.drawable.icon_logout);

        ProfileDrawerItem profile = new ProfileDrawerItem().withName(mAuth.getCurrentUser().getDisplayName()).withEmail(mAuth.getCurrentUser().getEmail());
        if (mImageUrl != null && !mImageUrl.equals("")) {
            profile.withIcon(mImageUrl);
        } else {
            profile.withIcon(R.drawable.icon_example);
        }


        // Create the AccountHeader
        mHeaderResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.header_orange_backg)
                .addProfiles(
                        profile
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        if (profile.getIcon() == null) {
                            profile.withIcon(mImageUrl);
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        //Create the drawer
        mDrawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withItemAnimator(new AlphaCrossFadeAnimator())
                .withAccountHeader(mHeaderResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        item3,
                        item4,
                        new DividerDrawerItem(),
                        item5,
                        item6
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        item1.withEnabled(true);
                        item2.withEnabled(true);
                        item3.withEnabled(true);
                        item4.withEnabled(true);
                        item5.withEnabled(true);
                        item6.withEnabled(true);

                        if (drawerItem == item1) {
                            if(item1.isSelected()) {
                                mDrawer.closeDrawer();

                                mCurrentFragmentID = RecipeManager.getInstance().getCurrentFragmentID();

                                if(mCurrentFragmentID == 0) {
                                    openNextFragment(new ChooseDocumentLayoutFragment());

                                    // resets data held in a RecipeManager and creates new Recipe
                                    RecipeManager.getInstance().resetManagerInstance();
                                    RecipeManager.getInstance().getCurrentOrCreateNewRecipe();
                                }
                                else if (mCurrentFragmentID != 0) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Do you want to create new recipe or continue writing previous one?");

                                    builder.setPositiveButton("Create NEW", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // resets data held in a RecipeManager and creates new Recipe
                                            RecipeManager.getInstance().resetManagerInstance();
                                            RecipeManager.getInstance().getCurrentOrCreateNewRecipe();
                                            openNextFragment(new ChooseDocumentLayoutFragment());
                                        }
                                    });

                                    builder.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(mCurrentFragmentID == ConstantsForFragmentsSelection.CHOOSE_DOCUMENT_LAYOUT_FRAGMENT) {
                                                openNextFragment(new ChooseDocumentLayoutFragment());
                                                item1.withEnabled(false);
                                            } else if(mCurrentFragmentID == ConstantsForFragmentsSelection.TITLE_FRAGMENT) {
                                                openNextFragment(new AddRecipeTitleFragment());
                                                item1.withEnabled(false);
                                            } else if(mCurrentFragmentID == ConstantsForFragmentsSelection.WRITE_RECIPE_FRAGMENT) {
                                                openNextFragment(new WriteRecipeFragment());
                                                item1.withEnabled(false);
                                            } else if(mCurrentFragmentID == ConstantsForFragmentsSelection.CHOOSE_PHOTO_FRAGMENT) {
                                                openNextFragment(new ChoosePhotoFragment());
                                                item1.withEnabled(false);
                                            }
                                        }
                                    });

                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            } else {
                                mCurrentFragmentID = RecipeManager.getInstance().getCurrentFragmentID();

                                if(mCurrentFragmentID == 0) {
                                    openNextFragment(new ChooseDocumentLayoutFragment());

                                    // resets data held in a RecipeManager and creates new Recipe
                                    RecipeManager.getInstance().resetManagerInstance();
                                    RecipeManager.getInstance().getCurrentOrCreateNewRecipe();
                                }
                                else if (mCurrentFragmentID != 0) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("Do you want to create new recipe or continue writing previous one?");

                                    builder.setPositiveButton("Create NEW", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // resets data held in a RecipeManager and creates new Recipe
                                            RecipeManager.getInstance().resetManagerInstance();
                                            RecipeManager.getInstance().getCurrentOrCreateNewRecipe();
                                            openNextFragment(new ChooseDocumentLayoutFragment());
                                        }
                                    });

                                    builder.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(mCurrentFragmentID == ConstantsForFragmentsSelection.CHOOSE_DOCUMENT_LAYOUT_FRAGMENT) {
                                                openNextFragment(new ChooseDocumentLayoutFragment());
                                                item1.withEnabled(false);
                                            } else if(mCurrentFragmentID == ConstantsForFragmentsSelection.TITLE_FRAGMENT) {
                                                openNextFragment(new AddRecipeTitleFragment());
                                                item1.withEnabled(false);
                                            } else if(mCurrentFragmentID == ConstantsForFragmentsSelection.WRITE_RECIPE_FRAGMENT) {
                                                openNextFragment(new WriteRecipeFragment());
                                                item1.withEnabled(false);
                                            } else if(mCurrentFragmentID == ConstantsForFragmentsSelection.CHOOSE_PHOTO_FRAGMENT) {
                                                openNextFragment(new ChoosePhotoFragment());
                                                item1.withEnabled(false);
                                            }
                                        }
                                    });

                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }

                            }

                        } else if (drawerItem == item2) {
                            if(item1.isSelected()) {
                                mDrawer.closeDrawer();
                            } else {
                                openNextFragment(new ApplicationTitleEmptyFragment());
                            }

                        } else if (drawerItem == item3) {
                            if(item1.isSelected()) {
                                mDrawer.closeDrawer();
                            } else {
                                //openNextFragment(new ApplicationTitleEmptyFragment());
                                openNextFragment(new DocumentTestFragment());
                            }
                        } else if (drawerItem == item4) {
                            if(item1.isSelected()) {
                                mDrawer.closeDrawer();
                            } else {
                                openNextFragment(new ApplicationTitleEmptyFragment());
                            }

                        }
                        else if (drawerItem == item5) {
                            if(item1.isSelected()) {
                                mDrawer.closeDrawer();
                            } else {
                                // TEST
                                openNextFragment(new ChoosePhotoFragment());

                                //openNextFragment(new ApplicationTitleEmptyFragment());
                            }
                        } else if (drawerItem == item6) {
                            mAuth.signOut();
                            LoginManager.getInstance().logOut();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        }
                        return false;
                    }
                })
                .withSavedInstance(savedInstanceState)
                .build();

        //only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 11
            mDrawer.setSelection(0, false);

            //set the active profile
            mHeaderResult.setActiveProfile(profile);
        }

        //initialize and create the image loader logic
        DrawerImageLoader.init(new DrawerImageLoader.IDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx) {
                return null;
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                return null;
            }
        });

        // mDrawer.setSelection(item1, true);

        if(mWasRecipeUploaded) {
            mDrawer.openDrawer();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // new ImageLoadTask(imageUrl, mProfilePicture).execute();
        //Referring to the name of the User who has logged in currently and adding a valueChangeListener
        myFirebaseRef.child(mUid).child("name").addValueEventListener(new ValueEventListener() {
            //onDataChange is called every time the name of the User changes in your Firebase Database
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Inside onDataChange we can get the data as an Object from the dataSnapshot
                //getValue returns an Object. We can specify the type by passing the type expected as a parameter
                String data = dataSnapshot.getValue(String.class);
                Toast.makeText(getApplicationContext(), "" + data, Toast.LENGTH_LONG).show();

                GeneralDataManager.getInstance().attachListeners(GeneralDataManager.ATTACH_ALL_LISTENERS);
            }

            //onCancelled is called in case of any error
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Toast.makeText(getApplicationContext(), "" + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                mAuth.signOut();
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = mDrawer.saveInstanceState(outState);
        //add the values which need to be saved from the accountHeader to the bundle
        outState = mHeaderResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (mDrawer != null && mDrawer.isDrawerOpen()) {
            mDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    public static Context getContextOfApplication() {
        return mContextOfApplication;
    }

    private void openNextFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
