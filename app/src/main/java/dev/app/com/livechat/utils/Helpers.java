package dev.app.com.livechat.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import dev.app.com.livechat.R;
import dev.app.com.livechat.activities.LoginActivity;
import dev.app.com.livechat.entities.User;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;

public class Helpers {
    private static FirebaseAuth mAuth;
    private static FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private static DatabaseReference userDB;

    public static final int SECOND_DIVIDER = 1000; // 1000
    public static final int MINUTE_DIVIDER = 60000; // 60 * 1000
    public static final int HOUR_DIVIDER = 3600000; // 60 * 60 * 1000
    public static final int DAY_DIVIDER = 86400000; // 24 * 60 * 60 * 1000
    private static final String TAG = Helpers.class.getSimpleName();

    public static boolean checkPermission(Context context, String accessFineLocation) {

        int res = context.checkCallingOrSelfPermission(accessFineLocation);
        return (res == PackageManager.PERMISSION_GRANTED);

    }

    public static void exit(Context context){
        Activity activity = (Activity) context;
        activity.finishAndRemoveTask();
        activity.finish();
        System.exit(0);

        int pid = Process.myPid();
        Process.killProcess(pid);

        Process.sendSignal(pid, Process.SIGNAL_KILL);
    }

    public static String saveToInternalStorage(Context context, Bitmap bitmapImage, String dir, String fileName, boolean forceSave){
        if (dir == null || dir.isEmpty()){
            dir = "images";
        }
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(dir, MODE_PRIVATE);
        // Create imageDir
        File path = new File(directory,fileName);

        if(path.exists() && !forceSave){
            return path.getAbsolutePath();
        } else {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return directory.getAbsolutePath();
        }
    }
    public static boolean loadImageFromStorage(Context context, String path, String fileName, int viewId)
    {
        try {
            File f=new File(path, fileName);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img = (ImageView) ((Activity) context).findViewById(viewId);
            img.setImageBitmap(b);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static boolean setUserOnline(Context context, boolean status){
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null){
            long lastSeen = getLastSeen();
            DatabaseReference userDB = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            Map<String, Object> update = new HashMap<>();
            update.put("online", status);
            if (status){
                update.put("last_seen", lastSeen);
            }

            userDB.updateChildren(update);

            PreferenceHelper userHelper = new PreferenceHelper(context, "user");
            userHelper.store("last_seen", lastSeen);
            userHelper.store("online", status);
            Log.d(TAG, "setUserOnline:"+lastSeen);
        }

        return false;
    }

    public static boolean updateUser(Context context, User user){
        if (user != null){
            PreferenceHelper userHelper = new PreferenceHelper(context, "user");
            userHelper.store("name", user.getName());
            userHelper.store("email", user.getEmail());
        }

        return false;
    }

    public static long getLastSeen(){
        return System.currentTimeMillis();
    }

    public static String getName(FirebaseUser currentUser, User user) {
        if (currentUser != null && user != null) {
            String name = currentUser.getDisplayName();
            if (name == null || name.isEmpty()) {
                name = user.getName();
            }
            if (name == null || name.isEmpty()) {
                name = user.getEmail();
            }
            return name;
        }

        return null;
    }

    public static String getName(User user) {
        if (user != null){
            String name = user.getName();
            if (name == null || name.isEmpty()){
                name = user.getEmail();
            }
            return name;
        }
        return null;
    }

    private boolean canResolveMobileLiveIntent(Context context) {
        Intent intent = new Intent("com.google.android.youtube.intent.action.CREATE_LIVE_STREAM")
                .setPackage("com.google.android.youtube");
        PackageManager pm = context.getPackageManager();
        List resolveInfo =
                pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo != null && !resolveInfo.isEmpty();
    }


    private void validateMobileLiveIntent(Context context) {
        if (canResolveMobileLiveIntent(context)) {
            // Launch the live stream Activity
        } else {
            // Prompt user to install or upgrade the YouTube app
        }
    }

    public static void follow(String uid){
        long lastSeen = Helpers.getLastSeen();
        userDB = FirebaseDatabase.getInstance().getReference("users");
        userDB.child(uid).child("followers").child(currentUser.getUid()).setValue(lastSeen);
        userDB.child(currentUser.getUid()).child("follows").child(uid).setValue(lastSeen);
    }

    public static void unfollow(String uid){
        userDB = FirebaseDatabase.getInstance().getReference("users");
        userDB.child(uid).child("followers").child(currentUser.getUid()).removeValue();
        userDB.child(currentUser.getUid()).child("follows").child(uid).removeValue();
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public static String formatPhoneNumber(String phone){
        if (phone != null && phone.length() > 0){
            phone = phone.replaceAll("[^\\d]", "" );
            if (phone.charAt(0) == '0'){
                return "+62" + phone.replaceFirst("^0+(?!$)", "");
            } else if (phone.charAt(0) == '6'){
                return "+" + phone;
            }
        }
        return phone;
    }

    public static GoogleSignInClient getGoogleApiClient(Context context, String email){
        GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.google_web_client_id))
                .requestEmail();
        if (email != null && email.length() > 0){
            builder.setAccountName(email);
        }
        GoogleSignInOptions gso = builder.build();
        return GoogleSignIn.getClient(context, gso);
    }

