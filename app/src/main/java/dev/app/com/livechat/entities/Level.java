package dev.app.com.livechat.entities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import dev.app.com.livechat.utils.PreferenceHelper;

public class Level {
    private static final String TAG = Level.class.getSimpleName();
    private int followers;
    private PreferenceHelper preferenceHelper;
    private Context context;
    private User user;
    private int level;
    int[] LEVEL_POINTS = new int[]{0, 10, 50, 100, 500, 1000, 5000, 10000, 100000, 500000, 1000000000};
    private int watchCount;

    public Level(Context context, User user) {
        this.context = context;
        this.user = user;
        if (user != null && user.getUid() != null){
            preferenceHelper = new PreferenceHelper(context, "levels");
            level = preferenceHelper.retrieve(user.getUid(), 0);
        }
    }

    public int getLevel() {
        return preferenceHelper.retrieve(user.getUid(), 0);
    }

    public void countLevel(final OnLevelListener onLevelListener){
        if (user != null){
            followers = user.getFollowers() != null ? user.getFollowers().size() : 0;
            watchCount = 0;
            FirebaseDatabase.getInstance().getReference("streams").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "countLevel:"+dataSnapshot.toString());
                    if (dataSnapshot.hasChildren()){
                        for (DataSnapshot snap: dataSnapshot.getChildren()){
                            watchCount += snap.child("watched").getChildrenCount();
                        }
                    }
                    Log.d(TAG, "calculateLevel:"+level+","+followers+","+watchCount);
                    level = calculateLevel(followers, watchCount);
                    preferenceHelper.store("level", level);
                    saveLevel(level);
                    onLevelListener.retrieve(level);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private int calculateLevel(int followers, int watchCount){
        for (int i = LEVEL_POINTS.length - 1; i >= 0; i--){
            int lvPoint = LEVEL_POINTS[i];
            if (lvPoint <= (followers + (watchCount / 3))){
                return i+1;
            }
        }
        return 0;
    }
    public interface OnLevelListener {
        void retrieve(int level);
    }

    public void saveLevel(final long level){
        if (level > user.getLevel()){
            FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("level").runTransaction(new Transaction.Handler() {

                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    Long value = mutableData.getValue(Long.class);
                    if (value == null || level > value) {
                        mutableData.setValue(level);
                    }

                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b,
                                       DataSnapshot dataSnapshot) {
                    Log.d(TAG, "transaction:onComplete:" + databaseError);
                }
            });
        }
    }
}
