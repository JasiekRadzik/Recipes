package com.example.radzik.recipes.fragment


import android.app.Fragment
import android.app.FragmentTransaction
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager

import android.content.DialogInterface
import android.content.Intent

import android.os.Bundle

import android.os.Handler
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

import com.airnauts.toolkit.utils.KeyboardUtils
import com.example.radzik.recipes.R
import com.example.radzik.recipes.activity.MainActivity
import com.example.radzik.recipes.database.ConstantsForFragmentsSelection
import com.example.radzik.recipes.database.ConstantsForRecipePartTypes
import com.example.radzik.recipes.database.EditTextPref
import com.example.radzik.recipes.database.EditTextPrefManager
import com.example.radzik.recipes.database.RecipeManager
import com.example.radzik.recipes.utils.IdCreatorUtils
import com.example.radzik.recipes.utils.PixelsToDensityConverter
import com.jmedeisis.draglinearlayout.DragLinearLayout

import java.util.ArrayList
import java.util.HashMap
import java.util.Locale

import butterknife.BindView
import butterknife.ButterKnife

import android.app.Activity.RESULT_OK
import android.content.Context.CLIPBOARD_SERVICE

/**
 * Created by Radzik on 31.07.2017.
 */

class WriteRecipeFragment : Fragment() {

    @BindView(R.id.main_scroll_view)
    internal var mMainScrollView: ScrollView? = null

    var layoutContainingRecipeParts: DragLinearLayout
        internal set

    @BindView(R.id.button_add)
    internal var mAddButton: Button? = null

    @BindView(R.id.hidden_layout_edit_part)
    internal var mHiddenLayoutEditPart: RelativeLayout? = null

    @BindView(R.id.mainEditText)
    internal var mMainEditText: EditText? = null

    @BindView(R.id.layoutWithTextTypesButtons)
    internal var mLayoutWithTextTypesButtons: LinearLayout? = null

    @BindView(R.id.ingredientAmountEditText)
    internal var mIngredientAmountEditText: EditText? = null

    private var mLayoutClickedToEdit: RelativeLayout? = null

    private var mManager: RecipeManager? = null
    private var mETPManager: EditTextPrefManager? = null
    private var mTextToSpeech: TextToSpeech? = null
    private var mEditTextPref: EditTextPref? = null

    private var mIsTextViewOpenedToEdit: Boolean = false

    private var mBottomUpAnim: Animation? = null
    private var mBottomDownAnim: Animation? = null

    private var mDefaultMainEditTextParams: LinearLayout.LayoutParams? = null

