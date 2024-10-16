package dev.app.com.livechat.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import dev.app.com.livechat.R;
import dev.app.com.livechat.activities.ChatActivity;
import dev.app.com.livechat.activities.LiveStreamActivity;
import dev.app.com.livechat.activities.WatchStreamActivity;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.PreferenceHelper;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = CustomFirebaseMessagingService.class.getSimpleName();
    private Intent intent;
    private String[] channelIds = new String[]{"chat", "stream"};
    private String[] channelTitle = new String[]{"New Message", "New Stream"};
    private FirebaseUser currentUser;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null){
            Helpers.updateDeviceToken(getApplicationContext(), currentUser.getUid(), token);
        }
        Log.d(TAG, "Refreshed token: " + token);
//        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // Implement this method to send token to your app's server
//        sendRegistrationToServer(token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (remoteMessage != null){
            Map<String, String> mapData = remoteMessage.getData();
            String uid = null;
            String streamId = null;
            int code = 0;

            if (mapData.size() > 0){
                uid = mapData.get("sender_id");
                code = Integer.parseInt(mapData.get("code"));
                streamId = mapData.get("stream_id");
            }

            Log.d(TAG, "onMessageReceived: " + remoteMessage.getMessageId());

            if (remoteMessage.getNotification() != null){
                String title = remoteMessage.getNotification().getTitle();
                String message = remoteMessage.getNotification().getBody();
                PendingIntent pendingIntent = null;
                NotificationManager notificationManager = null;
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, String.valueOf(code))
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_icon))
                        .setSmallIcon(R.mipmap.ic_icon)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true);

                if (code == 1){
                    //On click of notification it redirect to this Activity
                    intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("uid", uid);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    notificationBuilder.setPriority(PRIORITY_MAX);
                } else if (code == 2){
                    intent = new Intent(this, WatchStreamActivity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("stream_id", streamId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notificationBuilder.setSound(soundUri)
                        .setContentIntent(pendingIntent);

                notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (notificationManager != null){
                    notificationManager.notify(0, notificationBuilder.build());
                }
            }
        }
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.d(TAG, "onMessageSent: " + s);
    }

    private void sendMyNotification(String message, String source, String tag) {
        String title = source;
        if (source.equals("chat")){
            title = "New Message";
        } else if(source.equals("stream")){
            title = "New Stream";
        }
        //On click of notification it redirect to this Activity
        intent = new Intent(this, ChatActivity.class);
        intent.putExtra("uid", tag);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, source)
                .setSmallIcon(R.mipmap.ic_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null){
            notificationManager.notify(0, notificationBuilder.build());
        }
    }
}
