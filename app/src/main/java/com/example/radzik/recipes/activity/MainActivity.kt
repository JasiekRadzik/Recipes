package com.example.radzik.recipes.activity


import android.app.Fragment
import android.app.FragmentTransaction
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.radzik.recipes.R
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection
import com.example.radzik.recipes.database.RecipeManager
import com.example.radzik.recipes.database.firebase.GeneralDataManager
import com.example.radzik.recipes.fragment.AddRecipeTitleFragment
import com.example.radzik.recipes.fragment.ApplicationTitleEmptyFragment
import com.example.radzik.recipes.fragment.ChooseDocumentLayoutFragment
import com.example.radzik.recipes.fragment.ChoosePhotoFragment
import com.example.radzik.recipes.fragment.DocumentTestFragment
import com.example.radzik.recipes.fragment.WriteRecipeFragment
import com.facebook.login.LoginManager
import com.firebase.client.DataSnapshot
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import com.firebase.client.ValueEventListener
import com.google.firebase.auth.FirebaseAuth
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator
import com.mikepenz.materialdrawer.util.DrawerImageLoader
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private var mHeaderResult: AccountHeader? = null
    private var mDrawer: Drawer? = null
    private var myFirebaseRef: Firebase? = null
    private var mAuth: FirebaseAuth? = null

    private var mImageUrl: String? = null
    private var mUid: String? = null

    private var mWasRecipeUploaded = false

    internal var mManager: RecipeManager

    private var mCurrentFragmentID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mManager = RecipeManager.instance

        contextOfApplication = applicationContext

        // Handle Toolbar
        val toolbar = findViewById(R.id.toolbarMain) as Toolbar
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        myFirebaseRef!!.setAndroidContext(applicationContext)
        myFirebaseRef = Firebase("https://recipes-309da.firebaseio.com/users/")
        mAuth = FirebaseAuth.getInstance()

        GeneralDataManager.getInstance().keepSynced()

        openNextFragment(ApplicationTitleEmptyFragment())

        //Get the uid for the currently logged in User from intent data passed to this activity
        mUid = intent.extras!!.getString("user_id")

        //Get the imageUrl  for the currently logged in User from intent data passed to this activity
        mImageUrl = intent.extras!!.getString("profile_picture")

        // Get the boolean telling MainActivity if recipe was previously uploaded
        mWasRecipeUploaded = intent.extras!!.getBoolean("recipe_uploaded")

        val item1 = PrimaryDrawerItem().withIdentifier(1).withName(R.string.create_recipe).withIcon(R.drawable.icon_chefs_hat)
        val item2 = PrimaryDrawerItem().withIdentifier(2).withName("Cookbooks").withIcon(R.drawable.icon_book_shelf)
        val item3 = PrimaryDrawerItem().withIdentifier(3).withName(R.string.my_recipes).withIcon(R.drawable.icon_recipe_list)
        val item4 = PrimaryDrawerItem().withIdentifier(4).withName("Import recipe").withIcon(R.drawable.icon_document)
        val item5 = SecondaryDrawerItem().withIdentifier(5).withName("Settings").withIcon(R.drawable.icon_settings)
        val item6 = SecondaryDrawerItem().withIdentifier(6).withName("Log out").withIcon(R.drawable.icon_logout)

        val profile = ProfileDrawerItem().withName(mAuth!!.currentUser!!.displayName).withEmail(mAuth!!.currentUser!!.email)
        if (mImageUrl != null && mImageUrl != "") {
            profile.withIcon(mImageUrl)
        } else {
            profile.withIcon(R.drawable.icon_example)
        }


        // Create the AccountHeader
        mHeaderResult = AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.header_orange_backg)
                .addProfiles(
                        profile
                )
                .withOnAccountHeaderListener { view, profile, current ->
                    if (profile.icon == null) {
                        profile.withIcon(mImageUrl)
                    }
                    false
                }
                .withSavedInstance(savedInstanceState)
                .build()

        //Create the drawer
        mDrawer = DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withItemAnimator(AlphaCrossFadeAnimator())
                .withAccountHeader(mHeaderResult!!) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        item1,
                        DividerDrawerItem(),
                        item2,
                        item3,
                        item4,
                        DividerDrawerItem(),
                        item5,
                        item6
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener { view, position, drawerItem ->
                    item1.withEnabled(true)
                    item2.withEnabled(true)
                    item3.withEnabled(true)
                    item4.withEnabled(true)
                    item5.withEnabled(true)
                    item6.withEnabled(true)

                    if (drawerItem === item1) {
                        if (item1.isSelected) {
                            mDrawer!!.closeDrawer()

                            mCurrentFragmentID = RecipeManager.instance.currentFragmentID

                            if (mCurrentFragmentID == 0) {
                                openNextFragment(ChooseDocumentLayoutFragment())

                                // resets data held in a RecipeManager and creates new Recipe
                                RecipeManager.instance.resetManagerInstance()
                                RecipeManager.instance.currentOrCreateNewRecipe
                            } else if (mCurrentFragmentID != 0) {

                                val builder = AlertDialog.Builder(this@MainActivity)
                                builder.setTitle("Do you want to create new recipe or continue writing previous one?")

                                builder.setPositiveButton("Create NEW") { dialog, which ->
                                    // resets data held in a RecipeManager and creates new Recipe
                                    RecipeManager.instance.resetManagerInstance()
                                    RecipeManager.instance.currentOrCreateNewRecipe
                                    openNextFragment(ChooseDocumentLayoutFragment())
                                }

                                builder.setNegativeButton("Continue") { dialog, which ->
                                    if (mCurrentFragmentID == ConstantsForFragmentsSelection.CHOOSE_DOCUMENT_LAYOUT_FRAGMENT) {
                                        openNextFragment(ChooseDocumentLayoutFragment())
                                        item1.withEnabled(false)
                                    } else if (mCurrentFragmentID == ConstantsForFragmentsSelection.TITLE_FRAGMENT) {
                                        openNextFragment(AddRecipeTitleFragment())
                                        item1.withEnabled(false)
                                    } else if (mCurrentFragmentID == ConstantsForFragmentsSelection.WRITE_RECIPE_FRAGMENT) {
                                        openNextFragment(WriteRecipeFragment())
                                        item1.withEnabled(false)
                                    } else if (mCurrentFragmentID == ConstantsForFragmentsSelection.CHOOSE_PHOTO_FRAGMENT) {
                                        openNextFragment(ChoosePhotoFragment())
                                        item1.withEnabled(false)
                                    }
                                }

                                val alertDialog = builder.create()
                                alertDialog.show()
                            }
                        } else {
                            mCurrentFragmentID = RecipeManager.instance.currentFragmentID

                            if (mCurrentFragmentID == 0) {
                                openNextFragment(ChooseDocumentLayoutFragment())

                                // resets data held in a RecipeManager and creates new Recipe
                                RecipeManager.instance.resetManagerInstance()
                                RecipeManager.instance.currentOrCreateNewRecipe
                            } else if (mCurrentFragmentID != 0) {

                                val builder = AlertDialog.Builder(this@MainActivity)
                                builder.setTitle("Do you want to create new recipe or continue writing previous one?")

                                builder.setPositiveButton("Create NEW") { dialog, which ->
                                    // resets data held in a RecipeManager and creates new Recipe
                                    RecipeManager.instance.resetManagerInstance()
                                    RecipeManager.instance.currentOrCreateNewRecipe
                                    openNextFragment(ChooseDocumentLayoutFragment())
                                }

                                builder.setNegativeButton("Continue") { dialog, which ->
                                    if (mCurrentFragmentID == ConstantsForFragmentsSelection.CHOOSE_DOCUMENT_LAYOUT_FRAGMENT) {
                                        openNextFragment(ChooseDocumentLayoutFragment())
                                        item1.withEnabled(false)
                                    } else if (mCurrentFragmentID == ConstantsForFragmentsSelection.TITLE_FRAGMENT) {
                                        openNextFragment(AddRecipeTitleFragment())
                                        item1.withEnabled(false)
                                    } else if (mCurrentFragmentID == ConstantsForFragmentsSelection.WRITE_RECIPE_FRAGMENT) {
                                        openNextFragment(WriteRecipeFragment())
                                        item1.withEnabled(false)
                                    } else if (mCurrentFragmentID == ConstantsForFragmentsSelection.CHOOSE_PHOTO_FRAGMENT) {
                                        openNextFragment(ChoosePhotoFragment())
                                        item1.withEnabled(false)
                                    }
                                }

                                val alertDialog = builder.create()
                                alertDialog.show()
                            }

                        }

                    } else if (drawerItem === item2) {
                        if (item1.isSelected) {
                            mDrawer!!.closeDrawer()
                        } else {
                            openNextFragment(ApplicationTitleEmptyFragment())
                        }

                    } else if (drawerItem === item3) {
                        if (item1.isSelected) {
                            mDrawer!!.closeDrawer()
                        } else {
                            //openNextFragment(new ApplicationTitleEmptyFragment());
                            openNextFragment(DocumentTestFragment())
                        }
                    } else if (drawerItem === item4) {
                        if (item1.isSelected) {
                            mDrawer!!.closeDrawer()
                        } else {
                            openNextFragment(ApplicationTitleEmptyFragment())
                        }

                    } else if (drawerItem === item5) {
                        if (item1.isSelected) {
                            mDrawer!!.closeDrawer()
                        } else {
                            // TEST
                            openNextFragment(ChoosePhotoFragment())

                            //openNextFragment(new ApplicationTitleEmptyFragment());
                        }
                    } else if (drawerItem === item6) {
                        mAuth!!.signOut()
                        LoginManager.getInstance().logOut()
                        val intent = Intent(applicationContext, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    false
                }
                .withSavedInstance(savedInstanceState)
                .build()

        //only set the active selection or active profile if we do not recreate the activity
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 11
            mDrawer!!.setSelection(0, false)

            //set the active profile
            mHeaderResult!!.activeProfile = profile
        }

        //initialize and create the image loader logic
        DrawerImageLoader.init(object : DrawerImageLoader.IDrawerImageLoader {
            override fun set(imageView: ImageView, uri: Uri, placeholder: Drawable) {
                Picasso.with(imageView.context).load(uri).placeholder(placeholder).into(imageView)
            }

            override fun cancel(imageView: ImageView) {
                Picasso.with(imageView.context).cancelRequest(imageView)
            }

            override fun placeholder(ctx: Context): Drawable? {
                return null
            }

            override fun placeholder(ctx: Context, tag: String): Drawable? {
                return null
            }
        })

        // mDrawer.setSelection(item1, true);

        if (mWasRecipeUploaded) {
            mDrawer!!.openDrawer()
        }
    }

    override fun onStart() {
        super.onStart()

        // new ImageLoadTask(imageUrl, mProfilePicture).execute();
        //Referring to the name of the User who has logged in currently and adding a valueChangeListener
        myFirebaseRef!!.child(mUid!!).child("name").addValueEventListener(object : ValueEventListener {
            //onDataChange is called every time the name of the User changes in your Firebase Database
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //Inside onDataChange we can get the data as an Object from the dataSnapshot
                //getValue returns an Object. We can specify the type by passing the type expected as a parameter
                val data = dataSnapshot.getValue(String::class.java)
                Toast.makeText(applicationContext, "" + data, Toast.LENGTH_LONG).show()

                GeneralDataManager.getInstance().attachListeners(GeneralDataManager.ATTACH_ALL_LISTENERS)
            }

            //onCancelled is called in case of any error
            override fun onCancelled(firebaseError: FirebaseError) {
                Toast.makeText(applicationContext, "" + firebaseError.message, Toast.LENGTH_LONG).show()
                mAuth!!.signOut()
                LoginManager.getInstance().logOut()
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            }
        })

    }

    public override fun onStop() {
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        var outState = outState
        //add the values which need to be saved from the drawer to the bundle
        outState = mDrawer!!.saveInstanceState(outState)
        //add the values which need to be saved from the accountHeader to the bundle
        outState = mHeaderResult!!.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (mDrawer != null && mDrawer!!.isDrawerOpen) {
            mDrawer!!.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    private fun openNextFragment(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {

        private val PROFILE_SETTING = 100000

        private val TAG = "AndroidBash"

        var contextOfApplication: Context
    }
}
