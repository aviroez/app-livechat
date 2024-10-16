package dev.app.com.livechat.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dev.app.com.livechat.adapters.GridViewAdapter;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.entities.User;

public class ShowImageHelper {
    private Context context;
    private PreferenceHelper streamPreferenceHelper;
    private PreferenceHelper userPreferenceHelper;
    private StorageReference storageReference;
    private static String TAG = ShowImageHelper.class.getSimpleName();
    private static List<User> listUser = new ArrayList<>();
    private static List<ImageView> listImage = new ArrayList<>();

    public ShowImageHelper(Context context) {
        this.context = context;
    }

    public void showImageGrid(User user, final ImageView imageView) {
        if (!(user != null && user.getStream() != null)){
            Log.d(TAG, "showImageGrid");
            return;
        }
        if (streamPreferenceHelper == null) {
            streamPreferenceHelper = new PreferenceHelper(context, "streams");
        }
        final String streamKey = streamPreferenceHelper.generateStreamKey(user.getUid(), user.getStream().getStreamId());
        final Stream stream = streamPreferenceHelper.retrieveStream(streamKey);

        File rootPath = new File(Environment.getExternalStorageDirectory(), Helpers.getApplicationName(context) + "/.tempImages/streams/" + user.getUid());
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        final File localFile = new File(rootPath, user.getStream().getStreamId() + ".png");

        if (localFile.exists()) {
            Glide.with(context).load(localFile).into(imageView);
        } else {
            if (stream != null && stream.getProcess() == 0) {
                storageReference = FirebaseStorage.getInstance().getReference("streams").child(user.getUid()).child(user.getStream().getStreamId());
                Log.d("listUser", user.getName() + "," + user.getEmail() + ":" + storageReference.getPath());

                storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess:file " + taskSnapshot.toString());

                        Glide.with(context).load(localFile).into(imageView);
                        stream.setLastActiveStream(Helpers.getLastSeen());
                        stream.setProcess(2);
                        streamPreferenceHelper.storeObject(streamKey, stream);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(TAG,"onFailure:message " +exception.getMessage());
                        Log.d(TAG,"onFailure:file " +localFile.toString());
                        exception.printStackTrace();
                        stream.setProcess(0);
                        streamPreferenceHelper.storeObject(streamKey, stream);
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        stream.setProcess(1);
                        streamPreferenceHelper.storeObject(streamKey, stream);
                        Log.d(TAG, "onProgress:file " + taskSnapshot.getStorage().getPath() + ":" + taskSnapshot.getTotalByteCount() + "," + taskSnapshot.getBytesTransferred());
                    }
                })
                ;
            }
        }
    }

    public void showImageGrid(String uid, String streamId, final ImageView imageView) {
        if (streamPreferenceHelper == null) {
            streamPreferenceHelper = new PreferenceHelper(context, "streams");
        }
        final String streamKey = streamPreferenceHelper.generateStreamKey(uid, streamId);
        final Stream stream = streamPreferenceHelper.retrieveStream(streamKey);

        File rootPath = new File(Environment.getExternalStorageDirectory(), Helpers.getApplicationName(context) + "/.tempImages/streams/" + uid);
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        final File localFile = new File(rootPath, streamId + ".png");

        if (localFile.exists()) {
            Glide.with(context).load(localFile).into(imageView);
            imageView.setVisibility(View.VISIBLE);
        } else {
            if (stream != null && stream.getProcess() == 0) {
                storageReference = FirebaseStorage.getInstance().getReference("streams").child(uid).child(streamId);

                storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess:file " + taskSnapshot.toString());

                        Glide.with(context).load(localFile).into(imageView);
                        imageView.setVisibility(View.VISIBLE);
                        stream.setLastActiveStream(Helpers.getLastSeen());
                        stream.setProcess(2);
                        streamPreferenceHelper.storeObject(streamKey, stream);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d(TAG,"onFailure:message " +exception.getMessage());
                        Log.d(TAG,"onFailure:file " +localFile.toString());
                        exception.printStackTrace();
                        stream.setProcess(0);
                        streamPreferenceHelper.storeObject(streamKey, stream);
                    }
                }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        stream.setProcess(1);
                        streamPreferenceHelper.storeObject(streamKey, stream);
                        Log.d(TAG, "onProgress:file " + taskSnapshot.getStorage().getPath() + ":" + taskSnapshot.getTotalByteCount() + "," + taskSnapshot.getBytesTransferred());
                    }
                })
                ;
            }
        }
    }

    public void showImageProfile(final User user, final ImageView imageView){
//        listUser.add(0, user);
//        listImage.add(0, imageView);
        if (userPreferenceHelper == null){
            userPreferenceHelper = new PreferenceHelper(context, "users");
        }

        User userPref = userPreferenceHelper.retrieveUser(user.getUid());

        File rootPath = new File(Environment.getExternalStorageDirectory(), Helpers.getApplicationName(context)+"/.tempImages/users");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, user.getUid()+".png");

        boolean newUserParse = true;
        if (localFile.exists()){
            if (userPref != null){
                long lastSeen = Helpers.getLastSeen();
                long hours = (lastSeen - userPref.getLast_seen()) / Helpers.HOUR_DIVIDER;

                if (hours <= 24 * 7){
                    newUserParse = false;
                    Glide.with(context).load(localFile).into(imageView);
                }
            }
        }
