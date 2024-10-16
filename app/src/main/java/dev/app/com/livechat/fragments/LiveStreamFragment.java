package dev.app.com.livechat.fragments;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Helper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.plumillonforge.android.chipview.Chip;
import com.plumillonforge.android.chipview.ChipView;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.data.WOWZDataMap;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.geometry.WOWZSize;
import com.wowza.gocoder.sdk.api.graphics.WOWZColor;
import com.wowza.gocoder.sdk.api.h264.WOWZProfileLevel;
import com.wowza.gocoder.sdk.api.logging.WOWZLog;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import dev.app.com.livechat.BuildConfig;
import dev.app.com.livechat.R;
import dev.app.com.livechat.activities.MyProfileActivity;
import dev.app.com.livechat.activities.NavigationActivity;
import dev.app.com.livechat.adapters.ListUserAdapter;
import dev.app.com.livechat.adapters.MainChipViewAdapter;
import dev.app.com.livechat.adapters.RecyclerChatAdapter;
import dev.app.com.livechat.entities.Chat;
import dev.app.com.livechat.entities.Level;
import dev.app.com.livechat.entities.Recording;
import dev.app.com.livechat.entities.Tag;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.entities.WowzaSetting;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.PreferenceHelper;
import dev.app.com.livechat.utils.ShowImageHelper;
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

