package com.example.radzik.recipes.activity;

        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.os.Bundle;
        import android.support.annotation.NonNull;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.text.TextUtils;
        import android.util.Log;
        import android.view.View;
        import android.widget.EditText;
        import android.widget.Toast;

        import com.example.radzik.recipes.R;
        import com.example.radzik.recipes.database.User;
        import com.firebase.client.Firebase;
        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.AuthResult;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.FirebaseDatabase;


public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "AndroidBash";

    //Add YOUR Firebase Reference URL instead of the following URL
    private Firebase mRef = new Firebase("https://recipes-309da.firebaseio.com/");
    private User mUser;
    private EditText mName;
    private EditText mPhoneNumber;
    private EditText mEmail;
    private EditText mPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        mName = (EditText) findViewById(R.id.edit_text_username);
        mPhoneNumber = (EditText) findViewById(R.id.edit_text_phone_number);
        mEmail = (EditText) findViewById(R.id.edit_text_new_email);
        mPassword = (EditText) findViewById(R.id.edit_text_new_password);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    //This method sets up a new User by fetching the mUser entered details.
    protected void setUpUser() {
        mUser = new User();
        mUser.setName(mName.getText().toString());
        mUser.setPhoneNumber(mPhoneNumber.getText().toString());
        mUser.setEmail(mEmail.getText().toString());
        mUser.setPassword(mPassword.getText().toString());
    }

    public void onSignUpClicked(View view) {
        createNewAccount(mEmail.getText().toString(), mPassword.getText().toString());
        showProgressDialog();
    }


    private void createNewAccount(String email, String password) {
        Log.d(TAG, "createNewAccount:" + email);
        if (!validateForm()) {
            return;
        }
        //This method sets up a new User by fetching the mUser entered details.
        setUpUser();
        //This method  method  takes in an mEmail address and mPassword, validates them and then creates a new mUser
        // with the createUserWithEmailAndPassword method.
        // If the new account was created, the mUser is also signed in, and the AuthStateListener runs the onAuthStateChanged callback.
        // In the callback, you can use the getCurrentUser method to get the mUser's account data.

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        // If sign in fails, display a message to the mUser. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in mUser can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            onAuthenticationSucess(task.getResult().getUser());
                        }


                    }
                });

    }

    private void onAuthenticationSucess(FirebaseUser mUser) {
        // Write new mUser
        saveNewUser(mUser.getUid(), this.mUser.getName(), this.mUser.getPhoneNumber(), this.mUser.getEmail(), this.mUser.getPassword());
        signOut();

        // Go to LoginActivity
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    }

    private void saveNewUser(String userId, String name, String phone, String email, String password) {
        User user = new User(userId,name,phone,email,password);

        mRef.child("users").child(userId).setValue(user);
    }


    private void signOut() {
        mAuth.signOut();
    }

    //This method, validates mEmail address and mPassword
    private boolean validateForm() {
        boolean valid = true;

        String userEmail = mEmail.getText().toString();
        if (TextUtils.isEmpty(userEmail)) {
            mEmail.setError("Required.");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        String userPassword = mPassword.getText().toString();
        if (TextUtils.isEmpty(userPassword)) {
            mPassword.setError("Required.");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}