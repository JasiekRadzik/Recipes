package com.example.radzik.recipes.fragment;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.radzik.recipes.R;
import com.example.radzik.recipes.activity.MainActivity;
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection;
import com.example.radzik.recipes.database.RecipeManager;
import com.example.radzik.recipes.database.firebase.GeneralDataManager;
import com.example.radzik.recipes.utils.ImageEditor;
import com.example.radzik.recipes.utils.ResizeHeightAnimation;
import com.example.radzik.recipes.utils.ResizeWidthAnimation;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Radzik on 05.09.2017.
 */

public class ChoosePhotoFragment extends Fragment {

    @BindView(R.id.imageViewDisplayImage)
    ImageView mImgView;

    @BindView(R.id.imageButtonTakeAPhoto)
    ImageButton mTakePhotoButton;

    @BindView(R.id.imageButtonChooseFromGallery)
    ImageButton mChoosePhotoButton;

    @BindView(R.id.buttonChooseStyleFragment)
    Button mNextFragmentButton;

    @BindView(R.id.imageButtonCancelPhoto)
    ImageButton mImageButtonCancelPhoto;

    @BindView(R.id.horizontalPlusLine)
    RelativeLayout mHorizontalPlusLine;

    @BindView(R.id.verticalPlusLine)
    RelativeLayout mVerticalPlusLine;

    @BindView(R.id.buttonToOpenButtons)
    Button mButtonToOpenButtons;

    @BindView(R.id.relativeLayoutPhotoOptions)
    RelativeLayout mChoosePhotoOptionsLayout;

    private boolean isPhotoOptionsOpened = false;

    RecipeManager mManager;

    private static int RESULT_LOAD_IMAGE = 1111;
    private static final int CAMERA_REQUEST = 22222;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 333333;

