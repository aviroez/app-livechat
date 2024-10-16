package dev.app.com.livechat.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
//import com.facebook.UiLifecycleHelper;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.app.com.livechat.BuildConfig;
import dev.app.com.livechat.activities.MyVideoListActivity;
import dev.app.com.livechat.activities.UserListActivity;
import dev.app.com.livechat.adapters.CustomMenuProfileAdapter;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.activities.EditProfileActivity;
import dev.app.com.livechat.activities.LoginActivity;
import dev.app.com.livechat.R;
import dev.app.com.livechat.utils.ShowImageHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final int RC_SIGN_IN = 9001; //9001
    private static final int FB_SIGN_IN = 64206;
    private static final int TW_SIGN_IN = 140;
    private static final String TAG = ProfileFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private TextView textName;
    private TextView textCountFollowing;
    private TextView textCountFollower;
    private Intent intent;
    private ImageButton buttonEditProfile;
    private StorageReference storageReference;
    private ImageView imageProfile;
    private ImageButton buttonLogout;
    private Button buttonGoogle;
    private Button buttonFacebook;
    private Button buttonTwitter;
    private FirebaseDatabase database;
    private DatabaseReference userDB;
    private ImageView imageCheckGoogle;
    private ImageView imageCheckFacebook;
    private ImageView imageCheckTwitter;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;
    private LoginButton loginFacebook;
    private View view;
    private TwitterLoginButton loginTwitter;
    private FragmentActivity activity;
    private GoogleApiClient mGoogleApiClient;
    private List<Stream> listStream = new ArrayList<>();
    private DatabaseReference streamDB;
    private String uid;
    private Context context;
    private CustomMenuProfileAdapter customMenuProfileAdapter;
    private TextView textEditProfile;
    private TextView textLevel;
    private TextView textLocation;
    private ImageButton buttonShare;
//    private UiLifecycleHelper uiHelper;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(getContext());

        TwitterConfig config = new TwitterConfig.Builder(getContext())
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.twitter_api), getString(R.string.twitter_secret)))
                .debug(true)
                .build();
        Twitter.initialize(config);

        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }
        context = getContext();
        activity = getActivity();
