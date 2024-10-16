package dev.app.com.livechat.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Observable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableArrayMap;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.app.com.livechat.adapters.TagChipViewAdapter;
import dev.app.com.livechat.entities.Level;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.fragments.ExploreFragment;
import dev.app.com.livechat.fragments.HashtagFragment;
import dev.app.com.livechat.fragments.RankingFragment;
import dev.app.com.livechat.fragments.SettingFragment;
import dev.app.com.livechat.fragments.UserListFragment;
import dev.app.com.livechat.services.LastSeenService;
import dev.app.com.livechat.services.NotificationSchedulingService;
import dev.app.com.livechat.utils.GpsUtils;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.R;
import dev.app.com.livechat.fragments.NearbyFragment;
import dev.app.com.livechat.fragments.PopularFragment;
import dev.app.com.livechat.fragments.ProfileFragment;
import dev.app.com.livechat.utils.PreferenceHelper;

public class NavigationActivity extends AppCompatActivity implements LocationListener {

    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private String[] mRequiredPermissions = new String[] {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean mPermissionsGranted = false;

    private static final String TAG = NavigationActivity.class.getSimpleName();
    private List<User> listUser = new ArrayList<>();
    private ObservableArrayList<User> obsUser = new ObservableArrayList<>();
    private static ChildEventListener childEventListener;
    private TextView mTextMessage;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Intent intent;
    private int exitCount = 0;
    private Fragment fragment;
    private FirebaseDatabase database;
    private DatabaseReference userDB;
    private DatabaseReference streamDB;
    private Stream stream;
    private PreferenceHelper preferenceHelper;
    private PreferenceHelper preferenceStreamHelper;
    private Context context;
    private int lastId = NearbyFragment.MODE_NEAR;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationManager locationManager;
    private Location location;
    private FloatingActionButton buttonLiveStream;
    private User user;
    private View container;
    private int redirect;
    private String redirectString;
    private BottomNavigationView navigation;
    private BottomNavigationView bottomNavigation;
    private int notif = 0;
    private String search = null;
    private TabLayout navigationTab;
    private String country;
    private String province;
    private int level;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            showFragment(item.getItemId());

            // Highlight the selected item has been done by NavigationView
            item.setChecked(true);
            // Set action bar title
            setTitle(item.getTitle());
            // Close the navigation drawer
//            mDrawer.closeDrawers();

            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_navigation);

        context = this;

        mTextMessage = (TextView) findViewById(R.id.message);
        buttonLiveStream = (FloatingActionButton) findViewById(R.id.button_live_stream);
        container = findViewById(R.id.container);
//        navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
//        navigation.setItemIconSize(0);

        navigationTab = (TabLayout) findViewById(R.id.navigation_tab);

