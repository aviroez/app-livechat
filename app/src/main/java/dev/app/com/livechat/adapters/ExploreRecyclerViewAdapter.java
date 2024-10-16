package dev.app.com.livechat.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.ShowImageHelper;

public class ExploreRecyclerViewAdapter extends RecyclerView.Adapter<ExploreRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = ExploreRecyclerViewAdapter.class.getSimpleName();
    private final List<User> userList;
    private final ExploreRecyclerViewAdapter.OnItemClickListener listener;
    private final Context context;
    private ShowImageHelper showImageHelper;

    public ExploreRecyclerViewAdapter(Context context, List<User> userList, ExploreRecyclerViewAdapter.OnItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;

        showImageHelper = new ShowImageHelper(context);
    }

    @Override
    public ExploreRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_layout_explore, parent, false);
        return new ExploreRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ExploreRecyclerViewAdapter.ViewHolder holder, int position) {
        final User user = userList.get(position);
        Log.d(TAG, "onBindViewHolder:"+user.getUid());
        holder.user = user;
        if (user != null){
            holder.textDistance.setText(Helpers.getDistance(user.getDistance()));
            holder.textNameGrid.setText(user.getName());
            showImageHelper.showImageProfile(user.getUid(), holder.imageProfile);

            Stream stream = user.getStream();
            if (stream != null){
                holder.textViewCount.setText(stream.getWatchCount()+" views");
                holder.textTitleGrid.setText(stream.getTitle());
                showImageHelper.showImageProfile(user.getUid(), holder.imageProfileGrid);
                long minutes = (Helpers.getLastSeen() - stream.getLastActiveStream()) / Helpers.MINUTE_DIVIDER;

                switch (stream.getStatus()){
                    case 0:
                        if (minutes > 15){
                            holder.textNotif.setText("End");
                        } else {
                            holder.textNotif.setText("Start");
                        }
                        holder.layoutNotif.setBackground(context.getResources().getDrawable(R.drawable.shape_rectangular_primary));
                        break;
                    case 1:
                        holder.textNotif.setText("Live");
                        holder.layoutNotif.setBackground(context.getResources().getDrawable(R.drawable.shape_rectangular_accent));
                        break;
                    case 2:
                        holder.textNotif.setText("End");
                        holder.layoutNotif.setBackground(context.getResources().getDrawable(R.drawable.shape_rectangular_primary));
                        break;
                }
            }
        }


        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final View gridView;
        public final TextView textViewCount;
        public final TextView textDistance;
        public final TextView textTitleGrid;
        public final TextView textNameGrid;
        public final ImageView imageNotif;
        public final ImageView imageProfileGrid;
        public final ImageView imageProfile;
        public final View layoutNotif;
        public final TextView textNotif;
        public User user;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            gridView = view.findViewById(R.id.grid_view);
            textViewCount = (TextView) view.findViewById(R.id.text_view_count);
            textDistance = (TextView) view.findViewById(R.id.text_distance);
            imageNotif = (ImageView) view.findViewById(R.id.image_notif);
            layoutNotif = view.findViewById(R.id.layout_notif);
            textNotif = (TextView) view.findViewById(R.id.text_notif);
            textTitleGrid  = (TextView) view.findViewById(R.id.text_title_grid);
            textNameGrid = (TextView) view.findViewById(R.id.text_name_grid);
            imageProfileGrid = (ImageView) view.findViewById(R.id.image_profile_grid);
            imageProfile = (ImageView) view.findViewById(R.id.image_profile);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + user.getName() + "'";
        }
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }
}
