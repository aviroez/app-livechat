package dev.app.com.livechat.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.MediaRouteButton;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import dev.app.com.livechat.utils.GpsUtils;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.utils.PreferenceHelper;
import dev.app.com.livechat.utils.ShowImageHelper;

/**
 * A login screen that offers login via email/password.
 */
public class EditProfileActivity extends AppCompatActivity implements DatePicker.OnDateChangedListener {

    private static final String TAG = EditProfileActivity.class.getName();
    private static final int GPS_REQUEST = 99;
    private static final int PICK_IMAGE_REQUEST = 71;
    // UI references.
    private EditText textEmail;
    private EditText textName;
    private View mProgressView;
    private View mLoginFormView;
    private Intent intent;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference userDB;
    private String location;
    private EditText textLocation;
    private int gender = 0;
    private String name;
    private String email;
    private String defaultName;
    private String defaultEmail;
    private RadioButton radioMale;
    private RadioButton radioFemale;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location gpsLoc;
    private Location netLoc;
    private Location finalLoc;
    private double latitude;
    private double longitude;
    private Uri filePath;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ImageView imageThumbnail;
    private Uri uploadSession;
    private EditText textPhone;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private ImageButton buttonVerifyEmail;
    private ImageButton buttonVerifyPhone;
    private ImageButton buttonRefreshLocation;
    private String phone;
    private LoginButton loginFacebook;
    private TwitterLoginButton twitterLoginButton;
    private EditText textBirthDate;
    private Calendar calendar;
    private DatePickerDialog datePickerDialog;
    private String birthDateString;
    private AlertDialog phoneDialog;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private View container;
    private EditText textVerificationCode;
    private int duration = 60 * 2;
    private int durationTick = duration;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private Location myLocation;
    private EditText textBio;
    private String bio;
    private PreferenceHelper preferenceHelper;
    private Context context;
    private User user;
    private TextView textCountDownTimer;
    private ProgressBar progressBar;
    private ProgressBar progressBarRound;
    private ImageView imageRefresh;
    private int uploadImageStatus = 0;
    private Bitmap bitmap;
    private long birthLong;
    private boolean isGPSEnabled;
    private DatePickerFragment dFragment;
    private String country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_edit_profile);
        // Set up the login form.
        container = findViewById(R.id.container);
        textEmail = (EditText) findViewById(R.id.text_email);

        textName = (EditText) findViewById(R.id.text_name);
        textPhone = (EditText) findViewById(R.id.text_phone);
        textLocation = (EditText) findViewById(R.id.text_location);
        textBirthDate = (EditText) findViewById(R.id.text_birth_date);
        textBio = (EditText) findViewById(R.id.text_bio);
        radioMale = (RadioButton) findViewById(R.id.radio_male);
        radioFemale = (RadioButton) findViewById(R.id.radio_female);
        imageThumbnail = (ImageView) findViewById(R.id.image_thumbnail);

        buttonVerifyEmail = (ImageButton) findViewById(R.id.button_verify_email);
        buttonVerifyPhone = (ImageButton) findViewById(R.id.button_verify_phone);
        buttonRefreshLocation = (ImageButton) findViewById(R.id.button_refresh_location);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBarRound = (ProgressBar) findViewById(R.id.progress_bar_round);
        imageRefresh = (ImageView) findViewById(R.id.image_refresh);

        progressBar.setVisibility(View.GONE);
        progressBarRound.setVisibility(View.GONE);
        imageRefresh.setVisibility(View.GONE);

        buttonVerifyEmail.setVisibility(View.VISIBLE);
        buttonVerifyPhone.setVisibility(View.VISIBLE);
        buttonRefreshLocation.setVisibility(View.VISIBLE);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        context = getApplicationContext();

        imageThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageAction(view);
            }
        });

        imageRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage(filePath, bitmap);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        initializeUsers();
        initializeBirthDate();

        textName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus){
                    final String name = textName.getText().toString();
                    if (defaultName == null || !defaultName.equalsIgnoreCase(name)){
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        currentUser.updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                HashMap<String, Object> update = new HashMap<>();
                                update.put("name", name);
                                userDB.child(currentUser.getUid()).updateChildren(update);
                            }
                        });
                    }
                }
            }
        });

        ShowImageHelper showImageHelper = new ShowImageHelper(context);
        showImageHelper.showImageProfile(currentUser.getUid(), imageThumbnail);

        new GpsUtils(EditProfileActivity.this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPSEnabled = isGPSEnable;
                if (isGPSEnable) {
                    refreshLocationAction(null);
                }
            }
        });

        if (currentUser != null) {
            List<? extends UserInfo> providerData = currentUser.getProviderData();
            boolean emailVerified = false;

            for (UserInfo ui: providerData){
                Log.d("providerData", ui.getProviderId()+":"+ui.getEmail()+":"+ui.getPhoneNumber()+":"+ui.isEmailVerified());
                if ((ui.getProviderId().equals("firebase") && ui.isEmailVerified()) || ui.getProviderId().equals("google.com")){
                    emailVerified = true;
                } else if (ui.getProviderId().equals("phone")){
                    buttonVerifyPhone.setVisibility(View.GONE);
                }
                if (Helpers.isValidEmail(ui.getEmail()) && (defaultEmail == null || defaultEmail.isEmpty())){
                    defaultEmail = ui.getEmail();
                }
            }

            if (emailVerified) {
                buttonVerifyEmail.setVisibility(View.GONE);
            }

            userDB.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("dataSnapshotEP", dataSnapshot.getValue().toString()+":"+currentUser.getDisplayName());
                    user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        name = currentUser.getDisplayName() !=  null ? currentUser.getDisplayName() : user.getName();
                        email = currentUser.getEmail() !=  null ? currentUser.getEmail() : user.getEmail();
//                        email = user.getEmail();
                        phone = currentUser.getPhoneNumber() !=  null ? currentUser.getPhoneNumber() : user.getPhone_number();
                        gender = user.getGender();
                        location = user.getLocation();
                        bio = user.getBio();
                        birthLong = user.getBirth();

                        if (defaultName == null){
                            defaultName = name;
                        }

                        if (email == null || email.isEmpty()){
                            email = defaultEmail;
                        }

                        if (email == null || email.isEmpty()){
                            textEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                            textEmail.setFocusableInTouchMode(true);
                            textEmail.setFocusable(true);
                            buttonVerifyEmail.setVisibility(View.VISIBLE);
                        } else {
                            textEmail.setFocusableInTouchMode(false);
                            textEmail.setFocusable(false);
                            textEmail.setInputType(InputType.TYPE_NULL);
                        }

                        if (textName.getText().toString().isEmpty()){
                            textName.setText(name);
                        }

                        if (textEmail.getText().toString().isEmpty()){
                            textEmail.setText(email);
                        }

                        if (textPhone.getText().toString().isEmpty()){
                            textPhone.setText(phone);
                        }

                        if (textLocation.getText().toString().isEmpty()){
                            textLocation.setText(location);
                        }

                        if (textBio.getText().toString().isEmpty()){
                            textBio.setText(bio);
                        }

                        if (gender == 1) {
                            radioMale.setChecked(true);
                        } else if (gender == 2) {
                            radioFemale.setChecked(true);
                        }

                        if (birthLong > 0){
                            textBirthDate.setText(Helpers.formatDate(new Date(birthLong)));
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void initializeBirthDate() {
        calendar = Calendar.getInstance();
        dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                if (textBirthDate.getText().toString().isEmpty()){
                    textBirthDate.setText(Helpers.formatDate(calendar.getTime()));
                }
            }
        };
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isNameValid(String name) {
        //TODO: Replace this with your own logic
        return name.length() > 4;
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

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void editProfileAction(View view) {
        // Reset errors.
        textEmail.setError(null);
        textName.setError(null);
        textPhone.setError(null);

        // Store values at the time of the login attempt.
        email = textEmail.getText().toString();
        name = textName.getText().toString();
        phone = textPhone.getText().toString();
        location = textLocation.getText().toString();
        birthDateString = textBirthDate.getText().toString();
        bio = textBio.getText().toString();
        gender = 0;

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(email)) {
            textName.setError(getString(R.string.error_field_required));
            focusView = textName;
            cancel = true;
        } else if (!isNameValid(name)) {
            textName.setError(getString(R.string.name_invalid));
            focusView = textName;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            textEmail.setError(getString(R.string.error_field_required));
            focusView = textEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            textEmail.setError(getString(R.string.error_invalid_email));
            focusView = textEmail;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {
            textPhone.setError(getString(R.string.error_field_required));
            focusView = textPhone;
            cancel = true;
        } else if (buttonVerifyPhone.getVisibility() == View.VISIBLE) {
            textPhone.setError(getString(R.string.please_verify_your_phone));
            focusView = textPhone;
            cancel = true;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(birthDateString)) {
            textBirthDate.setError(getString(R.string.error_field_required));
            focusView = textBirthDate;
            cancel = true;
        }

        if (radioMale.isChecked()){
            gender = 1;
        } else if (radioFemale.isChecked()){
            gender = 2;
        }

        if (finalLoc != null){
            latitude = finalLoc.getLatitude();
            longitude = finalLoc.getLongitude();
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            showProgress(true);
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();

            currentUser.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Map<String, Object> update = new HashMap<>();
                                update.put("nama", name);
                                update.put("phone_number", phone);
                                update.put("location", location);
                                update.put("country", country);
                                update.put("gender", gender);
                                update.put("bio", bio);
                                if (dFragment != null && dFragment.getCalendar() != null){
                                    update.put("birth", dFragment.getCalendar().getTimeInMillis());
                                }
                                if (latitude != 0){
                                    update.put("latitude", latitude);
                                }
                                if (longitude != 0){
                                    update.put("longitude", longitude);
                                }

                                userDB.child(currentUser.getUid()).updateChildren(update);
                                showProgress(false);

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("redirect", R.id.navigation_profile);
                                hashMap.put("notif", R.string.profile_updated_succesfully);

                                Helpers.moveActivity(EditProfileActivity.this, NavigationActivity.class, hashMap);

                                User user = new User();
                                user.setName(name);
                                user.setEmail(email);
                                Helpers.updateUser(EditProfileActivity.this, user);
                            }
                        }
                    });
        }
    }

    private void initializeUsers() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        if (currentUser == null) {
            currentUser = mAuth.getCurrentUser();

            if (currentUser == null){
                finish();
                intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
        }

        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }
        if (userDB == null) {
            userDB = database.getReference("users");
        }
    }

    private void getlastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            myLocation = location;
                            String city = "";
                            try {
                                city = new LocationAccess(myLocation).execute().get();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            textLocation.setText(city);
                            Log.d("getlastLocation", location.getLatitude() + "," + location.getLongitude());
                        } else {
                            Log.d("getlastLocation", "Location null");
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("getlastLocation", e.getMessage());
                    }
                });
    }

    private Location getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (locationProviders == null || locationProviders.equals("")) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            } else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }

            Log.d("getMyLocation", ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)+", "+ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION));
            return null;
        }

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        List<String> providers = lm.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = lm.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
            Log.d("providers", provider);
        }