        navigationTab.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                onSelectedTab(tab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                onSelectedTab(tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onSelectedTab(tab);
            }
        });

        bottomNavigation = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        bottomNavigation.setItemIconTintList(null);

        buttonLiveStream.setEnabled(false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkApplicationPermissions();

        getNewToken();
    }

    private void onSelectedTab(TabLayout.Tab tab) {
        int position = tab.getPosition();
        switch (position){
            case 0: showFragment(R.id.navigation_nearby); break;
            case 1: showFragment(R.id.navigation_popular); break;
            case 2: showFragment(R.id.navigation_explore); break;
            case 3: showFragment(R.id.navigation_search); break;
            default: showFragment(R.id.navigation_popular); break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        intent = getIntent();
        redirect = intent.getIntExtra("redirect", 0);
        redirectString = intent.getStringExtra("redirectString");
        notif = intent.getIntExtra("notif", 0);
        search = intent.getStringExtra("search");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        enableLastSeen();

        Log.d(TAG, "intent:"+redirect+":"+redirectString+":"+notif+":"+search);

//        listUser.
        if (redirectString != null && !redirectString.isEmpty()){
            if (redirectString.equals(ProfileFragment.class.getSimpleName())){
                bottomNavigation.setSelectedItemId(R.id.navigation_profile);
            }
        } else {
            if (redirect > 0){
                navigationTab.setVisibility(View.VISIBLE);
                if (redirect == R.id.navigation_nearby){
                    navigationTab.getTabAt(0).select();
                } else if (redirect == R.id.navigation_popular){
                    navigationTab.getTabAt(1).select();
                } else if (redirect == R.id.navigation_explore){
                    navigationTab.getTabAt(2).select();
                } else if (redirect == R.id.navigation_search){
                    navigationTab.getTabAt(3).select();
                } else if (redirect == R.id.navigation_profile){
                    navigationTab.setVisibility(View.GONE);
                    bottomNavigation.setSelectedItemId(R.id.navigation_profile);
                } else if (redirect == R.id.navigation_profile){
                    navigationTab.setVisibility(View.GONE);
                    bottomNavigation.setSelectedItemId(R.id.navigation_profile);
                    navigationTab.setVisibility(View.GONE);
                } else if (redirect == R.id.navigation_ranking){
                    bottomNavigation.setSelectedItemId(R.id.navigation_ranking);
                } else if (redirect == R.id.navigation_inbox){
                    bottomNavigation.setSelectedItemId(R.id.navigation_inbox);
                } else {
                    navigation.setSelectedItemId(redirect);
                    bottomNavigation.setSelectedItemId(redirect);
                }
            } else {
                showFragment(0);
            }
        }

        if (notif > 0){
            Snackbar.make(container, getString(notif), Snackbar.LENGTH_LONG).show();
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        new GpsUtils(NavigationActivity.this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                Log.d(TAG, "isGPSEnable:"+isGPSEnable);
                if (isGPSEnable){
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1000, NavigationActivity.this);
                    }
                    if (location == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1000, NavigationActivity.this);
                    }
                    if (location == null && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){
                        location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 10000, 1000, NavigationActivity.this);
                    }

                    if (location != null){
                        Helpers.updateLocation(context, location);
                    }
                }
            }
        });

        initData();
    }

    private void checkApplicationPermissions() {
        if (context == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(NavigationActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(NavigationActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (! ActivityCompat.shouldShowRequestPermissionRationale(NavigationActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    ActivityCompat.requestPermissions(NavigationActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 102);
            }
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (! ActivityCompat.shouldShowRequestPermissionRationale(NavigationActivity.this,
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(NavigationActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 103);
            }
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (! ActivityCompat.shouldShowRequestPermissionRationale(NavigationActivity.this,
                    Manifest.permission.RECORD_AUDIO)) {
                ActivityCompat.requestPermissions(NavigationActivity.this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 104);
            }
        }
    }

    private void initData() {
        database = FirebaseDatabase.getInstance();
//        database.setPersistenceEnabled(true);
        userDB = database.getReference("users");
        streamDB = database.getReference("streams");

        if (preferenceHelper == null){
            preferenceHelper = new PreferenceHelper(context, "users");
        }

        if (preferenceStreamHelper == null){
            preferenceStreamHelper = new PreferenceHelper(context, "streams");
        }

        user = new User();
        userDB.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.d(TAG, "onDataChange:"+dataSnapshot.toString());
                Log.d(TAG, "onDataChange:"+user.getName()+":"+currentUser.getDisplayName());
                user = dataSnapshot.getValue(User.class);
                user.setUid(dataSnapshot.getKey());

                new Level(context, user).countLevel(new Level.OnLevelListener() {
                    @Override
                    public void retrieve(int lvl) {
                        level = lvl;
                    }
                });

                if ((user == null || user.getName() == null || user.getName().isEmpty()) && (currentUser.getDisplayName() == null || currentUser.getDisplayName().isEmpty())){
                    Snackbar.make(container, getString(R.string.please_update_your_profile), Snackbar.LENGTH_LONG).show();

                    finish();
                    intent = new Intent(NavigationActivity.this, EditProfileActivity.class);
                    startActivity(intent);
                } else {
                    buttonLiveStream.setEnabled(true);
                }

                if (user != null){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    if ((user.getName() == null || user.getName().isEmpty()) && currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()){
                        hashMap.put("name", currentUser.getDisplayName());
                    }
                    if ((user.getEmail() == null || user.getEmail().isEmpty()) && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()){
                        hashMap.put("email", currentUser.getEmail());
                    }
                    if (hashMap.size() > 0){
                        userDB.child(currentUser.getUid()).updateChildren(hashMap);
                    }

                    if (user.getLocation() == null || user.getLocation().isEmpty()){

                    }

                    if ((user.getCountry() == null || user.getCountry().isEmpty()) && location != null){
                        new LocationAccess(location).execute();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void broadcastAction(View view){
        intent = new Intent(this, LiveStreamActivity.class);
        intent.putExtra("stream", true);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Log.d(TAG, "exitCount:"+exitCount);
        if (exitCount < 1){
            exitCount++;
        } else if (exitCount == 1){
            exitCount++;
            Toast.makeText(this, R.string.click_back_to_exit, Snackbar.LENGTH_LONG).show();
        } else {
            exitCount = 0;
            Helpers.exit(this);
        }
    }

    private void showFragment(int id){
        fragment = null;
        Bundle bundle = new Bundle();
        if (search != null && search.length() > 0){
            bundle.putString("search", search);
        }

        Class fragmentClass = null;
        navigationTab.setVisibility(View.VISIBLE);
        switch(id) {
            case R.id.navigation_nearby:
                lastId = NearbyFragment.MODE_NEAR;
                bundle.putInt("mode", lastId);
                fragmentClass = NearbyFragment.newInstance().getClass();
                break;
            case R.id.navigation_popular:
                lastId = NearbyFragment.MODE_POPULAR;
                bundle.putInt("mode", lastId);
                fragmentClass = NearbyFragment.newInstance().getClass();
                break;
            case R.id.navigation_search:
                lastId = NearbyFragment.MODE_SEARCH;
                bundle.putInt("mode", lastId);
                fragmentClass = NearbyFragment.newInstance().getClass();
                break;
            case R.id.navigation_home:
                bundle.putInt("mode", lastId);
                fragmentClass = NearbyFragment.newInstance().getClass();
                break;
            case R.id.navigation_inbox:
                navigationTab.setVisibility(View.GONE);
                fragmentClass = UserListFragment.class;
                break;
            case R.id.navigation_ranking:
                navigationTab.setVisibility(View.GONE);
                fragmentClass = RankingFragment.class;
                break;
            case R.id.navigation_profile:
                navigationTab.setVisibility(View.GONE);
                fragmentClass = ProfileFragment.class;
                break;
            case R.id.navigation_explore:
//                fragmentClass = ProfileFragment.class;
                fragmentClass = ExploreFragment.class;
                break;
            default:
                bundle.putInt("mode", lastId);
                fragmentClass = NearbyFragment.newInstance().getClass();
                break;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
            fragment.setArguments(bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        fragment.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult", requestCode+","+resultCode);
//        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
//            fragment.onActivityResult(requestCode, resultCode, data);
//            Log.d("onActivityResult", requestCode+","+resultCode);
//        }
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged:"+location.getLatitude()+","+location.getLongitude());
        Helpers.updateLocation(context, location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }



    @Override
    protected void onResume() {
        super.onResume();

        // If running on Android 6 (Marshmallow) and later, check to see if the necessary permissions
        // have been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionsGranted = Helpers.hasPermissions(this, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        } else
            mPermissionsGranted = true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        mPermissionsGranted = true;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // Check the result of each permission granted
                for(int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = false;
                    }
                }
            }
        }
    }

    private void getNewToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        if (task.getResult() != null){
                            String token = task.getResult().getToken();
                            if (currentUser != null){
                                Helpers.updateDeviceToken(context, currentUser.getUid(), token);
                            }
                        }
                    }
                });
    }

    private void enableLastSeen(){
        Context context = getApplicationContext();
        Intent myIntent = new Intent(context, LastSeenService.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,  0, myIntent, 0);
        PendingIntent pendingIntent = PendingIntent.getService(context,  0, myIntent, 0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 60); // first time
        long frequency= 60 * 1000; // in ms
        if (alarmManager != null){
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency, pendingIntent);
        }
        Log.d(TAG, "enableLastSeen");
        Intent notifIntent = new Intent(context, NotificationSchedulingService.class);
        startService(notifIntent);
    }



    private class LocationAccess extends AsyncTask<Double, Integer, String> {

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
                    province = adminArea;
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

            if (province != null && country != null){
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("location", province);
                hashMap.put("country", country);
                userDB.child(currentUser.getUid()).updateChildren(hashMap);
            }
        }
    }
}
