package dev.app.com.livechat.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.plumillonforge.android.chipview.Chip;
import com.plumillonforge.android.chipview.ChipView;
import com.plumillonforge.android.chipview.OnChipClickListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.app.com.livechat.adapters.MainChipViewAdapter;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.R;
import dev.app.com.livechat.activities.StreamActivity;
import dev.app.com.livechat.entities.Tag;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.entities.WowzaSetting;
import dev.app.com.livechat.utils.PreferenceHelper;
import dev.app.com.livechat.utils.WowzaCloudApi;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LiveStreamPrepareFragment extends Fragment {

    public static final int PICK_IMAGE = 1;
    private static final String TAG = LiveStreamPrepareFragment.class.getSimpleName();

    private ImageButton imageButton;
    private EditText textTitle;
    private Button buttonStartStreaming;
    private View view;
    private StorageReference storageReference;
    private FirebaseUser user;
    private String streamId;
    private EditText textHashtag;
    private TextView textHashtagResult;
    private ChipView chipView;
    private List<Chip> chipList = new ArrayList<Chip>();
    private String stringTag = "";
    private MainChipViewAdapter tagChipViewAdapter;
    private Stream stream;
    private ProgressBar progressBar;
    private int uploadImageStatus = 0; // 0: no status, 1:uploading, 2:uploaded
    private ProgressBar progressBarRound;
    private ImageView imageRefresh;
    private Uri filePath;
    private Bitmap bitmap;
    private DatabaseReference wowzaSettingDB;
    private PreferenceHelper preferenceHelper;
    private View layoutContainer;
    private ImageButton buttonShare;
    private WowzaSetting wowzaSetting;
    private FirebaseDatabase database;
    private View layoutShare;
    private Retrofit retrofit;
    private WowzaCloudApi service;
    private boolean serviceReady = false;
    private Context context;
    private Activity activity;

    public static LiveStreamPrepareFragment newInstance() {
        return new LiveStreamPrepareFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_live_stream_prepare, container, false);
        layoutContainer = view.findViewById(R.id.container);
        imageButton = view.findViewById(R.id.image_thumbnail);
        textTitle = view.findViewById(R.id.text_name);
        textHashtag = view.findViewById(R.id.text_hashtag);
        textHashtagResult = view.findViewById(R.id.text_hashtag_result);
        buttonStartStreaming = view.findViewById(R.id.button_start_streaming);
        progressBar = view.findViewById(R.id.progress_bar);
        progressBarRound = view.findViewById(R.id.progress_bar_round);
        imageRefresh = view.findViewById(R.id.image_refresh);
        layoutShare = view.findViewById(R.id.layout_share);
        buttonShare = view.findViewById(R.id.button_share);

        chipView = view.findViewById(R.id.chipview);
//        chipView.setChipLayoutRes(R.layout.chip_close);
        chipView.setChipList(chipList);
        tagChipViewAdapter = new MainChipViewAdapter(getContext());
        chipView.setAdapter(tagChipViewAdapter);
        chipView.setChipLayoutRes(R.layout.chip_close);

        context = getContext();
        activity = getActivity();

        chipView.setOnChipClickListener(new OnChipClickListener() {
            @Override
            public void onChipClick(Chip chip) {
                chipView.remove(chip);
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageAction(view);
            }
        });

        buttonStartStreaming.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStreamingAction(view);
            }
        });

        textHashtag.addTextChangedListener(hashtagTextWatcher());

        imageRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage(filePath, bitmap);
            }
        });

        layoutShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareAction(view);
            }
        });

