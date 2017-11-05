package com.example.radzik.recipes.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.radzik.recipes.database.ConstantsForRecipeDocumentStyles;
import com.example.radzik.recipes.database.RecipeManager;

/**
 * Created by Radzik on 23.10.2017.
 */

public class ImageEditor {

    private static ImageEditor mInstance = null;

    public static synchronized ImageEditor getInstance() {
        if(mInstance == null) {
            mInstance = new ImageEditor();
        }

        return mInstance;
    }

    public ImageEditor() {}

    public Bitmap editImage(String pathName) {
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
        myOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(pathName);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);


        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawCircle(60, 50, 25, paint);


        /*ImageView imageView = (ImageView)findViewById(R.id.schoolboard_image_view);
        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(mutableBitmap); */

        return mutableBitmap;
    }

    public Bitmap resizeImageFromPath(String picturePath, int density, Activity activity) {
        Bitmap yourBitmap = BitmapFactory.decodeFile(picturePath);
        PhotosIdCreatorUtils pM = PhotosIdCreatorUtils.getInstance();

        Bitmap resized = Bitmap.createScaledBitmap(yourBitmap, pM.dpToPx(density, activity), pM.dpToPx(density, activity), true);

        return resized;
    }

    public Bitmap resizeImageFromBitmap(Bitmap bitmap, int density, Activity activity) {
        Bitmap yourBitmap = bitmap;
        PhotosIdCreatorUtils pM = PhotosIdCreatorUtils.getInstance();

        Bitmap resized = Bitmap.createScaledBitmap(yourBitmap, pM.dpToPx(density, activity), pM.dpToPx(density, activity), true);

        return resized;
    }

    public Bitmap resizeImageFromPathInPX(String picturePath, int pixels) {
        Bitmap yourBitmap = BitmapFactory.decodeFile(picturePath);
        PhotosIdCreatorUtils pM = PhotosIdCreatorUtils.getInstance();

        Bitmap resized = Bitmap.createScaledBitmap(yourBitmap, pixels, pixels, true);

        return resized;
    }

    public Bitmap resizeImageFromBitmapInPX(Bitmap bitmap, int pixels) {
        Bitmap yourBitmap = bitmap;
        PhotosIdCreatorUtils pM = PhotosIdCreatorUtils.getInstance();

        Bitmap resized = Bitmap.createScaledBitmap(yourBitmap, pixels, pixels, true);

        return resized;
    }

}
