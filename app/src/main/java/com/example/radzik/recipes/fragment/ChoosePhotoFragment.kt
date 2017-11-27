package com.example.radzik.recipes.fragment

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Fragment
import android.app.FragmentTransaction
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast

import com.example.radzik.recipes.R
import com.example.radzik.recipes.activity.MainActivity
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection
import com.example.radzik.recipes.database.RecipeManager
import com.example.radzik.recipes.database.firebase.GeneralDataManager
import com.example.radzik.recipes.utils.ImageEditor
import com.example.radzik.recipes.utils.PixelsToDensityConverter
import com.example.radzik.recipes.utils.ResizeHeightAnimation
import com.example.radzik.recipes.utils.ResizeWidthAnimation

import java.io.ByteArrayOutputStream

import butterknife.BindView
import butterknife.ButterKnife

import android.app.Activity.RESULT_OK

/**
 * Created by Radzik on 05.09.2017.
 */

class ChoosePhotoFragment : Fragment() {

    @BindView(R.id.imageViewDisplayImage)
    internal var mImgView: ImageView? = null

    @BindView(R.id.imageButtonTakeAPhoto)
    internal var mTakePhotoButton: ImageButton? = null

    @BindView(R.id.imageButtonChooseFromGallery)
    internal var mChoosePhotoButton: ImageButton? = null

    @BindView(R.id.buttonChooseStyleFragment)
    internal var mNextFragmentButton: Button? = null

    @BindView(R.id.imageButtonCancelPhoto)
    internal var mImageButtonCancelPhoto: ImageButton? = null

    @BindView(R.id.horizontalPlusLine)
    internal var mHorizontalPlusLine: RelativeLayout? = null

    @BindView(R.id.verticalPlusLine)
    internal var mVerticalPlusLine: RelativeLayout? = null

    @BindView(R.id.buttonToOpenButtons)
    internal var mButtonToOpenButtons: Button? = null

    @BindView(R.id.relativeLayoutPhotoOptions)
    internal var mChoosePhotoOptionsLayout: RelativeLayout? = null

    private var isPhotoOptionsOpened = false

    internal var mManager: RecipeManager

    internal var plusButtonListener: View.OnClickListener = View.OnClickListener {
        val converter = PixelsToDensityConverter.instance

        if (!isPhotoOptionsOpened) {
            val goalMarginPlusButton = converter.convertToInt(50, activity)
            val startMarginPlusButton = converter.convertToInt(16, activity)

            val varl = ValueAnimator.ofInt(startMarginPlusButton, goalMarginPlusButton)
            varl.duration = 500
            varl.addUpdateListener { animation ->
                val lp = mHorizontalPlusLine!!.layoutParams as RelativeLayout.LayoutParams
                lp.setMargins(0, 0, 0, animation.animatedValue as Int)
                mHorizontalPlusLine!!.layoutParams = lp
                Log.e("Animation UP", "MARGIN BOTTOM: " + animation.animatedValue)
            }
            varl.start()

            val goalWidthPlusButton = converter.convertToInt(50, activity)
            val heightAnim = ResizeHeightAnimation(mHorizontalPlusLine!!, goalWidthPlusButton)
            heightAnim.duration = 500
            mHorizontalPlusLine!!.startAnimation(heightAnim)
            mHorizontalPlusLine!!.postOnAnimation {
                val handler = Handler()
                handler.postDelayed({
                    mHorizontalPlusLine!!.visibility = View.INVISIBLE
                    val goalWidthChooseOptionsLayout = converter.convertToInt(210, activity)
                    val widthAnim = ResizeWidthAnimation(mChoosePhotoOptionsLayout, goalWidthChooseOptionsLayout)
                    widthAnim.duration = 500
                    mChoosePhotoOptionsLayout!!.startAnimation(widthAnim)
                }, 300)
            }

            isPhotoOptionsOpened = true
        } else if (isPhotoOptionsOpened) {

            // closes linear layout with buttons inside
            val goalWidthChooseOptionsLayout = 0
            val widthAnim = ResizeWidthAnimation(mChoosePhotoOptionsLayout, goalWidthChooseOptionsLayout)
            widthAnim.duration = 500
            mChoosePhotoOptionsLayout!!.startAnimation(widthAnim)

            // manages changing shape and size of a PLUS button to it's normal size
            mChoosePhotoOptionsLayout!!.postOnAnimation {
                val handler = Handler()
                handler.postDelayed({
                    mHorizontalPlusLine!!.visibility = View.VISIBLE
                    val goalMarginPlusButton = converter.convertToInt(16, activity)
                    val startMarginPlusButton = converter.convertToInt(50, activity)

                    val varl = ValueAnimator.ofInt(startMarginPlusButton, goalMarginPlusButton)
                    varl.duration = 500
                    varl.addUpdateListener { animation ->
                        val lp = mHorizontalPlusLine!!.layoutParams as RelativeLayout.LayoutParams
                        lp.setMargins(0, 0, 0, animation.animatedValue as Int)
                        mHorizontalPlusLine!!.layoutParams = lp
                        Log.e("Animation DOWN", "MARGIN BOTTOM: " + animation.animatedValue)
                    }
                    varl.start()

                    val goalWidthPlusButton = converter.convertToInt(30, activity)
                    val heightAnim = ResizeHeightAnimation(mHorizontalPlusLine!!, goalWidthPlusButton)
                    heightAnim.duration = 500
                    mHorizontalPlusLine!!.startAnimation(heightAnim)
                }, 300)
            }
            isPhotoOptionsOpened = false
        }
    }

