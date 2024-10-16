package dev.app.com.livechat.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import android.widget.ProgressBar;
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
import com.plumillonforge.android.chipview.Chip;
import com.plumillonforge.android.chipview.ChipView;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.geometry.WOWZSize;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.app.com.livechat.R;
import dev.app.com.livechat.activities.MyProfileActivity;
import dev.app.com.livechat.activities.NavigationActivity;
import dev.app.com.livechat.adapters.ListUserAdapter;
import dev.app.com.livechat.adapters.MainChipViewAdapter;
import dev.app.com.livechat.adapters.RecyclerChatAdapter;
import dev.app.com.livechat.entities.Chat;
import dev.app.com.livechat.entities.Level;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.entities.Tag;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.utils.Connectivity;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.ShowImageHelper;

public class WatchStreamFragment extends Fragment implements WOWZStatusCallback, View.OnClickListener{

    private static final String TAG = WatchStreamFragment.class.getSimpleName();
    // The top-level GoCoder API interface
    private WowzaGoCoder goCoder;

    // The GoCoder SDK audio device
    private WOWZAudioDevice goCoderAudioDevice;

    // The GoCoder SDK broadcaster
    private WOWZBroadcast goCoderBroadcaster;

    // The broadcast configuration settings
    private WOWZBroadcastConfig goCoderBroadcastConfig;

    private WOWZPlayerView mStreamPlayerView;
    private WOWZPlayerConfig mStreamPlayerConfig;
    private Intent intent;
    private Stream stream = new Stream();
    private User user;
    private FirebaseDatabase database;
    private DatabaseReference chatDB;
    private String streamId;
    private RecyclerView recyclerChat;
    private List<Chat> listChat = new ArrayList<>();
    private RecyclerChatAdapter recyclerChatAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText textChat;
    private Map<String, String> mapUser = new HashMap<>();
    private DatabaseReference userDB;
    private DatabaseReference streamDB;
    private ImageView imageProfile;
    private TextView textTitle;
    private TextView textViewCount;
    private StorageReference storageReference;
    private ImageButton imageButtonFollow;
    private ImageButton imageButtonUnfollow;
    private String name;
    private Dialog dialog;
    private String imageUser;
    private int buttonFollowType = 0; // 0 default, 1 follow, 2 unfollow
    private TextView textPopupFollowers;
    private TextView textPopupFollows;
    private Button buttonPopupFollow;
    private String uid;
    private TextView textTimer;
    private View container;
    private StatusCallback statusCallback;
    private ArrayList<User> listUser;
    private Context context;
    private Activity activity;
    private boolean showChat = false;
    private boolean showWatched = false;
    private ImageButton buttonSendChat;
    private int videoBitRate = 1500; // low=50000, medium=100000, high=548000
    private static final int BITRATE_LOW = 50000;
    private static final int BITRATE_MEDIUM = 100000;
    private static final int BITRATE_HIGH = 548000;
    private Connectivity connectivity;
    private String redirectString;
    private ChipView chipView;
    private List<Chip> chipList = new ArrayList<>();
    private MainChipViewAdapter tagChipViewAdapter;
    private LinearLayout layoutProfile;
    private TextView textPopupvideos;
    private User userPopup;
    private TextView textPopupName;
    private TextView textPopupLocation;
    private Button buttonPopupProfile;
    private HashMap<String, Object> mapWatched = new HashMap<>();
    private TextView textName;
    private View layoutView;
    private Level level;
    private TextView textLevel;
    private LinearLayout layoutShare;
    private ShowImageHelper showImageHelper;
    private int counter;
    private int position;
    private WOWZSize wowzSize;
    private ProgressBar progressBar;
    private LinearLayout layoutWatch;
    private LinearLayout layoutWatchChild;
    private boolean statusComponent = true;

    public static WatchStreamFragment newInstance() {
        return new WatchStreamFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watch_stream, container, false);

        context = getContext();
        activity = getActivity();