    public ChoosePhotoFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_choose_photo, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        mButtonToOpenButtons.setOnClickListener(plusButtonListener);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Explain to the user why we need to read the contacts
            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
            // app-defined int constant that should be quite unique
        }

        mManager = RecipeManager.getInstance();

        // set currently opened fragment as CHOOSE DOC LAYOUT FRAGMENT
        mManager.setCurrentFragment(ConstantsForFragmentsSelection.CHOOSE_PHOTO_FRAGMENT);

        mChoosePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        mTakePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        if(mManager.getCurrentOrCreateNewRecipe() != null && mManager.getCurrentOrCreateNewRecipe().getTitle() != null) {
            if(GeneralDataManager.getInstance().isRecipeInTheDatabase(mManager.getCurrentOrCreateNewRecipe().getTitle())) {
                //TODO: W tym miejscu będziemy pobierać zdjęcie z bazy danych i wyświetlać je w imageview
            }

        } else if(mManager.getCurrentOrCreateNewRecipe() != null && mManager.getPhotoUri() != null) {
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Context applicationContext = MainActivity.getContextOfApplication();

            Cursor cursor = applicationContext.getContentResolver().query(mManager.getPhotoUri(),
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            mImgView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
        }

        mNextFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextFragment(new RecipeSummaryFragment());
            }
        });

        mImageButtonCancelPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mImgView.setImageBitmap(null);
                mManager.setPhotoUri(null);
                mNextFragmentButton.setEnabled(false);
                mImageButtonCancelPhoto.setEnabled(false);
                RecipeManager.getInstance().setPicturePath(null);
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_question, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help_icon:
                Toast toast = Toast.makeText(getContext(), "Select a photo and press 'ADD' button to move to the next step", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Context applicationContext = MainActivity.getContextOfApplication();
            
            Cursor cursor = applicationContext.getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            // saves a path of an image to RecipeManager, so the photo can be used while creating a recipe part
            RecipeManager.getInstance().setPicturePath(picturePath);
            Log.e("Photo from gallery", picturePath);

            mImgView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            mManager.setPhotoUri(selectedImage);
            mNextFragmentButton.setEnabled(true);
            mImageButtonCancelPhoto.setEnabled(true);
        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            mImgView.setImageBitmap(photo);

            ImageEditor iE = ImageEditor.getInstance();

            Uri imgUri = getImageUri(getActivity().getApplicationContext(), photo);
            mManager.setPhotoUri(imgUri);
            mNextFragmentButton.setEnabled(true);
            mImageButtonCancelPhoto.setEnabled(true);
            String picturePath = getRealPathFromURI(imgUri);
            RecipeManager.getInstance().setPicturePath(picturePath);
            Log.e("Photo taken path", picturePath);
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    private void openNextFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    View.OnClickListener plusButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!isPhotoOptionsOpened) {
                int goalMarginPlusButton = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
                int startMarginPlusButton = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());

                ValueAnimator varl = ValueAnimator.ofInt(startMarginPlusButton, goalMarginPlusButton);
                varl.setDuration(500);
                varl.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {

                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHorizontalPlusLine.getLayoutParams();
                        lp.setMargins( 0, 0, 0, (Integer) animation.getAnimatedValue());
                        mHorizontalPlusLine.setLayoutParams(lp);
                        Log.e("Animation UP", "MARGIN BOTTOM: " + animation.getAnimatedValue());
                    }
                });
                varl.start();

                int goalWidthPlusButton = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());
                ResizeHeightAnimation heightAnim = new ResizeHeightAnimation(mHorizontalPlusLine, goalWidthPlusButton);
                heightAnim.setDuration(500);
                mHorizontalPlusLine.startAnimation(heightAnim);
                mHorizontalPlusLine.postOnAnimation(new Runnable() {
                    @Override
                    public void run() {

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mHorizontalPlusLine.setVisibility(View.INVISIBLE);
                                int goalWidthChooseOptionsLayout = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 210, getResources().getDisplayMetrics());
                                ResizeWidthAnimation widthAnim = new ResizeWidthAnimation(mChoosePhotoOptionsLayout, goalWidthChooseOptionsLayout);
                                widthAnim.setDuration(500);
                                mChoosePhotoOptionsLayout.startAnimation(widthAnim);
                            }
                        }, 300);


                    }
                });

                isPhotoOptionsOpened = true;

            }
            else if(isPhotoOptionsOpened) {

                // closes linear layout with buttons inside
                int goalWidthChooseOptionsLayout = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
                ResizeWidthAnimation widthAnim = new ResizeWidthAnimation(mChoosePhotoOptionsLayout, goalWidthChooseOptionsLayout);
                widthAnim.setDuration(500);
                mChoosePhotoOptionsLayout.startAnimation(widthAnim);

                // manages changing shape and size of a PLUS button to it's normal size
                mChoosePhotoOptionsLayout.postOnAnimation(new Runnable() {
                    @Override
                    public void run() {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mHorizontalPlusLine.setVisibility(View.VISIBLE);
                                int goalMarginPlusButton = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
                                int startMarginPlusButton = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

                                ValueAnimator varl = ValueAnimator.ofInt(startMarginPlusButton, goalMarginPlusButton);
                                varl.setDuration(500);
                                varl.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator animation) {

                                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHorizontalPlusLine.getLayoutParams();
                                        lp.setMargins( 0, 0, 0, (Integer) animation.getAnimatedValue());
                                        mHorizontalPlusLine.setLayoutParams(lp);
                                        Log.e("Animation DOWN", "MARGIN BOTTOM: " + animation.getAnimatedValue());
                                    }
                                });
                                varl.start();

                                int goalWidthPlusButton = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
                                ResizeHeightAnimation heightAnim = new ResizeHeightAnimation(mHorizontalPlusLine, goalWidthPlusButton);
                                heightAnim.setDuration(500);
                                mHorizontalPlusLine.startAnimation(heightAnim);
                            }
                        }, 300);
                    }
                });
                isPhotoOptionsOpened = false;
            }


        }
    };
}