//        chipView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
//        chipView.setChipBackgroundColorSelected(getResources().getColor(R.color.colorPrimaryDark));
//        chipView.setChipBackgroundRes(R.drawable.chipview_selector);


        return view;
    }

    private TextWatcher hashtagTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                String regex = "^\\s+";
                String lastRegex = "[\\s,;]+";

                text = text.replaceAll(regex, "");

                if(text.endsWith(" ") || text.endsWith(",") || text.endsWith(".")) {
                    if (!text.isEmpty()){
                        text = text.substring(0,text.length() - 1);
                        if (text.charAt(0) != '#'){
                            text = "#"+text;
                        }
                        if (!checkAvailableTag(chipView.getChipList(), text)){
                            chipView.add(new Tag(text));
                        }
                        editable.clear();
                    }
                }
                Log.d("afterTextChanged", text);
            }
        };
    }

    private void startStreamingAction(View view) {
        String tag = textHashtag.getText().toString();
        String title = textTitle.getText().toString();
        if (streamId == null && uploadImageStatus < 2){
            if (uploadImageStatus == 1){
                Snackbar.make(view, getString(R.string.image_is_still_uploading), Snackbar.LENGTH_LONG).show();
            } else if (!serviceReady) {
                Snackbar.make(view, getString(R.string.service_is_not_ready_try_again), Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(view, getString(R.string.upload_your_thumbnail), Snackbar.LENGTH_LONG).show();
            }
            return;
        } else if (title.length() <= 2){
            Snackbar.make(view, getString(R.string.title_is_invalid), Snackbar.LENGTH_LONG).show();
            return;
        }

        if (!tag.isEmpty()){
            if (tag.charAt(0) != '#'){
                tag = "#"+tag;
            }
            if (!checkAvailableTag(chipView.getChipList(), tag)){
                chipView.add(new Tag(tag));
            }
            textHashtag.setText("");
        }

        List<String> tags = new ArrayList<>();

        for (Chip chip: chipView.getChipList()){
            if (chip.getText().length() > 0 && chip.getText().charAt(0) == '#'){
                tags.add(chip.getText().substring(1));
            } else {
                tags.add(chip.getText());
            }
        }

        long lastSeen = Helpers.getLastSeen();

        stream = new Stream();
        stream.setTitle(title);
        stream.setTags(tags);
        stream.setStreamId(streamId);
        stream.setStartStream(lastSeen);
        stream.setLastActiveStream(lastSeen);

        if (wowzaSetting == null){
            wowzaSetting = getWowzaSetting();
        }

        updateStatusStreamer();

        stream.setAppName(wowzaSetting.getApp());
        stream.setStreamName(wowzaSetting.getName());
        stream.setWowzaSetting(wowzaSetting);
        stream.setStatus(getResources().getInteger(R.integer.stream_status_start));

        database.getReference("streams").child(user.getUid()).child(streamId).setValue(stream)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        preferenceHelper.storeObject("stream", stream);
                        Intent intent = new Intent(getContext(), StreamActivity.class);
                        intent.putExtra("stream", stream);
                        startActivity(intent);
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private WowzaSetting getWowzaSetting() {
        WowzaSetting wowzaSetting = new WowzaSetting();
        preferenceHelper = new PreferenceHelper(getContext(), "wowza.settings");
        HashMap<String, String> mapSetting = (HashMap<String, String>) preferenceHelper.all();

        Log.d(LiveStreamPrepareFragment.class.getSimpleName(), "mapSetting:"+mapSetting.size());

        if (mapSetting.size() > 0){
            for (HashMap.Entry<String, String> setting : mapSetting.entrySet()) {
                Log.d(LiveStreamPrepareFragment.class.getSimpleName(), setting.getKey()+":"+setting.getValue());
                WowzaSetting wz = new Gson().fromJson(setting.getValue(), WowzaSetting.class);

                if (wowzaSetting == null){
                    wowzaSetting = wz;
                    wowzaSetting.setKey(setting.getKey());
                }

                if (wz.getLastUpdate() > 0){
                    long minutes = (Helpers.getLastSeen() - wz.getLastUpdate()) / Helpers.MINUTE_DIVIDER;

                    if (minutes > 5){
                        wowzaSetting = wz;
                        wowzaSetting.setKey(setting.getKey());
                        break;
                    }
                } else {
                    wowzaSetting = wz;
                    wowzaSetting.setKey(setting.getKey());
                    break;
                }
            }
        }

//        wowzaSetting.setAuth(true);
//        wowzaSetting.setHost("7c7ff2.entrypoint.cloud.wowza.com");
//        wowzaSetting.setApp("app-41ec");
//        wowzaSetting.setName("ccdbd040");
////        wowzaSetting.setHost("192.168.43.168");
////        wowzaSetting.setApp("live");
////        wowzaSetting.setName("myStream");
        return wowzaSetting;
    }

    private void pickImageAction(View view) {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent, cameraIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:"+requestCode+":"+resultCode);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Snackbar.make(view, "Image Pick is failed", Snackbar.LENGTH_LONG).show();
                return;
            }

            Uri uri = data.getData();

            try {
                bitmap = null;
                if (uri != null){
                    File file = new File(uri.getPath());
                    filePath = uri;
                    bitmap = BitmapFactory.decodeFile(file.getPath());
                    uploadImage(uri, null);
                }else{
                    bitmap = (Bitmap) data.getExtras().get("data");
                    uploadImage(null, bitmap);
                }
//                InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());

                imageButton.setImageBitmap(bitmap);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(final Uri filePath, final Bitmap bitmap) {
        uploadImageStatus = 0;
        progressBar.setVisibility(View.VISIBLE);
        final String uid = String.valueOf(Helpers.getLastSeen());
        storageReference = FirebaseStorage.getInstance().getReference("streams");
        StorageReference ref = storageReference.child(user.getUid()+"/"+uid);
        StorageTask<UploadTask.TaskSnapshot> task = null;
        if(filePath != null) {
            task = ref.putFile(filePath);
        } else if (bitmap != null){
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            task = ref.putBytes(data);
        }
        Log.d("uploadImage", user.getUid()+"/"+uid+":"+task.toString());

        if (task != null){
            progressBarRound.setVisibility(View.VISIBLE);
            task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Snackbar.make(view, "Thumbnail Uploaded", Snackbar.LENGTH_LONG).show();
                    streamId = uid;
                    progressBar.setVisibility(View.GONE);
                    uploadImageStatus = 2;
                    progressBarRound.setVisibility(View.GONE);
                    imageRefresh.setVisibility(View.GONE);
                    if(filePath != null) {
                        imageButton.setImageURI(filePath);
                    } else if (bitmap != null){
                        imageButton.setImageBitmap(bitmap);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(view, "Upload Failed", Snackbar.LENGTH_LONG).show();
                    streamId = null;
                    progressBar.setVisibility(View.GONE);
                    e.printStackTrace();
                    uploadImageStatus = -1;
                    progressBarRound.setVisibility(View.GONE);
                    imageRefresh.setVisibility(View.VISIBLE);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                    streamId = null;
                    progressBar.setProgress((int) progress);
                    uploadImageStatus = 1;
                    Log.d("uploadImage", taskSnapshot.getBytesTransferred()+":"+taskSnapshot
                            .getTotalByteCount()+"/"+progress);
                    progressBarRound.setVisibility(View.VISIBLE);
                    imageRefresh.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (preferenceHelper == null){
            preferenceHelper = new PreferenceHelper(getContext());
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();


        List<? extends UserInfo> providerData = user.getProviderData();
        HashMap<String, Object> hashMapProvider = new HashMap<>();

        for (UserInfo ui: providerData){
            String json = new Gson().toJson(ui);
            Log.d("providerData", ui.getProviderId()+":"+ui.getEmail()+":"+ui.getPhoneNumber()+":"+ui.isEmailVerified());
            if (ui.getProviderId().equals("google.com")){

            } else if (ui.getProviderId().equals("facebook.com")){

            } else if (ui.getProviderId().equals("twitter.com")){

            } else if (ui.getProviderId().equals("phone")){

            }
        }

        stream = preferenceHelper.retrieveStream("stream");
//        initWowzaSettings();

        wowzaSetting = getWowzaSetting();
        if (stream != null && stream.getLastActiveStream() > 0 && wowzaSetting != null){
            if (stream.getWowzaSetting() == null){
                stream.setWowzaSetting(wowzaSetting);
            }
            updateStatusStreamer();
            float diffMinute = Helpers.getMinuteCount(stream.getLastActiveStream());

            if (diffMinute <= 5){
                Intent intent = new Intent(getContext(), StreamActivity.class);
                intent.putExtra("stream", stream);
                startActivity(intent);
            }
        }

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            // Camera permission granted
//            Snackbar.make(layoutContainer, R.string.camera_permission_granted, Snackbar.LENGTH_SHORT).show();
        } else {
            // Camera permission not granted
            Snackbar.make(layoutContainer,R.string.camera_permission_not_granted, Snackbar.LENGTH_SHORT).show();
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            // record audio permission granted
//            Snackbar.make(layoutContainer, R.string.record_audio_permission_granted, Snackbar.LENGTH_SHORT).show();
        } else {
            // record audio permission not granted
            Snackbar.make(layoutContainer,R.string.record_audio_permission_not_granted, Snackbar.LENGTH_SHORT).show();
        }
    }

    private boolean checkAvailableTag(List<Chip> chipList, String text){
        for (Chip chip: chipList){
            return (chip.getText().equals(text));
        }
        return false;
    }

    private void initWowzaSettings(){
        wowzaSettingDB = database.getReference("wowza/settings");
        wowzaSettingDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("dataSnapshot", dataSnapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void shareAction(View view) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        String host = getString(R.string.web_host) + "/watch.html?uid=" + user.getUid();
        String shareBody = getString(R.string.please_watch_my_show_in);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.my_show));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody + " " +  host);
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    private void updateStatusStreamer(){
        if (wowzaSetting.getId() != null){
            retrofit = Helpers.initRetrofit(getActivity());

            service = retrofit.create(WowzaCloudApi.class);
            Log.d(TAG, "updateStatusStreamer:"+wowzaSetting.getId());
            service.start(wowzaSetting.getId()).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()){
                        serviceReady = false;
                    } else {
                        serviceReady = true;
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
    }
}
