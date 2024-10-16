package dev.app.com.livechat.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;

public class Stream implements Parcelable {
    private String uid;
    private String title;
    private String streamId;
    private String appName;
    private String streamName;
    private String imageUrl;
    private List<String> tags;
    private int status;
    private long last_active_stream;
    private long start_stream;
    private int process;
    private HashMap<String, Long> watched;
    private HashMap<String, Chat> chats;
    private int watchCount;
    private int duration;
    private WowzaSetting wowzaSetting;
    private String download_url;

    public Stream() {
    }

    protected Stream(Parcel in) {
        uid = in.readString();
        title = in.readString();
        streamId = in.readString();
        appName = in.readString();
        streamName = in.readString();
        imageUrl = in.readString();
        tags = in.createStringArrayList();
        status = in.readInt();
        last_active_stream = in.readLong();
        start_stream = in.readLong();
        process = in.readInt();
//        watched = in.readHashMap(HashMap.class.getClassLoader());
        watched = (HashMap<String, Long>) in.readSerializable();
        chats = (HashMap<String, Chat>) in.readSerializable();
        watchCount = in.readInt();
        duration = in.readInt();
        wowzaSetting = in.readParcelable(WowzaSetting.class.getClassLoader());
        download_url = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(title);
        dest.writeString(streamId);
        dest.writeString(appName);
        dest.writeString(streamName);
        dest.writeString(imageUrl);
        dest.writeStringList(tags);
        dest.writeInt(status);
        dest.writeLong(last_active_stream);
        dest.writeLong(start_stream);
        dest.writeInt(process);
        dest.writeSerializable(watched);
        dest.writeSerializable(chats);
        dest.writeInt(watchCount);
        dest.writeInt(duration);
        dest.writeParcelable(wowzaSetting, flags);
        dest.writeString(download_url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Stream> CREATOR = new Creator<Stream>() {
        @Override
        public Stream createFromParcel(Parcel in) {
            return new Stream(in);
        }

        @Override
        public Stream[] newArray(int size) {
            return new Stream[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getLastActiveStream() {
        return last_active_stream;
    }

    public void setLastActiveStream(long lastActiveStream) {
        this.last_active_stream = lastActiveStream;
    }

    public int getProcess() {
        return process;
    }

    public void setProcess(int process) {
        this.process = process;
    }

    public WowzaSetting getWowzaSetting() {
        return wowzaSetting;
    }

    public void setWowzaSetting(WowzaSetting wowzaSetting) {
        this.wowzaSetting = wowzaSetting;
    }

    public HashMap<String, Long> getWatched() {
        return watched;
    }

    public void setWatched(HashMap<String, Long> watched) {
        this.watched = watched;
    }

    public int getWatchCount() {
        return watchCount;
    }

    public void setWatchCount(int watchCount) {
        this.watchCount = watchCount;
    }

    public long getStartStream() {
        return start_stream;
    }

    public void setStartStream(long start_stream) {
        this.start_stream = start_stream;
    }

    public void setLast_active_stream(long last_active_stream) {
        this.last_active_stream = last_active_stream;
    }

    public void setStart_stream(long start_stream) {
        this.start_stream = start_stream;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getDownload_url() {
        return download_url;
    }

    public HashMap<String, Chat> getChats() {
        return chats;
    }

    public void setChats(HashMap<String, Chat> chats) {
        this.chats = chats;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
