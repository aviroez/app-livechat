package dev.app.com.livechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.ShowImageHelper;

public class MyProfileActivity extends AppCompatActivity {

    private static final String TAG = MyProfileActivity.class.getSimpleName();
    private Intent intent;
    private String uid;
    private User user;
    private FirebaseUser currentUser;
    private TextView textName;
    private TextView textLevel;
    private TextView textLocation;
    private TextView textLastLogin;
    private TextView textFollowerCount;
    private TextView textFollowingCount;
    private TextView textBio;
    private ImageView imageProfile;
    private ShowImageHelper showImageHelper;
    private int watchCount = 0;
    private TextView textEditProfile;
    private View layoutMessage;
    private View layoutLogout;
    private View layoutProfile;
    private View layoutMyVideos;
    private ImageButton buttonShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my_profile);
        setContentView(R.layout.fragment_profile);

        intent = getIntent();
        uid = intent.getStringExtra("uid");
        user = intent.getParcelableExtra("user");

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                intent = new Intent(MyProfileActivity.this, ChatActivity.class);
//                intent.putExtra("uid", uid);
//                intent.putExtra("user", user);
//                startActivity(intent);
//            }
//        });

        setTitle("");

        imageProfile = findViewById(R.id.image_profile);
        textName = findViewById(R.id.text_name);
        textLevel = findViewById(R.id.text_level);
        textLocation = findViewById(R.id.text_location);
        textLastLogin = findViewById(R.id.text_last_login);
        textFollowerCount = findViewById(R.id.text_count_folowers);
        textFollowingCount = findViewById(R.id.text_count_following);
        textBio = findViewById(R.id.text_bio);
        textEditProfile = findViewById(R.id.text_edit_profile);
        buttonShare = findViewById(R.id.button_share);
        layoutMyVideos = findViewById(R.id.layout_my_videos);
        layoutMessage = findViewById(R.id.layout_message);
        layoutLogout = findViewById(R.id.layout_logout);
        layoutProfile = findViewById(R.id.layout_profile);

        textEditProfile.setVisibility(View.GONE);
        layoutMessage.setVisibility(View.VISIBLE);
        layoutProfile.setVisibility(View.GONE);
        layoutLogout.setVisibility(View.GONE);

        layoutMyVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myVideosAction(view);
            }
        });

        layoutMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageAction(view);
            }
        });

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareAction(view);
            }
        });
    }

    private void shareAction(View view) {
        if (user != null){
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String host = getString(R.string.web_host) + "/users.html?uid=" + user.getUid();
            String shareBody = getString(R.string.please_watch_my_show_in);
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.my_show));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody + " " +  host);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
    }

    private void messageAction(View view) {
        if (user != null){
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("uid", user.getUid());
            startActivity(intent);
        }
    }

    private void myVideosAction(View view) {

    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            uid = user.getUid();

            if (uid.equals(currentUser.getUid())){
                layoutMessage.setVisibility(View.GONE);
            }

            setTitle(user.getName());
            textName.setText(Helpers.getName(user));
            textLevel.setText("Lvl 1");
            textFollowerCount.setText("0");
            textFollowingCount.setText("0");

            if (textLocation != null){
                textLocation.setText(user.getLocation());
            }

            if (textLastLogin != null){
                textLastLogin.setText(Helpers.getTimeAgo(user.getLast_seen()));
            }

            if (textBio != null){
                textBio.setText(user.getBio());
            }
        }

        showImageHelper = new ShowImageHelper(this);
        showImageHelper.showImageProfile(uid, imageProfile);

        FirebaseDatabase.getInstance().getReference("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                Log.d(TAG, "onDataChange:"+dataSnapshot.toString());

                if (user != null){
                    user.setUid(dataSnapshot.getKey());
                    setTitle(Helpers.getName(user));
                    textName.setText(Helpers.getName(user));
                    textLevel.setText("Level: "+user.getLevel());
                    textLocation.setText(user.getLocation() != null ? user.getLocation() : "");
//                    textLastLogin.setText(Helpers.getTimeAgo(user.getLast_seen()));
                    if (dataSnapshot.child("follows").exists()){
                        textFollowingCount.setText(Helpers.getNumberCountFormat(dataSnapshot.child("follows").getChildrenCount()));
                    }
                    if (dataSnapshot.child("followers").exists()){
                        textFollowerCount.setText(Helpers.getNumberCountFormat(dataSnapshot.child("followers").getChildrenCount()));
                    }

                    if (textBio != null){
                        textBio.setText(user.getBio());
                    }

//                    FirebaseDatabase.getInstance().getReference("streams").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            Log.d(TAG, "streams:"+dataSnapshot.toString());
//                            if (dataSnapshot.hasChildren()){
//                                for (DataSnapshot snap: dataSnapshot.getChildren()){
//                                    if (snap.child("watched").hasChildren()){
//                                        watchCount += snap.child("watched").getChildrenCount();
//                                    }
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
