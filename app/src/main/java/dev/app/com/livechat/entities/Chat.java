package dev.app.com.livechat.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class Chat implements Parcelable {
    public static final int STATUS_SENT = 1;
    public static final int STATUS_READ = 2;
    private String uid;
    private String message;
    private String name;
    private String streamId;
    private String uidMessage;
    private long timer;
    private int status;

    public Chat() {
    }

    protected Chat(Parcel in) {
        uid = in.readString();
        message = in.readString();
        name = in.readString();
        streamId = in.readString();
        uidMessage = in.readString();
        timer = in.readLong();
        status = in.readInt();
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getUidMessage() {
        return uidMessage;
    }

    public void setUidMessage(String uidMessage) {
        this.uidMessage = uidMessage;
    }

    public long getTimer() {
        return timer;
    }

    public void setTimer(long timer) {
        this.timer = timer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uid);
        parcel.writeString(message);
        parcel.writeString(name);
        parcel.writeString(streamId);
        parcel.writeString(uidMessage);
        parcel.writeLong(timer);
        parcel.writeInt(status);
    }
}
