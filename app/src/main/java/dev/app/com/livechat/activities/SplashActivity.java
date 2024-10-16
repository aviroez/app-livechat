package dev.app.com.livechat.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import dev.app.com.livechat.entities.WowzaSetting;
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

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private int backClick = 0;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Intent intent;
    private int exitCount = 0;
    private Timer timer;
    private int i=0;
    private int maxProgress=50;
    private FirebaseDatabase database;
    private HashMap<String, Object> mapSetting = new HashMap<>();
    private PreferenceHelper preferenceHelper;
    private Retrofit retrofit;
    private Context context;
    private PreferenceHelper liveStreamServiceHelper;
    private WowzaCloudApi service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.activity_splash);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setMax(maxProgress);
        progressBar.setProgress(i);

        context = SplashActivity.this;
    }

    @Override
    protected void onStart() {
        super.onStart();

        progressBar.setProgress(++i);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        retrieveServerSetting();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //this repeats every 100 ms
                if (i < maxProgress){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            textView.setText(String.valueOf(i)+"%");
                        }
                    });
                    progressBar.setProgress(i);
                    i++;
                } else {
                    timer.cancel();
                    progressBar.setProgress(i);
                }
            }
        }, 0, maxProgress);
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

    private void retrieveServerSetting(){
        preferenceHelper = new PreferenceHelper(SplashActivity.this, "wowza.settings");
        HashMap<String, String> mapSetting = (HashMap<String, String>) preferenceHelper.all();
        Log.d(TAG, "retrieveServerSetting:mapSetting:" + mapSetting.size());

        database = FirebaseDatabase.getInstance();
        database.getReference("wowza").child("settings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "retrieveServerSetting:onDataChange:"+dataSnapshot.toString());
                if (dataSnapshot.hasChildren()){
                    for (DataSnapshot getSnapshot: dataSnapshot.getChildren()){
                        preferenceHelper.store(getSnapshot.getKey(), getSnapshot.getValue().toString());
                    }
                    timer.cancel();
                    progressBar.setProgress(maxProgress);
                }

//                loadService();
                getStartedActivity();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadService(){
        retrofit = Helpers.initRetrofit(context);
        Log.d(TAG, "loadService:retrofit:"+getString(R.string.wowza_url));

        liveStreamServiceHelper = new PreferenceHelper(context, "wowza.recordings");
        service = retrofit.create(WowzaCloudApi.class);
        service.liveStreams().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                if (response.isSuccessful()){
                try {
                    if (response.body()==null) {
                        getStartedActivity();
                        return;
                    }

                    String body = response.body().string();
                    JSONObject jsonObj = new JSONObject(body);
                    Log.d(TAG, "loadService:JSONObject:"+jsonObj.toString());

                    if (jsonObj.has("live_streams")){
                        final JSONArray jsonArray = jsonObj.getJSONArray("live_streams");
                        Log.d(TAG, "loadService:JSONArray:"+jsonArray.toString());

                        for (int i = 0; i < jsonArray.length(); i++){
                            JSONObject obj = jsonArray.getJSONObject(i);
                            Log.d(TAG, "loadService:JSONObject2:"+obj.toString());
                            if (obj.has("id")){
                                final String recordingId = obj.getString("id");
                                Log.d(TAG, "loadService:getString:"+liveStreamServiceHelper.retrieve(recordingId, 0L));
                                long lastStart = liveStreamServiceHelper.retrieve(recordingId, 0L);
//                                    long lastStart = 0L;
                                Log.d(TAG, "loadService:getString:"+recordingId+","+lastStart+"|"+Helpers.getMinuteCount(lastStart));
                                if (lastStart == 0L || Helpers.getMinuteCount(lastStart) >= 10){
                                    final int finalI = i;
                                    service.start(recordingId).enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            liveStreamServiceHelper.store(recordingId, Helpers.getLastSeen());

                                            if (finalI == jsonArray.length() - 1){
                                                getStartedActivity();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            if (finalI == jsonArray.length() - 1){
                                                getStartedActivity();
                                            }
                                        }
                                    });
                                } else {
                                    if (i == jsonArray.length() - 1){
                                        getStartedActivity();
                                    }
                                }
                            } else {
                                if (i == jsonArray.length() - 1){
                                    getStartedActivity();
                                }
                            }
                        }
                    } else {
                        getStartedActivity();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Crashlytics.log(Log.ERROR, TAG, t.getMessage());
                getStartedActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (timer != null){
            timer.cancel();

//            loadService();
            getStartedActivity();
        } else {
            i=0;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //this repeats every 100 ms
                    if (i < maxProgress){
                        progressBar.setProgress(i);
                        i++;
                    } else {
                        //closing the timer
                        timer.cancel();
                        progressBar.setProgress(i);

//                      loadService();
                        getStartedActivity();
                    }
                }
            }, 0, maxProgress);
        }
    }

    private void getStartedActivity(){
        intent = new Intent(this, GetStartedActivity.class);
        startActivity(intent);
        finish();
    }
}
