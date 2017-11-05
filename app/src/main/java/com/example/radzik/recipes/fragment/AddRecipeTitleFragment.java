package com.example.radzik.recipes.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.airnauts.toolkit.utils.KeyboardUtils;
import com.example.radzik.recipes.R;
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection;
import com.example.radzik.recipes.database.CookBook;
import com.example.radzik.recipes.database.Recipe;
import com.example.radzik.recipes.database.RecipeManager;
import com.example.radzik.recipes.database.firebase.GeneralDataManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Radzik on 11.09.2017.
 */

// TODO: spinner for cookbook titles and to create a new cookbook
    // todo: dialog to put a new cookbook title

// TODO: spinner for recipe course type
// TODO: spinner for style of a document

public class AddRecipeTitleFragment extends Fragment {

    @BindView(R.id.edit_text_title)
    EditText mEditTextRecipeTitle;

    //@BindView(R.id.)
    //EditText mEditTextRecipeSubheading;

    @BindView(R.id.addRecipeTitleCookbookSpinner)
    Spinner mCookBookSpinner;

    private boolean mIsCookBookSpinnerTouched = false;
    private boolean mIsNewCookBookCreated = false;
    private CookBook mLastCookBookCreated;

    @BindView(R.id.addRecipeCourseTypeCookbookSpinner)
    Spinner mCourseTypeSpinner;

    @BindView(R.id.buttonChooseStyleFragment)
    Button mChooseStyleFragmentButton;

    RecipeManager mRecipeManager;
    GeneralDataManager mGeneralDataManager;

    private HashMap<CookBook, ArrayList<Recipe>> mCookBooksAndRecipesMap;
    private HashMap<String, String> mCurrentUserCookBookKeyTitleMap;
    private List<String> mCookBooksTitles;
    private String mCookBookTitle = "";
    private String mCookBookUploadKey;

    private List<String> mCourseTypeList;

    public AddRecipeTitleFragment() {
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_recipe_title, container, false);
        ButterKnife.bind(this, view);

        // used to change menu on fragment change
        setHasOptionsMenu(true);

        // sets listener, so that when you click outside the edit text it will hide the keyboard
        setupUI(view);

        // set currently opened fragment as CHOOSE DOC LAYOUT FRAGMENT
        RecipeManager.getInstance().setCurrentFragment(ConstantsForFragmentsSelection.TITLE_FRAGMENT);

        // sets up data managers
        mRecipeManager = RecipeManager.getInstance();
        mGeneralDataManager = GeneralDataManager.getInstance();

        // sets up cookBookTitles array
        mCookBooksTitles = new ArrayList<>();
        mCookBooksTitles.add(0, "new CookBook");
        mCurrentUserCookBookKeyTitleMap = mGeneralDataManager.getCurrentUserCookBookKeyTitleMap();
        for(String key : mCurrentUserCookBookKeyTitleMap.keySet()) {
            mCookBooksTitles.add(mCurrentUserCookBookKeyTitleMap.get(key));
        }

        mChooseStyleFragmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEditTextRecipeTitle.getText().toString().length() == 0) {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_no_recipe_title), Toast.LENGTH_SHORT).show();
                    mEditTextRecipeTitle.requestFocus();
                    KeyboardUtils.show(mEditTextRecipeTitle);
                } else if(mEditTextRecipeTitle.getText().toString().length() < 3) {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_recipe_title_too_short), Toast.LENGTH_SHORT).show();
                    mEditTextRecipeTitle.requestFocus();
                    KeyboardUtils.show(mEditTextRecipeTitle);
                } else if(mEditTextRecipeTitle.getText().toString().length() >= 3 && mGeneralDataManager.isRecipeInTheDatabase(mEditTextRecipeTitle.getText().toString())) {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.toast_recipe_is_in_database), Toast.LENGTH_SHORT).show();
                    mEditTextRecipeTitle.requestFocus();
                    KeyboardUtils.show(mEditTextRecipeTitle);
                } else if(mEditTextRecipeTitle.getText().toString().length() >= 3 && !mGeneralDataManager.isRecipeInTheDatabase(mEditTextRecipeTitle.getText().toString())) {
                    openNextFragment(new WriteRecipeFragment());
                }
            }
        });

        // sets up listeners updating cookbooks and recipes inside cookbooks
        GeneralDataManager.getInstance().setOnCookBookUploadedListener(cookBookTitlesUpdateListener);
        GeneralDataManager.getInstance().setOnRecipesUpdateListener(recipesUpdateListener);

        // sets up cookbooks spinner
        ArrayAdapter<String> cookBooksTitlesAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, mCookBooksTitles);
        mCookBookSpinner.setAdapter(cookBooksTitlesAdapter);
        mCookBookSpinner.post(new Runnable() {
            @Override
            public void run() {
                mCookBookSpinner.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        mIsCookBookSpinnerTouched = true;
                        return false;
                    }
                });

                mCookBookSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(mIsCookBookSpinnerTouched) {
                            if(position == 0) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                                alertDialog.setTitle("Create CookBook");
                                alertDialog.setMessage("Title must contain minimum 3 letters");

                                final EditText input = new EditText(getActivity());
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                input.setLayoutParams(lp);
                                alertDialog.setView(input);

                                alertDialog.setPositiveButton("YES",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                mCookBookTitle = input.getText().toString();
                                                if (mCookBookTitle.isEmpty() || mCookBookTitle.length() < 3) {
                                                    Toast.makeText(getContext(), "Title too short", Toast.LENGTH_SHORT).show();
                                                    mCookBookSpinner.setSelected(false);
                                                } else if (mCookBooksTitles.contains(mCookBookTitle)) {
                                                    Toast.makeText(getContext(), "There already is a CookBook with such title!", Toast.LENGTH_LONG).show();
                                                    mCookBookSpinner.setSelected(false);
                                                } else {
                                                    mLastCookBookCreated = new CookBook();
                                                    mLastCookBookCreated.setTitle(mCookBookTitle);
                                                    mCookBookUploadKey = mGeneralDataManager.uploadCookBook(getActivity(), mLastCookBookCreated);
                                                    mRecipeManager.setCurrentCookBookKey(mCookBookUploadKey);
                                                    mCookBookSpinner.setSelected(false);
                                                    mIsNewCookBookCreated = true;
                                                }
                                            }
                                        });

                                alertDialog.setNegativeButton("NO",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });

                                alertDialog.show();

                            } else {
                                Log.e("POSITION: ", "selected " + position);

                                String title = mCookBooksTitles.get(position);
                                mCookBookUploadKey = GeneralDataManager.getInstance().getCurrentUserCookBookKey(title);
                                mRecipeManager.setCurrentCookBookKey(mCookBookUploadKey);
                                mRecipeManager.setCurrentCookBookTitle(title);
                                mCookBookSpinner.setSelection(position, true);
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        mCookBookSpinner.clearFocus();
                        mCookBookSpinner.setSelected(false);
                    }
                });
            }
        });




        // sets up Course Type Spinner data
        mCourseTypeList = RecipeManager.getInstance().getAllCourseTypesArray(getActivity());

        ArrayAdapter<String> courseTypeAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, mCourseTypeList);
        mCourseTypeSpinner.setAdapter(courseTypeAdapter);

        if(RecipeManager.getInstance().getCurrentOrCreateNewRecipe().getCourseType() != 99) {
            mCourseTypeSpinner.setSelection(RecipeManager.getInstance().getCurrentOrCreateNewRecipe().getCourseType());
        }

        // sets up course type spinner
        mCourseTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mRecipeManager.getCurrentOrCreateNewRecipe().setCourseType(position);
                mCourseTypeSpinner.setSelection(position, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCourseTypeSpinner.clearFocus();
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_question, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    mEditTextRecipeTitle.clearFocus();
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    GeneralDataManager.OnCookBookUploadedListener cookBookTitlesUpdateListener = new GeneralDataManager.OnCookBookUploadedListener() {
        @Override
        public void OnCookBookUploaded(HashMap<String, String> cookBooksMap, String cookBookTitle) {
            mCurrentUserCookBookKeyTitleMap = cookBooksMap;
            mCookBooksTitles = new ArrayList<>();
            mCookBooksTitles.add(0, "new CookBook");

            mCurrentUserCookBookKeyTitleMap = mGeneralDataManager.getCurrentUserCookBookKeyTitleMap();
            for(String key : mCurrentUserCookBookKeyTitleMap.keySet()) {
                mCookBooksTitles.add(mCurrentUserCookBookKeyTitleMap.get(key));
            }

            ArrayAdapter<String> cookBooksTitlesAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, mCookBooksTitles);
            mCookBookSpinner.setAdapter(cookBooksTitlesAdapter);

            if(mIsNewCookBookCreated) {
                for(int i = 0; i < mCookBooksTitles.size() - 1; i++) {
                    if(mCookBooksTitles.get(i).equals(mLastCookBookCreated.getTitle())) {
                        mCookBookSpinner.setSelection(i);
                        mIsNewCookBookCreated = false;
                        mLastCookBookCreated = null;
                    }
                }
            }
        }
    };

    // sets listener for MAP
    GeneralDataManager.OnRecipesUpdateListener recipesUpdateListener = new GeneralDataManager.OnRecipesUpdateListener() {
        public void onMapChanged(HashMap<CookBook, ArrayList<Recipe>> map) {
            mCookBooksAndRecipesMap = map;

            mCookBooksAndRecipesMap = mGeneralDataManager.getRecipesInCookBooksMap();
            for(CookBook cookBook : mCookBooksAndRecipesMap.keySet()) {
                mCookBooksTitles.add(cookBook.getTitle());
            }
        }
    };

    private void openNextFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
