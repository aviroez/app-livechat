package dev.app.com.livechat.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Recording {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("transcoder_id")
    @Expose
    private String transcoderId;
    @SerializedName("transcoder_name")
    @Expose
    private String transcoderName;
    @SerializedName("starts_at")
    @Expose
    private String startsAt;
    @SerializedName("transcoding_uptime_id")
    @Expose
    private String transcodingUptimeId;
    @SerializedName("file_name")
    @Expose
    private String fileName;
    @SerializedName("file_size")
    @Expose
    private Integer fileSize;
    @SerializedName("duration")
    @Expose
    private Integer duration;
    @SerializedName("download_url")
    @Expose
    private String downloadUrl;
    @SerializedName("reason")
    @Expose
    private String reason;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTranscoderId() {
        return transcoderId;
    }

    public void setTranscoderId(String transcoderId) {
        this.transcoderId = transcoderId;
    }

    public String getTranscoderName() {
        return transcoderName;
    }

    public void setTranscoderName(String transcoderName) {
        this.transcoderName = transcoderName;
    }

    public String getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(String startsAt) {
        this.startsAt = startsAt;
    }

    public String getTranscodingUptimeId() {
        return transcodingUptimeId;
    }

    public void setTranscodingUptimeId(String transcodingUptimeId) {
        this.transcodingUptimeId = transcodingUptimeId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}