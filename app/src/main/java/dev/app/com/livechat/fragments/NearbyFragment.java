package dev.app.com.livechat.fragments;

import android.app.Application;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Timer;

import dev.app.com.livechat.R;
import dev.app.com.livechat.activities.NavigationActivity;
import dev.app.com.livechat.activities.StreamActivity;
import dev.app.com.livechat.activities.WatchStreamActivity;
import dev.app.com.livechat.adapters.GridViewAdapter;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.ui.UserViewModel;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.PreferenceHelper;
import dev.app.com.livechat.utils.ShowImageHelper;

public class NearbyFragment extends Fragment {

    private static final String TAG = NearbyFragment.class.getSimpleName();
    public static final int MODE_SEARCH = 0;
    public static final int MODE_NEAR = 1;
    public static final int MODE_POPULAR = 2;
    public static final int MODE_NEWEST = 3;
    public static final int SHOW_LAST_STREAM_MINUTE = 24 * 60 * 3;
    private GridView gridView;
    private ArrayList<User> listUser = new ArrayList<User>();
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference userDB;
    private Intent intent;
    private GridViewAdapter gridViewNearby;
    private DatabaseReference streamDB;
    private Stream stream;
    private PreferenceHelper preferenceHelper;
    private PreferenceHelper preferenceStreamHelper;
    private Context context;
    private User myUser = new User();
    private int mode = 0; // mode 0=view; 1=near
    private EditText textSearch;
    private String search = null;
    private boolean showLowMemoryMessage = true;

