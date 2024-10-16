package dev.app.com.livechat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dev.app.com.livechat.R;

public class LocationRecyclerViewAdapter extends RecyclerView.Adapter<LocationRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = LocationRecyclerViewAdapter.class.getSimpleName();
    private final Context context;
    private final Set<String> setString;
    private final LocationRecyclerViewAdapter.OnItemClickListener listener;

    public LocationRecyclerViewAdapter(Context context, Set<String> setString, LocationRecyclerViewAdapter.OnItemClickListener listener) {
        this.context = context;
        this.setString = setString;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.view_hashtag, viewGroup, false);
        return new LocationRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationRecyclerViewAdapter.ViewHolder viewHolder, int i) {
        List<String> stringsList = new ArrayList<>(setString);
        final String hashtag = "#"+stringsList.get(i);
        Log.d(TAG, "onBindViewHolder:"+hashtag);
        viewHolder.textHashtag.setText(hashtag);

        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(hashtag);
            }
        });
    }

    @Override
    public int getItemCount() {
        return setString.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View view;
        public final TextView textHashtag;

        public ViewHolder(@NonNull View view) {
            super(view);
            this.view = view;
            this.textHashtag = view.findViewById(R.id.text_hashtag);
        }

        public View getView() {
            return view;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String string);
    }
}
