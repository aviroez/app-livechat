package dev.app.com.livechat.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import dev.app.com.livechat.R;
import dev.app.com.livechat.activities.NavigationActivity;
import dev.app.com.livechat.utils.PreferenceHelper;

public class NotificationSchedulingService extends Service {

    private static final String TAG = NotificationSchedulingService.class.getSimpleName();

    private static Long MILLISECS_PER_DAY = 86400000L;
    private static Long MILLISECS_PER_MIN = 60000L;

//      private static long delay = MILLISECS_PER_MIN * 3;   // 3 minutes (for testing)
    private static long delay = MILLISECS_PER_DAY;   // 1 day

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceHelper preferenceHelper = new PreferenceHelper(getApplicationContext(), "user");

        Log.v(TAG, "Service started");
        // Is it time for a notification?
        if (preferenceHelper.retrieve("last_seen", Long.MAX_VALUE) < System.currentTimeMillis() - delay){
            sendNotification();
        }

        // Set an alarm for the next time this service should run:
        setAlarm();

    }

    public void setAlarm() {
        Intent serviceIntent = new Intent(this, NotificationSchedulingService.class);
        PendingIntent pi = PendingIntent.getService(this, 131313, serviceIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pi);
        Log.v(TAG, "Alarm set");
    }

    public void sendNotification() {
        PreferenceHelper userHelper = new PreferenceHelper(this, "user");
        String user = userHelper.retrieve("user", null);
        String helloString = "Hello!";
        if (user != null && user.length()>0){
            helloString = "Hello "+user+"!";
        }
        Intent mainIntent = new Intent(this, NavigationActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager notificationManager
                = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notif = new NotificationCompat.Builder(this, "schedule")
                .setAutoCancel(true)
                .setContentIntent(contentIntent)
                .setContentTitle(helloString)
                .setContentText(getString(R.string.you_have_not_active_in))
                .setDefaults(Notification.DEFAULT_ALL)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_icon))
                .setSmallIcon(R.mipmap.ic_icon)
                .setTicker("ticker message")
                .setWhen(System.currentTimeMillis())
                .build();

        if (notificationManager != null){
            notificationManager.notify(0, notif);
            stopSelf();
            Log.v(TAG, "Service stopped");
        }

        Log.v(TAG, "Notification sent");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