    internal var onADDClickListener: View.OnClickListener = View.OnClickListener {
        if (mMainScrollView!!.visibility == View.VISIBLE) {

            setDefaultCreateRecipePartLayout()

            mAddButton!!.isEnabled = false
            mAddButton!!.text = resources.getString(R.string.button_bottom_save_edited_recipe_part)

            // hides layout containing textViews and shows layout for create recipe
            mHiddenLayoutEditPart!!.startAnimation(mBottomUpAnim)
            mHiddenLayoutEditPart!!.visibility = View.VISIBLE
            mMainScrollView!!.visibility = View.GONE

            // clears mMainEditText so that we can create another part of a recipe
            mMainEditText!!.setText("")
            mIngredientAmountEditText!!.setText("")
            mMainEditText!!.addTextChangedListener(txtWatcher)

            // creates object to store information about typeface, size etc.
            mEditTextPref = EditTextPref()

            for (i in 0 until mLayoutWithTextTypesButtons!!.childCount - 1) {
                val button = mLayoutWithTextTypesButtons!!.getChildAt(i) as Button
                button.isSelected = false
            }

            mMainEditText!!.requestFocus()
            KeyboardUtils.show(mMainEditText)

            // configures button which closes this window
            val buttonClear = mHiddenLayoutEditPart!!.findViewById(R.id.button_clear) as ImageButton
            buttonClear.setOnClickListener(buttonClearListener)

        } else if (mMainScrollView!!.visibility == View.GONE && mIsTextViewOpenedToEdit) {

            KeyboardUtils.hide(mMainEditText)

            // saves text into EditTextPref object
            mEditTextPref!!.text = mMainEditText!!.text.toString()
            mEditTextPref!!.ingredientAmount = mIngredientAmountEditText!!.text.toString()

            if (mEditTextPref!!.recipePartType == ConstantsForRecipePartTypes.INGREDIENT) {
                val txtViewIngredientAmount = mLayoutClickedToEdit!!.findViewById(R.id.textViewIngredientAmount) as TextView
                txtViewIngredientAmount.visibility = View.VISIBLE
                txtViewIngredientAmount.text = mEditTextPref!!.ingredientAmount
            } else if (mEditTextPref!!.recipePartType != ConstantsForRecipePartTypes.INGREDIENT) {
                val txtViewIngredientAmount = mLayoutClickedToEdit!!.findViewById(R.id.textViewIngredientAmount) as TextView
                txtViewIngredientAmount.visibility = View.GONE
            }

            val txtViewContent = mLayoutClickedToEdit!!.findViewById(R.id.textViewContent) as TextView
            val txtViewType = mLayoutClickedToEdit!!.findViewById(R.id.textViewType) as TextView
            txtViewContent.text = mEditTextPref!!.text!!.toString()
            singleButtonChecker()
            txtViewType.text = mEditTextPref!!.recipePartName


            // hides layout for create recipe and shows layout containing textViews
            mHiddenLayoutEditPart!!.startAnimation(mBottomDownAnim)
            mHiddenLayoutEditPart!!.postOnAnimation { mHiddenLayoutEditPart!!.visibility = View.GONE }
            mMainScrollView!!.visibility = View.VISIBLE

            mAddButton!!.text = resources.getString(R.string.button_bottom_add_recipe_part)

            mIsTextViewOpenedToEdit = false

        } else {

            KeyboardUtils.hide(mMainEditText)

            if (mEditTextPref!!.recipePartType == ConstantsForRecipePartTypes.INGREDIENT) {
                if (mIngredientAmountEditText!!.text != null) {
                    mEditTextPref!!.ingredientAmount = mIngredientAmountEditText!!.text.toString()
                }

            }
            // saves text into EditTextPref object
            mEditTextPref!!.text = mMainEditText!!.text.toString()

            // adds EditTextPref object to the list inside Recipe object, which contains all parts of the recipe
            val x: EditTextPref?
            x = mEditTextPref
            mManager!!.currentOrCreateNewRecipe.list.add(x)

            // resets mEditTextPref so it is empty for another part of the recipe
            mEditTextPref = null


            createTextViewWithEditTextPref()

            // hides layout for create recipe and shows layout containing textViews
            mHiddenLayoutEditPart!!.startAnimation(mBottomDownAnim)
            mHiddenLayoutEditPart!!.postOnAnimation { mHiddenLayoutEditPart!!.visibility = View.GONE }
            mMainScrollView!!.visibility = View.VISIBLE

            setDefaultCreateRecipePartLayout()

            mAddButton!!.text = resources.getString(R.string.button_bottom_add_recipe_part)

        }
    }

    // listening to buttons in recipe edition
    internal var recipeButtonsListener: View.OnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.button_how_to_cook -> {
                unselectEditRecipeTextStyleButtons()
                mEditTextPref!!.recipePartType = ConstantsForRecipePartTypes.HOW_TO_COOK
                mETPManager!!.translateTypeToRecipePartName(mEditTextPref!!, activity)
                (v as Button).isSelected = true
                setDefaultCreateRecipePartLayout()
            }

            R.id.ingredient -> {
                unselectEditRecipeTextStyleButtons()
                mEditTextPref!!.recipePartType = ConstantsForRecipePartTypes.INGREDIENT
                mETPManager!!.translateTypeToRecipePartName(mEditTextPref!!, activity)
                (v as Button).isSelected = true
                mIngredientAmountEditText!!.visibility = View.VISIBLE
            }

            R.id.button_short_description -> {
                unselectEditRecipeTextStyleButtons()
                mEditTextPref!!.recipePartType = ConstantsForRecipePartTypes.SHORT_DESCRIPTION
                mETPManager!!.translateTypeToRecipePartName(mEditTextPref!!, activity)
                (v as Button).isSelected = true
                setDefaultCreateRecipePartLayout()
            }