        if (activity != null && activity.getWindow() != null){
            activity.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        goCoder = WowzaGoCoder.init(context, getString(R.string.wowza_gocoder_key));

        mStreamPlayerView = (WOWZPlayerView) view.findViewById(R.id.vwStreamPlayer);

        container = view.findViewById(R.id.fragment_container);
        textChat = (EditText) view.findViewById(R.id.text_chat);
        imageProfile = (ImageView) view.findViewById(R.id.image_profile);
        textTitle = (TextView) view.findViewById(R.id.text_title);
        textName = (TextView) view.findViewById(R.id.text_name);
        layoutView = view.findViewById(R.id.layout_view);
        textViewCount = (TextView) view.findViewById(R.id.text_view_count);
        imageButtonFollow = (ImageButton) view.findViewById(R.id.image_button_follow);
        imageButtonUnfollow = (ImageButton) view.findViewById(R.id.image_button_unfollow);
        buttonSendChat = (ImageButton) view.findViewById(R.id.send_chat);
        recyclerChat = (RecyclerView) view.findViewById(R.id.recycler_chat);
        chipView = (ChipView) view.findViewById(R.id.chipview);
        layoutProfile = (LinearLayout) view.findViewById(R.id.layout_profile);
        layoutShare = (LinearLayout) view.findViewById(R.id.layout_share);
        layoutWatch = (LinearLayout) view.findViewById(R.id.layout_watch);
        layoutWatchChild = (LinearLayout) view.findViewById(R.id.layout_watch_child);
        textLevel = (TextView) view.findViewById(R.id.text_level);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        recyclerChatAdapter = new RecyclerChatAdapter(getContext(), listChat, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Chat chat = listChat.get(position);
                layoutProfileAction(view, chat.getUid());
            }
        });

        recyclerChat.setAdapter(recyclerChatAdapter);
        recyclerChat.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        llm.setStackFromEnd(true);
        llm.setReverseLayout(false);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerChat.setLayoutManager(llm);

        imageButtonFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followAction(view, uid, user);
            }
        });

        imageButtonUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unfollowAction(view, uid, user);
            }
        });

        buttonSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChatAction(view);
            }
        });

        textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layoutProfileAction(view, uid);
            }
        });

        layoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewPopupAction(view);
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

        layoutWatch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int action = event.getActionMasked();
                switch(action) {
                    case (MotionEvent.ACTION_DOWN) :
                        statusComponent = !statusComponent;
                        showComponent(statusComponent);
                        return true;
                    case (MotionEvent.ACTION_MOVE) :
                        Log.d(TAG,"Action was MOVE");
                        return true;
                    case (MotionEvent.ACTION_UP) :
                        statusComponent = !statusComponent;
                        showComponent(statusComponent);
                        return true;
                    case (MotionEvent.ACTION_CANCEL) :
                        Log.d(TAG,"Action was CANCEL");
                        return true;
                    case (MotionEvent.ACTION_OUTSIDE) :
                        Log.d(TAG,"Movement occurred outside bounds " +
                                "of current screen element");
                        return true;
                    default :
                        return false;
                }
            }
        });

        chipView.setChipList(chipList);
        tagChipViewAdapter = new MainChipViewAdapter(getContext(), R.layout.chip);
        chipView.setAdapter(tagChipViewAdapter);
        chipView.setChipLayoutRes(R.layout.chip);

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

    @Override
    public void onStart() {
        super.onStart();

        intent = activity.getIntent();
        position = intent.getIntExtra("position", -1);

        Bundle bundle = getArguments();
        streamId = bundle.getString("streamId");
        uid = bundle.getString("uid");

        if (!(uid != null && streamId != null)){
            intent = new Intent(context, NavigationActivity.class);
            startActivity(intent);
            return;
        }
        redirectString = bundle.getString("redirectString");
        Log.d(TAG, "onStart:"+uid+","+streamId+":"+position);

        initFirebase();

        watchStream();
        showChat(null);
        showWatched();
        showFollowUnfollowButton();

        showImageHelper = new ShowImageHelper(context);
        showImageHelper.showImageProfile(uid, imageProfile);

        userDB.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(WatchStreamFragment.class.getSimpleName(), "userDB:"+dataSnapshot.toString());
                user = dataSnapshot.getValue(User.class);
                if (user != null && dataSnapshot.getKey() != null){
                    user.setUid(dataSnapshot.getKey());

                    level = new Level(context, user);
                    textLevel.setText("Level: "+level.getLevel());

                    level.countLevel(new Level.OnLevelListener() {
                        @Override
                        public void retrieve(int lvl) {
                            textLevel.setText("Level: "+lvl);
                        }
                    });

                    streamDB.child(uid).child(streamId).addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

//                        streamDB.child(uid).child(streamId).child("watched").child(currentUser.getUid()).setValue(Helpers.getLastSeen());
                            Log.d(WatchStreamFragment.class.getSimpleName(), "streamDB:addValueEventListener:"+dataSnapshot.toString());
                            stream = dataSnapshot.getValue(Stream.class);
                            stream.setStreamId(streamId);
                            user.setStream(stream);

                            textTitle.setText(stream.getTitle());
                            textName.setText(user.getName() != null ? user.getName() : user.getEmail());
                            showImageHelper.showImageGrid(uid, streamId, imageProfile);

                            if (stream.getTags() != null && stream.getTags().size() > 0){
                                for (String tag: stream.getTags()){
                                    if (tag.length() > 0 && tag.charAt(0) != '#'){
                                        tag = "#"+tag;
                                    }
                                    if (!availableTag(tag)){
                                        chipView.add(new Tag(tag));
                                    }
                                }
                            }

                            initStreamer();
                            float lastOnline = Helpers.getMinuteCount(stream.getLastActiveStream());
                            Log.d(TAG, "status:onDataChange:"+stream.getStatus()+","+lastOnline+":"+uid+","+streamId);
//                            if (stream.getStatus() == getResources().getInteger(R.integer.stream_status_stop) || lastOnline >= 3){
                            if (stream.getStatus() == getResources().getInteger(R.integer.stream_status_stop) || lastOnline >= 3){
                                showProgress(View.GONE);
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

    private boolean availableTag(String tag) {
        List<Chip> listChip = chipView.getChipList();
        if (listChip != null && listChip.size() > 0){
            for (Chip chip: listChip){
                if (chip.getText().equals(tag)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Bundle bundle = getArguments();
        streamId = bundle.getString("streamId");
        uid = bundle.getString("uid");
        position = bundle.getInt("position");

        Log.d(TAG, "setUserVisibleHint:"+uid+"/"+streamId+":"+position);
    }

    private void showFollowUnfollowButton() {
        userDB.child(currentUser.getUid()).child("follows").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("showFollowUnfollow", dataSnapshot.toString());
                if (dataSnapshot.exists()){
                    imageButtonFollow.setVisibility(View.GONE);
                    imageButtonUnfollow.setVisibility(View.VISIBLE);
                    buttonFollowType = 2;
                } else {
                    imageButtonFollow.setVisibility(View.VISIBLE);
                    imageButtonUnfollow.setVisibility(View.GONE);
                    buttonFollowType = 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onWZStatus(WOWZStatus wowzStatus) {
        // A successful status transition has been reported by the GoCoder SDK
        final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");

        switch (wowzStatus.getState()) {
            case WOWZState.STARTING:
                statusMessage.append("Broadcast initialization");
                break;

            case WOWZState.READY:
                statusMessage.append("Ready to begin streaming");
                break;

            case WOWZState.RUNNING:
                statusMessage.append("Streaming is active");
                break;

            case WOWZState.STOPPING:
                statusMessage.append("Broadcast shutting down");
                break;

            case WOWZState.IDLE:
                statusMessage.append("The broadcast is stopped");
                break;

            default:
                return;
        }

        // Display the status message using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                showSnackbar(statusMessage.toString());
            }
        });
    }

    @Override
    public void onWZError(final WOWZStatus wowzStatus) {
        // If an error is reported by the GoCoder SDK, display a message
        // containing the error details using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                showSnackbar("Streaming error: " + wowzStatus.getLastError().getErrorDescription());
            }
        });
    }

    @Override
    public void onClick(View view) {
        playStreaming(view);
    }

    private void playStreaming(View view) {

        // Ensure the minimum set of configuration settings have been specified necessary to
        // initiate a broadcast streaming session
        WOWZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();

        if (configValidationError != null) {
            Toast.makeText(context, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
        } else if (goCoderBroadcaster.getStatus().isRunning()) {
            // Stop the broadcast that is currently running
            goCoderBroadcaster.endBroadcast(this);
        } else {
            // Start streaming
            goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);
        }
    }

    class StatusCallback implements WOWZStatusCallback {
        @Override
        public void onWZStatus(WOWZStatus wzStatus) {
            if (wzStatus != null){
                Log.d("onWZStatus", wzStatus.toString());
                if (wzStatus.getLastError() != null){
                    Log.d("onWZStatus", wzStatus.getLastError().getErrorDescription());
                    showSnackbar( wzStatus.getLastError().getErrorDescription());
                }

                if (wzStatus.isRunning()){
                    showProgress(View.GONE);
                }
            }
        }
        @Override
        public void onWZError(WOWZStatus wzStatus) {
            if (wzStatus != null){
                Log.d("onWZError", wzStatus.toString());
                if (wzStatus.getLastError() != null) {
                    Log.d("onWZError", wzStatus.getLastError().getErrorDescription());
                    showSnackbar(wzStatus.getLastError().getErrorDescription());
                }
            }
        }
    }

    private void showProgress(final int visibility){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(visibility);
            }
        });
    }

    private void showChat(Chat chat){
        if (chat != null){
            listChat.add(chat);
        }
        streamDB.child(uid).child(streamId).child("chats").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("dataSnapshot", dataSnapshot.getValue().toString());

                Chat chat = new Chat();
                chat.setTimer(Long.parseLong(dataSnapshot.getKey()));
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Log.d("listChat", snapshot.toString());
                    if (snapshot.getKey().equals("uid")) {
                        String uid = snapshot.getValue(String.class);
                        chat.setUid(uid);
                        if (uid.equals("admin")){
                            chat.setName("Admin");
                        } else {
                            chat.setName(uid);
                        }
                    } else if (snapshot.getKey().equals("message")){
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

    private void showWatched(){
        if (uid != null && streamId != null) {
            streamDB.child(uid).child(streamId).child("watched").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "showWatched:"+dataSnapshot.toString());
                    setViewCount(dataSnapshot.getChildrenCount());
                    if (dataSnapshot.hasChildren()){
                        for (DataSnapshot snap: dataSnapshot.getChildren()){
                            mapWatched.put(snap.getKey(), snap.getValue());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void sendChatAction(View view){
        Chat chat = new Chat();
        String text = textChat.getText().toString();

        if (!text.isEmpty()){
            long time = Helpers.getLastSeen();
            chat.setUid(currentUser.getUid());
            chat.setMessage(text);
            streamDB.child(uid).child(streamId).child("chats").child(String.valueOf(time)).setValue(chat);

            textChat.setText("");
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
        } else {
            dialog.dismiss();
        }
        dialog.setContentView(R.layout.popup_profile);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        userPopup = new User();
        ImageButton buttonClose = (ImageButton) dialog.findViewById(R.id.button_close);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        ImageView imagePopupProfile = (ImageView) dialog.findViewById(R.id.image_popup_profile);

        ShowImageHelper showImageHelper = new ShowImageHelper(context);
        showImageHelper.showImageProfile(uid, imagePopupProfile);
        textPopupName = (TextView) dialog.findViewById(R.id.text_popup_name);
        textPopupLocation = (TextView) dialog.findViewById(R.id.text_popup_location);
        textPopupFollowers = (TextView) dialog.findViewById(R.id.text_popup_followers);
        textPopupFollows = (TextView) dialog.findViewById(R.id.text_popup_follows);
        textPopupvideos = (TextView) dialog.findViewById(R.id.text_popup_videos);
        buttonPopupFollow = (Button) dialog.findViewById(R.id.button_popup_follow);
        buttonPopupProfile = (Button) dialog.findViewById(R.id.button_popup_profile);
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

        textPopupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(context, MyProfileActivity.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

        buttonPopupProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(context, MyProfileActivity.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });
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
                textPopupvideos.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initStreamer(){
        String host = "";
        String application = "";
        String streamName = "";
//        if (Connectivity.isConnectedFast(context) || Connectivity.isConnectedWifi(context)){
//            videoBitRate = BITRATE_HIGH;
//        } else if (Connectivity.isConnectedMobile(context)){
//            videoBitRate = BITRATE_MEDIUM;
//        }
        if (stream != null){

            if (goCoder == null) {
                // If initialization failed, retrieve the last error and display it
                WOWZError goCoderInitError = WowzaGoCoder.getLastError();
                Toast.makeText(context,
                        "GoCoder SDK error: " + goCoderInitError.getErrorDescription(),
                        Toast.LENGTH_LONG).show();
                return;
            }

            mStreamPlayerConfig = new WOWZPlayerConfig();
            mStreamPlayerConfig.setIsPlayback(true);
            if (stream.getWowzaSetting() != null){
                if (stream.getWowzaSetting().getHost() != null) {
                    host = stream.getWowzaSetting().getHost();
                    mStreamPlayerConfig.setHostAddress(host);
                }
                if (stream.getWowzaSetting().getApp() != null) {
                    application = stream.getWowzaSetting().getApp();
                    mStreamPlayerConfig.setApplicationName(application);
                }
                if (stream.getWowzaSetting().getName() != null) {
                    streamName = stream.getWowzaSetting().getName();
                    mStreamPlayerConfig.setStreamName(streamName);
                }
                if (stream.getWowzaSetting().isAuth()){
                    mStreamPlayerConfig.setUsername("devwalle");
                    mStreamPlayerConfig.setPassword("devwalle2019");
                }
            }
            mStreamPlayerConfig.setPortNumber(1935);

            wowzSize = new WOWZSize(WOWZMediaConfig.DEFAULT_VIDEO_FRAME_WIDTH, WOWZMediaConfig.DEFAULT_VIDEO_FRAME_HEIGHT);
//            mStreamPlayerConfig.setVideoBitRate(videoBitRate);
            mStreamPlayerConfig.setVideoFramerate(15);
            mStreamPlayerConfig.setVideoFrameSize(wowzSize);
            mStreamPlayerConfig.setVideoBitRate(videoBitRate);

            mStreamPlayerConfig.setVideoFrameWidth(640);
            mStreamPlayerConfig.setVideoFrameHeight(480);

            mStreamPlayerConfig.setAudioEnabled(true);
            mStreamPlayerConfig.setVideoEnabled(true);

//            goCoderBroadcastConfig.setVideoFrameSize(wowzSize);
//            goCoderBroadcastConfig.getVideoSourceConfig().setVideoFrameSize(wowzSize);
//            goCoderBroadcastConfig.getVideoSourceConfig().setVideoFramerate(15);
//            goCoderBroadcastConfig.setVideoBitRate(videoBitRate);

            // When specified, the player will switch to the Apple HLS URL after three failed attempts to play over the primary protocol
//            mStreamPlayerConfig.setHLSEnabled(true);
//            mStreamPlayerConfig.setHLSBackupURL("http://"+host+":1935/"+application+"/"+streamName+"/playlist.m3u8");
            //

            mStreamPlayerView.setScaleMode(WOWZMediaConfig.FILL_VIEW);
            statusCallback = new StatusCallback();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "=============== Broadcast Configuration ===============\n"
                            + mStreamPlayerConfig.toString()
                            + "\n=======================================================");
                    mStreamPlayerView.play(mStreamPlayerConfig, statusCallback);
                }
            }, 5*1000);

//            playStreaming(null);
        }

    }

    private void initFirebase(){
        if (mAuth == null){
            mAuth = FirebaseAuth.getInstance();
        }
        if (currentUser == null){
            currentUser = mAuth.getCurrentUser();
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
    }

    private void stopBroadcast(){
        if (goCoderBroadcaster != null && goCoderBroadcaster.getStatus().isRunning()) {
            // Stop the broadcast that is currently running
            goCoderBroadcaster.endBroadcast(statusCallback);
        }
        if (goCoder != null && goCoder.isStreaming()){
            goCoder.endStreaming();
        }
        if (goCoderAudioDevice != null && goCoderAudioDevice.isAudioEnabled()){
            goCoderAudioDevice.stopBroadcasting();
        }
        if (mStreamPlayerView != null && mStreamPlayerView.isPlaying()){
            mStreamPlayerView.stop(statusCallback);
        }
    }

    private void watchStream(){
        if (uid != null && streamId != null){
            streamDB.child(uid).child(streamId).child("watched").child(currentUser.getUid()).setValue(Helpers.getLastSeen());
        }
    }

    private void exitStream(){
//        streamDB.child(uid).child(streamId).child("watched").child(currentUser.getUid()).removeValue();
        stopBroadcast();
    }

    private void setViewCount(long count){
        String countString = "";
        if (count > 1){
            countString = count + " " + context.getString(R.string.views);
        } else {
            countString = count + " " + context.getString(R.string.view);
        }
        textViewCount.setText(countString);
        stream.setWatchCount((int) count);
    }

    private void showSnackbar(String text){
        if (activity != null){
            Snackbar.make(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show();
        }
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
                        Log.d(TAG, "viewPopupAction:"+dataSnapshot.toString());
                        Log.d(TAG, "viewPopupAction:size:"+listUser.size()+":"+mapWatched.size()+"|"+dataSnapshot.getChildrenCount());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        exitStream();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        exitStream();
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
            textChatCount.setText(Helpers.getNumberCountFormat(listChat.size()));

            counter = user.getCounter();

            String minute = Helpers.getMinute(counter);
            String second = Helpers.getSecond(counter);
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

            buttonDownload.setVisibility(View.GONE);
            buttonDownload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

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

    private void showComponent(boolean status){
        layoutWatchChild.setVisibility(status ? View.VISIBLE : View.GONE);
    }
}
