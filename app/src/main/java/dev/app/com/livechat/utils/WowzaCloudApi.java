package dev.app.com.livechat.utils;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WowzaCloudApi {
    @GET("versions")
    Call<ResponseBody> versions();

    @GET("live_streams")
    Call<ResponseBody> liveStreams();

    @PUT("live_streams/{id}/start")
    Call<ResponseBody> start(@Path("id") String liveStreamId);

    @GET("recordings")
    Call<ResponseBody> recordings();

    @GET("recordings/{id}")
    Call<ResponseBody> recordings(@Path("id") String recordingId);

    @GET("recordings")
    Call<ResponseBody> recordingsByTranscoderId(@Query("transcoder_id") String transcoderId);
}
