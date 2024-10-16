package dev.app.com.livechat.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import dev.app.com.livechat.R;

public class PostMessage extends AsyncTask<String, Integer, String> {
    private Context context;

    private String title = "FCM Message";
    private String topic = "foo-bar";
    private String body = "This is a Firebase Cloud Messaging Topic Message!";

    public PostMessage(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        String firebaseId = context.getString(R.string.firebase_id);
        URL url = null;
        HttpURLConnection client = null;
        try {
            url = new URL("https://fcm.googleapis.com/v1/projects/"+firebaseId+"/messages:send");
            client = (HttpURLConnection) url.openConnection();

            client.setRequestMethod("POST");
            client.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            client.setRequestProperty("Accept","application/json");
            client.setDoOutput(true);
            client.setDoInput(true);

            HashMap<String, String> mapNotif = new HashMap<>();
            mapNotif.put("title", title);
            mapNotif.put("body", body);
            HashMap<String, Object> mapMessage = new HashMap<>();
            mapMessage.put("topic", topic);
            mapMessage.put("notification", mapNotif);

            String jsonString = new Gson().toJson(mapMessage);

            Log.i("JSON", "jsonString:"+jsonString);
            DataOutputStream os = new DataOutputStream(client.getOutputStream());
            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
            os.writeBytes(jsonString);

            os.flush();
            os.close();

            Log.i("STATUS", String.valueOf(client.getResponseCode()));
            Log.i("MSG" , client.getResponseMessage());

            client.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
