package dev.app.com.livechat.activities;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import dev.app.com.livechat.BuildConfig;
import dev.app.com.livechat.R;
import dev.app.com.livechat.adapters.ListMyVideoAdapter;
import dev.app.com.livechat.entities.Stream;

public class MyVideoListActivity extends AppCompatActivity {

    private static final String TAG = MyVideoListActivity.class.getSimpleName();
    private ListView listVideoView;
    private Context context;
    private FirebaseUser currentUser;
    private DatabaseReference streamDB;
    private ListMyVideoAdapter listMyVideoAdapter;
    private List<Stream> listStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_video_list);

        if (getActionBar() != null){
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setDisplayShowHomeEnabled(true);
        } else if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        listVideoView = (ListView) findViewById(R.id.list_video);
    }

    @Override
    protected void onStart() {
        super.onStart();

        context = MyVideoListActivity.this;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        streamDB = FirebaseDatabase.getInstance().getReference("streams");

        listStream = new ArrayList<>();

        listMyVideoAdapter = new ListMyVideoAdapter(context, listStream);
        listVideoView.setAdapter(listMyVideoAdapter);

        listVideoView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(context, "Downloading Video", Toast.LENGTH_LONG).show();
                Stream stream = listStream.get(i);
                if (stream.getDownload_url() != null){
                    String path =  Environment.getExternalStorageDirectory().getPath() + "/" + BuildConfig.APPLICATION_ID;
                    String fileName = path.substring(path.lastIndexOf('/') + 1);
                    Log.d(TAG, path);
                    Log.d(TAG, fileName);

                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(stream.getDownload_url()));
                    request.setDescription(stream.getDownload_url());
                    request.setTitle(stream.getTitle());
                    // in order for this if to run, you must use the android 3.2 to compile your app
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    }
//                        request.setDestinationInExternalPublicDir(path, fileName);
                    request.setDestinationInExternalFilesDir(context, path, fileName);

                    // get download service and enqueue file
                    DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    manager.enqueue(request);
                }
            }
        });

        streamDB.child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    Stream stream = childSnapshot.getValue(Stream.class);
                    Log.d(TAG, childSnapshot.toString());
                    Log.d(TAG, new Gson().toJson(stream));
                    Log.d(TAG, "title:"+stream.getTitle()+",download_url:"+stream.getDownload_url());

                    if (stream != null && stream.getDownload_url() != null){
                        listStream.add(stream);

                        listMyVideoAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
