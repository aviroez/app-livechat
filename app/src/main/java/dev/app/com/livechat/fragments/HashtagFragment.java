package dev.app.com.livechat.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.plumillonforge.android.chipview.Chip;
import com.plumillonforge.android.chipview.ChipView;
import com.plumillonforge.android.chipview.OnChipClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.activities.LoginActivity;
import dev.app.com.livechat.activities.NavigationActivity;
import dev.app.com.livechat.adapters.HashTagChipViewAdapter;
import dev.app.com.livechat.adapters.MainChipViewAdapter;
import dev.app.com.livechat.entities.HashTag;
import dev.app.com.livechat.entities.Tag;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HashtagFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HashtagFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HashtagFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Intent intent;
    private String uid;
    private FirebaseDatabase database;
    private DatabaseReference tagDB;
    private DatabaseReference userDB;
    private ChipView chipViewHashtag;
    private ChipView chipViewLocation;
    private List<Chip> chipListHashtag = new ArrayList<>();
    private List<Chip> chipListLocation = new ArrayList<>();
    private List<HashTag> listLocation = new ArrayList<>();
    private HashTagChipViewAdapter tagChipViewAdapterHashtag;
    private HashTagChipViewAdapter tagChipViewAdapterLocation;
    private Context context;
    private Activity activity;

    public HashtagFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HashtagFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HashtagFragment newInstance(String param1, String param2) {
        HashtagFragment fragment = new HashtagFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        context = getContext();
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hashtag, container, false);
        chipViewHashtag = (ChipView) view.findViewById(R.id.chipview_hashtag);
        chipViewLocation = (ChipView) view.findViewById(R.id.chipview_location);

        chipViewHashtag.setChipList(chipListHashtag);
        tagChipViewAdapterHashtag = new HashTagChipViewAdapter(getContext());
        chipViewHashtag.setAdapter(tagChipViewAdapterHashtag);
        chipViewHashtag.setOnChipClickListener(new OnChipClickListener() {
            @Override
            public void onChipClick(Chip chip) {
                intent = new Intent(context, NavigationActivity.class);
                intent.putExtra("redirect", R.id.navigation_search);
                intent.putExtra("search", chip.getText());
                startActivity(intent);
            }
        });

        chipViewLocation.setChipList(chipListLocation);
        tagChipViewAdapterLocation = new HashTagChipViewAdapter(getContext(), true);
        chipViewLocation.setAdapter(tagChipViewAdapterLocation);
        chipViewHashtag.setOnChipClickListener(new OnChipClickListener() {
            @Override
            public void onChipClick(Chip chip) {
                intent = new Intent(context, NavigationActivity.class);
                intent.putExtra("redirect", R.id.navigation_search);
                intent.putExtra("search", chip.getText());
                startActivity(intent);
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeUsers();

        tagDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d(HashtagFragment.class.getSimpleName(), dataSnapshot.toString());
                setTagDB(dataSnapshot);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                setTagDB(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                setTagDB(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                setTagDB(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                setLocationDB(dataSnapshot);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
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

    private void initializeUsers() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        if (currentUser == null) {
            currentUser = mAuth.getCurrentUser();
        }
        uid = currentUser.getUid();

        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }
        if (tagDB == null) {
            tagDB = database.getReference("tags");
        }
        if (userDB == null) {
            userDB = database.getReference("users");
        }
    }

    private void setLocationDB(DataSnapshot dataSnapshot) {
        Log.d("setLocationDB", dataSnapshot.toString());
        if (dataSnapshot.child("location").exists()){
            String location = dataSnapshot.child("location").getValue(String.class);

            if (location != null && location.length() > 0){
                double latitude = 0;
                double longitude = 0;

                if (dataSnapshot.child("latitude").exists()){
                    latitude = dataSnapshot.child("latitude").getValue(Double.class);
                }
                if (dataSnapshot.child("longitude").exists()){
                    longitude = dataSnapshot.child("longitude").getValue(Double.class);
                }

                HashTag hashTag = new HashTag();
                hashTag.setText(location);

                Log.d(HashtagFragment.class.getSimpleName(), chipListLocation.size()+":"+location);
                int index = getLocationIndex(chipListLocation, location);
                if (index < 0){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(dataSnapshot.getKey(), latitude+","+longitude);
                    hashTag.setHashMap(hashMap);
                    chipViewLocation.add(hashTag);
                    listLocation.add(hashTag);
                } else {
                    HashMap<String, Object> hashMap = listLocation.get(index).getHashMap();
                    hashMap.put(dataSnapshot.getKey(), latitude+","+longitude);
                    hashTag.setHashMap(hashMap);
                    chipViewLocation.getChipList().set(index, hashTag);
//                    chipListLocation.set(index, hashTag);
                    listLocation.add(hashTag);
                }
            }
        }
    }

    private int getLocationIndex(List<Chip> listChip, String location){
        int index = -1;
        if (listChip != null && listChip.size() > 0){
            for (Chip c: listChip){
                Log.d(HashtagFragment.class.getSimpleName(), "getLocationIndex:"+c.getText()+":"+location);
                if (c.getText().equals(location)){
                    return listChip.indexOf(c);
                }
            }
        }
        return index;
    }

    private void setTagDB(DataSnapshot dataSnapshot) {
        HashMap<String, Object> hashMap = new HashMap<>();
        if (dataSnapshot.hasChildren()){
            for (DataSnapshot childSnapshot: dataSnapshot.getChildren()){
                hashMap.put(childSnapshot.getKey(), childSnapshot.getValue());
            }
        }
        HashTag hashTag = new HashTag(dataSnapshot.getKey(), hashMap);
        int index = getHashTagIndex(chipListHashtag, hashTag);
        if (index < 0){
            chipViewHashtag.add(hashTag);
        } else {
            chipViewHashtag.getChipList().set(index, hashTag);
        }
    }

    private int getHashTagIndex(List<Chip> listChip, HashTag hashTag){
        int index = -1;
        if (listChip != null && listChip.size() > 0){
            for (Chip c: listChip){
                if (c.getText().equals(hashTag.getText())){
                    return listChip.indexOf(c);
                }
            }
        }
        return index;
    }
}
