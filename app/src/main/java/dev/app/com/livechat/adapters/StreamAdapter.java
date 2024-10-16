package dev.app.com.livechat.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import dev.app.com.livechat.activities.WatchStreamActivity;
import dev.app.com.livechat.entities.Stream;

import java.util.ArrayList;
import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.fragments.ProfileFragment;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.ShowImageHelper;

public class StreamAdapter extends RecyclerView.Adapter<StreamAdapter.CustomViewHolder> {

    private ShowImageHelper showImageHelper;
    private List<Stream> streamList = new ArrayList<>();
    private Context context;

    /**
     * View holder class
     * */
    class CustomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProfile;
        TextView textTitle;
        TextView textView;
        TextView textTime;
        String uid;
        int position;
        ArrayList<String> streamKeys = new ArrayList<String>();

        CustomViewHolder(View view) {
            super(view);
            imageProfile = (ImageView) view.findViewById(R.id.image_profile);
            textTitle = (TextView) view.findViewById(R.id.text_name);
            textView = (TextView) view.findViewById(R.id.text_view);
            textTime = (TextView) view.findViewById(R.id.text_time);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (streamList.size() > 0){
                        for (Stream stream: streamList){
                            streamKeys.add(stream.getUid()+"/"+stream.getStreamId());
                        }
                    }
                    Intent intent = new Intent(context, WatchStreamActivity.class);
                    intent.putExtra("uid", uid);
                    intent.putExtra("position", position);
                    intent.putExtra("redirectString", ProfileFragment.class.getSimpleName());
                    intent.putStringArrayListExtra("streamKeys", streamKeys);
                    context.startActivity(intent);
                }
            });
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

    public StreamAdapter(Context context, List<Stream> streamList) {
        this.context = context;
        this.streamList = streamList;
        showImageHelper = new ShowImageHelper(context);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        Stream stream = streamList.get(position);
        holder.textTitle.setText(stream.getTitle());
        String watchValue = "";
        if (stream.getWatchCount() > 1){
            watchValue = stream.getWatchCount() + " " +context.getString(R.string.views);
        } else {
            watchValue = stream.getWatchCount() + " " +context.getString(R.string.view);
        }
        holder.textView.setText(watchValue);

        String timeValue = Helpers.getTimeAgo(stream.getLastActiveStream());
        holder.textTime.setText(timeValue);

        holder.setUid(stream.getUid());
        holder.setPosition(position);
        showImageHelper.showImageGrid(stream.getUid(), stream.getStreamId(), holder.imageProfile);
    }

    @Override
    public int getItemCount() {
        return streamList.size();
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_stream, parent, false);
        return new CustomViewHolder(v);
    }
}