package dev.app.com.livechat.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import dev.app.com.livechat.BuildConfig;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.R;
import dev.app.com.livechat.utils.PreferenceHelper;
import dev.app.com.livechat.utils.WowzaCloudApi;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class GetStartedActivity extends AppCompatActivity {
    private static final String TAG = GetStartedActivity.class.getSimpleName();
    private int exitCount = 0;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Intent intent;
    private WowzaCloudApi service;
    private Retrofit retrofit;
    private Context context;
    private PreferenceHelper liveStreamServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String sign= Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.d("KeyHash", sign);
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Log.d("KeyHash", BuildConfig.APPLICATION_ID);
        Log.d("KeyHash", getString(R.string.google_web_client_id));
        getKeyHash("SHA");
        getKeyHash("MD5");

        context = getApplicationContext();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = "token:"+token;
                        Log.d(TAG, msg);
//                        Toast.makeText(GetStartedActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
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

    public void getStartedAction(View view){
        if (currentUser != null){
            intent = new Intent(this, NavigationActivity.class);
            startActivity(intent);
        } else {
            intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        finish();
    }

    private void getKeyHash(String key) {
        try {
            final PackageInfo info = getPackageManager()
                    .getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance(key);
                md.update(signature.toByteArray());

                final byte[] digest = md.digest();
                final StringBuilder toRet = new StringBuilder();
                for (int i = 0; i < digest.length; i++) {
                    if (i != 0) toRet.append(":");
                    int b = digest[i] & 0xff;
                    String hex = Integer.toHexString(b);
                    if (hex.length() == 1) toRet.append("0");
                    toRet.append(hex);
                }

                Log.d("KeyHash", key + ":" + toRet.toString());
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.d("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.d("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.d("exception", e.toString());
        }
    }

}