    init {
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        val view = inflater.inflate(R.layout.fragment_choose_photo, container, false)
        ButterKnife.bind(this, view)

        setHasOptionsMenu(true)

        mButtonToOpenButtons!!.setOnClickListener(plusButtonListener)

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE)

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique
        }

        mManager = RecipeManager.instance

        // set currently opened fragment as CHOOSE DOC LAYOUT FRAGMENT
        mManager.setCurrentFragment(ConstantsForFragmentsSelection.CHOOSE_PHOTO_FRAGMENT)

        mChoosePhotoButton!!.setOnClickListener {
            val i = Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            startActivityForResult(i, RESULT_LOAD_IMAGE)
        }

        mTakePhotoButton!!.setOnClickListener {
            val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST)
        }

        if (mManager.currentOrCreateNewRecipe != null && mManager.currentOrCreateNewRecipe.title != null) {
            if (GeneralDataManager.getInstance().isRecipeInTheDatabase(mManager.currentOrCreateNewRecipe.title)) {
                //TODO: W tym miejscu będziemy pobierać zdjęcie z bazy danych i wyświetlać je w imageview
            }

        } else if (mManager.currentOrCreateNewRecipe != null && mManager.photoUri != null) {
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val applicationContext = MainActivity.contextOfApplication

            val cursor = applicationContext.contentResolver.query(mManager.photoUri!!,
                    filePathColumn, null, null, null)
            cursor!!.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            mImgView!!.setImageBitmap(BitmapFactory.decodeFile(picturePath))
        }

        mNextFragmentButton!!.setOnClickListener { openNextFragment(RecipeSummaryFragment()) }

        mImageButtonCancelPhoto!!.setOnClickListener {
            mImgView!!.setImageBitmap(null)
            mManager.photoUri = null
            mNextFragmentButton!!.isEnabled = false
            mImageButtonCancelPhoto!!.isEnabled = false
            RecipeManager.instance.picturePath = null
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_question, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.help_icon -> {
                val toast = Toast.makeText(context, "Select a photo and press 'ADD' button to move to the next step", Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
                toast.show()
                return super.onOptionsItemSelected(item)
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            val applicationContext = MainActivity.contextOfApplication

            val cursor = applicationContext.contentResolver.query(selectedImage!!,
                    filePathColumn, null, null, null)
            cursor!!.moveToFirst()

            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
            val picturePath = cursor.getString(columnIndex)
            cursor.close()

            // saves a path of an image to RecipeManager, so the photo can be used while creating a recipe part
            RecipeManager.instance.picturePath = picturePath
            Log.e("Photo from gallery", picturePath)

            mImgView!!.setImageBitmap(BitmapFactory.decodeFile(picturePath))
            mManager.photoUri = selectedImage
            mNextFragmentButton!!.isEnabled = true
            mImageButtonCancelPhoto!!.isEnabled = true
        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val photo = data!!.extras!!.get("data") as Bitmap
            mImgView!!.setImageBitmap(photo)

            val iE = ImageEditor.getInstance()

            val imgUri = getImageUri(activity.applicationContext, photo)
            mManager.photoUri = imgUri
            mNextFragmentButton!!.isEnabled = true
            mImageButtonCancelPhoto!!.isEnabled = true
            val picturePath = getRealPathFromURI(imgUri)
            RecipeManager.instance.picturePath = picturePath
            Log.e("Photo taken path", picturePath)
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = activity.contentResolver.query(uri, null, null, null, null)
        cursor!!.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        return cursor.getString(idx)
    }

    private fun openNextFragment(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {

        private val RESULT_LOAD_IMAGE = 1111
        private val CAMERA_REQUEST = 22222
        private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 333333
    }
}
