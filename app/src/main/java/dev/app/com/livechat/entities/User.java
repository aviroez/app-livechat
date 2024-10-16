package dev.app.com.livechat.entities;

import android.databinding.BaseObservable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.Map;

public class User extends BaseObservable implements Parcelable {
    private String uid;
    private String name;
    private String email;
    private String phone_number;
    private String status;
    private String profile;
    private String image_url;
    private int gender;
    private int level;
    private long birth;
    private long latitude;
    private long longitude;
    private String location;
    private String country;
    private String bio;
    private String device_token;
    private int count;
    private int counter;
    private long last_seen;
    private boolean online;
    private double distance;
    private Stream stream;
    private Chat chat;
    private String[] login_using;
    private Map<String, Object> follows;
    private Map<String, Object>  followers;

    public User() {
    }

    protected User(Parcel in) {
        uid = in.readString();
        name = in.readString();
        email = in.readString();
        phone_number = in.readString();
        status = in.readString();
        profile = in.readString();
        image_url = in.readString();
        gender = in.readInt();
        level = in.readInt();
        birth = in.readLong();
        latitude = in.readLong();
        longitude = in.readLong();
        location = in.readString();
        country = in.readString();
        bio = in.readString();
        device_token = in.readString();
        count = in.readInt();
        counter = in.readInt();
        last_seen = in.readLong();
        online = in.readByte() != 0;
        distance = in.readDouble();
        stream = in.readParcelable(Stream.class.getClassLoader());
        chat = in.readParcelable(Chat.class.getClassLoader());
        login_using = in.createStringArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(phone_number);
        dest.writeString(status);
        dest.writeString(profile);
        dest.writeString(image_url);
        dest.writeInt(gender);
        dest.writeInt(level);
        dest.writeLong(birth);
        dest.writeLong(latitude);
        dest.writeLong(longitude);
        dest.writeString(location);
        dest.writeString(country);
        dest.writeString(bio);
        dest.writeString(device_token);
        dest.writeInt(count);
        dest.writeInt(counter);
        dest.writeLong(last_seen);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeDouble(distance);
        dest.writeParcelable(stream, flags);
        dest.writeParcelable(chat, flags);
        dest.writeStringArray(login_using);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public long getBirth() {
        return birth;
    }

    public void setBirth(long birth) {
        this.birth = birth;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(long last_seen) {
        this.last_seen = last_seen;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Stream getStream() {
        return stream;
    }

    public void setStream(Stream stream) {
        this.stream = stream;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String[] getLoginUsing() {
        return login_using;
    }

    public void setLoginUsing(String[] login_using) {
        this.login_using = login_using;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public String getDeviceToken() {
        return device_token;
    }

    public void setDeviceToken(String device_token) {
        this.device_token = device_token;
    }

    public Map<String, Object> getFollows() {
        return follows;
    }

    public void setFollows(Map<String, Object> follows) {
        this.follows = follows;
    }

    public Map<String, Object> getFollowers() {
        return followers;
    }

    public void setFollowers(Map<String, Object> followers) {
        this.followers = followers;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}
