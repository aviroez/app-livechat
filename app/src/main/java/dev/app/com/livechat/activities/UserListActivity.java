package dev.app.com.livechat.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.app.com.livechat.R;
import dev.app.com.livechat.adapters.ListUserAdapter;
import dev.app.com.livechat.adapters.RecyclerUserListAdapter;
import dev.app.com.livechat.entities.Chat;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.fragments.ProfileFragment;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.PreferenceHelper;

public class UserListActivity extends AppCompatActivity {

    private static final String TAG = UserListActivity.class.getSimpleName();
    private Context context;
    private List<User> listUser = new ArrayList<>();
    private ListView listUserView;
    private ListUserAdapter listUserAdapter;
    private FirebaseUser currentUser;
    private DatabaseReference userDB;
    private DatabaseReference chatDB;
    private Intent intent;
    private HashMap<String, String> mapUser = new HashMap<>();
    private PreferenceHelper preferenceHelper;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        setActionBar(toolbar);

        if (getActionBar() != null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(true);
        } else if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        listUserView = (ListView) findViewById(R.id.list_user);
    }

    @Override
    protected void onStart() {
        super.onStart();

        context = UserListActivity.this;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userDB = FirebaseDatabase.getInstance().getReference("users");
        chatDB = FirebaseDatabase.getInstance().getReference("chats");
        preferenceHelper = new PreferenceHelper(this, "user_chats");

        Map<String, ?> keys = preferenceHelper.all();

        for(Map.Entry<String, ?> entry : keys.entrySet()){
            String value = (String) entry.getValue();
            listUser.add(new Gson().fromJson(value, User.class));
        }

        listUserAdapter = new ListUserAdapter(context, listUser);
        listUserView.setAdapter(listUserAdapter);

        listUserView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogBelow(listUser.get(i));
                return true;
            }
        });

        listUserView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                dialogBelow(listUser.get(i));
            }
        });

        chatDB.child(currentUser.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "onChildAdded:"+dataSnapshot.toString());
                Query lastQuery = dataSnapshot.getRef().orderByKey().limitToLast(1);
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot querySnapshot) {
                        Log.d(TAG, "onChildAdded:onDataChange:"+querySnapshot.toString());
                        for (DataSnapshot childSnapshot: querySnapshot.getChildren()) {
                            Chat chat = childSnapshot.getValue(Chat.class);
                            if (chat != null){
                                chat.setUid(dataSnapshot.getKey());
                                chat.setTimer(Long.parseLong(childSnapshot.getKey()));
                                parseUser(querySnapshot, chat);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void parseUser(DataSnapshot dataSnapshot, final Chat chat) {
        userDB.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange:"+dataSnapshot.toString());
                User user = dataSnapshot.getValue(User.class);
                if (user != null){
                    user.setUid(dataSnapshot.getKey());
                    user.setChat(chat);
                    int index = getUserIndex(user);

                    if (index >= 0){
                        listUser.set(index, user);
                    } else {
                        listUser.add(user);
                    }
                    preferenceHelper.store(dataSnapshot.getKey(), new Gson().toJson(user));

                    listUserAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private int getUserIndex(User user){
        int index = -1;

        if (listUser != null && listUser.size() > 0){
            for (int i = 0; i < listUser.size(); i++){
                if (user.getUid().equals(listUser.get(i).getUid())){
                    return i;
                }
            }
        }

        return index;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        intent = new Intent(this, NavigationActivity.class);
        intent.putExtra("redirectString", ProfileFragment.class.getSimpleName());
        startActivity(intent);
    }



    public void dialogBelow(final User user){
        if (dialog == null){
            dialog = new Dialog(context, R.style.AppThemeDialogTransparent);
        }

        dialog.setContentView(R.layout.layout_user_list);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

//        dialog.getWindow().setBackgroundDrawable(null);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.show();

        TextView textName = dialog.findViewById(R.id.text_name);
        TextView textDelete = dialog.findViewById(R.id.text_delete_next);
        TextView textArchive = dialog.findViewById(R.id.text_archive_next);
        TextView textReport = dialog.findViewById(R.id.text_report_spam);

        ImageButton buttonClose = dialog.findViewById(R.id.button_close);
        ImageButton buttonDelete = dialog.findViewById(R.id.button_delete_next);
        ImageButton buttonArchive = dialog.findViewById(R.id.button_archive_next);
        ImageButton buttonReport = dialog.findViewById(R.id.button_report_spam);

        textName.setText(user.getName());

        textName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MyProfileActivity.class);
                intent.putExtra("uid", user.getUid());
                startActivity(intent);
            }
        });

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatDB.child(user.getUid()).child(currentUser.getUid()).removeValue();
                chatDB.child(currentUser.getUid()).child(user.getUid()).removeValue();

                Toast.makeText(context, "Deleting Chat", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        textDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chatDB.child(user.getUid()).child(currentUser.getUid()).removeValue();
                chatDB.child(currentUser.getUid()).child(user.getUid()).removeValue();

                Toast.makeText(context, "Deleting Chat", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        buttonArchive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Chat is archived", Toast.LENGTH_LONG).show();
                dialog.dismiss();

            }
        });

        textArchive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Chat is archived", Toast.LENGTH_LONG).show();
                dialog.dismiss();

            }
        });

        buttonReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDB.child(user.getUid()).child("report").child(currentUser.getUid()).setValue(Helpers.getLastSeen());

                Toast.makeText(context, "User is reported successfully", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        textReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userDB.child(user.getUid()).child("report").child(currentUser.getUid()).setValue(Helpers.getLastSeen());

                Toast.makeText(context, "User is reported successfully", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
    }
}
