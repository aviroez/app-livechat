package dev.app.com.livechat.activities;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.app.com.livechat.BuildConfig;
import dev.app.com.livechat.adapters.ZoomOutPageTransformer;
import dev.app.com.livechat.entities.Level;
import dev.app.com.livechat.entities.Tag;
import dev.app.com.livechat.fragments.LiveStreamFragment;
import dev.app.com.livechat.fragments.ProfileFragment;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.R;
import dev.app.com.livechat.adapters.RecyclerChatAdapter;
import dev.app.com.livechat.entities.Chat;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.fragments.WatchStreamFragment;

public class WatchStreamActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private static final String TAG = WatchStreamActivity.class.getSimpleName();
    private String[] mRequiredPermissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter pagerAdapter;
    private Intent intent;
    private User user;
    private ArrayList<User> listUser;
    private Stream stream;
    private String uid;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference userDB;
    private DatabaseReference streamDB;

    // Properties needed for Android 6+ permissions handling
    private boolean mPermissionsGranted = true;
    private ArrayList<String> streamKeys = new ArrayList<String>();
    private int position;
    private String redirectString;
    private String streamId;
    private Dialog dialog;
    private WatchStreamActivity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_stream_activity);

        intent = getIntent();
        position = intent.getIntExtra("position", 0);
        uid = intent.getStringExtra("uid");
        streamId = intent.getStringExtra("streamId");
        redirectString = intent.getStringExtra("redirectString");
        streamKeys = intent.getStringArrayListExtra("streamKeys");
        Log.d(TAG, "onCreate:"+uid+","+streamId+":"+redirectString+"|"+streamKeys);

        if (streamKeys == null || streamKeys.size() <= 0){
            streamKeys = new ArrayList<>();
            streamKeys.add(uid+"/"+streamId);
        }

        context = WatchStreamActivity.this;
        initFirebase();

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new WatchStreamPagerAdapter(getSupportFragmentManager(), streamKeys);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        mPager.setAdapter(pagerAdapter);
        mPager.setCurrentItem(position);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected:"+position);
                checkStatusStream(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        checkStatusStream(position);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
    protected void onPause() {
        super.onPause();
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

    @Override
    public void onBackPressed() {
        intent = new Intent(this, NavigationActivity.class);
        if (redirectString != null && redirectString.equals(ProfileFragment.class.getSimpleName())){
            intent.putExtra("redirectString", redirectString);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initFirebase(){
        if (mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }
        if (currentUser == null){
            currentUser = mAuth.getCurrentUser();

            if (currentUser == null){
                Helpers.logout(this);
            }
        }
        if (database == null){
            database = FirebaseDatabase.getInstance();
        }
        if (userDB == null){
            userDB = database.getReference("users");
        }
        if (streamDB == null){
            streamDB = database.getReference("streams");
        }
    }

    private void checkStatusStream(int position){
        String streamKey = streamKeys.get(position);
        String[] tokens = streamKey.split("/");
        final String uid = tokens[0];
        final String streamId = tokens[1];

        userDB.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "userDB:"+dataSnapshot.toString());
                user = dataSnapshot.getValue(User.class);
                if (user != null && dataSnapshot.getKey() != null){
                    user.setUid(dataSnapshot.getKey());

                    streamDB.child(uid).child(streamId).addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

//                        streamDB.child(uid).child(streamId).child("watched").child(currentUser.getUid()).setValue(Helpers.getLastSeen());
                            Log.d(TAG, "status:onDataChange:"+dataSnapshot.toString());
                            stream = dataSnapshot.getValue(Stream.class);
                            stream.setStreamId(streamId);
                            user.setStream(stream);

                            float lastOnline = Helpers.getMinuteCount(stream.getLastActiveStream());
                            Log.d(TAG, "status:onDataChange:"+stream.getStatus()+","+lastOnline+":"+uid+","+streamId);
                            if (stream.getStatus() == getResources().getInteger(R.integer.stream_status_stop) || lastOnline >= 3){
                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialogStop();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    public void dialogStop(){
        if (dialog == null){
            dialog = new Dialog(context);
        }
        dialog.setContentView(R.layout.layout_chat_ended);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dialog.show();

        TextView textViewer = dialog.findViewById(R.id.text_viewer);
        TextView textNewFollower = dialog.findViewById(R.id.text_new_followers);
        TextView textChatCount = dialog.findViewById(R.id.text_chat_count);
        TextView textTimerCount = dialog.findViewById(R.id.text_timer_count);
        TextView textFinishStreaming = dialog.findViewById(R.id.text_finish_streaming);
        TextView textShareLive = dialog.findViewById(R.id.text_share_live);

        if (user != null){
            textViewer.setText(Helpers.getNumberCountFormat(stream.getWatchCount()));
            if (user.getFollowers() != null && user.getFollowers().size() > 0){
                textNewFollower.setText(Helpers.getNumberCountFormat(user.getFollowers().size()));
            }
            int chatCount = 0;

            int counter = stream.getDuration();
            if (stream.getChats() != null){
                chatCount = stream.getChats().size();
            }
            textChatCount.setText(Helpers.getNumberCountFormat(chatCount));

//            String minute = Helpers.getMinute(counter);
//            String second = Helpers.getSecond(counter);

            String minute = Helpers.getMinute(counter / 1000);
            String second = Helpers.getSecond(counter / 1000);
            textTimerCount.setText(minute+":"+second);

            ImageButton buttonBack = dialog.findViewById(R.id.button_back);
            ImageButton buttonDownload = dialog.findViewById(R.id.button_download);
            Button buttonShare = dialog.findViewById(R.id.button_share);
            textFinishStreaming.setText("Streaming is ended");
            textShareLive.setText("Share this streaming to your friend");

            buttonBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent = new Intent(context, NavigationActivity.class);
                    startActivity(intent);
                }
            });

            if (stream.getDownload_url() != null){
                buttonDownload.setVisibility(View.VISIBLE);
            } else {
                buttonDownload.setVisibility(View.GONE);
            }

            buttonDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, "Downloading video", Toast.LENGTH_LONG).show();
                    String downloadUrl = stream.getDownload_url();
                    if (downloadUrl != null){
                        String path =  Environment.getExternalStorageDirectory().getPath() + "/" + BuildConfig.APPLICATION_ID;
                        String fileName = path.substring(path.lastIndexOf('/') + 1);
                        Log.d(TAG, path);
                        Log.d(TAG, fileName);

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                        request.setDescription(stream.getTitle());
                        // in order for this if to run, you must use the android 3.2 to compile your app
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }
//                        request.setDestinationInExternalPublicDir(path, fileName);
                        request.setDestinationInExternalFilesDir(context, path, fileName);

                        // get download service and enqueue file
                        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);

                    }
                }
            });

            buttonShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    String host = getString(R.string.web_host) + "/watch.html?uid=" + user.getUid();
                    String shareBody = getString(R.string.please_watch_my_show_in);
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.my_show));
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody + " " +  host);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            });
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class WatchStreamPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<String> streamKeys = new ArrayList<String>();
        public WatchStreamPagerAdapter(FragmentManager fm, ArrayList<String> streamKeys) {
            super(fm);
            this.streamKeys = streamKeys;
        }

        @Override
        public Fragment getItem(int position) {
            WatchStreamFragment watchStreamFragment = new WatchStreamFragment();
            String streamKey = streamKeys.get(position);
            String[] tokens = streamKey.split("/");
            String uid = tokens[0];
            String streamId = tokens[1];
            Log.d(TAG, "streamKey:"+streamKey+"|"+uid+","+streamId+":"+position);

            Bundle bundle = new Bundle();
            bundle.putString("streamId", streamId);
            bundle.putString("uid", uid);
            bundle.putString("redirectString", redirectString);
            bundle.putString("streamKey", streamKey);
            bundle.putStringArrayList("streamKeys", streamKeys);
            bundle.putInt("position", position);
            watchStreamFragment.setArguments(bundle);
            return watchStreamFragment;
        }

        @Override
        public int getCount() {
            return streamKeys.size();
        }
    }
}