//        Log.d("getMyLocation", bestLocation.getProvider()+",");
//        Log.d("getMyLocation", bestLocation.getLatitude()+","+bestLocation.getLongitude());
        return bestLocation;
    }

    @Override
    public void onDateChanged(DatePicker datePicker, int year, int month, int day) {
        Calendar date = Calendar.getInstance();
        date.set(year, month, day);
        textBirthDate.setText(Helpers.formatDate(date.getTime()));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class LocationAccess extends AsyncTask<Double, Integer, String>{

        private Location location;

        public LocationAccess(Location location) {
            this.location = location;
        }

        @Override
        protected String doInBackground(Double... voids) {
            if (location != null){
                Address address = Helpers.getCityName(context, location.getLatitude(), location.getLongitude());
                if (address != null){
                    String subLocally = address.getSubLocality();
                    String locality = address.getLocality();
                    String adminArea = address.getAdminArea();
                    Log.d("getCityName", "address:"+subLocally);
                    Log.d("getCityName", "cityName:"+locality);
                    Log.d("getCityName", "stateName:"+adminArea);
                    if (address.getCountryName() != null){
                        country = address.getCountryName();
                        Log.d("getCityName", "country:"+country);
                    }
                    return subLocally;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null){
                textLocation.setText(s);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressWarnings("deprecation")
    public static boolean isGpsEnabled(Context context) {

        if (Helpers.checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            String providers = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (TextUtils.isEmpty(providers)) {
                return false;
            }
            return providers.contains(LocationManager.GPS_PROVIDER);
        } else {
            final int locationMode;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            switch (locationMode) {

                case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                    return true;
                case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                case Settings.Secure.LOCATION_MODE_OFF:
                default:
                    return false;
            }
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage(Uri filePath, Bitmap bitmap) {
        progressBar.setVisibility(View.VISIBLE);
        storageReference = FirebaseStorage.getInstance().getReference("images");
        StorageReference ref = storageReference.child(currentUser.getUid());
        StorageTask<UploadTask.TaskSnapshot> task = null;
        if(filePath != null) {
            task = ref.putFile(filePath);
            bitmap = null;
        } else if (bitmap != null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            task = ref.putBytes(data);
            filePath = null;
        }

        if(task != null)
        {
            uploadImageStatus = 0;
            progressBar.setVisibility(View.VISIBLE);
            progressBarRound.setVisibility(View.VISIBLE);

            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            uploadImageStatus = 2;
                            progressBar.setVisibility(View.GONE);
                            progressBarRound.setVisibility(View.GONE);
                            imageRefresh.setVisibility(View.GONE);
                            imageThumbnail.setVisibility(View.VISIBLE);
                            Toast.makeText(EditProfileActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                            uploadSession = taskSnapshot.getUploadSessionUri();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            uploadImageStatus = -1;
                            progressBar.setVisibility(View.GONE);
                            progressBarRound.setVisibility(View.GONE);
                            imageRefresh.setVisibility(View.VISIBLE);
                            imageThumbnail.setVisibility(View.GONE);
                            Toast.makeText(EditProfileActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            uploadImageStatus = 1;
                            int progress = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot
                                                                .getTotalByteCount());
                            progressBar.setProgress(progress);
                            progressBarRound.setVisibility(View.VISIBLE);
                            imageRefresh.setVisibility(View.GONE);
                            imageThumbnail.setVisibility(View.GONE);
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        }
                    })
            ;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:"+requestCode+","+resultCode+","+new Gson().toJson(data));
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GPS_REQUEST) {

            } else if(requestCode == PICK_IMAGE_REQUEST) {
                if (data == null) {
                    Snackbar.make(container, "Image Pick is failed", Snackbar.LENGTH_LONG).show();
                    return;
                }

                Uri uri = data.getData();
                try {
                    bitmap = null;
                    filePath = null;
                    if (uri != null){
                        File file = new File(uri.getPath());
                        filePath = uri;
                        bitmap = BitmapFactory.decodeFile(file.getPath());
                        uploadImage(filePath, null);
                    }else{
                        bitmap = (Bitmap) data.getExtras().get("data");
                        uploadImage(null, bitmap);
                    }
                    imageThumbnail.setVisibility(View.VISIBLE);
                    imageThumbnail.setImageBitmap(bitmap);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void profileUploadAction(View view){
        chooseImage();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        intent = new Intent(getApplicationContext(), NavigationActivity.class);
        startActivityForResult(intent, 0);
        return true;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finish();
        intent = new Intent(this, NavigationActivity.class);
        startActivity(intent);
    }

    public void verifyEmail(){
        currentUser.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "sendEmailVerification:success");
                            Toast.makeText(EditProfileActivity.this,
                                    "Verification email sent to " + currentUser.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            buttonVerifyEmail.setVisibility(View.GONE);
                            textEmail.setFocusableInTouchMode(false);
                            textEmail.setFocusable(false);
                            textEmail.setInputType(InputType.TYPE_NULL);
                        } else {
                            Log.d(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(EditProfileActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        if (currentUser != null){
            userDB.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void linkAccount(AuthCredential credential, final int type){
//        type 1 email, type 2 sms
        currentUser.linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
//                            updateUI(user);

                            if (type == 1){
                                buttonVerifyEmail.setVisibility(View.GONE);
                            }
                            if (type == 2){
                                buttonVerifyPhone.setVisibility(View.GONE);
                            }
                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            String message = "Authentication failed.";
                            if (task.getException() != null){
                                message += " "+task.getException().getMessage();
                            }
                            Snackbar.make(container, message,
                                    Toast.LENGTH_LONG).show();
//                            updateUI(null);
                        }
                    }
                });
    }

    private void unlinkAccount(String providerId){
        currentUser.unlink(providerId)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "unlinkWithCredential:success");
                        } else {
                            Log.d(TAG, "unlinkWithCredential:failure", task.getException());
                        }
                    }
                });

    }

    private void verifyPhoneNumber(String phoneNumber){
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);

                linkAccount(credential, 2);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);
                EditProfileActivity.this.verificationId = verificationId;
                EditProfileActivity.this.resendToken = token;


                showDialogCode();

                // Save verification ID and resending token so we can use them later
//                mVerificationId = verificationId;
//                mResendToken = token;

                // ...
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                duration,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    public void verifyEmailAction(View view){
        email = textEmail.getText().toString();
        if (phone.length() > 0) {
            verifyEmail();
        }
    }

    public void verifyPhoneAction(View view){
        phone = textPhone.getText().toString();
        if (phone.length() > 0){
            phone = Helpers.formatPhoneNumber(phone);
            textPhone.setText(phone);
            verifyPhoneNumber(phone);

            Snackbar.make(view, getString(R.string.verifying_phone_number), Snackbar.LENGTH_LONG).show();
        }

    }

    private void showDialogCode() {
        if (phoneDialog == null){
            AlertDialog.Builder phoneDialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_phone_code, null);
            phoneDialogBuilder.setView(dialogView);
            phoneDialogBuilder.setCancelable(true);
            phoneDialogBuilder.setIcon(R.mipmap.ic_icon);
            phoneDialogBuilder.setTitle(R.string.verification_code);
            textVerificationCode = (EditText) dialogView.findViewById(R.id.text_verification_code);
            textCountDownTimer = (TextView) dialogView.findViewById(R.id.text_count_down_timer);

            phoneDialogBuilder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String code = textVerificationCode.getText().toString();

                    if (code.isEmpty()){
                        Snackbar.make(container, getString(R.string.please_input_verification_code), Snackbar.LENGTH_LONG).show();
                    }
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    linkAccount(credential, 2);
                    dialog.dismiss();
                }
            });

            phoneDialogBuilder.setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            phoneDialogBuilder.setNeutralButton(getString(R.string.resend), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            phoneDialog = phoneDialogBuilder.create();
        }


        phoneDialog.show();


        String time = (--durationTick) + " " + getString(R.string.seconds).toLowerCase();

        durationTick = duration;
        String s = durationTick + " " + getString(R.string.seconds).toLowerCase();
        textCountDownTimer.setText(s);

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (durationTick > 0){
                            String s = (--durationTick) + " " + getString(R.string.seconds).toLowerCase();
                            textCountDownTimer.setText(s);
                        } else {
                            timer.cancel();
                            phoneDialog.dismiss();
                        }
                    }
                });
            }

        },0,1000);//Update text every second
    }

    public void refreshLocationAction(View view){
//        Toast.makeText(this, "Refresh Location", Toast.LENGTH_LONG).show();
        try {
            if (myLocation != null){
                String city = new LocationAccess(myLocation).execute().get();
                textLocation.setText(city);
            } else {
                getlastLocation();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void birthDateAction(View view){
//        datePickerDialog = new DatePickerDialog(this, dateSetListener, calendar
//                .get(Calendar.YEAR) - 10, calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH));
//        datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
//            @Override
//            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
//
//            }
//        });
//        datePickerDialog.show();
        // Initialize a new date picker dialog fragment
        dFragment = new DatePickerFragment();

        // Show the date picker dialog fragment
        dFragment.show(getSupportFragmentManager(), getString(R.string.prompt_birth_date));
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
//                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    private void pickImageAction(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent, cameraIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
    }

    private Calendar getCalendar(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar;
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private Calendar calendar;

        public Calendar getCalendar() {
            return calendar;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR) - 14;
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
//            c.set(year, month, day);
            c.set(Calendar.YEAR, year);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            TextView tv = (TextView) getActivity().findViewById(R.id.text_birth_date);

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            tv.setText(Helpers.formatDate(calendar.getTime()));
            this.calendar = calendar;
        }
    }




}

