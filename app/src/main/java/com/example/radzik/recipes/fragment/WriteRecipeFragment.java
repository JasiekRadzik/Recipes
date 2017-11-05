package com.example.radzik.recipes.fragment;


import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airnauts.toolkit.utils.KeyboardUtils;
import com.example.radzik.recipes.R;
import com.example.radzik.recipes.activity.MainActivity;
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection;
import com.example.radzik.recipes.database.ConstantsForRecipePartTypes;
import com.example.radzik.recipes.database.EditTextPref;
import com.example.radzik.recipes.database.EditTextPrefManager;
import com.example.radzik.recipes.database.RecipeManager;
import com.example.radzik.recipes.utils.IdCreatorUtils;
import com.jmedeisis.draglinearlayout.DragLinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by Radzik on 31.07.2017.
 */

public class WriteRecipeFragment extends Fragment {

    @BindView(R.id.main_scroll_view)
    ScrollView mMainScrollView;

    DragLinearLayout mDragLinearLayout;

    @BindView(R.id.button_add)
    Button mAddButton;

    @BindView(R.id.hidden_layout_edit_part)
    RelativeLayout mHiddenLayoutEditPart;

    @BindView(R.id.mainEditText)
    EditText mMainEditText;

    @BindView(R.id.layoutWithTextTypesButtons)
    LinearLayout mLayoutWithTextTypesButtons;

    private RelativeLayout mLayoutClickedToEdit;

    private static final int REQ_CODE_SPEECH_INPUT = 100;

    private RecipeManager mManager;
    private EditTextPrefManager mETPManager;
    private TextToSpeech mTextToSpeech;
    private EditTextPref mEditTextPref;

    private boolean mIsTextViewOpenedToEdit;