//        Log.d(TAG, "gson:"+new Gson().toJson(userPref));
//        Log.d(TAG, "class:"+new Gson().toJson(user));

        if (newUserParse){
            storageReference = FirebaseStorage.getInstance().getReference("images").child(user.getUid());

            Log.d(TAG, "localFile:"+localFile.getAbsolutePath());
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG,"onSuccess:file " +taskSnapshot.toString());

//                            user.setImage_url(localFile.getPath());
                            user.setLast_seen(Helpers.getLastSeen());
                            Glide.with(context).load(localFile).into(imageView);
                            userPreferenceHelper.storeObject(user.getUid(), user);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG,"onFailure:message " +e.getMessage());
                            Log.d(TAG,"onFailure:file " +localFile.toString());
                            e.printStackTrace();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG,"onProgress:file " +taskSnapshot.getBytesTransferred()+"/"+taskSnapshot.getTotalByteCount());
                            Log.d(TAG,"onProgress:file " +localFile.toString());
                        }
                    })
            ;
        }
    }

    public void showImageProfile(final String uid, final ImageView imageView){
        if (userPreferenceHelper == null){
            userPreferenceHelper = new PreferenceHelper(context, "users");
        }

        User userPref = userPreferenceHelper.retrieveUser(uid);

        File rootPath = new File(Environment.getExternalStorageDirectory(), context.getPackageName()+"/.tempImages/users");
        if(!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, uid+".png");

        boolean newUserParse = true;
        if (localFile.exists()){
            if (userPref != null){
                long lastSeen = Helpers.getLastSeen();
                long hours = (lastSeen - userPref.getLast_seen()) / Helpers.HOUR_DIVIDER;

                Glide.with(context).load(localFile).into(imageView);

                if (hours <= 24){
                    newUserParse = false;
                }
            }
        }

        if (newUserParse){
            storageReference = FirebaseStorage.getInstance().getReference("images").child(uid);

            Log.d(TAG, "localFile:"+localFile.getAbsolutePath());
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG,"onSuccess:file " +taskSnapshot.toString());
                            Log.d(TAG,"onSuccess:file " +localFile.toString());
                            try {
                                Glide.with(context).load(localFile).into(imageView);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG,"onFailure:message " +e.getMessage());
                            Log.d(TAG,"onFailure:file " +localFile.toString());
                            e.printStackTrace();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Log.d(TAG,"onProgress:file " +taskSnapshot.getBytesTransferred()+"/"+taskSnapshot.getTotalByteCount());
                            Log.d(TAG,"onProgress:file " +localFile.toString());
                        }
                    })
                    ;
        }
    }
}
