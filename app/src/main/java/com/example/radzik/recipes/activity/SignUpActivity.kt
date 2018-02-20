package com.example.radzik.recipes.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast

import com.example.radzik.recipes.R
import com.example.radzik.recipes.database.User
import com.firebase.client.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


open class SignUpActivity : AppCompatActivity() {

    //Add YOUR Firebase Reference URL instead of the following URL
    private val mRef = Firebase("https://recipes-309da.firebaseio.com/")
    private var mUser: User? = null
    private var mName: EditText? = null
    private var mPhoneNumber: EditText? = null
    private var mEmail: EditText? = null
    private var mPassword: EditText? = null
    private var mAuth: FirebaseAuth? = null
    private var mProgressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        mAuth = FirebaseAuth.getInstance()

    }

    override fun onStart() {
        super.onStart()

        mName = findViewById(R.id.edit_text_username) as EditText
        mPhoneNumber = findViewById(R.id.edit_text_phone_number) as EditText
        mEmail = findViewById(R.id.edit_text_new_email) as EditText
        mPassword = findViewById(R.id.edit_text_new_password) as EditText
    }

    public override fun onStop() {
        super.onStop()
    }

    //This method sets up a new User by fetching the mUser entered details.
    private fun setUpUser() {
        mUser = User()
        mUser!!.name = mName!!.text.toString()
        mUser!!.phoneNumber = mPhoneNumber!!.text.toString()
        mUser!!.email = mEmail!!.text.toString()
        mUser!!.password = mPassword!!.text.toString()
    }

    fun onSignUpClicked(view: View) {
        createNewAccount(mEmail!!.text.toString(), mPassword!!.text.toString())
        showProgressDialog()
    }


    private fun createNewAccount(email: String, password: String) {
        Log.d(TAG, "createNewAccount:" + email)
        if (!validateForm()) {
            return
        }
        //This method sets up a new User by fetching the mUser entered details.
        setUpUser()
        //This method  method  takes in an mEmail address and mPassword, validates them and then creates a new mUser
        // with the createUserWithEmailAndPassword method.
        // If the new account was created, the mUser is also signed in, and the AuthStateListener runs the onAuthStateChanged callback.
        // In the callback, you can use the getCurrentUser method to get the mUser's account data.

        mAuth!!.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful)
                    hideProgressDialog()

                    // If sign in fails, display a message to the mUser. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in mUser can be handled in the listener.
                    if (!task.isSuccessful) {
                        Toast.makeText(this@SignUpActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        onAuthenticationSucess(task.result.user)
                    }
                }

    }

    private fun onAuthenticationSucess(mUser: FirebaseUser) {
        // Write new mUser
        saveNewUser(mUser.uid, this.mUser!!.name!!, this.mUser!!.phoneNumber!!, this.mUser!!.email!!, this.mUser!!.password!!)
        signOut()

        // Go to LoginActivity
        startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
        finish()
    }

    private fun saveNewUser(userId: String, name: String, phone: String, email: String, password: String) {
        val user = User(userId, name, phone, email, password)

        mRef.child("users").child(userId).setValue(user)
    }


    private fun signOut() {
        mAuth!!.signOut()
    }

    //This method, validates mEmail address and mPassword
    private fun validateForm(): Boolean {
        var valid = true

        val userEmail = mEmail!!.text.toString()
        if (TextUtils.isEmpty(userEmail)) {
            mEmail!!.error = "Required."
            valid = false
        } else {
            mEmail!!.error = null
        }

        val userPassword = mPassword!!.text.toString()
        if (TextUtils.isEmpty(userPassword)) {
            mPassword!!.error = "Required."
            valid = false
        } else {
            mPassword!!.error = null
        }

        return valid
    }


    fun showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog(this)
            mProgressDialog!!.setMessage(getString(R.string.loading))
            mProgressDialog!!.isIndeterminate = true
        }

        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
        }
    }

    companion object {

        private val TAG = "AndroidBash"
    }

}