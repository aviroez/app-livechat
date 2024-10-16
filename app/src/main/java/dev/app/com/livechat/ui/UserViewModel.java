package dev.app.com.livechat.ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.PreferenceHelper;

public class UserViewModel extends AndroidViewModel {
    private static final String TAG = UserViewModel.class.getSimpleName();
    private MutableLiveData<User> mutableUser = new MutableLiveData<User>();
    private List<User> listUser = new ArrayList<>();
    private HashMap<String, User> listUserMap = new HashMap<>();
    private FirebaseDatabase database;
    private DatabaseReference userDB;
    private DatabaseReference streamDB;
    private ChildEventListener childEventListener;
    private FirebaseUser currentUser;
    private Stream stream;
    private PreferenceHelper preferenceStreamHelper;
    private PreferenceHelper preferenceUserHelper;
    private Context context;

    public UserViewModel(@NonNull Application application) {
        super(application);
        context = application;
    }

    public MutableLiveData<User> getMutableUser() {
        return mutableUser;
    }
    private MutableLiveData<List<User>> userList;
    private MutableLiveData<HashMap<String, User>> userMap;

    public LiveData<List<User>> getUserList() {
        if (userList == null) {
            userList = new MutableLiveData<>();
            loadUsers();
        }
        return userList;
    }

    public MutableLiveData<HashMap<String, User>> getUserMap() {
        if (userMap == null) {
            userMap = new MutableLiveData<>();
            loadUsersMap();
        }
        return userMap;
    }

    private void loadUsers() {
        if (context == null){
            context = getApplication().getApplicationContext();
        }
        if (preferenceStreamHelper == null){
            preferenceStreamHelper = new PreferenceHelper(context, "streams");
        }
        database = FirebaseDatabase.getInstance();
        userDB = database.getReference("users");
        streamDB = database.getReference("streams");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (childEventListener == null) {
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String streamId = null;
                    try {
                        Log.d(TAG, dataSnapshot.toString());
                        if (!currentUser.getUid().equals(dataSnapshot.getKey()) && dataSnapshot.hasChildren()) {
                            Query lastQuery = dataSnapshot.getRef().orderByKey().limitToLast(1);
                            final String uid = dataSnapshot.getKey();
                            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d(TAG, dataSnapshot.toString());
                                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                        Log.d(TAG, childSnapshot.getKey() + ":" + childSnapshot.toString());
                                        stream = childSnapshot.getValue(Stream.class);
                                        stream.setUid(uid);
                                        stream.setStreamId(childSnapshot.getKey());
                                        stream.setWatchCount((int) childSnapshot.child("watched").getChildrenCount());

                                        preferenceStreamHelper.storeObject(dataSnapshot.getKey()+"."+childSnapshot.getKey(), stream);

                                        parseUserData(stream);
                                        return;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //Handle possible errors.
                                }
                            });
                        }
                    } catch (DatabaseException e) {
                        e.printStackTrace();
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
            };

            streamDB.addChildEventListener(childEventListener);
        }
    }

    private void parseUserData(final Stream stream){
        if (preferenceUserHelper == null){
            preferenceUserHelper = new PreferenceHelper(context, "users");
        }
        User user = preferenceUserHelper.retrieveUser(stream.getUid());
        boolean newUser = true;
        if (user != null) {
            long lastSeen = Helpers.getLastSeen();
            long hours = (lastSeen - user.getLast_seen()) / Helpers.HOUR_DIVIDER;

            if (hours <= 24){
                newUser = false;
            }
        }

        if (newUser){
            userDB.child(stream.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, dataSnapshot.toString());
                    final User user = dataSnapshot.getValue(User.class);
                    user.setStream(stream);
                    user.setUid(dataSnapshot.getKey());
                    user.setLast_seen(Helpers.getLastSeen());
//                    obsUser.add(user);
                    listUser.add(user);

                    preferenceUserHelper.storeObject(stream.getUid(), user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            user.setStream(stream);
            user.setUid(stream.getUid());
            listUser.add(user);

            preferenceUserHelper.storeObject(stream.getUid(), user);
        }
    }

    private void loadUsersMap() {
        if (context == null){
            context = getApplication().getApplicationContext();
        }
        if (preferenceStreamHelper == null){
            preferenceStreamHelper = new PreferenceHelper(context, "streams");
        }
        database = FirebaseDatabase.getInstance();
        userDB = database.getReference("users");
        streamDB = database.getReference("streams");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (childEventListener == null) {
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String streamId = null;
                    try {
                        final String uid = dataSnapshot.getKey();
                        Log.d(TAG, dataSnapshot.toString());
                        if ((listUserMap.isEmpty() || !listUserMap.containsKey(uid)) && !currentUser.getUid().equals(uid) && dataSnapshot.hasChildren()) {
                            Query lastQuery = dataSnapshot.getRef().orderByKey().limitToLast(1);
                            lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d(TAG, dataSnapshot.toString());
                                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
//                                        Log.d(TAG, childSnapshot.getKey() + ":" + childSnapshot.toString());
                                        stream = childSnapshot.getValue(Stream.class);
                                        stream.setUid(uid);
                                        stream.setStreamId(childSnapshot.getKey());
                                        stream.setWatchCount((int) childSnapshot.child("watched").getChildrenCount());

                                        preferenceStreamHelper.storeObject(dataSnapshot.getKey()+"."+childSnapshot.getKey(), stream);

                                        parseUserMapData(stream);
                                        return;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    //Handle possible errors.
                                }
                            });
                        }
                    } catch (DatabaseException e) {
                        e.printStackTrace();
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
            };

            streamDB.addChildEventListener(childEventListener);
        }
    }

    private void parseUserMapData(final Stream stream){
        if (preferenceUserHelper == null){
            preferenceUserHelper = new PreferenceHelper(context, "users");
        }
        User user = preferenceUserHelper.retrieveUser(stream.getUid());
        boolean newUser = true;
        if (user != null) {
            long lastSeen = Helpers.getLastSeen();
            long hours = (lastSeen - user.getLast_seen()) / Helpers.HOUR_DIVIDER;

            if (hours <= 24){
                newUser = false;
            }
        }

        if (newUser){
            userDB.child(stream.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final User user = dataSnapshot.getValue(User.class);
                    user.setStream(stream);
                    user.setUid(dataSnapshot.getKey());
                    user.setLast_seen(Helpers.getLastSeen());
//                    obsUser.add(user);
                    listUserMap.put(stream.getUid(), user);
                    userMap.setValue(listUserMap);
                    Log.d(TAG, listUserMap.size()+":"+dataSnapshot.toString());

                    preferenceUserHelper.storeObject(stream.getUid(), user);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            user.setStream(stream);
            user.setUid(stream.getUid());
            listUser.add(user);

            preferenceUserHelper.storeObject(stream.getUid(), user);
        }
    }

}