            R.id.button_to_eat_with -> {
                unselectEditRecipeTextStyleButtons()
                mEditTextPref!!.recipePartType = ConstantsForRecipePartTypes.ADDITION_TO_EAT_WITH
                mETPManager!!.translateTypeToRecipePartName(mEditTextPref!!, activity)
                (v as Button).isSelected = true
                setDefaultCreateRecipePartLayout()
            }

            R.id.buttonEditPartRefresh -> {
                if (mIngredientAmountEditText!!.visibility == View.VISIBLE) {
                    mIngredientAmountEditText!!.setText("")
                }
                mMainEditText!!.setText("")
            }

            R.id.buttonEditPartCopyTextToClipboard -> {
                val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Edit Part text", mMainEditText!!.text.toString())
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                clipboard.primaryClip = clip
            }

            R.id.buttonEditPartTTS -> {


                var speak = ""
                try {
                    if (mIngredientAmountEditText!!.visibility == View.VISIBLE) {
                        speak = mIngredientAmountEditText!!.text.toString() + "of " + mMainEditText!!.text.toString()
                    } else {
                        speak = mMainEditText!!.text.toString()
                    }

                } catch (e: NullPointerException) {
                    speak = ""
                }

                mTextToSpeech!!.speak(speak, TextToSpeech.QUEUE_FLUSH, null)
            }

