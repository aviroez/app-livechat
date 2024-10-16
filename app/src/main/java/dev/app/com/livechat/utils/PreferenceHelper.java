package dev.app.com.livechat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Map;
import java.util.Set;

import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.entities.User;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceHelper {
    private Context context;
    private SharedPreferences sharedPreferences;
    private Gson gson = new Gson();;
    private String preferenceName = "default";

    public PreferenceHelper(Context context) {
        this.context = context;

        if (sharedPreferences == null){
            sharedPreferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);
        }
    }

    public PreferenceHelper(Context context, String preferenceName) {
        this.context = context;
        this.preferenceName = preferenceName;

        if (sharedPreferences == null){
            sharedPreferences = context.getSharedPreferences(preferenceName, MODE_PRIVATE);
        }
    }

    public void storeObject(String key, Object object){
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        String json = gson.toJson(object);
        prefsEditor.putString(key, json);
        prefsEditor.apply();
    }

    public void store(String key, String value){
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    public void store(String key, int value){
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putInt(key, value);
        prefsEditor.apply();
    }

    public void store(String key, float value){
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putFloat(key, value);
        prefsEditor.apply();
    }

    public void store(String key, long value){
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putLong(key, value);
        prefsEditor.apply();
    }

    public void store(String key, boolean value){
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }

    public void store(String key, Set<String> set){
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putStringSet(key, set);
        prefsEditor.apply();
    }

    public void clearAll(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public String retrieve(String key, String defaultValue){
        return sharedPreferences.getString(key, defaultValue);
    }

    public int retrieve(String key, int defaultValue){
        return sharedPreferences.getInt(key, defaultValue);
    }

    public boolean retrieve(String key, boolean defaultValue){
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public float retrieve(String key, float defaultValue){
        return sharedPreferences.getFloat(key, defaultValue);
    }

    public long retrieve(String key, long defaultValue){
        return sharedPreferences.getLong(key, defaultValue);
    }

    public Set<String> retrieveSet(String key, Set<String> defaultValue){
        return sharedPreferences.getStringSet(key, defaultValue);
    }

    public Map<String, ?> all(){
        return sharedPreferences.getAll();
    }

    public Object retrieveObject(String key, String defaultStr){
        String json = sharedPreferences.getString(key, defaultStr);
        Log.d("json", key+":"+json);
        return gson.fromJson(json, Object.class);
    }

    public Object retrieveObject(String key){
        return retrieveObject(key, null);
    }

    public Stream retrieveStream(String key){
        String json = sharedPreferences.getString(key, null);
        Log.d("json", key+":"+json);
        return gson.fromJson(json, Stream.class);
    }

    public User retrieveUser(String key){
        String json = sharedPreferences.getString(key, null);
        return gson.fromJson(json, User.class);
    }

    public String generateStreamKey(String uid, String streamId){
        return uid + "." + streamId;
    }


}