    public static NearbyFragment newInstance() {
        return new NearbyFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "status:onCreate");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby, container, false);
        Log.d(TAG, "status:onCreateView");
        textSearch = (EditText) view.findViewById(R.id.text_search);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        context = getContext();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null){
            mode = getArguments().getInt("mode", MODE_NEAR);
            search = getArguments().getString("search");
        } else if (savedInstanceState != null){
            mode = savedInstanceState.getInt("mode", MODE_NEAR);
        }
        Log.d(TAG, "status:onViewCreated,mode:"+mode+"|listUser:"+listUser.size());

        if (mode == MODE_NEAR){
            textSearch.setVisibility(View.GONE);
            gridViewNearby = new GridViewAdapter( context, listUser);
        } else if (mode == MODE_POPULAR){
            textSearch.setVisibility(View.GONE);
            gridViewNearby = new GridViewAdapter( context, listUser);
        } else if (mode == MODE_SEARCH){
            gridViewNearby = new GridViewAdapter( context, listUser);
            textSearch.setVisibility(View.VISIBLE);
            if (search != null && search.length() > 0){
                textSearch.setText(search);
                searchText(search);
            }
        }
        gridView.setAdapter(gridViewNearby);

        textSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                searchText(text);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "status:onStart");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userDB = database.getReference("users");

        preferenceHelper = new PreferenceHelper(context, "users");
        preferenceStreamHelper = new PreferenceHelper(context, "streams");

        initGridData();
        getMyUser();
    }

    private void getMyUser() {
        userDB.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.d(TAG, "myUser:"+dataSnapshot.toString());
                myUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//            Log.d(TAG, "onChildAdded:"+dataSnapshot.toString());
            parseStreamData(dataSnapshot);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//            Log.d(TAG, "onChildChanged:"+dataSnapshot.toString());
            parseStreamData(dataSnapshot);
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

    private void parseUserData(final Stream stream){
        User user = preferenceHelper.retrieveUser(stream.getUid());
        boolean newUser = true;
        if (user != null) {
            long lastSeen = Helpers.getLastSeen();
            long hours = (lastSeen - user.getLast_seen()) / Helpers.HOUR_DIVIDER;
            Log.d(TAG, "parseUserData:hours:"+hours);

            if (hours <= 24){
                newUser = false;
            }
        }

        if (newUser){
            userDB.child(stream.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "parseUserData:"+dataSnapshot.toString());
                    final User user = dataSnapshot.getValue(User.class);
                    if (user != null){
                        user.setUid(stream.getUid());
                        showUser(user, stream);

                        if (Helpers.isAppInLowMemory(context) && showLowMemoryMessage){
                            Toast.makeText(context, "Your memory is almost full. Please Free up some spaces!", Toast.LENGTH_SHORT).show();
                            showLowMemoryMessage = false;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            user.setStream(stream);
            user.setUid(stream.getUid());
            showUser(user, stream);
        }
    }

    private void showUser(User user, Stream stream) {
        String uid = stream.getUid();
        Log.d(TAG, "showUser:uid:"+uid+","+stream.getStreamId());
        user.setStream(stream);
        user.setUid(uid);
        user.setLast_seen(Helpers.getLastSeen());

        if (listUser != null && listUser.size() > 0){
            for (int i = 0; i < listUser.size(); i++){
                if (listUser.get(i).getUid().equals(uid)){
                    listUser.remove(i);
                    i--;
                    Log.d(TAG, "showUser:"+i+","+uid);
//                    index = i;
//                    break;
                }
            }
        }

        if (mode == MODE_NEAR || mode == MODE_POPULAR || mode == MODE_NEWEST){
            rearrange(listUser, user);
        } else if (mode == MODE_SEARCH){

            listUser.add(user);
        }
        gridViewNearby.notifyDataSetChanged();
    }

    private void addUser(User user) {
        int index = -1;
        if (listUser.size() > 0){
            for (int i=0; i < listUser.size(); i++){
                User u = listUser.get(i);
                if (u.getUid().equals(user.getUid())){
                    index = i;
                    break;
                }
            }
        }

        if (index >= 0){
            listUser.set(index, user);
        } else {
            listUser.add(user);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }

    private void initGridData(){
        if (preferenceStreamHelper == null){
            preferenceStreamHelper = new PreferenceHelper(context, "streams");
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ArrayList<String> streamKeys = new ArrayList<>();
                if (listUser.size() > 0){
                    for (User u: listUser){
                        if (u.getUid() != null && u.getStream() != null && u.getStream().getStreamId() != null){
                            streamKeys.add(u.getUid()+"/"+u.getStream().getStreamId());
                        }
                    }
                }
                User user = listUser.get(position);
                intent = new Intent(getContext(), WatchStreamActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("streamId", user.getStream().getStreamId());
//                intent.putExtra("user", user);
                intent.putStringArrayListExtra("streamKeys", streamKeys);
                Log.d(TAG, "streamKeys:"+streamKeys);
//                Log.d(NearbyFragment.class.getSimpleName(), "intent:"+new Gson().toJson(streamKeys));
                startActivity(intent);
            }
        });

        database = FirebaseDatabase.getInstance();
        userDB = database.getReference("users");
        streamDB = database.getReference("streams");

        streamDB.addChildEventListener(childEventListener);
    }

    private void rearrange(List<User> listUser, User user){
        boolean check = (user.getLatitude() != 0 || user.getLongitude() != 0) && (myUser.getLatitude() != 0 | myUser.getLongitude() != 0);
        Log.d(TAG, "rearrange:"+user.getLatitude()+","+user.getLongitude()+"|"+check);
        if ((user.getLatitude() != 0 || user.getLongitude() != 0) && (myUser.getLatitude() != 0 | myUser.getLongitude() != 0)){
            double distance = distance(myUser.getLatitude(), myUser.getLongitude(), user.getLatitude(), user.getLongitude()) / 1000; // in km
            user.setDistance(distance);
        }

        if (listUser != null && listUser.size() > 0){
            int index = -1;
            double lastDistance = 0;
            int watchCount  = 0;
            long lastActiveStream  = 0;

            for (int i = listUser.size()-1; i >= 0; i--){
                User u = listUser.get(i);
                Stream currentStream = user.getStream();
                Stream uStream = u.getStream();

                if (mode == MODE_NEAR && user.getDistance() < u.getDistance() && (u.getDistance() < lastDistance || lastDistance == 0)){
                    lastDistance = u.getDistance();
                    index = i;
                } else if (mode == MODE_POPULAR && (currentStream != null && u.getStream() != null
                        && currentStream.getWatchCount() > uStream.getWatchCount()
                        && (uStream.getWatchCount() > watchCount || watchCount == 0))){
                    watchCount = u.getStream().getWatchCount();
                    index = i;
                } else if (mode == MODE_NEWEST
                        && (currentStream != null && u.getStream() != null && currentStream.getLastActiveStream() > uStream.getLastActiveStream()
                        && (uStream.getLastActiveStream() > lastActiveStream || lastActiveStream == 0))){
                    lastActiveStream = currentStream.getLastActiveStream();
                    index = i;
                }
            }



            if (index < 0){
                listUser.add(user);
            } else {
                listUser.add(index, user);
            }
        } else {
            listUser.add(user);
        }
    }

    private void rearrange(List<User> listUser) {
        List<User> listResult = new ArrayList<>();
        for (int i=0; i < listUser.size(); i++){
            User u = listUser.get(i);
            int index = getIndex(listResult, u);

            listResult.add(index, u);
        }
    }

    private int getIndex(List<User> list, User user){
        int index = 0;
        if (list != null && list.size() > 0){
            int i = 0;
            for (User u: list){
                if (mode == MODE_NEAR && user.getDistance() > u.getDistance()){
                    index = i;
                } else if (mode == MODE_POPULAR && (user.getStream() != null && u.getStream() != null && user.getStream().getWatchCount() > u.getStream().getWatchCount())){
                    index = i;
                }
                i++;
            }
        }
        return index;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2){
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);

        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);

        return loc1.distanceTo(loc2);
    }

    private void searchText(String text) {
        Log.d(TAG, "afterTextChanged:"+listUser.size()+":"+mode);
        gridViewNearby.setListUser(listUser);
        gridViewNearby.search(text.replace("#", ""));
        gridViewNearby.notifyDataSetChanged();
    }

    private void parseStreamData(DataSnapshot dataSnapshot){
        String streamId = null;
        try {
//            Log.d(TAG, "parseStreamData:"+dataSnapshot.toString());
            if (!currentUser.getUid().equals(dataSnapshot.getKey()) && dataSnapshot.hasChildren()){
                Query lastQuery = dataSnapshot.getRef().orderByKey().limitToLast(1);
                final String uid = dataSnapshot.getKey();
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Log.d(TAG, dataSnapshot.toString());
                        for (DataSnapshot childSnapshot: dataSnapshot.getChildren()){

                            if (childSnapshot.getKey() != null){
                                Stream stream = childSnapshot.getValue(Stream.class);
                                if (stream != null && stream.getLastActiveStream() > 0){
                                    stream.setUid(uid);
                                    stream.setStreamId(childSnapshot.getKey());
                                    stream.setWatchCount((int) childSnapshot.child("watched").getChildrenCount());
                                    long minute = (Helpers.getLastSeen() - stream.getLastActiveStream()) / Helpers.MINUTE_DIVIDER;
//                                    Log.d(TAG, "parseStreamData:onDataChange:"+dataSnapshot.getKey()+"."+childSnapshot.getKey()+"|"+mode+","+minute);

                                    if (mode == MODE_SEARCH || minute <= SHOW_LAST_STREAM_MINUTE){
                                        preferenceStreamHelper.storeObject(dataSnapshot.getKey()+"."+childSnapshot.getKey(), stream);

                                        parseUserData(stream);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Handle possible errors.
                    }
                });
            }
        } catch (DatabaseException e){
            e.printStackTrace();
        }
    }
}
