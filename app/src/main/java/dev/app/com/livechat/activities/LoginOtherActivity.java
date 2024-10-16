package dev.app.com.livechat.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.User;

public class LoginOtherActivity extends AppCompatActivity {

    private static final String TAG = LoginOtherActivity.class.getSimpleName();
    private EditText textEmail;
    private CheckBox checkRegister;
    private EditText textPassword;
    private Button buttonLogin;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ScrollView loginForm;
    private ProgressBar loginProgress;
    private FirebaseDatabase database;
    private DatabaseReference userDB;
    private Intent intent;
    private boolean updateUser = false;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_login_other);

        loginForm = findViewById(R.id.login_form);
        loginProgress = findViewById(R.id.login_progress);

        // Set up the login form.
        textEmail = (EditText) findViewById(R.id.text_email);
        textPassword = (EditText) findViewById(R.id.text_password);
        checkRegister = (CheckBox) findViewById(R.id.check_register);
        buttonLogin = (Button) findViewById(R.id.button_login);

        textPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        checkRegister.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                Log.d("onCheckedChanged", compoundButton.isChecked()+":"+isChecked);
                if (isChecked){
                    buttonLogin.setText(R.string.register);
                } else {
                    buttonLogin.setText(R.string.log_in);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        intent = getIntent();
        email = intent.getStringExtra("email");

        textEmail.setText(email);

        initializeUsers();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        textEmail.setError(null);
        textPassword.setError(null);

        // Store values at the time of the login attempt.
        String email = textEmail.getText().toString();
        String password = textPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            textPassword.setError(getString(R.string.error_invalid_password));
            focusView = textPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            textEmail.setError(getString(R.string.error_field_required));
            focusView = textPassword;
            cancel = true;
        } else if (!isEmailValid(email)) {
            textEmail.setError(getString(R.string.error_invalid_email));
            focusView = textEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if (checkRegister.isChecked()){
                register(email, password);
            } else {
                login(email, password);
            }
        }
    }

    public void loginAction(View view){
        attemptLogin();

        if (currentUser != null){
            loginSuccess(currentUser);
        }
    }

    private void login(String email, String password) {
        Log.d(TAG, "signInWithEmail:"+email+", "+password);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            loginSuccess(user);
                            showProgress(false);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginOtherActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        }
                    }
                });
    }

    private void register(String email, String password) {
        Log.d(TAG, "createUserWithEmail:"+email+", "+password);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            loginSuccess(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginOtherActivity.this, "Register failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                            showProgress(false);
                        }
                    }
                });
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            loginForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            loginProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loginProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void initializeUsers(){
        if (mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }
        if (currentUser == null){
            currentUser = mAuth.getCurrentUser();
        }
        if (database == null){
            database = FirebaseDatabase.getInstance();
        }
        if (userDB == null){
            userDB = database.getReference("users");
        }
    }

    private void loginSuccess(final FirebaseUser firebaseUser) {
        updateUser = false;
        userDB.child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                showProgress(false);
                if (dataSnapshot.getValue() == null) {
                    if (!updateUser){
                        User user = new User();
                        user.setName(firebaseUser.getDisplayName());
                        user.setEmail(firebaseUser.getEmail());
                        user.setOnline(true);
                        user.setLast_seen(System.currentTimeMillis());
                        user.setStatus("active");
                        userDB.child(firebaseUser.getUid()).setValue(user);
                        Helpers.updateUser(LoginOtherActivity.this, user);
                        Helpers.setUserOnline(LoginOtherActivity.this, true);

                        updateUser = true;
                        intent = new Intent(LoginOtherActivity.this, NavigationActivity.class);
                        startActivity(intent);
                    }
                } else {
                    String status = dataSnapshot.child("status").getValue(String.class);
                    if (status != null){
                        if (status.equals("ban")){
                            showSnackbar("Your account had been banned. Please Contact our Administrator");
                        } else if (status.equals("suspend")){
                            showSnackbar("Your account had been suspended. Please Contact our Administrator");
                        } else {
                            if (!updateUser){
                                updateUser = true;
                                Helpers.setUserOnline(LoginOtherActivity.this, true);

                                User user = dataSnapshot.getValue(User.class);
                                Helpers.updateUser(LoginOtherActivity.this, user);

                                Helpers.moveActivity(LoginOtherActivity.this, NavigationActivity.class);
                            }
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showProgress(false);
            }
        });
    }

    private void showSnackbar(String s) {
        Snackbar.make(findViewById(R.id.login_form), s, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
//        intent = new Intent(getApplicationContext(), NavigationActivity.class);
//        startActivityForResult(intent, 0);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        intent = new Intent(this, LoginActivity.class);
//        startActivity(intent);
//        finish();
        Helpers.moveActivity(LoginOtherActivity.this, LoginActivity.class);
    }
}