//        uiHelper = new UiLifecycleHelper(activity, callback);
//        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        textName = (TextView) view.findViewById(R.id.text_name);
        textLocation = (TextView) view.findViewById(R.id.text_location);
        textLevel = (TextView) view.findViewById(R.id.text_level);
        textEditProfile = (TextView) view.findViewById(R.id.text_edit_profile);
        textCountFollowing = (TextView) view.findViewById(R.id.text_count_following);
        textCountFollower = (TextView) view.findViewById(R.id.text_count_folowers);
        imageProfile = (ImageView) view.findViewById(R.id.image_profile);
        buttonShare = (ImageButton) view.findViewById(R.id.button_share);
        buttonGoogle = (Button) view.findViewById(R.id.button_google);
        buttonFacebook = (Button) view.findViewById(R.id.button_facebook);
        buttonTwitter = (Button) view.findViewById(R.id.button_twitter);

        imageCheckGoogle = (ImageView) view.findViewById(R.id.image_check_google);
        imageCheckFacebook = (ImageView) view.findViewById(R.id.image_check_facebook);
        imageCheckTwitter = (ImageView) view.findViewById(R.id.image_check_twitter);

        imageCheckGoogle.setVisibility(View.GONE);
        imageCheckFacebook.setVisibility(View.GONE);
        imageCheckTwitter.setVisibility(View.GONE);

        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginGoogle();
            }
        });

        loginFacebook();
        buttonFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "facebook:performClick");
                loginFacebook.performClick();
            }
        });

        loginTwitter();
        buttonTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginTwitter.performClick();
            }
        });

        if (currentUser != null){
            textName.setText(currentUser.getDisplayName());
        }

        LinearLayout layoutMyVideo = (LinearLayout) view.findViewById(R.id.layout_my_videos);
        layoutMyVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listVideoAction(view);
            }
        });

        LinearLayout layoutEditProfile = (LinearLayout) view.findViewById(R.id.layout_edit_profile);
        layoutEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfile(view);
            }
        });

        LinearLayout layoutLogout = (LinearLayout) view.findViewById(R.id.layout_logout);
        layoutLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpers.logout(getContext());
            }
        });

        textEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfile(view);
            }
        });

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareAction(view);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        initializeUsers();

        List<? extends UserInfo> providerData = currentUser.getProviderData();
        HashMap<String, Object> hashMapProvider = new HashMap<>();

        for (UserInfo ui: providerData){
            String json = new Gson().toJson(ui);
            Log.d("providerData", ui.getProviderId()+":"+ui.getEmail()+":"+ui.getPhoneNumber()+":"+ui.isEmailVerified());
            if (ui.getProviderId().equals("google.com")){
                buttonGoogle.setEnabled(false);
                imageCheckGoogle.setVisibility(View.VISIBLE);
            }
            if (ui.getProviderId().equals("facebook.com")){
                buttonFacebook.setEnabled(false);
                imageCheckFacebook.setVisibility(View.VISIBLE);
            }
            if (ui.getProviderId().equals("twitter.com")){
                buttonTwitter.setEnabled(false);
                imageCheckTwitter.setVisibility(View.VISIBLE);
            }
            hashMapProvider.put(ui.getProviderId().replace('.', '-'), ui.isEmailVerified());
        }

        userDB.child(uid).child("providers").updateChildren(hashMapProvider);

        final ShowImageHelper showImageHelper = new ShowImageHelper(activity);
        showImageHelper.showImageProfile(uid, imageProfile);

        userDB.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null){
                    user.setUid(uid);

                    if (user.getLevel() > 0){
                        textLevel.setText("Level: "+user.getLevel());
                    }

                    if (user.getName() != null && !user.getName().isEmpty()){
                        textName.setText(user.getName());
                    }
                    textLocation.setText(user.getLocation());
                }

                if (dataSnapshot.child("follows").exists()){
                    textCountFollowing.setText(String.valueOf(dataSnapshot.child("follows").getChildrenCount()));
                }

                if (dataSnapshot.child("followers").exists()){
                    textCountFollower.setText(String.valueOf(dataSnapshot.child("followers").getChildrenCount()));
                }

                Log.d(TAG, dataSnapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            mAuth = FirebaseAuth.getInstance();
            currentUser = mAuth.getCurrentUser();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void myVideo(View view){
        intent = new Intent(getContext(), EditProfileActivity.class);
        startActivity(intent);
//        Helpers.moveActivity(getActivity(), EditProfileActivity.class);
    }

    public void editProfile(View view){
        intent = new Intent(getContext(), EditProfileActivity.class);
        startActivity(intent);
//        Helpers.moveActivity(getActivity(), EditProfileActivity.class);
    }

    private void initializeUsers() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        if (currentUser == null) {
            currentUser = mAuth.getCurrentUser();

            if (currentUser == null){
                getActivity().finish();
                intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        }
        uid = currentUser.getUid();

        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }
        if (userDB == null) {
            userDB = database.getReference("users");
        }
        if (streamDB == null) {
            streamDB = database.getReference("streams");
        }

        if (storageReference == null){
            storageReference = FirebaseStorage.getInstance().getReference("images").child(uid);
        }
    }

    public void loginGoogle(){
        mGoogleSignInClient = Helpers.getGoogleApiClient(context, currentUser.getEmail());

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        getActivity().startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void loginFacebook(){

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        loginFacebook = (LoginButton) view.findViewById(R.id.login_facebook);
        loginFacebook.setFragment(this);
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
//                showProgress(false);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
//                showProgress(false);
                Snackbar.make(view, error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void loginTwitter(){
        loginTwitter = (TwitterLoginButton) view.findViewById(R.id.twitter_login_button);
        loginTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                Snackbar.make(view, exception.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("requestCode", requestCode+":"+resultCode);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
//                showProgress(false);
                Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_LONG).show();
                // Google Sign In failed, update twitterLoginUI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        } else if (requestCode == FB_SIGN_IN) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == TW_SIGN_IN) {
            loginTwitter.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode > 0){
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }

//        showProgress(false);
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

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
        Log.d(TAG, "signInWithCredential:" + credential.getProvider()+","+credential.getSignInMethod());

        currentUser.linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "linkWithCredential:success"+"|"+new Gson().toJson(task));
                    FirebaseUser user = task.getResult().getUser();
                    if (user != null){
                        Log.d(TAG, "getProviderId:"+user.getProviderId());
                    }
                    if (user.getProviderId().equals("google.com")){
                        buttonGoogle.setEnabled(false);
                        imageCheckGoogle.setVisibility(View.VISIBLE);
                    }
                    if (user.getProviderId().equals("facebook.com")){
                        buttonFacebook.setEnabled(false);
                        imageCheckFacebook.setVisibility(View.VISIBLE);
                    }
                    if (user.getProviderId().equals("twitter.com")){
                        buttonTwitter.setEnabled(false);
                        imageCheckTwitter.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.w(TAG, "linkWithCredential:failure", task.getException());
                    Toast.makeText(getActivity(), "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void listVideoAction(View view) {
        intent = new Intent(context, MyVideoListActivity.class);
        startActivity(intent);
    }

    private void chatListAction(View view) {
        intent = new Intent(context, UserListActivity.class);
        startActivity(intent);
    }

    private void shareAction(View view) {
        if (currentUser != null){
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String host = getString(R.string.web_host) + "/users.html?uid=" + currentUser.getUid();
            String shareBody = getString(R.string.please_watch_my_show_in);
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.my_show));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody + " " +  host);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
    }
}