    public static void logout(Context context){
        FirebaseAuth.getInstance().signOut();
        GoogleSignInClient googleSignInClient = getGoogleApiClient(context, null);
        if (googleSignInClient != null){
            googleSignInClient.signOut();
        }
        new PreferenceHelper(context, "user").clearAll();
        new PreferenceHelper(context, "user_chats").clearAll();
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    public static String formatDate(Date date){
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        return sdf.format(date);
    }

    public static void updateLocation(Context context, Location location){
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null){
            Log.d("updateLocation", "gpsStatus:success"+location.getLatitude()+","+location.getLongitude());
            PreferenceHelper preferenceHelper = new PreferenceHelper(context, "user");
            double latitude = preferenceHelper.retrieve("latitude", 0f);
            double longitude = preferenceHelper.retrieve("longitude", 0f);

            if (latitude != 0 && longitude != 0 && location.getLatitude() == latitude && location.getLongitude() == longitude){
                return;
            }

            Map<String, Object> update = new HashMap<>();
            update.put("latitude", location.getLatitude());
            update.put("longitude", location.getLongitude());
            FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).updateChildren(update);

            preferenceHelper.store("latitude", (float) location.getLatitude());
            preferenceHelper.store("longitude", (float) location.getLongitude());
        }
    }

    public static void moveActivity(Context context, java.lang.Class<?> cls){
        moveActivity(context, cls, null);
    }

    public static void moveActivity(Context context, java.lang.Class<?> cls, HashMap<String, Object> hashMap){
        if (context != null){
            if ( context instanceof Activity ) {
                ((Activity)context).finish();
            }
            Intent intent = new Intent(context, cls);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            if (hashMap != null && hashMap.size() > 0){
                for (Map.Entry<String, Object> map: hashMap.entrySet()){
                    if (map.getValue() instanceof Boolean){
                        intent.putExtra(map.getKey(), (boolean) map.getValue());
                    } else if (map.getValue() instanceof Integer){
                        intent.putExtra(map.getKey(), (Integer) map.getValue());
                    } else if (map.getValue() instanceof Integer){
                        intent.putExtra(map.getKey(), (int) map.getValue());
                    } else if (map.getValue() instanceof Float){
                        intent.putExtra(map.getKey(), (float) map.getValue());
                    } else if (map.getValue() instanceof String){
                        intent.putExtra(map.getKey(), (String) map.getValue());
                    }
                }
            }
            context.startActivity(intent);
        }
    }


    public static boolean hasPermissions(Context context, String[] permissions) {
        for(String permission : permissions)
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public final static String getTimeAgo(long activeStream){
        String timeValue = "";
        if (activeStream > 0){
            long now = getLastSeen();
            long days = (now - activeStream) / Helpers.DAY_DIVIDER;
            long hours = (now - activeStream) / Helpers.HOUR_DIVIDER;
            long minutes = (now - activeStream) / Helpers.MINUTE_DIVIDER;
            long seconds = (now - activeStream) / Helpers.SECOND_DIVIDER;
            if (days == 1){
                timeValue = days + " day ago";
            } else if (days > 1){
                timeValue = days + " days ago";
            } else if (hours == 1){
                timeValue = hours + " hour ago";
            } else if (hours > 1){
                timeValue = hours + " hours ago";
            } else if (minutes == 1){
                timeValue = minutes + " minute ago";
            } else if (minutes > 1){
                timeValue = minutes + " minutes ago";
            } else {
                timeValue = "Just now";
            }
        }
        Log.d(TAG, "getTimeAgo:"+timeValue);
        return timeValue;
    }

    public final static float getMinuteCount(long activeStream){
        if (activeStream > 0){
            long now = getLastSeen();
            return (now - activeStream) / (float) Helpers.MINUTE_DIVIDER;
        }
        return 0;
    }

    public static void sendFCM(Context context, String message, String uid, String source){
        long time = Helpers.getLastSeen();
        String senderId = context.getResources().getString(R.string.firebase_id);
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(senderId + "@gcm.googleapis.com")
                .setMessageId(uid+"_"+time)
                .addData("message", message)
                .addData("source",source)
                .addData("uid",uid)
                .build());
    }

    public static void sendFCMTopic(Context context, String message, String uid, String source){
        long time = Helpers.getLastSeen();
        String senderId = context.getResources().getString(R.string.firebase_id);
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(senderId + "@gcm.googleapis.com")
                .setMessageId(uid+"_"+time)
                .addData("message", message)
                .addData("source",source)
                .addData("uid",uid)
                .build());
    }

    public static void updateDeviceToken(Context context, String uid, String deviceToken){
        if (deviceToken != null){
            PreferenceHelper preferenceHelper = new PreferenceHelper(context, "setting");
            String deviceTokenPref = preferenceHelper.retrieve("device_token", null);
            if (deviceTokenPref == null || (deviceTokenPref != null && !deviceTokenPref.equals(deviceToken))){
                preferenceHelper.store("device_token", deviceToken);
                preferenceHelper.store("device_token_created", Helpers.getLastSeen());

                FirebaseDatabase.getInstance().getReference("users").child(uid).child("device_token").setValue(deviceToken);
            }
        }
    }

    public static void sendNotif(String uid, String title, String message, String sender_id, int code){
        // code 0 = chat, code 1 = follow
        long time  = getLastSeen();
        HashMap<String, Object> mapNotif = new HashMap<>();
        mapNotif.put("title", title);
        mapNotif.put("message", message);
        mapNotif.put("code", code);
        mapNotif.put("time", time);
        mapNotif.put("sender_id", sender_id);
        FirebaseDatabase.getInstance().getReference("notifications").child(uid).push().setValue(mapNotif);
    }

    public static String getNumberCountFormat(long value){
        NavigableMap<Long, String> suffixes = new TreeMap<>();
        suffixes.put(1_000L, "rb");
        suffixes.put(1_000_000L, "jt");
        suffixes.put(1_000_000_000L, "m");
//        suffixes.put(1_000_000_000_000L, "t");
//        suffixes.put(1_000_000_000_000_000L, "p");
//        suffixes.put(1_000_000_000_000_000_000L, "e");

        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return getNumberCountFormat(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + getNumberCountFormat(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static String getDistance(double distance) {
        String result = "0 km";
        DecimalFormat df = new DecimalFormat("###.#");
        result = df.format(distance) + " km";
        return result;
    }

    public static Address getCityName(Context context, double latitude, double longitude) {
        if (latitude != 0 && longitude != 0) {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                return addresses.get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static String getMinute(int counter) {
        String minute = String.valueOf((int) counter / 60);
        return minute.length() == 1 ? "0" + minute : minute;
    }

    public static String getSecond(int counter) {
        String second = String.valueOf(counter % 60);
        return second.length() == 1 ? "0" + second : second;
    }

    public static String getHour(int counter) {
        String hour = String.valueOf((int) counter / 60 / 60);
        return hour.length() == 1 ? "0" + hour : hour;
    }

    public static boolean isAppInLowMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        assert activityManager != null;
        activityManager.getMemoryInfo(memoryInfo);
        Log.d(TAG, "isAppInLowMemory:"+memoryInfo.availMem);

        return memoryInfo.lowMemory;
    }

    public static Retrofit initRetrofit(final Context context){

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(interceptor);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .header("wsc-api-key", context.getString(R.string.wsc_api_key))
                        .header("wsc-access-key", context.getString(R.string.wsc_access_key))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
        });

        OkHttpClient client = httpClient.build();
        return new Retrofit.Builder()
                .baseUrl(context.getString(R.string.wowza_url))
                .client(client)
                .build();
    }

}
