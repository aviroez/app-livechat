package dev.app.com.livechat.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

//import com.bambuser.broadcaster.BroadcastStatus;
//import com.bambuser.broadcaster.Broadcaster;
//import com.bambuser.broadcaster.CameraError;
//import com.bambuser.broadcaster.ConnectionError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastAPI.Broadcaster;

import java.io.File;
import java.io.IOException;
import java.util.Timer;

import dev.app.com.livechat.R;
import dev.app.com.livechat.RecorderService;

import static dev.app.com.livechat.BuildConfig.APPLICATION_ID;

public class StreamFirebaseActivity extends AppCompatActivity implements
    SurfaceHolder.Callback {

    private static final String TAG = StreamFirebaseActivity.class.getName();
    public static SurfaceView surfaceView;
    public static SurfaceHolder surfaceHolder;
    private Intent intent;
    public static Camera camera;
    public static boolean previewRunning;
    boolean found = false;
    private MediaRecorder mediaRecorder;
    private String fullPath;
    private SurfaceView previewSurface;
    private Broadcaster mBroadcaster;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_firebase);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        babuserOnCreate();
    }

    private void firebaseOnCreate(){
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        camera = Camera.open();
        camera.setDisplayOrientation(90);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureAction();
            }
        });
    }

    private void actionOnClick() {
        if (intent == null){
            intent = new Intent(StreamFirebaseActivity.this,
                    RecorderService.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(intent);
            finish();
        } else {
            stopService(new Intent(StreamFirebaseActivity.this,
                    RecorderService.class));
            intent = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (camera != null){
            Camera.Parameters params = camera.getParameters();
            camera.setParameters(params);
        }
        else {
            Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        camera.stopPreview();
        camera.release();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private static final int VIDEO_CAPTURE = 101;
    Uri videoUri;
    public void startRecordingVideo() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            File mediaFile = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/myvideo.mp4");
            videoUri = Uri.fromFile(mediaFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            startActivityForResult(intent, VIDEO_CAPTURE);
        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Video has been saved to:\n" + data.getData(), Toast.LENGTH_LONG).show();
                playbackRecordedVideo();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",  Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to record video",  Toast.LENGTH_LONG).show();
            }
        }
    }

    public void playbackRecordedVideo() {
        VideoView mVideoView = (VideoView) findViewById(R.id.video_view);
        mVideoView.setVideoURI(videoUri);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.start();
    }

    protected void startRecording() throws IOException
    {
        mediaRecorder = new MediaRecorder();  // Works well
        camera.unlock();
        String fileName = System.currentTimeMillis()+".mp4";
        File dir = new File(Environment.getExternalStorageDirectory().getPath()+"/"+getApplicationContext().getPackageName()+"/.videos");
        try {
            dir.mkdirs();
        } catch (Exception e){
            e.printStackTrace();
        }

        fullPath = dir.getPath()+"/"+fileName;
        Log.d("fullPath", fullPath);

        Toast.makeText(this, fullPath, Toast.LENGTH_SHORT).show();

        mediaRecorder.setCamera(camera);

        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        mediaRecorder.setOutputFile(fullPath);

        mediaRecorder.prepare();
        mediaRecorder.start();
    }

    protected void stopRecording() {
        if (mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();
        }
        if (camera != null){
            camera.release();
        }
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (camera != null){
            camera.release();        // release the camera for other applications
            camera = null;
        }
    }

    private boolean isRecording = false;
    private void captureAction(){
        if (!isRecording){
            try {
                Toast.makeText(this, "start Recording", Toast.LENGTH_SHORT).show();
                startRecording();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                String message = e.getMessage();
                Log.i("captureAction", "Problem Start"+message);
                stopRecording();
                releaseMediaRecorder();
                releaseCamera();
            }
        } else {
            Toast.makeText(this, "Stop Recording", Toast.LENGTH_SHORT).show();
            releaseMediaRecorder();
            stopRecording();
            releaseCamera();


            uploadFile(fullPath);
        }

        isRecording = !isRecording;
    }

    private void uploadFile(String fileName){
        // File or Blob
        Uri file = Uri.fromFile(new File(fileName));

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

// Create the file metadata
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("video/mp4")
                .build();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference("videos").child(firebaseAuth.getUid());

// Upload file and metadata to the path 'audio/audio.mp3'
        UploadTask uploadTask = storageReference.child(file.getLastPathSegment()).putFile(file, metadata);

// Listen for state changes, errors, and completion of the upload.
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Upload is " + progress + "% done");
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("Upload is paused");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Handle successful uploads on complete
//                Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
// Firebase setting
//        if (!hasPermission(Manifest.permission.CAMERA)
//                && !hasPermission(Manifest.permission.RECORD_AUDIO))
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA,
//                    Manifest.permission.RECORD_AUDIO}, 1);
//        else if (!hasPermission(Manifest.permission.RECORD_AUDIO))
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, 1);
//        else if (!hasPermission(Manifest.permission.CAMERA))
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);

//        mBroadcaster.setCameraSurface(previewSurface);
//        mBroadcaster.onActivityResume();

    }

    private boolean hasPermission(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
