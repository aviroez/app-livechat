package dev.app.com.livechat.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dev.app.com.livechat.R;
import dev.app.com.livechat.activities.MyProfileActivity;
import dev.app.com.livechat.adapters.RankingRecyclerViewAdapter;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.ShowImageHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class RankingFragment extends Fragment {

    private static final String TAG = RankingFragment.class.getSimpleName();
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private RankingRecyclerViewAdapter.OnItemClickListener listener;
    private List<User> listUser = new ArrayList<>();
    private List<User> listUserCountry = new ArrayList<>();
    private RecyclerView recyclerView;
    private RankingRecyclerViewAdapter rankingRecyclerViewAdapter;
    private RankingRecyclerViewAdapter rankingCountryRecyclerViewAdapter;
    private FirebaseUser currentUser;
    private TextView textName;
    private TextView textLevel;
    private ShowImageHelper showImageHelper;
    private ImageView imageProfile;
    private TextView textFollowersCount;
    private TextView textRanking;
    private Intent intent;
    private RecyclerView recyclerViewCountry;
    private TabLayout tabRanking;
    private String country = null;
    private DatabaseReference userDB;
    private int ranking;
    private int rankingCountry;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RankingFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static RankingFragment newInstance(int columnCount) {
        RankingFragment fragment = new RankingFragment();
        Bundle args = new Bundle();
//        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        final Context context = view.getContext();
        listener = new RankingRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                intent = new Intent(context, MyProfileActivity.class);
                intent.putExtra("user", user);
                startActivity(intent);
            }
        };
        rankingRecyclerViewAdapter = new RankingRecyclerViewAdapter(context, listUser, listener);
        rankingCountryRecyclerViewAdapter = new RankingRecyclerViewAdapter(context, listUserCountry, listener);
        textName = (TextView) view.findViewById(R.id.text_name);
        textRanking = (TextView) view.findViewById(R.id.text_ranking);
        textLevel = (TextView) view.findViewById(R.id.text_level);
        textFollowersCount = (TextView) view.findViewById(R.id.text_follower_count);
        imageProfile = (ImageView) view.findViewById(R.id.image_profile);
        tabRanking = (TabLayout) view.findViewById(R.id.tab_ranking);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerViewCountry = (RecyclerView) view.findViewById(R.id.recycler_view_country);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(rankingRecyclerViewAdapter);

        recyclerViewCountry.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewCountry.setAdapter(rankingCountryRecyclerViewAdapter);

        tabRanking.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        recyclerView.setVisibility(View.VISIBLE);
                        recyclerViewCountry.setVisibility(View.GONE);
                        textRanking.setText(String.valueOf(ranking));
                        break;
                    case 1:
                        recyclerView.setVisibility(View.GONE);
                        recyclerViewCountry.setVisibility(View.VISIBLE);
                        textRanking.setText(String.valueOf(rankingCountry));
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        showImageHelper = new ShowImageHelper(context);
        showImageHelper.showImageProfile(currentUser.getUid(), imageProfile);
        userDB = FirebaseDatabase.getInstance().getReference("users");
        if (listUser.size() <= 0){
            userDB.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User myUser = dataSnapshot.getValue(User.class);
                    if (myUser != null){
                        String name = Helpers.getName(currentUser, myUser);
                        int size = 0;
                        if (myUser.getFollowers() != null){
                            size = myUser.getFollowers().size();
                        }
                        textName.setText(name);
                        textLevel.setText("Level: "+myUser.getLevel());
                        textFollowersCount.setText(Helpers.getNumberCountFormat(size));
                        country = myUser.getCountry();
                    }
                    userDB.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            parseUser(dataSnapshot);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            parseUser(dataSnapshot);
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
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void parseUser(DataSnapshot dataSnapshot) {
        User user = dataSnapshot.getValue(User.class);
        if (user != null){
            user.setUid(dataSnapshot.getKey());
            if (user.getLevel() > 0){
                int index = getIndex(user);
                Log.e(TAG, "parseUser:"+user.getEmail()+"|index:"+index);

                if (index >= 0){
                    listUser.remove(index);
                }

                index = rearrange(user, listUser);
                Log.e(TAG, "parseUser:"+user.getEmail()+"|rearrange:"+index);
                if (index >= 0){
                    listUser.add(index, user);
                } else {
                    listUser.add(user);
                }
                rankingRecyclerViewAdapter.notifyDataSetChanged();
                if (user.getCountry() != null && user.getCountry().equalsIgnoreCase(country)){
                    int indexCountry = rearrange(user, listUserCountry);
                    Log.e(TAG, "parseUserCountry:"+user.getEmail()+"|rearrange:"+indexCountry);
                    if (indexCountry >= 0){
                        listUserCountry.add(indexCountry, user);
                    } else {
                        listUserCountry.add(user);
                    }
                    rankingCountryRecyclerViewAdapter.notifyDataSetChanged();
                }

                if (user.getUid().equals(currentUser.getUid())){
                    ranking = listUser.indexOf(user) + 1;
                    rankingCountry = listUserCountry.indexOf(user) + 1;

                    int pos = tabRanking.getSelectedTabPosition();
                    if (pos == 1){
                        textRanking.setText(String.valueOf(rankingCountry));
                    } else {
                        textRanking.setText(String.valueOf(ranking));
                    }
                }
            }
        }
    }

    private int getIndex(User user) {
        if (listUser.size() > 0){
            for (int i = 0; i < listUser.size(); i++){
                if (listUser.get(i).getUid().equals(user.getUid())){
                    return i;
                }
            }
        }
        return -1;
    }

    private int rearrange(User user, List<User> listUser) {
        int index = -1;
        User tempUser = null;
        if (listUser.size() > 0){
            for (int i = 0; i < listUser.size(); i++){
                User u = listUser.get(i);
                boolean checkLevelCurrent = (user.getLevel() >= u.getLevel() && user.getFollowers() != null && u.getFollowers() != null && user.getFollowers().size() > u.getFollowers().size());
                if (tempUser != null){
                    boolean checkLevelTemp = (user.getLevel() >= tempUser.getLevel() && user.getFollowers() != null && tempUser.getFollowers() != null && user.getFollowers().size() > tempUser.getFollowers().size());
                    if (checkLevelCurrent && checkLevelTemp){
                        index = i;
                        tempUser = u;
                    }
                } else {
                    if (checkLevelCurrent){
                        index = i;
                        tempUser = u;
                    }
                }
            }
        }
        return index;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
