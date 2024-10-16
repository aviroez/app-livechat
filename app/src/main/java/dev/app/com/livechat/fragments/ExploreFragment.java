package dev.app.com.livechat.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

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
import com.plumillonforge.android.chipview.Chip;
import com.plumillonforge.android.chipview.ChipView;
import com.plumillonforge.android.chipview.OnChipClickListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dev.app.com.livechat.R;
import dev.app.com.livechat.activities.NavigationActivity;
import dev.app.com.livechat.activities.WatchStreamActivity;
import dev.app.com.livechat.adapters.ExploreRecyclerViewAdapter;
import dev.app.com.livechat.adapters.GridViewAdapter;
import dev.app.com.livechat.adapters.HashTagChipViewAdapter;
import dev.app.com.livechat.adapters.HashtagRecyclerViewAdapter;
import dev.app.com.livechat.adapters.LocationAdapter;
import dev.app.com.livechat.adapters.LocationRecyclerViewAdapter;
import dev.app.com.livechat.adapters.MainChipViewAdapter;
import dev.app.com.livechat.adapters.RankingRecyclerViewAdapter;
import dev.app.com.livechat.entities.HashTag;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.entities.Tag;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.PreferenceHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExploreFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExploreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExploreFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SEARCH_PARAM = "search";
    private static final String TAG = ExploreFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String search;

    private OnFragmentInteractionListener mListener;
    private ChipView chipViewHashtag;
    private ChipView chipViewLocation;
    private RecyclerView recyclerViewHot;
    private RecyclerView recyclerViewNew;
    private TextView textMoreRegions;
    private TextView textMoreHotLive;
    private TextView textMoreNewPlayer;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private PreferenceHelper preferenceHelper;
    private PreferenceHelper preferenceStreamHelper;
    private Context context;
    private DatabaseReference userDB;
    private DatabaseReference streamDB;
    private User user;
    private Stream stream;
    private String streamId;
    private List<User> listUser = new ArrayList<>();
    private List<User> listUserHot = new ArrayList<>();
    private List<User> listUserNew = new ArrayList<>();
    private Set<String> listTag = new LinkedHashSet<>();
    private Set<String> listLocation = new LinkedHashSet<>();
    private ExploreRecyclerViewAdapter exploreRecyclerViewHotAdapter;
    private ExploreRecyclerViewAdapter exploreRecyclerViewNewAdapter;
    private ExploreRecyclerViewAdapter.OnItemClickListener listenerHot;
    private ExploreRecyclerViewAdapter.OnItemClickListener listenerNew;
    private HashtagRecyclerViewAdapter.OnItemClickListener listenerHashtag;
    private LocationAdapter.OnItemClickListener listenerLocation;
    private RecyclerView recyclerViewHashtag;
    private TextView textMoreHashtag;
    private GridView gridCountry;
    private DatabaseReference tagDB;
    private HashtagRecyclerViewAdapter hashtagRecyclerViewAdapter;
    private Intent intent;
    private LocationRecyclerViewAdapter locationRecyclerViewAdapter;
    private LocationAdapter locationAdapter;
    private Dialog dialog;

    public ExploreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param search Parameter SEARCH query.
     * @return A new instance of fragment ExploreFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExploreFragment newInstance(String search) {
        ExploreFragment fragment = new ExploreFragment();
        Bundle args = new Bundle();
        args.putString(SEARCH_PARAM, search);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            search = getArguments().getString(SEARCH_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        recyclerViewHashtag = view.findViewById(R.id.recycler_view_hashtag);
        gridCountry = view.findViewById(R.id.grid_country);

        recyclerViewHot = view.findViewById(R.id.recycler_view_hot);
        recyclerViewNew = view.findViewById(R.id.recycler_view_new);

        textMoreHashtag = view.findViewById(R.id.text_more_hashtag);
        textMoreRegions = view.findViewById(R.id.text_more_regions);
        textMoreHotLive = view.findViewById(R.id.text_more_hot_live);
        textMoreNewPlayer = view.findViewById(R.id.text_more_new_player);
        context = getContext();
        if (context == null){
            context = getActivity();
        }
        listenerHot = new ExploreRecyclerViewAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(User user) {
                intent = new Intent(context, WatchStreamActivity.class);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("streamId", user.getStream().getStreamId());
                intent.putExtra("streamKeys", user.getUid()+"/"+user.getStream().getStreamId());
                startActivity(intent);
            }
        };
        listenerNew = new ExploreRecyclerViewAdapter.OnItemClickListener(){
            @Override
            public void onItemClick(User user) {
                intent = new Intent(context, WatchStreamActivity.class);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("streamId", user.getStream().getStreamId());
                intent.putExtra("streamKeys", user.getUid()+"/"+user.getStream().getStreamId());
                startActivity(intent);
            }
        };
        listenerHashtag = new HashtagRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String string) {
                intent = new Intent(context, NavigationActivity.class);
                intent.putExtra("redirect", R.id.navigation_search);
                intent.putExtra("search", string);
                startActivity(intent);
            }
        };
        listenerLocation = new LocationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String string) {
                intent = new Intent(context, NavigationActivity.class);
                intent.putExtra("redirect", R.id.navigation_search);
                intent.putExtra("search", string);
                startActivity(intent);
            }
        };

        LinearLayoutManager layoutManagerHashtag = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        hashtagRecyclerViewAdapter = new HashtagRecyclerViewAdapter(context, listTag, listenerHashtag);
        recyclerViewHashtag.setAdapter(hashtagRecyclerViewAdapter);
        recyclerViewHashtag.setLayoutManager(layoutManagerHashtag);

        locationAdapter = new LocationAdapter(context, listLocation, listenerLocation);
        gridCountry.setAdapter(locationAdapter);

        LinearLayoutManager layoutManagerHot = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        exploreRecyclerViewHotAdapter = new ExploreRecyclerViewAdapter(context, listUserHot, listenerHot);
        recyclerViewHot.setAdapter(exploreRecyclerViewHotAdapter);
        recyclerViewHot.setLayoutManager(layoutManagerHot);

        LinearLayoutManager layoutManagerNew = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        exploreRecyclerViewNewAdapter = new ExploreRecyclerViewAdapter(context, listUserNew, listenerNew);
        recyclerViewNew.setAdapter(exploreRecyclerViewNewAdapter);
        recyclerViewNew.setLayoutManager(layoutManagerNew);

        recyclerViewHot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        recyclerViewNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        textMoreHashtag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreHashtagAction(view);
            }
        });

        textMoreRegions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreRegionAction(view);
            }
        });

        textMoreHotLive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreHotLiveAction(view);
            }
        });

        textMoreNewPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreNewPlayerAction(view);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userDB = database.getReference("users");
        streamDB = database.getReference("streams");
        tagDB = database.getReference("tags");

        preferenceHelper = new PreferenceHelper(context, "users");
        preferenceStreamHelper = new PreferenceHelper(context, "streams");

        initGridData();
        getMyUser();
        getTagList();
    }

    private void getTagList() {
        tagDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "getTagList:"+dataSnapshot.toString());
                listTag.add(dataSnapshot.getKey());
                hashtagRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(TAG, "getTagList:"+dataSnapshot.toString());
                listTag.add(dataSnapshot.getKey());
                hashtagRecyclerViewAdapter.notifyDataSetChanged();
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

    private void initGridData(){
        streamDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                parseStreamData(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
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
        });
    }

    private void getMyUser() {
        userDB.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "myUser:"+dataSnapshot.toString());
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void parseStreamData(DataSnapshot dataSnapshot){
        streamId = null;
        try {
            Log.d(TAG, "parseStreamData:"+dataSnapshot.toString());
            if (!currentUser.getUid().equals(dataSnapshot.getKey()) && dataSnapshot.hasChildren()){
                Query lastQuery = dataSnapshot.getRef().orderByKey().limitToLast(1);
                final String uid = dataSnapshot.getKey();
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "parseStreamData:onDataChange:"+dataSnapshot.toString());
                        for (DataSnapshot childSnapshot: dataSnapshot.getChildren()){
                            Log.d(TAG, "parseStreamData:"+childSnapshot.getKey()+":"+childSnapshot.toString());

                            if (childSnapshot.getKey() != null){
                                streamId = childSnapshot.getKey();

                                stream = childSnapshot.getValue(Stream.class);
                                if (stream != null && stream.getLastActiveStream() > 0){
                                    stream.setUid(uid);
                                    stream.setStreamId(streamId);
                                    stream.setWatchCount((int) childSnapshot.child("watched").getChildrenCount());
                                    preferenceStreamHelper.storeObject(uid+"."+streamId, stream);

                                    parseUserData(stream);
                                    addTags(stream);
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

    private void parseUserData(final Stream stream){
        User user = preferenceHelper.retrieveUser(stream.getUid());
        boolean newUser = true;
        if (user != null) {
            long lastSeen = Helpers.getLastSeen();
            long hours = (lastSeen - user.getLast_seen()) / Helpers.HOUR_DIVIDER;
        }

        if (newUser){
            userDB.child(stream.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "parseUserData:"+dataSnapshot.toString());
                    final User user = dataSnapshot.getValue(User.class);
                    if (user != null){
                        user.setUid(dataSnapshot.getKey());
                        addLocation(user);
                        showUser(user, stream);
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

    private void addLocation(User user) {
        if (user != null && user.getLocation() != null){
            listLocation.add(user.getLocation());
            locationAdapter.notifyDataSetChanged();
        }
    }

    private void addTags(Stream stream) {
        if (stream != null && stream.getTags() != null && stream.getTags().size() > 0){
            listTag.addAll(stream.getTags());
            exploreRecyclerViewHotAdapter.notifyDataSetChanged();
        }
    }

    private void showUser(User user, Stream stream) {
        Log.d(TAG, "showUser:uid:"+user.getUid());
        user.setStream(stream);
        user.setLast_seen(Helpers.getLastSeen());

        if (listUser != null && listUser.size() > 0){
            for (int i = 0; i < listUser.size(); i++){
                if (listUser.get(i).getUid().equals(user.getUid())){
                    listUser.remove(i);
                    i--;
                    Log.d(TAG, "showUser:"+i+","+user.getUid());
                }
            }
        }
        listUser.add(user);
        if (stream.getWatchCount() >= context.getResources().getInteger(R.integer.hot_count)){
            listUserHot.add(user);
            exploreRecyclerViewHotAdapter.notifyDataSetChanged();
        } else {
            long minutes = (Helpers.getLastSeen() - stream.getStartStream()) / Helpers.MINUTE_DIVIDER;
//        if (minutes <= context.getResources().getInteger(R.integer.new_count)){
                listUserNew.add(user);
                exploreRecyclerViewNewAdapter.notifyDataSetChanged();
//            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void moreHashtagAction(View view) {
        dialog = getDialog(R.string.hashtag);
        ChipView chipView = (ChipView) dialog.findViewById(R.id.chip_view);

        chipView.setVisibility(View.VISIBLE);
        List<Chip> chipList = new ArrayList<>();

        chipView.setChipList(chipList);
        MainChipViewAdapter adapter = new MainChipViewAdapter(getContext(), R.layout.chip);
        chipView.setAdapter(adapter);
        if (listTag.size() > 0){
            for (String string: listTag){
                chipView.add(new Tag("#"+string));
            }
        }
        chipView.setOnChipClickListener(new OnChipClickListener() {
            @Override
            public void onChipClick(Chip chip) {
                intent = new Intent(context, NavigationActivity.class);
                intent.putExtra("redirect", R.id.navigation_search);
                intent.putExtra("search", chip.getText());
                startActivity(intent);
            }
        });
    }

    private void moreNewPlayerAction(View view) {
        dialog = getDialog(R.string.new_player);
        GridView gridView = (GridView) dialog.findViewById(R.id.grid_view);

        gridView.setVisibility(View.VISIBLE);

        GridViewAdapter adapter = new GridViewAdapter(context, listUserNew);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = listUserNew.get(i);
                intent = new Intent(context, WatchStreamActivity.class);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("streamId", user.getStream().getStreamId());
                intent.putExtra("streamKeys", user.getUid()+"/"+user.getStream().getStreamId());
                startActivity(intent);
            }
        });
    }

    private void moreHotLiveAction(View view) {
        dialog = getDialog(R.string.hot_live);
        GridView gridView = (GridView) dialog.findViewById(R.id.grid_view);

        gridView.setVisibility(View.VISIBLE);

        GridViewAdapter adapter = new GridViewAdapter(context, listUserHot);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User user = listUserHot.get(i);
                intent = new Intent(context, WatchStreamActivity.class);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("streamId", user.getStream().getStreamId());
                intent.putExtra("streamKeys", user.getUid()+"/"+user.getStream().getStreamId());
                startActivity(intent);
            }
        });
    }

    private void moreRegionAction(View view) {
        dialog = getDialog(R.string.countries_region);
        ListView listPopupView = (ListView) dialog.findViewById(R.id.list_popup_view);

        listPopupView.setVisibility(View.VISIBLE);

        LocationAdapter.OnItemClickListener listener = new LocationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String string) {
                intent = new Intent(context, NavigationActivity.class);
                intent.putExtra("redirect", R.id.navigation_search);
                intent.putExtra("search", string);
                startActivity(intent);
            }
        };
        LocationAdapter adapter = new LocationAdapter(context, listLocation, listener);
        adapter.setHideCountry(true);
        listPopupView.setAdapter(adapter);
    }

    private Dialog getDialog(int title){
        if (dialog == null){
            dialog = new Dialog(context);
        } else {
            dialog.dismiss();
        }
        dialog.setContentView(R.layout.popup_list);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        TextView textTitle = dialog.findViewById(R.id.text_title);
        textTitle.setText(context.getText(title));
        return dialog;
    }
}