public class LiveStreamFragment extends Fragment
        implements WOWZStatusCallback, View.OnTouchListener {

    // Properties needed for Android 6+ permissions handling
    private static final String TAG = LiveStreamFragment.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private boolean mPermissionsGranted = true;
    private String[] mRequiredPermissions = new String[] {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private View topLayout;
    private String streamId;
    private HashMap<String, Object> mapWatched = new HashMap<>();
    private View layoutShare;
    private View layoutView;
    private TextView textName;
    private Level level;
    private TextView textLevel;
    private TimerTask timerTask;
    private Handler handler;
    private int counter = 0;
    private TextView textWatchCount;
    private ImageView imageView1, imageView2;
    private Timer timerCounter;
    private TimerTask timerTaskCounter;
    private int statusButton;
    private Retrofit retrofit;
    private WowzaCloudApi service;
    private ImageButton buttonDownload;
    private String downloadUrl = null;
    private String title;
    private boolean showLowMemoryMessage = true;
    private WOWZStatus audioStatus;
    private TimerTask task;

    private static Object sBroadcastLock = new Object();
    private static boolean sBroadcastEnded = true;
    private int videoBitRate = 1500; // low=50000, medium=100000, high=548000
    private static final int BITRATE_LOW = 50000;
    private static final int BITRATE_MEDIUM = 100000;
    private static final int BITRATE_HIGH = 548000;
    private WOWZSize wowzSize;
    private Callback<ResponseBody> customStartResponse;
    private View layoutWatch;
    private View layoutWatchChild;
    private boolean statusComponent = true;
    private GestureDetectorCompat detector;

    private interface ButtonFloat {
        int RECORD = R.mipmap.ic_record;
        int PLAY = R.mipmap.ic_play;
        int PAUSE = R.mipmap.ic_pause;
        int STOP = R.mipmap.ic_stop;
    }

    // The GoCoder SDK camera view
    private WOWZCameraView goCoderCameraView;
    private ImageView imageProfile;
    private TextView textTitle;
    private TextView textView;
    private RecyclerView recyclerChat;
    private EditText textChat;
    // The top-level GoCoder API interface
    private WowzaGoCoder goCoder;
    private List<Chat> listChat = new ArrayList<>();
    private RecyclerChatAdapter recyclerChatAdapter;

    // The GoCoder SDK audio device
    private WOWZAudioDevice goCoderAudioDevice;

    // The GoCoder SDK broadcaster
    private WOWZBroadcast goCoderBroadcaster;

    // The broadcast configuration settings
    private WOWZBroadcastConfig goCoderBroadcastConfig;

    private Context context;
    private Activity activity;
    private Intent intent;
    private User user;
    private Stream stream;
    private PreferenceHelper preferenceHelper;
    private WowzaSetting wowzaSetting;
    private String uid;
    private DatabaseReference streamDB;
    private StatusCallback statusCallback;
    private View layout;
    private Map<String, String> mapUser = new HashMap<>();
    private FirebaseDatabase database;
    private DatabaseReference chatDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userDB;
    private int buttonFollowType = 0; // 0 default, 1 follow, 2 unfollow
    private String name;
    private Dialog dialog;
    private TextView textPopupFollowers;
    private TextView textPopupFollows;
    private Button buttonPopupFollow;
    private TextView textTimer;
    private DatabaseReference tagsDB;
    private String imageUser;
    private StorageReference storageReference;
    private ShowImageHelper showImageHelper;
    private ImageButton buttonSendChat;
    private ChipView chipView;
    private List<Chip> chipList = new ArrayList<Chip>();
    private MainChipViewAdapter tagChipViewAdapter;
    private User userPopup;
    private TextView textPopupVideos;
    private ImageButton imageClose;
    private Timer timer;
    private PreferenceHelper preferenceSettingHelper;
    private int frameRateSet;
    private boolean muteSet;
    private FloatingActionButton buttonRecord;
    private WOWZStreamingError configValidationError;
    private int retry = 0;

    public static LiveStreamFragment newInstance() {
        return new LiveStreamFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_stream, container, false);
        layout = view.findViewById(R.id.layout_stream);
        topLayout = view.findViewById(R.id.top_layout);
        textChat = view.findViewById(R.id.text_chat);
        imageProfile = view.findViewById(R.id.image_profile);
        textName = view.findViewById(R.id.text_name);
        textTitle = view.findViewById(R.id.text_title);
        textView = view.findViewById(R.id.text_view);
        textWatchCount = view.findViewById(R.id.text_watch_count);
        textLevel = view.findViewById(R.id.text_level);
        textTimer = view.findViewById(R.id.text_timer);
        goCoderCameraView = view.findViewById(R.id.camera_preview);
        recyclerChat = view.findViewById(R.id.recycler_chat);
        buttonSendChat = view.findViewById(R.id.send_chat);
        imageClose = view.findViewById(R.id.image_close);
        chipView = view.findViewById(R.id.chipview);
        buttonRecord = view.findViewById(R.id.button_record);
        imageView1 = view.findViewById(R.id.image_view_1);
        imageView2 = view.findViewById(R.id.image_view_2);
        layoutWatch = view.findViewById(R.id.layout_watch);
        layoutWatchChild = view.findViewById(R.id.layout_watch_child);
        layoutView = view.findViewById(R.id.layout_view);
        layoutShare = view.findViewById(R.id.layout_share);

        buttonRecord.setImageResource(ButtonFloat.RECORD);
        buttonRecord.setTag(ButtonFloat.RECORD);
        buttonRecord.setEnabled(false);

        recyclerChatAdapter = new RecyclerChatAdapter(getContext(), listChat, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Chat chat = listChat.get(position);
                layoutProfileAction(view, chat.getUid());
            }
        });

        textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutProfileAction(view, currentUser.getUid());
            }
        });

        layoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPopupAction(view);
            }
        });
        detector = new GestureDetectorCompat(context, new MyGestureListener());
        layoutWatch.setOnTouchListener(this);

        recyclerChat.setAdapter(recyclerChatAdapter);
        recyclerChat.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        llm.setStackFromEnd(true);
        llm.setReverseLayout(false);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerChat.setLayoutManager(llm);

        context = getContext();
        activity = getActivity();

        if (activity != null && activity.getWindow() != null){
            activity.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        intent = activity.getIntent();
        uid = intent.getStringExtra("uid");
        user = intent.getParcelableExtra("user");
        streamId = intent.getParcelableExtra("stream_id");
        if (user != null && user.getStream() != null){
            stream = user.getStream();

            retrieveLevel(user);
        } else {
            stream = intent.getParcelableExtra("stream");
        }

        chipView.setChipList(chipList);
        tagChipViewAdapter = new MainChipViewAdapter(getContext(), R.layout.chip);
        chipView.setAdapter(tagChipViewAdapter);
        chipView.setChipLayoutRes(R.layout.chip);

        buttonSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChatAction(view);
            }
        });

        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeAction(view);
            }
        });

        buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onToggleBroadcast(view);
            }
        });

        layoutShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewShareAction(view);
            }
        });

        textChat.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    sendChatAction(null);
                    return true;
                }
                return false;
            }
        });

        showImageHelper = new ShowImageHelper(context);

        return view;
    }

    private void viewShareAction(View view) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String host = getString(R.string.web_host) + "/watch.html?uid=" + uid;
        String shareBody = getString(R.string.please_watch_my_show_in);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.my_show));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody + " " +  host);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void setToRecord(){
        int tag = (Integer) buttonRecord.getTag();
        if (tag != ButtonFloat.RECORD) {
            buttonRecord.setImageResource(ButtonFloat.RECORD);
            buttonRecord.setTag(ButtonFloat.RECORD);
        } else {
            buttonRecord.setImageResource(ButtonFloat.STOP);
            buttonRecord.setTag(ButtonFloat.STOP);
        }
    }

    private void setToPlay(){
        int tag = (Integer) buttonRecord.getTag();
        if (tag != ButtonFloat.PLAY) {
            buttonRecord.setImageResource(ButtonFloat.PLAY);
            buttonRecord.setTag(ButtonFloat.PLAY);
        } else {
            buttonRecord.setImageResource(ButtonFloat.STOP);
            buttonRecord.setTag(ButtonFloat.STOP);
        }
    }

    private void setToPause(){
        int tag = (Integer) buttonRecord.getTag();
        if (tag != ButtonFloat.PAUSE) {
            buttonRecord.setImageResource(ButtonFloat.PAUSE);
            buttonRecord.setTag(ButtonFloat.PAUSE);
        } else {
            buttonRecord.setImageResource(ButtonFloat.STOP);
            buttonRecord.setTag(ButtonFloat.STOP);
        }
    }

    private void setToStop(){
        int tag = (Integer) buttonRecord.getTag();
        if (tag != ButtonFloat.STOP) {
            buttonRecord.setImageResource(ButtonFloat.STOP);
            buttonRecord.setTag(ButtonFloat.STOP);
        } else {
            buttonRecord.setImageResource(ButtonFloat.RECORD);
            buttonRecord.setTag(ButtonFloat.RECORD);
            dialogStop();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (activity != null) {
            final InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null){
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }

    private void showWatched(){
        if (uid != null && streamId != null){
            streamDB.child(uid).child(streamId).child("watched").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    Log.d(TAG, "showWatched:"+dataSnapshot.toString());
                    setViewCount(dataSnapshot.getChildrenCount());
                    if (dataSnapshot.hasChildren() && dataSnapshot.getChildrenCount() > 0){
                        for (DataSnapshot snap: dataSnapshot.getChildren()){
                            mapWatched.put(snap.getKey(), snap.getValue());
                            if (dataSnapshot.getChildrenCount() % 2 == 0){
                                showImageHelper.showImageProfile(snap.getKey(), imageView2);
                            } else if (dataSnapshot.getChildrenCount() % 2 == 0){
                                showImageHelper.showImageProfile(snap.getKey(), imageView1);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void setViewCount(long count){
        String countString = "";
        if (count > 1){
            countString = count + " " + context.getString(R.string.views);
        } else {
            countString = count + " " + context.getString(R.string.view);
        }
        textView.setText(countString);
        textWatchCount.setText(Helpers.getNumberCountFormat(count));
    }

    public void sendChatAction(View view){
        Chat chat = new Chat();
        String text = textChat.getText().toString();

        if (!text.isEmpty()){
            long time = System.currentTimeMillis();
            chat.setMessage(text);
            chat.setUid(currentUser.getUid());
            if (uid != null && streamId != null){
                streamDB.child(uid).child(streamId).child("chats").child(String.valueOf(time)).setValue(chat);
            }

            textChat.setText("");
        }
    }

    private void liveStream(Stream stream){
        goCoder = WowzaGoCoder.init(context, getString(R.string.wowza_gocoder_key));

        // Start the camera preview display
        if (mPermissionsGranted && goCoderCameraView != null) {

//            goCoderCameraView.setCameraConfig(goCoderBroadcastConfig);
//            goCoderCameraView.setScaleMode(GoCoderSDKPrefs.getScaleMode(sharedPrefs));
//            goCoderCameraView.setVideoBackgroundColor(WOWZColor.DARKGREY);

            if (goCoderCameraView.isPreviewPaused()) {
                goCoderCameraView.onResume();
            } else {
                goCoderCameraView.startPreview();
            }
        }

        if (goCoder == null) {
            // If initialization failed, retrieve the last error and display it
            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
            Toast.makeText(context,
                    "GoCoder SDK error: " + goCoderInitError.getErrorDescription(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Create an audio device instance for capturing and broadcasting audio
        goCoderAudioDevice = new WOWZAudioDevice();
        goCoderAudioDevice.setAudioEnabled(true);

        // Create a broadcaster instance
        goCoderBroadcaster = new WOWZBroadcast();

        // Create a configuration instance for the broadcaster
        goCoderBroadcastConfig = new WOWZBroadcastConfig();
        wowzSize = new WOWZSize(WOWZMediaConfig.DEFAULT_VIDEO_FRAME_WIDTH, WOWZMediaConfig.DEFAULT_VIDEO_FRAME_HEIGHT);
        goCoderBroadcastConfig.setVideoFrameSize(wowzSize);
        goCoderBroadcastConfig.getVideoSourceConfig().setVideoFrameSize(wowzSize);
        goCoderBroadcastConfig.getVideoSourceConfig().setVideoFramerate(15);
        goCoderBroadcastConfig.setVideoBitRate(videoBitRate);

        // Designate the camera preview as the video source
        goCoderBroadcastConfig.setVideoBroadcaster(goCoderCameraView);

        wowzaSetting = stream.getWowzaSetting();

        if (wowzaSetting != null && wowzaSetting.getHost() != null) {
            updateStatusStreamer();

            goCoderBroadcastConfig.setHostAddress(wowzaSetting.getHost());
            goCoderBroadcastConfig.setPortNumber(1935);
            goCoderBroadcastConfig.setApplicationName(wowzaSetting.getApp());
            goCoderBroadcastConfig.setStreamName(wowzaSetting.getName());

            if (wowzaSetting.isAuth()){
                goCoderBroadcastConfig.setUsername("devwalle");
                goCoderBroadcastConfig.setPassword("devwalle2019");
            }

            Log.d("goCoderBroadcastConfig", goCoderBroadcastConfig.getHostAddress()+"/"+goCoderBroadcastConfig.getApplicationName()+":"+goCoderBroadcastConfig.getPortNumber()+":"+goCoderBroadcastConfig.getStreamName());

            // Designate the audio device as the audio broadcaster
            goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);
            goCoderAudioDevice.startAudioSampler();

            goCoderBroadcastConfig.setVideoEnabled(true);

            goCoderBroadcastConfig.setVideoFrameWidth(640);
            goCoderBroadcastConfig.setVideoFrameHeight(480);
            goCoderBroadcastConfig.setVideoFramerate(15);

//            goCoderBroadcastConfig.setVideoKeyFrameInterval(WOWZMediaConfig.DEFAULT_VIDEO_KEYFRAME_INTERVAL);
            goCoderBroadcastConfig.setABREnabled(true);
            goCoderBroadcastConfig.setHLSEnabled(false);
            goCoderBroadcastConfig.setHLSBackupURL(null);
//        mediaConfig.setPlayerExampleAutoconfig(sharedPrefs.getString("wz_player_example_config","None"));

            int profile = -1;
            int level = -1;
            if (profile != -1 && level != -1) {
                WOWZProfileLevel profileLevel = new WOWZProfileLevel(profile, level);
                if (profileLevel.validate()) {
                    goCoderBroadcastConfig.setVideoProfileLevel(profileLevel);
                }
            } else {
                goCoderBroadcastConfig.setVideoProfileLevel(null);
            }

            goCoderBroadcastConfig.setAudioSampleRate(WOWZMediaConfig.DEFAULT_AUDIO_SAMPLE_RATE);
            goCoderBroadcastConfig.setAudioChannels(WOWZMediaConfig.AUDIO_CHANNELS_STEREO);
            goCoderBroadcastConfig.setAudioBitRate(WOWZMediaConfig.DEFAULT_AUDIO_BITRATE);
//            WOWZStatus prepareStatus = goCoderAudioDevice.prepareForBroadcast(goCoderBroadcastConfig);
//            if (prepareStatus != null && (prepareStatus.isRunning() || prepareStatus.isReady())){
//                goCoderAudioDevice.setAudioEnabled(true);
//            }
//            goCoderBroadcastConfig.getAudioBroadcaster().setAudioEnabled(true);
//            WOWZStatus startStatus = goCoderAudioDevice.startBroadcasting();
//            Log.d(TAG, "wowza:audioStatus:"+audioStatus.getState()+",prepareStatus:"+prepareStatus.getState()+",startStatus:"+startStatus.getState());
//            Log.d(TAG, "wowza:audioStatus:"+audioStatus.getState());

            // Ensure the minimum set of configuration settings have been specified necessary to
            // initiate a broadcast streaming session

//            Log.d(TAG, "=============== Broadcast Configuration ===============\n"
//                    + goCoderBroadcastConfig.toString()
//                    + "\n=======================================================");
//            configValidationError = goCoderBroadcastConfig.validateForBroadcast();
            statusCallback = new StatusCallback();
            // Designate the audio device as the audio broadcaster

//            if (configValidationError != null) {
//                Log.d(TAG, "wowza:configValidationError:"+configValidationError.getErrorDescription());
//                WOWZLog.error(configValidationError);
//                statusButton = 2;
//            }

            // return if the user hasn't granted the app the necessary permissions
            if (!mPermissionsGranted) return;
            setLastOnline();

            setBroadcastNotif(stream);
        } else {
            intent = new Intent(context, NavigationActivity.class);
            startActivity(intent);
        }
    }

    private void setBroadcastNotif(final Stream stream) {
        userDB.child(uid).child("followers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.d(TAG, "setBroadcastNotif:"+dataSnapshot.toString());
                if (dataSnapshot != null && dataSnapshot.hasChildren()){
                    for (DataSnapshot snap: dataSnapshot.getChildren()){
                        if (snap.getValue() != null){
                            long time  = Helpers.getLastSeen();
                            HashMap<String, Object> mapNotif = new HashMap<>();
                            mapNotif.put("title", "Watch "+currentUser.getDisplayName()+" now");
                            mapNotif.put("message", "Watch now! "+name);
                            mapNotif.put("code", 2);
                            mapNotif.put("time", time);
                            mapNotif.put("sender_id", currentUser.getUid());
                            mapNotif.put("stream_id", stream.getStreamId());
                            FirebaseDatabase.getInstance().getReference("notifications").child(snap.getKey()).push().setValue(mapNotif);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLastOnline(){
        final Handler handler = new Handler();
        timer = new Timer();

        task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            updateStateBroadcast(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 60*1000);

    }

    private void updateStateBroadcast(boolean b) {
        Log.d(TAG, "updateStateBroadcast:"+wowzaSetting.getKey());
        HashMap<String, Object> hashMapStream = new HashMap<>();
        HashMap<String, Object> hashMapWowza = new HashMap<>();
        long lastActiveStream = Helpers.getLastSeen();
        if (b){
            if (stream.getStatus() == getResources().getInteger(R.integer.stream_status_start)){
                hashMapStream.put("status", getResources().getInteger(R.integer.stream_status_running));
            }
            hashMapStream.put("lastActiveStream", lastActiveStream);
            hashMapStream.put("last_active_stream", lastActiveStream);
            preferenceHelper.storeObject("stream", stream);

            hashMapWowza.put("uid", uid);
            hashMapWowza.put("last_update", lastActiveStream);
        } else {
            hashMapStream.put("status", getResources().getInteger(R.integer.stream_status_stop));
            preferenceHelper.storeObject("stream", stream);

            hashMapWowza.put("uid", null);
            hashMapWowza.put("last_update", 0l);
        }
        if (wowzaSetting != null && wowzaSetting.getKey() != null){
            database.getReference("wowza").child("settings").child(wowzaSetting.getKey()).updateChildren(hashMapWowza);
        }
        if (uid != null && streamId != null){
            database.getReference("streams").child(uid).child(streamId).updateChildren(hashMapStream);
        }
    }

    private void initChat(){
        if (uid != null && streamId != null){
            streamDB.child(uid).child(streamId).child("chats").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    Log.d(TAG, "initChat:"+dataSnapshot.toString());
                    if (dataSnapshot.exists()){
                        showChat(null);
                    } else {
                        Chat chat = new Chat();
                        chat.setUid("admin");
                        chat.setMessage("Your Streaming is Ready");
                        chat.setTimer(Helpers.getLastSeen());
                        if (uid != null && streamId != null){
                            streamDB.child(uid).child(streamId).child("chat").setValue(chat);
                        }
                        showChat(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void showChat(Chat chat){
        if (chat != null){
            listChat.add(chat);
        }

        if (uid != null && streamId != null){
            streamDB.child(uid).child(streamId).child("chats").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.d("dataSnapshot", "chats:"+dataSnapshot.getValue().toString());

                    Chat chat = new Chat();
                    chat.setTimer(Long.parseLong(dataSnapshot.getKey()));
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    Log.d("listChat", snapshot.toString());
                        if (snapshot.getKey().equals("uid")) {
                            String uid = snapshot.getValue(String.class);
                            chat.setUid(uid);
                            if (uid.equals("admin")){
                                chat.setName("Admin");
                            } else {
                                chat.setName(uid);
                            }
                        }
                        if (snapshot.getKey().equals("message")){
                            chat.setMessage(snapshot.getValue(String.class));
                        }
                    }
                    Log.d("listChat", chat.getName()+","+chat.getMessage()+","+chat.getTimer()+":"+listChat.size());
                    if (!isExist(chat)){
                        listChat.add(chat);
//                    recyclerChatAdapter.notifyDataSetChanged();
                        recyclerChatAdapter.notifyItemInserted(listChat.size() - 1);
                        recyclerChat.smoothScrollToPosition(listChat.size() - 1);

                        updateName(chat.getName(), listChat.size() - 1);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    private boolean isExist(Chat chat){
        if (listChat.size() > 0){
            for (Chat c: listChat){
                if (c.getTimer() == chat.getTimer()){
                    return true;
                }
            }
        }
        return false;
    }

    private void updateName(final String key, final int index){
        Log.d("updateName", key+","+index+":"+mapUser.size());
        if (mapUser.size() > 0 && mapUser.containsKey(key)){
            Chat chat = listChat.get(index);
            chat.setName(mapUser.get(key));
            listChat.set(index, chat);
            recyclerChatAdapter.notifyItemChanged(index);
        }
        if (!(key.equals("Admin"))){
            Log.d("updateName", key+",onDataChange:"+index);
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = null;
                    Chat chat = listChat.get(index);
                    Log.d("updateName", "dataSnapshot:"+dataSnapshot.child("email").toString());
                    if (dataSnapshot.child("name").exists() && dataSnapshot.child("name").getValue().toString().length() > 0){
                        name = dataSnapshot.child("name").getValue(String.class);
                    } else if (dataSnapshot.child("email").exists()){
                        name = dataSnapshot.child("email").getValue(String.class);
                    }

                    if (name != null){
                        chat.setName(name);
                        mapUser.put(key, name);
                        listChat.set(index, chat);

                        Log.d("updateName", "onDataChange("+key+","+name+"):"+index);
                        recyclerChatAdapter.notifyItemChanged(index);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            userDB.child(key).addValueEventListener(valueEventListener);
        }
    }

    public void followAction(View view, String uid, User user){
        Helpers.follow(uid);
        buttonFollowType = 2;

        String message = null;
        if (user != null){
            message = "You Follow "+user.getName();
        } else if (context != null){
            message = "You Follow";
        }
        showSnackbar(message);
    }

    public void unfollowAction(View view, String uid, User user){
        Helpers.unfollow(uid);
        buttonFollowType = 1;

        String message = null;
        if (user != null){
            message = "You unfollow "+user.getName();
        } else if (context != null){
            message = "You unfollow";
        }
        showSnackbar(message);
    }

    public void layoutProfileAction(View view, final String uid){
        if (dialog == null){
            dialog = new Dialog(context);
        }
        dialog.setContentView(R.layout.popup_profile);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        userPopup = new User();
        ImageButton buttonClose = dialog.findViewById(R.id.button_close);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        ImageView imagePopupProfile = dialog.findViewById(R.id.image_popup_profile);

        showImageHelper.showImageProfile(uid, imagePopupProfile);
        final TextView textPopupName = dialog.findViewById(R.id.text_popup_name);
        final TextView textPopupLocation = dialog.findViewById(R.id.text_popup_location);
        textPopupFollowers = dialog.findViewById(R.id.text_popup_followers);
        textPopupFollows = dialog.findViewById(R.id.text_popup_follows);
        textPopupVideos = dialog.findViewById(R.id.text_popup_videos);
        buttonPopupFollow = dialog.findViewById(R.id.button_popup_follow);
        textPopupFollowers.setText("0");
        textPopupFollows.setText("0");
        setTextPopupFollowers(uid);
        setTextPopupFollows(uid);
        setTextVideos(uid);

        userDB.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userPopup = dataSnapshot.getValue(User.class);
                textPopupName.setText(userPopup.getName());
                textPopupLocation.setText(userPopup.getLocation());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (buttonFollowType == 1){
            buttonPopupFollow.setText(R.string.follow);
            buttonPopupFollow.setVisibility(View.VISIBLE);
        } else if(buttonFollowType == 2){
            buttonPopupFollow.setText(R.string.unfollow);
            buttonPopupFollow.setVisibility(View.VISIBLE);
        } else {
            buttonPopupFollow.setVisibility(View.GONE);
        }

        buttonPopupFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonFollowType == 1){
                    followAction(view, uid, userPopup);
                    buttonPopupFollow.setText(R.string.unfollow);

                    setTextPopupFollowers(uid);
                } else if(buttonFollowType == 2){
                    unfollowAction(view, uid, userPopup);
                    buttonPopupFollow.setText(R.string.follow);

                    setTextPopupFollows(uid);
                }
            }
        });

    }

    private void viewPopupAction(View view) {
        final List<User> listUser = new ArrayList<>();
        if (dialog == null){
            dialog = new Dialog(context);
        } else {
            dialog.dismiss();
        }
        dialog.setContentView(R.layout.popup_list_user);
        final ListView listView = dialog.findViewById(R.id.list_popup_view);
        final ListUserAdapter listUserAdapter = new ListUserAdapter(context, listUser);
        listView.setAdapter(listUserAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                User user = listUser.get(position);
                Intent intent = new Intent(context, MyProfileActivity.class);
                intent.putExtra("uid", user.getUid());
                startActivity(intent);
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        if (mapWatched != null && mapWatched.size() > 0){
            Query query = FirebaseDatabase.getInstance().getReference("users").orderByKey();
            String startAt = null;
            String endAt = null;
            for (Map.Entry<String, Object> map: mapWatched.entrySet()){
                if (startAt== null){
                    if (map.getValue() != null){
                        startAt = map.getKey();
                    }
                }
                if (map.getValue() != null){
                    endAt = map.getKey();
                }
            }
            query.startAt(startAt).endAt(endAt).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()){
                        for (DataSnapshot snap: dataSnapshot.getChildren()){
                            if (mapWatched.containsKey(snap.getKey())){
                                User u = snap.getValue(User.class);
                                u.setUid(snap.getKey());
                                listUser.add(u);
                            }
                            listUserAdapter.notifyDataSetChanged();
                        }
//                        Log.d(TAG, "viewPopupAction:"+dataSnapshot.toString());
                        Log.d(TAG, "viewPopupAction:size:"+listUser.size()+":"+mapWatched.size()+"|"+dataSnapshot.getChildrenCount());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void setTextPopupFollowers(String uid){
        userDB.child(uid).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("followers", dataSnapshot.toString()+":"+dataSnapshot.getChildrenCount()+"{"+dataSnapshot.hasChildren()+"}");
                if (dataSnapshot.exists()){
                    textPopupFollowers.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setTextPopupFollows(String uid){
        userDB.child(uid).child("follows").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("follows", dataSnapshot.toString()+":"+dataSnapshot.getChildrenCount()+"{"+dataSnapshot.hasChildren()+"}");
                if (dataSnapshot.exists()){
                    textPopupFollows.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setTextVideos(String uid){
        streamDB.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textPopupVideos.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initFirebase(){
        if (mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }
        if (currentUser == null){
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
        }
        if (database == null){
            database = FirebaseDatabase.getInstance();
        }
        if (chatDB == null){
            chatDB = database.getReference("chats");
        }
        if (userDB == null){
            userDB = database.getReference("users");
        }
        if (streamDB == null){
            streamDB = database.getReference("streams");
        }
        if (tagsDB == null){
            tagsDB = database.getReference("tags");
        }
    }

    private void stopBroadcast(){
//        streamDB.child(uid).child(streamId).child("watched").child(currentUser.getUid()).removeValue();
        if (task != null){
            task.cancel();
        }
        if (timer != null){
            timer.cancel();
        }
        if (goCoderAudioDevice != null && goCoderAudioDevice.isAudioEnabled() && goCoderAudioDevice.getStatus().isRunning()){
            goCoderAudioDevice.stopBroadcasting();
        }
        if (goCoder != null && goCoder.isStreaming()){
            goCoder.endStreaming();
        }
        if (goCoderCameraView != null && (goCoderCameraView.isPreviewing() || goCoderCameraView.isPreviewPaused() || goCoderCameraView.isPreviewReady())){
            goCoderCameraView.stopBroadcasting();
            goCoderCameraView.clearView();
            goCoderCameraView.stopPreview();

            WOWZCamera activeCamera = goCoderCameraView.getCamera();
            if (activeCamera != null){
                activeCamera.stopPreview();
                activeCamera.release();
            }
        }
        if (goCoderBroadcaster != null && (goCoderBroadcaster.getStatus().isRunning() || goCoderBroadcaster.getStatus().isBuffering() || goCoderBroadcaster.getStatus().isPaused())) {
            // Stop the broadcast that is currently running
            goCoderBroadcaster.endBroadcast(statusCallback);
        }
        stopTask();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialogStop();
            }
        });
        stopStatus();
    }

    private void stopStatus() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", getResources().getInteger(R.integer.stream_status_stop));
        map.put("counter", counter);
        if (uid != null && streamId != null){
            database.getReference("streams").child(uid).child(streamId).updateChildren(map);
        }
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

    class StatusCallback implements WOWZStatusCallback {
        @Override
        public void onWZStatus(WOWZStatus wowzStatus) {
            // A successful status transition has been reported by the GoCoder SDK
            final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");

            switch (wowzStatus.getState()) {
                case WOWZState.STARTING:
                    statusMessage.append("Broadcast initialization");
                    statusButton = 2;
                    break;

                case WOWZState.READY:
                    statusMessage.append("Ready to begin streaming");
                    statusButton = 4;
                    break;

                case WOWZState.RUNNING:
                    statusMessage.append("Streaming is active");
                    statusButton = 5;
                    break;

                case WOWZState.STOPPING:
                    statusMessage.append("Broadcast shutting down");
                    statusButton = 4;
                    break;

                case WOWZState.IDLE:
                    statusMessage.append("The broadcast is stopped");
                    statusButton = 2;
                    break;

                default:
                    return;
            }
            Log.d("onWZStatus", "statusMessage:"+statusMessage.toString());
            // Display the status message using the U/I thread
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(layout, statusMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
        @Override
        public void onWZError(WOWZStatus wzStatus) {
            Log.d("onWZStatus", "onWZError:"+wzStatus.toString());
            if (wzStatus.getLastError() != null) {
                Log.d("onWZStatus", "onWZError:"+wzStatus.getLastError().getErrorDescription());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        textChat.clearFocus();
//        layout.requestFocus();

        preferenceHelper = new PreferenceHelper(context);
        initFirebase();
        preferenceSettingHelper = new PreferenceHelper(getContext(), "setting");
        frameRateSet = preferenceSettingHelper.retrieve("framerate_set", 15);
        muteSet = preferenceSettingHelper.retrieve("mute_set", true);

        uid = mAuth.getUid();

        if (stream != null){
            parseStream(stream);
        } else if (streamId != null && uid != null){
            streamDB.child(uid).child(streamId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    stream = dataSnapshot.getValue(Stream.class);
                    parseStream(stream);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void parseStream(Stream stream) {
        if (streamId == null){
            streamId = stream.getStreamId();
        }

        liveStream(stream);

        name = currentUser.getDisplayName();
        title = stream.getTitle();
        textName.setText(name);
        textTitle.setText(stream.getTitle());
        textView.setText(stream.getWatchCount()+" view");
        textWatchCount.setText(Helpers.getNumberCountFormat(stream.getWatchCount()));

        if (uid == null){
            uid = currentUser.getUid();
        }

        initChat();
        setTagValue();
        showWatched();
        updateStateBroadcast(true);

        showImageHelper.showImageGrid(uid, streamId, imageProfile);
        Log.d(TAG, "parseStream:"+uid+","+streamId);

        List<String> tags = stream.getTags();

        if (tags != null && tags.size() > 0){
            for (String value: tags){
                if (value != null && value.length() > 0 && value.charAt(0) != '#'){
                    value = "#"+value;
                }
                chipView.add(new Tag(value));
            }
        } else {
            streamDB.child(uid).child(streamId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                    Log.d(TAG, "onChildAdded:"+dataSnapshot.toString());
                    if (dataSnapshot.getKey().equals("tags") && dataSnapshot.hasChildren()){
                        for (DataSnapshot snap: dataSnapshot.getChildren()){
                            String value = snap.getValue(String.class);
                            if (value != null && value.length() > 0 && value.charAt(0) != '#'){
                                value = "#"+value;
                            }
                            chipView.add(new Tag(value));
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                    Log.d(TAG, "onChildChanged:"+dataSnapshot.toString());
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        if (user == null){
            userDB.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    user.setUid(dataSnapshot.getKey());
                    retrieveLevel(user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void setTagValue() {
        if (stream != null){
            if (stream.getTags() != null && stream.getTags().size() > 0){
                for (String string : stream.getTags()){
                    if (!string.isEmpty()){
//                        tagsDB.child(string).child(uid).setValue(stream.getStreamId());
                        HashMap<String, Object> map = new HashMap<>();
                        if (uid != null && streamId != null){
                            map.put(uid, streamId);
                            tagsDB.child(string).updateChildren(map);
                        } else {
                            streamId = stream.getStreamId();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        textChat.clearFocus();
//        layout.requestFocus();

        // If running on Android 6 (Marshmallow) and later, check to see if the necessary permissions
        // have been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionsGranted = Helpers.hasPermissions(context, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(activity, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        } else
            mPermissionsGranted = true;

        // Start the camera preview display
        if (mPermissionsGranted && goCoderCameraView != null) {
            if (goCoderCameraView.isPreviewPaused())
                goCoderCameraView.onResume();
            else
                goCoderCameraView.startPreview();
        }
    }

    private void showSnackbar(String text){
        if (activity != null){
            Snackbar.make(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show();
        }
    }

    private void retrieveLevel(User user){
        if (level == null){
            level = new Level(context, user);
            textLevel.setText("Level: "+level.getLevel());

            level.countLevel(new Level.OnLevelListener() {
                @Override
                public void retrieve(int lvl) {
                    textLevel.setText("Level: "+lvl);
                }
            });
        }
    }

    private void closeAction(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.are_you_sure_to_exit)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        stopBroadcast();
                        activity.finish();
                        Intent intent = new Intent(activity, NavigationActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builder.show();

    }

    @Override
    public void onStop() {
        super.onStop();
        stopBroadcast();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopBroadcast();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopBroadcast();
    }

    public void doTimerTask(){
        counter = 0;
        stopTask();
        handler = new Handler();
        timerCounter = new Timer();
        timerTaskCounter = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        counter++;
                        String minute = Helpers.getMinute(counter);

                        String second = Helpers.getSecond(counter);
                        textTimer.setText(minute+":"+second);

                        Log.d("TIMER", "TimerTask run");

                        if (Helpers.isAppInLowMemory(context) && showLowMemoryMessage){
                            Toast.makeText(context, "Your memory is almost full. Please Free up some spaces!", Toast.LENGTH_SHORT).show();
                            showLowMemoryMessage = false;
                        }
                    }
                });
            }};

        // public void schedule (TimerTask task, long delay, long period) 
        timerCounter.schedule(timerTaskCounter, 0, 1000);
    }

    public void stopTask(){
        if(timerTaskCounter!=null){
//            hTextView.setText("Timer canceled: " + nCounter);
            timerTaskCounter.cancel();
        }

        if (timerCounter != null){
            timerCounter.cancel();
        }

    }

    public void dialogStop(){
        updateDownloadURL();
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

        if (user != null){
            textViewer.setText(Helpers.getNumberCountFormat(stream.getWatchCount()));
            if (user.getFollowers() != null && user.getFollowers().size() > 0){
                textNewFollower.setText(Helpers.getNumberCountFormat(user.getFollowers().size()));
            }
            textChatCount.setText(Helpers.getNumberCountFormat(listChat.size()));

            String minute = Helpers.getMinute(counter);
            String second = Helpers.getSecond(counter);
            textTimerCount.setText(minute+":"+second);

            ImageButton buttonBack = dialog.findViewById(R.id.button_back);
            buttonDownload = dialog.findViewById(R.id.button_download);
            Button buttonShare = dialog.findViewById(R.id.button_share);

            buttonBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    intent = new Intent(context, NavigationActivity.class);
                    startActivity(intent);
                }
            });

            buttonDownload.setVisibility(View.GONE);
            buttonDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(activity, "Downloading video", Toast.LENGTH_LONG).show();
                    if (downloadUrl != null){
                        String path =  Environment.getExternalStorageDirectory().getPath() + "/" + BuildConfig.APPLICATION_ID;
                        String fileName = path.substring(path.lastIndexOf('/') + 1);
                        Log.d(TAG, path);
                        Log.d(TAG, fileName);

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                        request.setDescription(title);
                        request.setTitle(name);
                            // in order for this if to run, you must use the android 3.2 to compile your app
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }
//                        request.setDestinationInExternalPublicDir(path, fileName);
                        request.setDestinationInExternalFilesDir(activity, path, fileName);

                        // get download service and enqueue file
                        DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
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

    private void updateDownloadURL(){
        downloadUrl = null;
        if (wowzaSetting.getId() != null){
            retrofit = Helpers.initRetrofit(getActivity());

            service = retrofit.create(WowzaCloudApi.class);
            service.recordingsByTranscoderId(wowzaSetting.getId()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()){
                        try {
                            if (response.body() != null){
                                String body = response.body().string();

                                JSONObject jsonObject = new JSONObject(body);

                                if (jsonObject.has("recordings")){
                                    JSONArray jsonArray = jsonObject.getJSONArray("recordings");

                                    if (jsonArray.length() > 0){
                                        JSONObject json = jsonArray.getJSONObject(jsonArray.length() - 1);
                                        Log.d(TAG, "recordings:json:"+json.toString());
                                        Recording recording = new Gson().fromJson(json.toString(), Recording.class);
                                        int duration = 0;
                                        if (recording != null && recording.getDownloadUrl() != null){
                                            downloadUrl = recording.getDownloadUrl();
                                            duration = recording.getDuration();
                                        }

                                        if (buttonDownload != null && downloadUrl != null){
                                            HashMap<String, Object> map = new HashMap<>();
                                            map.put("download_url", downloadUrl);
                                            map.put("duration", duration);
                                            streamDB.child(uid).child(streamId).updateChildren(map);
                                            buttonDownload.setVisibility(View.VISIBLE);
                                        }
                                    }
                                    Log.d(TAG, "recordings:"+jsonArray.length()+"|"+body);
                                }

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
    }

    protected synchronized WOWZStreamingError startBroadcast() {
        WOWZStreamingError configValidationError = null;

        if (goCoderBroadcaster.getStatus().isIdle()) {

            // Set the detail level for network logging output
            goCoderBroadcaster.setLogLevel(WOWZLog.MAX_LOG_LEVEL);

            //
            // An example of adding metadata values to the stream for use with the onMetadata()
            // method of the IMediaStreamActionNotify2 interface of the Wowza Streaming Engine Java
            // API for server modules.
            //
            // See http://www.wowza.com/resources/serverapi/com/wowza/wms/stream/IMediaStreamActionNotify2.html
            // for additional usage information on IMediaStreamActionNotify2.
            //

            // Add stream metadata describing the current device and platform
            WOWZDataMap streamMetadata = new WOWZDataMap();
            streamMetadata.put("androidRelease", Build.VERSION.RELEASE);
            streamMetadata.put("androidSDK", Build.VERSION.SDK_INT);
            streamMetadata.put("deviceProductName", Build.PRODUCT);
            streamMetadata.put("deviceManufacturer", Build.MANUFACTURER);
            streamMetadata.put("deviceModel", Build.MODEL);

            goCoderBroadcastConfig.setStreamMetadata(streamMetadata);

            //
            // An example of adding query strings for use with the getQueryStr() method of
            // the IClient interface of the Wowza Streaming Engine Java API for server modules.
            //
            // See http://www.wowza.com/resources/serverapi/com/wowza/wms/client/IClient.html#getQueryStr()
            // for additional usage information on getQueryStr().
            //
            try {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

                // Add query string parameters describing the current app
                WOWZDataMap connectionParameters = new WOWZDataMap();
                connectionParameters.put("appPackageName", pInfo.packageName);
                connectionParameters.put("appVersionName", pInfo.versionName);
                connectionParameters.put("appVersionCode", pInfo.versionCode);

                goCoderBroadcastConfig.setConnectionParameters(connectionParameters);

            } catch (PackageManager.NameNotFoundException e) {
                WOWZLog.error(TAG, e);
            }

            WOWZMediaConfig mediaConfig = goCoderBroadcastConfig.getVideoSourceConfig();
            mediaConfig.setVideoFramerate(goCoderBroadcastConfig.getVideoFramerate());
            mediaConfig.setVideoFrameHeight(goCoderBroadcastConfig.getVideoFrameHeight());
            mediaConfig.setVideoFrameWidth(goCoderBroadcastConfig.getVideoFrameWidth());
            mediaConfig.setABREnabled(goCoderBroadcastConfig.isABREnabled());
            mediaConfig.setAudioEnabled(goCoderBroadcastConfig.isVideoEnabled());
            goCoderBroadcastConfig.setVideoSourceConfig(mediaConfig);
            Log.d(TAG, "=============== Broadcast Configuration ===============\n"
                    + goCoderBroadcastConfig.toString()
                    + "\n=======================================================");


            configValidationError = goCoderBroadcastConfig.validateForBroadcast();

            if (configValidationError == null) {


                /// Setup abr bitrate and framerate listeners. EXAMPLE
//                mWZBroadcastConfig.setABREnabled(false);
//                ListenToABRChanges abrHandler = new ListenToABRChanges();
//                mWZBroadcast.registerAdaptiveBitRateListener(abrHandler);
//                mWZBroadcast.registerAdaptiveFrameRateListener(abrHandler);
//                mWZBroadcastConfig.setFrameRateLowBandwidthSkipCount(1);

                WOWZLog.debug("***** [FPS]GoCoderSDKActivity "+goCoderBroadcastConfig.getVideoFramerate());
                goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);

                buttonRecord.setImageResource(ButtonFloat.STOP);
                buttonRecord.setTag(ButtonFloat.STOP);

                doTimerTask();

                if (retry == 0){
                    Toast.makeText(context, "Preparing server. Please wait!", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            WOWZLog.error(TAG, "startBroadcast() called while another broadcast is active");
        }
        return configValidationError;
    }

    protected synchronized void endBroadcast(boolean appPausing) {
        WOWZLog.debug(TAG,"endBroadcast");
        if (!goCoderBroadcaster.getStatus().isIdle()) {
            WOWZLog.debug(TAG,"endBroadcast-notidle");
            if (appPausing) {
                // Stop any active live stream
                sBroadcastEnded = false;
                goCoderBroadcaster.endBroadcast(new WOWZStatusCallback() {
                    @Override
                    public void onWZStatus(WOWZStatus wzStatus) {
                        Log.d(TAG,"onWZStatus::"+wzStatus.toString());
                        synchronized (sBroadcastLock) {
                            sBroadcastEnded = true;
                            sBroadcastLock.notifyAll();
                        }
                    }

                    @Override
                    public void onWZError(WOWZStatus wzStatus) {
                        Log.d(TAG,"onWZStatus:onWZError::"+wzStatus.getLastError());
                        WOWZLog.error(TAG, wzStatus.getLastError());
                        synchronized (sBroadcastLock) {
                            sBroadcastEnded = true;
                            sBroadcastLock.notifyAll();
                        }
                    }
                });

                while(!sBroadcastEnded) {
                    try{
                        sBroadcastLock.wait();
                    } catch (InterruptedException e) {}
                }
            } else {
                goCoderBroadcaster.endBroadcast(this);
            }

            stopBroadcast();

            buttonRecord.setImageResource(ButtonFloat.RECORD);
            buttonRecord.setTag(ButtonFloat.RECORD);
            Toast.makeText(context, "Streaming is ENDED", Toast.LENGTH_LONG).show();
        }  else {
            WOWZLog.error(TAG, "endBroadcast() called without an active broadcast");
        }
    }


    public void onToggleBroadcast(View v) {
        if (goCoderBroadcaster == null) return;

        Log.d("onToggleBroadcast", "goCoderBroadcaster:"+goCoderBroadcaster.getStatus());

        if (goCoderBroadcaster.getStatus().isIdle()) {
            if (!goCoderBroadcastConfig.isVideoEnabled() && !goCoderBroadcastConfig.isAudioEnabled()) {
                Toast.makeText(context, "Unable to publish if both audio and video are disabled", Toast.LENGTH_LONG).show();
            }
            else{

                WOWZLog.debug("Scale Mode: -> "+goCoderCameraView.getScaleMode());

                if(!goCoderBroadcastConfig.isAudioEnabled()){
                    Toast.makeText(context, "The audio stream is currently turned off", Toast.LENGTH_LONG).show();
                }

                if (!goCoderBroadcastConfig.isVideoEnabled()) {
                    Toast.makeText(context, "The video stream is currently turned off", Toast.LENGTH_LONG).show();
                }
                WOWZStreamingError configError = startBroadcast();
                if (configError != null) {
                    Toast.makeText(context, configError.getErrorDescription(), Toast.LENGTH_LONG).show();
                }
            }
        } else {
            endBroadcast(false);
        }
    }

    protected synchronized void endBroadcast() {
        endBroadcast(false);
    }

    @Override
    public void onWZStatus(WOWZStatus wowzStatus) {
        WOWZLog.debug(TAG,"onWZStatus::"+wowzStatus.toString());
        Log.d(TAG,"onWZStatus::"+wowzStatus.toString());

        if (wowzStatus.isRunning()){
            showToast("Streaming is running!");
        }

        synchronized (sBroadcastLock) {
            sBroadcastEnded = true;
            sBroadcastLock.notifyAll();
        }
    }

    @Override
    public void onWZError(WOWZStatus wowzStatus) {
        WOWZLog.debug(TAG,"onWZStatus:onWZError::"+wowzStatus.getLastError());
        Log.d(TAG,"onWZStatus:onWZError::"+wowzStatus.toString()+","+wowzStatus.isStopping());
        synchronized (sBroadcastLock) {
            sBroadcastEnded = true;
            sBroadcastLock.notifyAll();
        }
        if (wowzStatus.getLastError() != null){
            showToast(wowzStatus.getLastError().getErrorDescription());

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buttonRecord.setImageResource(ButtonFloat.RECORD);
                    buttonRecord.setTag(ButtonFloat.RECORD);
                    stopTask();
                }
            });

        }
        if (wowzStatus.isStopping()){
            if (retry < 3){
                retry++;
                startBroadcast();
//                    showToast("Retrying broadcast!");
            } else {
                retry = 0;
                buttonRecord.setImageResource(ButtonFloat.RECORD);
                buttonRecord.setTag(ButtonFloat.RECORD);

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        buttonRecord.setImageResource(ButtonFloat.RECORD);
                        buttonRecord.setTag(ButtonFloat.RECORD);
                        stopTask();
                    }
                });

                showToast("Streaming is failed, Try again later!");
            }
        }
    }

    private void showToast(final String messsage){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, messsage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateStatusStreamer(){
        if (wowzaSetting.getId() != null){
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(interceptor);
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    Request request = original.newBuilder()
                            .header("wsc-api-key", getString(R.string.wsc_api_key))
                            .header("wsc-access-key", getString(R.string.wsc_access_key))
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .method(original.method(), original.body())
                            .build();

                    return chain.proceed(request);
                }
            });

            OkHttpClient client = httpClient.build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(getString(R.string.wowza_url))
                    .client(client)
                    .build();

            service = retrofit.create(WowzaCloudApi.class);
            Log.d(TAG, "updateStatusStreamer:"+wowzaSetting.getId());
            customStartResponse = new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, "updateStatusStreamer:"+response.body()+"|"+response.isSuccessful());
                    if (response.body() != null) {
                        try {
                            Log.d(TAG, "updateStatusStreamer:"+response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (response.isSuccessful()){
                        buttonRecord.setEnabled(false);
                        service.start(wowzaSetting.getId()).clone().enqueue(this);
//                        updateStatusStreamer();
                    } else {
                        buttonRecord.setEnabled(true);
                        showToast("Streaming is ready. Please click the record button!");
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            };
            service.start(wowzaSetting.getId()).enqueue(customStartResponse);
        }
    }

    private void showComponent(boolean status){
        layoutWatchChild.setVisibility(status ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return detector.onTouchEvent(motionEvent);
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "Gestures";

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d(DEBUG_TAG,"onDown: " + event.toString());
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d(DEBUG_TAG, "onFling: " + velocityX + "," + velocityY + ":" + event1.toString());
            Log.d(DEBUG_TAG, "onFling: " + velocityX + "," + velocityY + ":" + event2.toString());
            statusComponent = !statusComponent;
            if (velocityY > 0){//down
//                if (statusComponent) {
//                    ObjectAnimator animation = ObjectAnimator.ofFloat(layoutWatchChild, "translationY", -5000);
//                    animation.setDuration(500);
//                    animation.start();
//                }
                showComponent(statusComponent);
            } else if (velocityY < 0) {//up
                showComponent(statusComponent);
            }

            return true;
        }
    }
}
