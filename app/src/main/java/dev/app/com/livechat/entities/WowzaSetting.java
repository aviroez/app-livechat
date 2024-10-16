package dev.app.com.livechat.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class WowzaSetting implements Parcelable {
    private String id;
    private String key;
    private String host;
    private String app;
    private String name;
    private boolean auth;
    private String uid;
    private long last_update;

    public WowzaSetting() {
    }

    protected WowzaSetting(Parcel in) {
        id = in.readString();
        key = in.readString();
        host = in.readString();
        app = in.readString();
        name = in.readString();
        auth = in.readByte() != 0;
        uid = in.readString();
        last_update = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(key);
        dest.writeString(host);
        dest.writeString(app);
        dest.writeString(name);
        dest.writeByte((byte) (auth ? 1 : 0));
        dest.writeString(uid);
        dest.writeLong(last_update);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<WowzaSetting> CREATOR = new Creator<WowzaSetting>() {
        @Override
        public WowzaSetting createFromParcel(Parcel in) {
            return new WowzaSetting(in);
        }

        @Override
        public WowzaSetting[] newArray(int size) {
            return new WowzaSetting[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAuth() {
        return auth;
    }

    public void setAuth(boolean auth) {
        this.auth = auth;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getLastUpdate() {
        return last_update;
    }

    public void setLastUpdate(long last_update) {
        this.last_update = last_update;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
