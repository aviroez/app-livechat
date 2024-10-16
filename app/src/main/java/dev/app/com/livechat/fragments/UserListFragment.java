package dev.app.com.livechat.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
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
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.app.com.livechat.R;
import dev.app.com.livechat.activities.ChatActivity;
import dev.app.com.livechat.activities.MyProfileActivity;
import dev.app.com.livechat.activities.UserListActivity;
import dev.app.com.livechat.adapters.ListUserAdapter;
import dev.app.com.livechat.entities.Chat;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.PreferenceHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserListFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = UserListFragment.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ListView listUserView;
    private Context context;
    private FirebaseUser currentUser;
    private DatabaseReference userDB;
    private DatabaseReference chatDB;
    private PreferenceHelper preferenceHelper;
    private List<User> listUser = new ArrayList<>();
    private ListUserAdapter listUserAdapter;
    private Intent intent;
    private Dialog dialog;

    public UserListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserListFragment newInstance(String param1, String param2) {
        UserListFragment fragment = new UserListFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        listUserView = (ListView) view.findViewById(R.id.list_user);

        return view;
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

    @Override
    public void onStart() {
        super.onStart();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userDB = FirebaseDatabase.getInstance().getReference("users");
        chatDB = FirebaseDatabase.getInstance().getReference("chats");
        preferenceHelper = new PreferenceHelper(context, "user_chats");

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
                User user = listUser.get(i);
                intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("uid", user.getUid());
                intent.putExtra("user", user);
                startActivity(intent);
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
            public void onChildRemoved(@NonNull final DataSnapshot dataSnapshot) {
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
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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

    public void dialogBelow(final User user){
        if (dialog == null){
            dialog = new Dialog(context);
        }

        dialog.setContentView(R.layout.layout_user_list);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
