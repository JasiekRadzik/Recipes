package com.example.radzik.recipes.activity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast

import com.example.radzik.recipes.R
import com.example.radzik.recipes.database.User
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.firebase.client.Firebase
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Created by AndroidBash on 10/07/16
 */

class LoginActivity : AppCompatActivity() {
    var mUser: User
    private var mEmail: EditText? = null
    private var mPassword: EditText? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mProgressDialog: ProgressDialog? = null

    //Add YOUR Firebase Reference URL instead of the following URL
    internal var mRef: Firebase

    //FaceBook callbackManager
    private var callbackManager: CallbackManager? = null
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.setAndroidContext(this)

        mRef = Firebase("https://recipes-309da.firebaseio.com/users/")
        setContentView(R.layout.activity_login)

        val scrollView = findViewById(R.id.scrollToHoldBackground) as ScrollView
        scrollView.isEnabled = false


        mAuth = FirebaseAuth.getInstance()

        val mUser = mAuth!!.currentUser
        if (mUser != null) {
            // User is signed in
            val intent = Intent(applicationContext, MainActivity::class.java)
            val uid = mAuth!!.currentUser!!.uid
            var image: String? = ""
            try {
                image = mAuth!!.currentUser!!.photoUrl!!.toString()
            } catch (e: NullPointerException) {
                Log.e("Null Pointer Exception", "" + e.toString())
            }

            intent.putExtra("user_id", uid)
            if (image != null || image !== "") {
                intent.putExtra("profile_picture", image)
            }
            startActivity(intent)
            finish()
            Log.d(TAG, "onAuthStateChanged:signed_in:" + mUser.uid)
        }

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val mUser = firebaseAuth.currentUser
            if (mUser != null) {
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + mUser.uid)
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out")
            }
        }

        //FaceBook
        FacebookSdk.sdkInitialize(applicationContext)

        callbackManager = CallbackManager.Factory.create()
        val loginButton = findViewById(R.id.button_facebook_login) as LoginButton
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(100))
        params.setMargins(dpToPx(30), 0, dpToPx(30), 0)
        loginButton.layoutParams = params

        loginButton.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10))
        loginButton.setBackgroundResource(R.drawable.button_facebook_log_in)
        loginButton.setReadPermissions("email", "public_profile")
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult)
                signInWithFacebook(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
            }
        })
        //

    }

    override fun onStart() {
        super.onStart()
        mEmail = findViewById(R.id.edit_text_email_id) as EditText
        mPassword = findViewById(R.id.edit_text_password) as EditText
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    public override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
    }


    //FaceBook
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }
    //

    protected fun setUpUser() {
        mUser = User()
        mUser.email = mEmail!!.text.toString()
        mUser.password = mPassword!!.text.toString()
    }

    fun onSignUpClicked(view: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    fun onLoginClicked(view: View) {
        setUpUser()
        signIn(mEmail!!.text.toString(), mPassword!!.text.toString())
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:" + email)
        if (!validateForm()) {
            return
        }

        showProgressDialog()

        mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful)

                    // If sign in fails, display a message to the mUser. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in mUser can be handled in the listener.
                    if (!task.isSuccessful) {
                        Log.w(TAG, "signInWithEmail", task.exception)
                        Toast.makeText(this@LoginActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        val uid = mAuth!!.currentUser!!.uid
                        intent.putExtra("user_id", uid)
                        startActivity(intent)
                        finish()
                    }

                    hideProgressDialog()
                }
        //
    }

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


    private fun signInWithFacebook(token: AccessToken) {
        Log.d(TAG, "signInWithFacebook:" + token)

        showProgressDialog()


        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful)

                    // If sign in fails, display a message to the mUser. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in mUser can be handled in the listener.
                    if (!task.isSuccessful) {
                        Log.w(TAG, "signInWithCredential", task.exception)
                        Toast.makeText(this@LoginActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    } else {
                        val uid = task.result.user.uid
                        val name = task.result.user.displayName
                        val email = task.result.user.email
                        val image = task.result.user.photoUrl!!.toString()

                        //Create a new User and Save it in Firebase database
                        val user = User(uid, name, null, email, null)

                        mRef.child(uid).setValue(user)

                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.putExtra("user_id", uid)
                        intent.putExtra("profile_picture", image)
                        startActivity(intent)
                        finish()
                    }

                    hideProgressDialog()
                }
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

    fun dpToPx(dp: Int): Int {
        val displayMetrics = applicationContext.resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    companion object {

        private val TAG = "AndroidBash"
    }
}