            R.id.buttonEditPartVoiceWrite -> startVoiceInput()
        }
    }

    // attached to every single layout inside mDragLinearLayout, so it can be deleted
    internal var removeTextViewButtonListener: View.OnClickListener = View.OnClickListener { v ->
        val popup = PopupMenu(context, v)

        //Inflating the Popup using xml file
        popup.menuInflater
                .inflate(R.menu.menu_popup_confirm_delete, popup.menu)

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.yes_i_am_sure -> {
                    // 1 step:  we have to find EditTextPref responding to this TextView
                    val relative = v.parent as RelativeLayout
                    val linear = relative.getChildAt(0) as LinearLayout
                    val hiddenID = linear.getChildAt(linear.childCount - 1) as TextView
                    val id = hiddenID.text.toString()

                    // 2 step: we remove this EditTextPref from ArrayList and then we remove it from hashMap
                    mManager!!.currentOrCreateNewRecipe.list.remove(mManager!!.currentOrCreateNewRecipe.mapForEditTextPrefs[id])
                    mManager!!.currentOrCreateNewRecipe.mapForEditTextPrefs.remove(id)

                    // 3 step: we remove this view from the layout which contains all views
                    (layoutContainingRecipeParts as ViewManager).removeView(relative)
                }
                R.id.no_i_am_not -> {
                }
            }
            true
        }

        popup.show() //showing popup menu
    }

    internal var buttonClearListener: View.OnClickListener = View.OnClickListener {
        mIngredientAmountEditText!!.visibility = View.GONE
        // hides layout for create recipe and shows layout containing textViews
        mHiddenLayoutEditPart!!.startAnimation(mBottomDownAnim)
        mHiddenLayoutEditPart!!.postOnAnimation { mHiddenLayoutEditPart!!.visibility = View.GONE }
        mMainScrollView!!.visibility = View.VISIBLE

        mAddButton!!.text = resources.getString(R.string.button_bottom_add_recipe_part)
        KeyboardUtils.hide(mMainEditText)
        mMainEditText!!.clearFocus()
        mAddButton!!.isEnabled = true
    }

    // txtWatcher handles disabling and enabling mAddButton depending on the length of editText
    internal var txtWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (count != 0) {
                mAddButton!!.isEnabled = true
            } else
                mAddButton!!.isEnabled = false
        }

        override fun afterTextChanged(s: Editable) {}
    }

    // ----- ----- ----- ----- ----- listener which handles opening textViews with recipeParts ----- ----- ----- ----- ----- //
    internal var txtViewOpener: View.OnClickListener = View.OnClickListener { v ->
        mIsTextViewOpenedToEdit = true
        mLayoutClickedToEdit = v as RelativeLayout

        mAddButton!!.isEnabled = true
        mAddButton!!.text = resources.getString(R.string.button_bottom_save_edited_recipe_part)

        // hides layout containing textViews and shows layout for create recipe
        mHiddenLayoutEditPart!!.startAnimation(mBottomUpAnim)
        mHiddenLayoutEditPart!!.visibility = View.VISIBLE
        mMainScrollView!!.visibility = View.GONE

        // binds listeners to buttons
        bindButtonsListenerAndSetSelectedFalse()

        // retrieves EditTextPref object by ID from HashMap contained in Recipe object
        val id = (v.findViewById(R.id.textViewWithIDHidden) as TextView).text.toString()
        mEditTextPref = mManager!!.currentOrCreateNewRecipe.mapForEditTextPrefs[id]
        mMainEditText!!.setText(mEditTextPref!!.text)

        if (mEditTextPref!!.recipePartType == ConstantsForRecipePartTypes.INGREDIENT) {
            mIngredientAmountEditText!!.visibility = View.VISIBLE
            mIngredientAmountEditText!!.setText(mEditTextPref!!.ingredientAmount)
        }
        singleButtonChecker()

        mMainEditText!!.requestFocus()
        KeyboardUtils.show(mMainEditText)

        // configures button which closes this window
        val buttonClear = mHiddenLayoutEditPart!!.findViewById(R.id.button_clear) as ImageButton
        buttonClear.setOnClickListener(buttonClearListener)
    }

    // ----- ----- ----- ----- ----- HANDLES SWAP EVENTS INSIDE DRAG LINEAR LAYOUT ----- ----- ----- ----- ----- //
    internal var dragListener: DragLinearLayout.OnViewSwapListener = DragLinearLayout.OnViewSwapListener { firstView, firstPosition, secondView, secondPosition ->
        if (layoutContainingRecipeParts.childCount > 1 && firstPosition != secondPosition) {
            val oldList = mManager!!.currentOrCreateNewRecipe.list
            val newList = ArrayList<EditTextPref>()

            Log.e("Old List", ": " + oldList)

            val x = oldList[secondPosition]

            oldList.removeAt(secondPosition)

            for (i in 0 until secondPosition) {
                newList.add(oldList[i])
            }

            newList.add(x)

            for (i in secondPosition until oldList.size) {
                newList.add(oldList[i])
            }

            mManager!!.currentOrCreateNewRecipe.list = newList

            Log.e("Number", "first position: $firstPosition, second position:$secondPosition")
            Log.e("New list", ": " + newList)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        val view = inflater.inflate(R.layout.fragment_write, container, false)
        ButterKnife.bind(this, view)

        setHasOptionsMenu(true)

        mBottomUpAnim = AnimationUtils.loadAnimation(context, R.anim.bottom_up_recipe_edit_view)
        mBottomDownAnim = AnimationUtils.loadAnimation(context, R.anim.bottom_down_recipe_edit_view)

        layoutContainingRecipeParts = view.findViewById(R.id.drag_linear_layout_container) as DragLinearLayout

        layoutContainingRecipeParts.setContainerScrollView(mMainScrollView)
        layoutContainingRecipeParts.isLongClickable = true
        layoutContainingRecipeParts.setOnViewSwapListener(dragListener)

        mAddButton!!.setOnClickListener(onADDClickListener)

        mManager = RecipeManager.instance
        mETPManager = EditTextPrefManager.instance

        // set currently opened fragment as CHOOSE DOC LAYOUT FRAGMENT
        mManager!!.setCurrentFragment(ConstantsForFragmentsSelection.WRITE_RECIPE_FRAGMENT)

        mIsTextViewOpenedToEdit = false

        mTextToSpeech = TextToSpeech(MainActivity.getContextOfApplication(), TextToSpeech.OnInitListener { mTextToSpeech!!.language = Locale.UK })

        // binds listeners to buttons
        bindButtonsListenerAndSetSelectedFalse()

        if (mManager!!.currentOrCreateNewRecipe != null && mManager!!.currentOrCreateNewRecipe.list.size != 0) {
            val list = mManager!!.currentOrCreateNewRecipe.list
            for (i in list.indices.reversed()) {
                recreateTextViewsFromRecipe(i)
            }
        } else if (mManager!!.currentOrCreateNewRecipe != null && mManager!!.currentOrCreateNewRecipe.list.size == 0) {
            mManager!!.currentOrCreateNewRecipe.mapForEditTextPrefs = HashMap()
        }


        // sets default layout for create recipe part
        mDefaultMainEditTextParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mDefaultMainEditTextParams!!.setMargins(0, 0, 0, 0)
        setDefaultCreateRecipePartLayout()

        return view
    }

    private fun bindButtonsListenerAndSetSelectedFalse() {

        for (i in 0 until mLayoutWithTextTypesButtons!!.childCount - 1) {
            val button = mLayoutWithTextTypesButtons!!.getChildAt(i) as Button
            button.isClickable = true
            button.isSelected = false
            button.setOnClickListener(recipeButtonsListener)
        }

        val linearLayout2 = mHiddenLayoutEditPart!!.findViewById(R.id.layoutWithOtherOptionsButtons) as LinearLayout
        for (i in 0 until linearLayout2.childCount - 1) {
            linearLayout2.getChildAt(i).setOnClickListener(recipeButtonsListener)
        }
    }

    private fun singleButtonChecker() {
        when (mEditTextPref!!.recipePartType) {
            ConstantsForRecipePartTypes -> (mLayoutWithTextTypesButtons!!.getChildAt(0) as Button).isSelected = true
            ConstantsForRecipePartTypes.INGREDIENT -> (mLayoutWithTextTypesButtons!!.getChildAt(1) as Button).isSelected = true
            ConstantsForRecipePartTypes.SHORT_DESCRIPTION -> (mLayoutWithTextTypesButtons!!.getChildAt(2) as Button).isSelected = true
            ConstantsForRecipePartTypes.ADDITION_TO_EAT_WITH -> (mLayoutWithTextTypesButtons!!.getChildAt(3) as Button).isSelected = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_right_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_icon -> {
                if (mManager!!.currentOrCreateNewRecipe.list.size != 0) {
                    openNextFragment(ChoosePhotoFragment())
                } else {
                    Toast.makeText(context, "Recipe can't be empty!", Toast.LENGTH_SHORT).show()
                }

                return true
            }
            R.id.clear_icon -> {

                val alertDialog = AlertDialog.Builder(activity).create()
                alertDialog.setTitle("")
                alertDialog.setMessage(resources.getString(R.string.delete_current_recipe_question))
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { dialog, which ->
                    if (WriteRecipeFragment::class.java != null) {
                        layoutContainingRecipeParts.removeAllViewsInLayout()
                        RecipeManager.instance.createNewRecipe()
                    }
                    alertDialog.dismiss()
                }
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No") { dialog, which -> alertDialog.dismiss() }
                alertDialog.show()

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // ----- ----- ----- ----- ----- VOICE RECOGNITION METHODS ----- ----- ----- ----- ----- //
    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hello, How can I help you?")
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            Log.e("VOICE INPUT", "Problem with voice recording" + a)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    mMainEditText!!.setText(result[0])
                }
            }
        }
    }

    override fun onPause() {
        if (mTextToSpeech != null) {
            mTextToSpeech!!.stop()
            mTextToSpeech!!.shutdown()
        }
        super.onPause()
    }

    private fun unselectEditRecipeTextStyleButtons() {
        for (i in 0 until mLayoutWithTextTypesButtons!!.childCount - 1) {
            val button = mLayoutWithTextTypesButtons!!.getChildAt(i) as Button
            button.isSelected = false
        }
    }

    private fun recreateTextViewsFromRecipe(position: Int) {

        // finds and binds views required to build layout
        val topSingleView = View.inflate(context, R.layout.single_edit_text_recipe1, null)

        val textLayout = topSingleView.findViewById(R.id.textViewSingle) as LinearLayout
        val textViewType = textLayout.findViewById(R.id.textViewType) as TextView
        val textViewContent = textLayout.findViewById(R.id.textViewContent) as TextView
        val textViewIngredient = textLayout.findViewById(R.id.textViewIngredientAmount) as TextView

        // sets margins of a single view
        val params1 = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val scale = resources.displayMetrics.density
        val dpAsPixels = (8 * scale + 0.5f).toInt()
        params1.setMargins(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels)
        textLayout.layoutParams = params1

        // sets annotation in the top right of single view, describing if it's a normal text, headline etc.
        val list = mManager!!.currentOrCreateNewRecipe.list
        textViewType.text = list[position].recipePartName

        // sets content of a textView
        textViewContent.text = list[position].text
        if (list[position].recipePartType == ConstantsForRecipePartTypes.INGREDIENT) {
            textViewIngredient.text = list[position].text
            textViewIngredient.visibility = View.VISIBLE
        }


        // sets hiddenID to textLayout
        val hiddenID = textLayout.findViewById(R.id.textViewWithIDHidden) as TextView
        hiddenID.setText(list[position].getID())

        // manages focus on the last textView
        textViewContent.isFocusable = true
        textViewContent.requestFocus()

        // adds single view to a DragLinearLayout
        layoutContainingRecipeParts.addDragView(topSingleView, textLayout, layoutContainingRecipeParts.childCount)

        // attaches a listener to textLayout
        topSingleView.setOnClickListener(txtViewOpener)

        // configures buttons who delete textviews in mDragLinearLayout
        val removeTextViewButton = topSingleView.findViewById(R.id.buttonRemoveTextView) as ImageButton

        val handler = Handler()
        handler.postDelayed({
            removeTextViewButton?.setOnClickListener(removeTextViewButtonListener)
        }, 1000)
    }

    private fun createTextViewWithEditTextPref() {

        // finds and binds views required to build layout
        val topSingleView = View.inflate(context, R.layout.single_edit_text_recipe1, null)

        val textLayout = topSingleView.findViewById(R.id.textViewSingle) as LinearLayout
        val textViewType = textLayout.findViewById(R.id.textViewType) as TextView
        val textViewContent = textLayout.findViewById(R.id.textViewContent) as TextView
        val txtViewIngredientAmount = textLayout.findViewById(R.id.textViewIngredientAmount) as TextView

        // sets margins of a single view
        val params1 = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val scale = resources.displayMetrics.density
        val dpAsPixels = (8 * scale + 0.5f).toInt()
        params1.setMargins(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels)
        textLayout.layoutParams = params1

        // sets annotation in the top right of single view, describing if it's a normal text, headline etc.
        val list = mManager!!.currentOrCreateNewRecipe.list
        val editTextPref = list[list.size - 1]
        textViewType.text = editTextPref.recipePartName

        // sets content of a textView
        textViewContent.text = list[list.size - 1].text
        if (list[list.size - 1].recipePartType == ConstantsForRecipePartTypes.INGREDIENT) {
            txtViewIngredientAmount.visibility = View.VISIBLE
            txtViewIngredientAmount.text = editTextPref.ingredientAmount
        }

        // creates a unique ID for EditTextPref and puts it into HashMap
        list[list.size - 1].setID(IdCreatorUtils.getInstance().id)
        mManager!!.currentOrCreateNewRecipe.mapForEditTextPrefs.put(list[list.size - 1].getID(), list[list.size - 1])

        // sets hiddenID to textLayout
        val hiddenID = textLayout.findViewById(R.id.textViewWithIDHidden) as TextView
        hiddenID.setText(list[list.size - 1].getID())

        // manages focus on the last textView
        textViewContent.isFocusable = true
        textViewContent.requestFocus()

        // adds single view to a DragLinearLayout
        layoutContainingRecipeParts.addDragView(topSingleView, textLayout, layoutContainingRecipeParts.childCount)

        // attaches a listener to textLayout
        topSingleView.setOnClickListener(txtViewOpener)

        // configures buttons who delete textviews in mDragLinearLayout
        val removeTextViewButton = topSingleView.findViewById(R.id.buttonRemoveTextView) as ImageButton

        val handler = Handler()
        handler.postDelayed({
            removeTextViewButton?.setOnClickListener(removeTextViewButtonListener)
        }, 1000)
    }

    private fun openNextFragment(fragment: Fragment) {
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setDefaultCreateRecipePartLayout() {
        mIngredientAmountEditText!!.visibility = View.GONE
        mMainEditText!!.hint = ""
        mIngredientAmountEditText!!.setText("")
    }

    companion object {

        private val REQ_CODE_SPEECH_INPUT = 100
    }
}