    private Animation mBottomUpAnim;
    private Animation mBottomDownAnim;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write, container, false);
        ButterKnife.bind(this, view);

        setHasOptionsMenu(true);

        mBottomUpAnim = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up_recipe_edit_view);
        mBottomDownAnim = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_down_recipe_edit_view);

        mDragLinearLayout = (DragLinearLayout) view.findViewById(R.id.drag_linear_layout_container);

        mDragLinearLayout.setContainerScrollView(mMainScrollView);
        mDragLinearLayout.setLongClickable(true);
        mDragLinearLayout.setOnViewSwapListener(dragListener);

        mAddButton.setOnClickListener(onADDClickListener);

        mManager = RecipeManager.getInstance();
        mETPManager = EditTextPrefManager.getInstance();

        // set currently opened fragment as CHOOSE DOC LAYOUT FRAGMENT
        mManager.setCurrentFragment(ConstantsForFragmentsSelection.WRITE_RECIPE_FRAGMENT);

        mIsTextViewOpenedToEdit = false;

        mTextToSpeech = new TextToSpeech(MainActivity.getContextOfApplication(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mTextToSpeech.setLanguage(Locale.UK);
            }
        });

        // binds listeners to buttons
        bindButtonsListenerAndSetSelectedFalse();

        if(mManager.getCurrentOrCreateNewRecipe() != null && mManager.getCurrentOrCreateNewRecipe().getList().size() != 0) {
            ArrayList<EditTextPref> list = mManager.getCurrentOrCreateNewRecipe().getList();
            for (int i = list.size() - 1; i >= 0; i--) {
                recreateTextViewsFromRecipe(i);
            }
        } else if(mManager.getCurrentOrCreateNewRecipe() != null && mManager.getCurrentOrCreateNewRecipe().getList().size() == 0) {
            mManager.getCurrentOrCreateNewRecipe().setMapForEditTextPrefs(new HashMap<String, EditTextPref>());
        }

        return view;
    }

    View.OnClickListener onADDClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (mMainScrollView.getVisibility() == View.VISIBLE) {

                mAddButton.setEnabled(false);
                mAddButton.setText(getResources().getString(R.string.button_bottom_save_edited_recipe_part));

                // hides layout containing textViews and shows layout for create recipe
                mHiddenLayoutEditPart.startAnimation(mBottomUpAnim);
                mHiddenLayoutEditPart.setVisibility(View.VISIBLE);
                mMainScrollView.setVisibility(View.GONE);

                // clears mMainEditText so that we can create another part of a recipe
                mMainEditText.setText("");
                mMainEditText.addTextChangedListener(txtWatcher);

                // creates object to store information about typeface, size etc.
                mEditTextPref = new EditTextPref();

                for (int i = 0; i < mLayoutWithTextTypesButtons.getChildCount() - 1; i++) {
                    Button button = (Button) mLayoutWithTextTypesButtons.getChildAt(i);
                    button.setSelected(false);
                }

                mMainEditText.requestFocus();
                KeyboardUtils.show(mMainEditText);

                // configures button which closes this window
                ImageButton buttonClear = (ImageButton) mHiddenLayoutEditPart.findViewById(R.id.button_clear);
                buttonClear.setOnClickListener(buttonClearListener);

            } else if (mMainScrollView.getVisibility() == View.GONE && mIsTextViewOpenedToEdit) {

                KeyboardUtils.hide(mMainEditText);

                // saves text into EditTextPref object
                mEditTextPref.setText(mMainEditText.getText().toString());

                TextView txtViewContent = (TextView) mLayoutClickedToEdit.findViewById(R.id.textViewContent);
                TextView txtViewType = (TextView) mLayoutClickedToEdit.findViewById(R.id.textViewType);
                txtViewContent.setText(mEditTextPref.getText().toString());
                singleButtonChecker();
                txtViewType.setText(mEditTextPref.getRecipePartName());

                // hides layout for create recipe and shows layout containing textViews
                mHiddenLayoutEditPart.startAnimation(mBottomDownAnim);
                mHiddenLayoutEditPart.postOnAnimation(new Runnable() {
                    @Override
                    public void run() {
                        mHiddenLayoutEditPart.setVisibility(View.GONE);
                    }
                });
                mMainScrollView.setVisibility(View.VISIBLE);

                mAddButton.setText(getResources().getString(R.string.button_bottom_add_recipe_part));

                mIsTextViewOpenedToEdit = false;

            } else {

                KeyboardUtils.hide(mMainEditText);

                // saves text into EditTextPref object
                mEditTextPref.setText(mMainEditText.getText().toString());

                // adds EditTextPref object to the list inside Recipe object, which contains all parts of the recipe
                EditTextPref x;
                x = mEditTextPref;
                mManager.getCurrentOrCreateNewRecipe().getList().add(x);

                // resets mEditTextPref so it is empty for another part of the recipe
                mEditTextPref = null;

                createTextViewWithEditTextPref();

                // hides layout for create recipe and shows layout containing textViews
                mHiddenLayoutEditPart.startAnimation(mBottomDownAnim);
                mHiddenLayoutEditPart.postOnAnimation(new Runnable() {
                    @Override
                    public void run() {
                        mHiddenLayoutEditPart.setVisibility(View.GONE);
                    }
                });
                mMainScrollView.setVisibility(View.VISIBLE);


                mAddButton.setText(getResources().getString(R.string.button_bottom_add_recipe_part));

            }
        }
    };

    // listening to buttons in recipe edition
    View.OnClickListener recipeButtonsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.button_how_to_cook:
                    unselectEditRecipeTextStyleButtons();
                    mEditTextPref.setRecipePartType(ConstantsForRecipePartTypes.HOW_TO_COOK);
                    mETPManager.translateTypeToRecipePartName(mEditTextPref, getActivity());
                    ((Button) v).setSelected(true);
                    break;

                case R.id.ingredient:
                    unselectEditRecipeTextStyleButtons();
                    mEditTextPref.setRecipePartType(ConstantsForRecipePartTypes.INGREDIENT);
                    mETPManager.translateTypeToRecipePartName(mEditTextPref, getActivity());
                    ((Button) v).setSelected(true);
                    break;

                case R.id.button_short_description:
                    unselectEditRecipeTextStyleButtons();
                    mEditTextPref.setRecipePartType(ConstantsForRecipePartTypes.SHORT_DESCRIPTION);
                    mETPManager.translateTypeToRecipePartName(mEditTextPref, getActivity());
                    ((Button) v).setSelected(true);
                    break;

                case R.id.button_to_eat_with:
                    unselectEditRecipeTextStyleButtons();
                    mEditTextPref.setRecipePartType(ConstantsForRecipePartTypes.ADDITION_TO_EAT_WITH);
                    mETPManager.translateTypeToRecipePartName(mEditTextPref, getActivity());
                    ((Button) v).setSelected(true);
                    break;

                case R.id.buttonEditPartRefresh:
                    mMainEditText.setText("");
                    break;

                case R.id.buttonEditPartCopyTextToClipboard:
                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Edit Part text", mMainEditText.getText().toString());
                    Toast.makeText(getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                    clipboard.setPrimaryClip(clip);
                    break;

                case R.id.buttonEditPartTTS:
                    String speak = "";
                    try {
                        speak = mMainEditText.getText().toString();
                    } catch (NullPointerException e) {
                        speak = "";
                    }

                    mTextToSpeech.speak(speak, TextToSpeech.QUEUE_FLUSH, null);
                    break;

                case R.id.buttonEditPartVoiceWrite:
                    startVoiceInput();
                    break;

            }
        }
    };

    // attached to every single layout inside mDragLinearLayout, so it can be deleted
    View.OnClickListener removeTextViewButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            final PopupMenu popup = new PopupMenu(getContext(), v);

            //Inflating the Popup using xml file
            popup.getMenuInflater()
                    .inflate(R.menu.menu_popup_confirm_delete, popup.getMenu());

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.yes_i_am_sure:
                            // 1 step:  we have to find EditTextPref responding to this TextView
                            RelativeLayout relative = (RelativeLayout) v.getParent();
                            LinearLayout linear = (LinearLayout) relative.getChildAt(0);
                            TextView hiddenID = (TextView) linear.getChildAt(linear.getChildCount() - 1);
                            String id = hiddenID.getText().toString();

                            // 2 step: we remove this EditTextPref from ArrayList and then we remove it from hashMap
                            mManager.getCurrentOrCreateNewRecipe().getList().remove(mManager.getCurrentOrCreateNewRecipe().getMapForEditTextPrefs().get(id));
                            mManager.getCurrentOrCreateNewRecipe().getMapForEditTextPrefs().remove(id);

                            // 3 step: we remove this view from the layout which contains all views
                            ((ViewManager) mDragLinearLayout).removeView(relative);
                            break;
                        case R.id.no_i_am_not:
                            break;
                    }
                    return true;
                }
            });

            popup.show(); //showing popup menu
        }
    };

    View.OnClickListener buttonClearListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // hides layout for create recipe and shows layout containing textViews
            mHiddenLayoutEditPart.startAnimation(mBottomDownAnim);
            mHiddenLayoutEditPart.postOnAnimation(new Runnable() {
                @Override
                public void run() {
                    mHiddenLayoutEditPart.setVisibility(View.GONE);
                }
            });
            mMainScrollView.setVisibility(View.VISIBLE);

            mAddButton.setText(getResources().getString(R.string.button_bottom_add_recipe_part));
            KeyboardUtils.hide(mMainEditText);
            mMainEditText.clearFocus();
            mAddButton.setEnabled(true);
        }
    };

    private void bindButtonsListenerAndSetSelectedFalse() {

        for (int i = 0; i < mLayoutWithTextTypesButtons.getChildCount() - 1; i++) {
            Button button = (Button) mLayoutWithTextTypesButtons.getChildAt(i);
            button.setClickable(true);
            button.setSelected(false);
            button.setOnClickListener(recipeButtonsListener);
        }

        LinearLayout linearLayout2 = (LinearLayout) mHiddenLayoutEditPart.findViewById(R.id.layoutWithOtherOptionsButtons);
        for (int i = 0; i < linearLayout2.getChildCount() - 1; i++) {
            linearLayout2.getChildAt(i).setOnClickListener(recipeButtonsListener);
        }
    }
    private void singleButtonChecker() {
        switch (mEditTextPref.getRecipePartType()) {
            case ConstantsForRecipePartTypes.HOW_TO_COOK:
                ((Button) mLayoutWithTextTypesButtons.getChildAt(0)).setSelected(true);
                break;
            case ConstantsForRecipePartTypes.INGREDIENT:
                ((Button) mLayoutWithTextTypesButtons.getChildAt(1)).setSelected(true);
                break;
            case ConstantsForRecipePartTypes.SHORT_DESCRIPTION:
                ((Button) mLayoutWithTextTypesButtons.getChildAt(2)).setSelected(true);
                break;
            case ConstantsForRecipePartTypes.ADDITION_TO_EAT_WITH:
                ((Button) mLayoutWithTextTypesButtons.getChildAt(3)).setSelected(true);
                break;
        }
    }

    // txtWatcher handles disabling and enabling mAddButton depending on the length of editText
    TextWatcher txtWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count != 0) {
                mAddButton.setEnabled(true);
            } else
                mAddButton.setEnabled(false);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    // ----- ----- ----- ----- ----- listener which handles opening textViews with recipeParts ----- ----- ----- ----- ----- //
    View.OnClickListener txtViewOpener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIsTextViewOpenedToEdit = true;
            mLayoutClickedToEdit = (RelativeLayout) v;

            mAddButton.setEnabled(true);
            mAddButton.setText(getResources().getString(R.string.button_bottom_save_edited_recipe_part));

            // hides layout containing textViews and shows layout for create recipe
            mHiddenLayoutEditPart.startAnimation(mBottomUpAnim);
            mHiddenLayoutEditPart.setVisibility(View.VISIBLE);
            mMainScrollView.setVisibility(View.GONE);

            // binds listeners to buttons
            bindButtonsListenerAndSetSelectedFalse();

            // retrieves EditTextPref object by ID from HashMap contained in Recipe object
            String id = ((TextView) v.findViewById(R.id.textViewWithIDHidden)).getText().toString();
            mEditTextPref = mManager.getCurrentOrCreateNewRecipe().getMapForEditTextPrefs().get(id);
            mMainEditText.setText(mEditTextPref.getText());
            singleButtonChecker();

            mMainEditText.requestFocus();
            KeyboardUtils.show(mMainEditText);

            // configures button which closes this window
            ImageButton buttonClear = (ImageButton) mHiddenLayoutEditPart.findViewById(R.id.button_clear);
            buttonClear.setOnClickListener(buttonClearListener);
        }
    };



    public DragLinearLayout getLayoutContainingRecipeParts() {
        return mDragLinearLayout;
    }

    // ----- ----- ----- ----- ----- HANDLES SWAP EVENTS INSIDE DRAG LINEAR LAYOUT ----- ----- ----- ----- ----- //
    DragLinearLayout.OnViewSwapListener dragListener = new DragLinearLayout.OnViewSwapListener() {
        @Override
        public void onSwap(View firstView, int firstPosition, View secondView, int secondPosition) {
            if (mDragLinearLayout.getChildCount() > 1 && firstPosition != secondPosition) {
                ArrayList<EditTextPref> oldList = mManager.getCurrentOrCreateNewRecipe().getList();
                ArrayList<EditTextPref> newList = new ArrayList<>();

                Log.e("Old List", ": " + oldList);

                EditTextPref x = oldList.get(secondPosition);

                oldList.remove(secondPosition);

                for (int i = 0; i < secondPosition; i++) {
                    newList.add(oldList.get(i));
                }

                newList.add(x);

                for (int i = secondPosition; i < oldList.size(); i++) {
                    newList.add(oldList.get(i));
                }

                mManager.getCurrentOrCreateNewRecipe().setList(newList);

                Log.e("Number", "first position: " + firstPosition + ", second position:" + secondPosition);
                Log.e("New list", ": " + newList);
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.top_right_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_icon:
                if(mManager.getCurrentOrCreateNewRecipe().getList().size() != 0) {
                    openNextFragment(new ChoosePhotoFragment());
                } else {
                    Toast.makeText(getContext(), "Recipe can't be empty!", Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.clear_icon:

                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle("");
                alertDialog.setMessage(getResources().getString(R.string.delete_current_recipe_question));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (WriteRecipeFragment.class != null) {
                            getLayoutContainingRecipeParts().removeAllViewsInLayout();
                            RecipeManager.getInstance().createNewRecipe();
                        }
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ----- ----- ----- ----- ----- VOICE RECOGNITION METHODS ----- ----- ----- ----- ----- //
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Log.e("VOICE INPUT", "Problem with voice recording" + a);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mMainEditText.setText(result.get(0));
                }
                break;
            }

        }
    }

    public void onPause(){
        if(mTextToSpeech !=null){
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
        super.onPause();
    }

    private void unselectEditRecipeTextStyleButtons() {
        for (int i = 0; i < mLayoutWithTextTypesButtons.getChildCount() - 1; i++) {
            Button button = (Button) mLayoutWithTextTypesButtons.getChildAt(i);
            button.setSelected(false);
        }
    }

    private void recreateTextViewsFromRecipe(int position) {

        // finds and binds views required to build layout
        final View topSingleView = View.inflate(getContext(), R.layout.single_edit_text_recipe1, null);

        LinearLayout textLayout = (LinearLayout) topSingleView.findViewById(R.id.textViewSingle);
        TextView textViewType = (TextView) textLayout.findViewById(R.id.textViewType);
        TextView textViewContent = (TextView) textLayout.findViewById(R.id.textViewContent);

        // sets margins of a single view
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (8 * scale + 0.5f);
        params1.setMargins(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
        textLayout.setLayoutParams(params1);

        // sets annotation in the top right of single view, describing if it's a normal text, headline etc.
        ArrayList<EditTextPref> list = mManager.getCurrentOrCreateNewRecipe().getList();
        textViewType.setText(list.get(position).getRecipePartName());

        // sets content of a textView
        textViewContent.setText(list.get(position).getText());

        // sets hiddenID to textLayout
        TextView hiddenID = (TextView) textLayout.findViewById(R.id.textViewWithIDHidden);
        hiddenID.setText(list.get(position).getID());

        // manages focus on the last textView
        textViewContent.setFocusable(true);
        textViewContent.requestFocus();

        // adds single view to a DragLinearLayout
        mDragLinearLayout.addDragView(topSingleView, textLayout, mDragLinearLayout.getChildCount());

        // attaches a listener to textLayout
        topSingleView.setOnClickListener(txtViewOpener);

        // configures buttons who delete textviews in mDragLinearLayout
        final ImageButton removeTextViewButton = (ImageButton) topSingleView.findViewById(R.id.buttonRemoveTextView);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (removeTextViewButton != null)
                    removeTextViewButton.setOnClickListener(removeTextViewButtonListener);
            }
        }, 1000);
    }

    private void createTextViewWithEditTextPref() {

        // finds and binds views required to build layout
        final View topSingleView = View.inflate(getContext(), R.layout.single_edit_text_recipe1, null);

        LinearLayout textLayout = (LinearLayout) topSingleView.findViewById(R.id.textViewSingle);
        TextView textViewType = (TextView) textLayout.findViewById(R.id.textViewType);
        TextView textViewContent = (TextView) textLayout.findViewById(R.id.textViewContent);

        // sets margins of a single view
        RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (8 * scale + 0.5f);
        params1.setMargins(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
        textLayout.setLayoutParams(params1);

        // sets annotation in the top right of single view, describing if it's a normal text, headline etc.
        ArrayList<EditTextPref> list = mManager.getCurrentOrCreateNewRecipe().getList();
        textViewType.setText(list.get(list.size() - 1).getRecipePartName());

        // sets content of a textView
        textViewContent.setText(list.get(list.size() - 1).getText());

        // creates a unique ID for EditTextPref and puts it into HashMap
        list.get(list.size() - 1).setID(IdCreatorUtils.getInstance().getID());
        mManager.getCurrentOrCreateNewRecipe().getMapForEditTextPrefs().put(list.get(list.size() - 1).getID(), list.get(list.size() - 1));

        // sets hiddenID to textLayout
        TextView hiddenID = (TextView) textLayout.findViewById(R.id.textViewWithIDHidden);
        hiddenID.setText(list.get(list.size() - 1).getID());

        // manages focus on the last textView
        textViewContent.setFocusable(true);
        textViewContent.requestFocus();

        // adds single view to a DragLinearLayout
        mDragLinearLayout.addDragView(topSingleView, textLayout, mDragLinearLayout.getChildCount());

        // attaches a listener to textLayout
        topSingleView.setOnClickListener(txtViewOpener);

        // configures buttons who delete textviews in mDragLinearLayout
        final ImageButton removeTextViewButton = (ImageButton) topSingleView.findViewById(R.id.buttonRemoveTextView);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (removeTextViewButton != null)
                    removeTextViewButton.setOnClickListener(removeTextViewButtonListener);
            }
        }, 1000);
    }

    private void openNextFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
