package dev.app.com.livechat.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;


import java.util.HashMap;
import java.util.Map;

import dev.app.com.livechat.BuildConfig;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.utils.PreferenceHelper;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();
    private static final int RC_SIGN_IN = 9001;
    private static final int FB_SIGN_IN = 64206;
    private static final int TW_SIGN_IN = 140;


    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private CheckBox checkRegister;
    private View loginProgress;
    private View mLoginFormView;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Intent intent;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;
    private FirebaseUser mUser;
    private FirebaseDatabase database;
    private DatabaseReference userDB;
    private SignInButton loginGoogle;
    private LoginButton loginFacebook;
    private TwitterLoginButton loginTwitter;
    private int exitCount = 0;
    private View container;
    private boolean updateUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.twitter_api), getString(R.string.twitter_secret)))
                .debug(true)
                .build();
        Twitter.initialize(config);

        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }

        setContentView(R.layout.activity_login);

        container = findViewById(R.id.container);
        mLoginFormView = findViewById(R.id.login_form);
        loginProgress = findViewById(R.id.login_progress);

        loginGoogle();
        loginFacebook();
        loginTwitter();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userDB = database.getReference("users");
        if (currentUser != null){
            loginSuccess(currentUser);
        }
    }

    private void loginSuccess(final FirebaseUser firebaseUser) {
        updateUser = false;
        userDB.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("loginSuccess", dataSnapshot.toString());
                User user = new User();
                showProgress(false);
                if (dataSnapshot.getValue() == null) {
                    if (!updateUser){
                        updateUser = true;

                        user.setName(firebaseUser.getDisplayName());
                        user.setEmail(firebaseUser.getEmail());
                        user.setOnline(true);
                        userDB.child(firebaseUser.getUid()).setValue(user);
                        Helpers.setUserOnline(LoginActivity.this, true);
                        Helpers.updateUser(LoginActivity.this, user);

//                        intent = new Intent(LoginActivity.this, NavigationActivity.class);
//                        startActivity(intent);
                        Helpers.moveActivity(LoginActivity.this, NavigationActivity.class);
                    }
                } else {
                    String status = dataSnapshot.child("status").getValue(String.class);
                    if (status != null){
                        if (status.equals("ban")){
                            showSnackbar("Your account had been banned. Please Contact our Administrator");
                            logout();
                        } else if (status.equals("suspend")){
                            showSnackbar("Your account had been suspended. Please Contact our Administrator");
                            logout();
                        } else {
                            Helpers.setUserOnline(LoginActivity.this, true);

                            Helpers.moveActivity(LoginActivity.this, NavigationActivity.class);
                        }
                        user = dataSnapshot.getValue(User.class);
                        Helpers.updateUser(LoginActivity.this, user);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showProgress(false);
            }
        });
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void loginGoogle(){
//        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.google_web_client_id))
//                .requestEmail()
//                .build();
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient = Helpers.getGoogleApiClient(this, null);
    }

    public void loginFacebook(){
        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        loginFacebook = (LoginButton) findViewById(R.id.login_facebook);
        loginFacebook.setReadPermissions("email", "public_profile");
        loginFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult.getAccessToken());
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                showProgress(false);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                showProgress(false);
                Snackbar.make(mLoginFormView, error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void loginTwitter(){
        loginTwitter = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                showProgress(false);
                Snackbar.make(mLoginFormView, exception.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("requestCode", requestCode+":"+resultCode);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                showProgress(false);
                Snackbar.make(mLoginFormView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                // Google Sign In failed, update twitterLoginUI appropriately
                Log.w(TAG, "Google sign in failed", e);
                e.printStackTrace();
            }
        } else if (requestCode == FB_SIGN_IN) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == TW_SIGN_IN) {
            loginTwitter.onActivityResult(requestCode, resultCode, data);
        }

        showProgress(false);
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId()+":"+acct.getIdToken());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        signInWithCredential(credential);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        signInWithCredential(credential);
    }

    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        signInWithCredential(credential);
    }

    private void signInWithCredential(final AuthCredential credential){
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success:");
                            FirebaseUser user = mAuth.getCurrentUser();
                            loginSuccess(user);
                        } else {
                            String email = null;
//                            linkUserCredential(credential);
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            showProgress(false);
                            intent = new Intent(LoginActivity.this, LoginOtherActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        }
                    }
                })
        .addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.d(TAG, "signInWithCredential:onFailure:"+e.getMessage());
            }
        });
    }

    private void linkUserCredential(AuthCredential credential){
        mUser = mAuth.getCurrentUser();
        if (mUser != null){
            mAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "linkWithCredential:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                loginSuccess(user);
                            } else {
                                Log.w(TAG, "linkWithCredential:failure", task.getException());
                                showProgress(false);
                            }
                        }
                    });
        }

    }

    public void loginGoogleAction(View view){
//        loginGoogle.performClick();
        Log.d(TAG, "loginGoogleAction");
        showProgress(true);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
//        showProgress(false);
    }

    public void loginFacebookAction(View view){
        Log.d(TAG, "loginFacebookAction");
        loginFacebook.performClick();
    }

    public void loginTwitterAction(View view){
        loginTwitter.performClick();
    }

    public void loginOtherAction(View view){
//        intent = new Intent(this, LoginOtherActivity.class);
//        startActivity(intent);
        Helpers.moveActivity(LoginActivity.this, LoginOtherActivity.class);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        if (exitCount < 1){
            exitCount++;
        } else if (exitCount == 2){
            exitCount++;
            Toast.makeText(this, R.string.click_back_to_exit, Snackbar.LENGTH_LONG).show();
        } else {
            exitCount = 0;
            Helpers.exit(this);
        }
    }

    private void showSnackbar(String s) {
        Snackbar.make(container, s, Snackbar.LENGTH_LONG).show();
    }

    private void logout(){
        LoginManager loginManager = LoginManager.getInstance();
        if (loginManager != null){
            loginManager.logOut();
        }
        if (mGoogleSignInClient != null){
            mGoogleSignInClient.signOut();
        }
        if (loginFacebook != null){
            loginFacebook.clearPermissions();
        }
//        if (loginTwitter != null){
//            loginTwitter.logOut();
//        }
        Helpers.logout(getApplicationContext());
    }

}

