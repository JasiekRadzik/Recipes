package com.example.radzik.recipes.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

import com.example.radzik.recipes.database.ConstantsForRecipeDocumentStyles
import com.example.radzik.recipes.database.RecipeManager

/**
 * Created by Radzik on 23.10.2017.
 */

class ImageEditor {

    fun editImage(pathName: String): Bitmap {
        val myOptions = BitmapFactory.Options()
        myOptions.inDither = true
        myOptions.inScaled = false
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888// important
        myOptions.inPurgeable = true

        val bitmap = BitmapFactory.decodeFile(pathName)
        val paint = Paint()
        paint.isAntiAlias = true

        val workingBitmap = Bitmap.createBitmap(bitmap)
        val mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true)


        val canvas = Canvas(mutableBitmap)
        canvas.drawCircle(60f, 50f, 25f, paint)


        /*ImageView imageView = (ImageView)findViewById(R.id.schoolboard_image_view);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(mutableBitmap); */

        return mutableBitmap
    }

    fun resizeImageFromPath(picturePath: String, density: Int, activity: Activity): Bitmap {
        val yourBitmap = BitmapFactory.decodeFile(picturePath)
        val pM = PhotosIdCreatorUtils.instance

        return Bitmap.createScaledBitmap(yourBitmap, pM.dpToPx(density, activity), pM.dpToPx(density, activity), true)
    }

    fun resizeImageFromBitmap(bitmap: Bitmap, density: Int, activity: Activity): Bitmap {
        val pM = PhotosIdCreatorUtils.instance

        return Bitmap.createScaledBitmap(bitmap, pM.dpToPx(density, activity), pM.dpToPx(density, activity), true)
    }

    fun resizeImageFromPathInPX(picturePath: String, pixels: Int): Bitmap {
        val yourBitmap = BitmapFactory.decodeFile(picturePath)
        val pM = PhotosIdCreatorUtils.instance

        return Bitmap.createScaledBitmap(yourBitmap, pixels, pixels, true)
    }

    fun resizeImageFromBitmapInPX(bitmap: Bitmap, pixels: Int): Bitmap {
        val pM = PhotosIdCreatorUtils.instance

        return Bitmap.createScaledBitmap(bitmap, pixels, pixels, true)
    }

    companion object {

        private var mInstance: ImageEditor? = null

        val instance: ImageEditor
            @Synchronized get() {
                if (mInstance == null) {
                    mInstance = ImageEditor()
                }

                return mInstance
            }
    }

}
