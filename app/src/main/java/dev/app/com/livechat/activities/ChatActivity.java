package dev.app.com.livechat.activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.adapters.RecyclerDirectMessageAdapter;
import dev.app.com.livechat.entities.Chat;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.fragments.ProfileFragment;
import dev.app.com.livechat.fragments.UserListFragment;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.PostMessage;

import static dev.app.com.livechat.entities.Chat.STATUS_READ;
import static dev.app.com.livechat.entities.Chat.STATUS_SENT;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    private Context context;
    private List<Chat> listChat = new ArrayList<>();
    private RecyclerView recyclerChat;
    private RecyclerDirectMessageAdapter recyclerDirectMessageAdapter;
    private String uid;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference chatDB;
    private EditText textChat;
    private ImageButton buttonSend;
    private Intent intent;
    private User user;
    private String title;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        actionBar = getActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        } else if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerChat = findViewById(R.id.recycler_chat);
        textChat = findViewById(R.id.text_chat);
        buttonSend = findViewById(R.id.button_send);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null){
            Helpers.logout(this);
            return;
        }

        database = FirebaseDatabase.getInstance();
        chatDB = database.getReference("chats");

        recyclerDirectMessageAdapter = new RecyclerDirectMessageAdapter(context, currentUser.getUid(), listChat, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Chat chat = listChat.get(position);
            }
        });

        recyclerChat.setAdapter(recyclerDirectMessageAdapter);
        recyclerChat.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        llm.setStackFromEnd(true);
        llm.setReverseLayout(false);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerChat.setLayoutManager(llm);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendChatAction(view);
            }
        });

        context = ChatActivity.this;

        intent = getIntent();
        uid = intent.getStringExtra("uid");
        user = intent.getParcelableExtra("user");

        if (user != null) {
            setTitle(Helpers.getName(user));
            updateLogo();
        } else {
            FirebaseDatabase.getInstance().getReference("users").child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    user = dataSnapshot.getValue(User.class);
                    if (user != null){
                        user.setUid(dataSnapshot.getKey());
                        setTitle(Helpers.getName(user));
                        updateLogo();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        chatDB.child(currentUser.getUid()).child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded:"+listChat.size()+":"+dataSnapshot.toString());
                showChat(dataSnapshot, false);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                showChat(dataSnapshot, true);
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

    private void updateLogo() {
        if (actionBar != null){
            Drawable drawable = null;
//        getActionBar().setLogo(drawable);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void sendChatAction(View view){
        Chat chat = new Chat();
        String text = textChat.getText().toString();

        if (!text.isEmpty()){
            long time = System.currentTimeMillis();
            chat.setUid(currentUser.getUid());
            chat.setMessage(text);
            chat.setStatus(STATUS_SENT);
            chat.setTimer(time);
            chatDB.child(uid).child(currentUser.getUid()).child(String.valueOf(time)).setValue(chat);
            chatDB.child(currentUser.getUid()).child(uid).child(String.valueOf(time)).setValue(chat);

//            Helpers.sendFCM(context, text, uid, "chat");
            PostMessage postMessage = new PostMessage(context);
            postMessage.setBody(text);
            postMessage.setTitle("New Chat From "+currentUser.getDisplayName());
            postMessage.setTopic("chat_"+uid);
            textChat.setText("");

            Helpers.sendNotif(uid, "New Chat From "+currentUser.getDisplayName(), text, currentUser.getUid(), 1);
        }
    }

    private void showChat(DataSnapshot dataSnapshot, boolean edit){
        Chat chat = dataSnapshot.getValue(Chat.class);
        if (chat != null){
//            chat.setUid(dataSnapshot.getKey());
            chat.setTimer(Long.parseLong(dataSnapshot.getKey()));
            if (chat.getStatus() == STATUS_SENT && chat.getUid() != null && chat.getUid().equals(uid)){
                chatDB.child(currentUser.getUid()).child(uid).child(dataSnapshot.getKey()).child("status").setValue(STATUS_READ);
//                chatDB.child(uid).child(currentUser.getUid()).child(dataSnapshot.getKey()).child("status").setValue(STATUS_READ);
            }
            int index = -1;
            if (listChat != null && listChat.size() > 0){
                if (edit) {
                    for (int i = 0; i < listChat.size(); i++){
                        if (Long.parseLong(dataSnapshot.getKey()) == listChat.get(i).getTimer()){
                            index = i;
                            break;
                        }
                    }
                }
            }

            if (index >= 0){
                listChat.set(index, chat);
                recyclerDirectMessageAdapter.notifyItemChanged(index);
                recyclerChat.smoothScrollToPosition(index);
            } else {
                listChat.add(chat);
                recyclerDirectMessageAdapter.notifyItemInserted(listChat.size() - 1);
                recyclerChat.smoothScrollToPosition(listChat.size() - 1);
            }
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

//        intent = new Intent(context, UserListActivity.class);
//        startActivity(intent);
        intent = new Intent(this, NavigationActivity.class);
        intent.putExtra("redirect", R.id.navigation_inbox);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
