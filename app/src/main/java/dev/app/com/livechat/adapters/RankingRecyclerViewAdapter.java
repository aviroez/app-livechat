package dev.app.com.livechat.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.fragments.dummy.DummyContent.DummyItem;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.ShowImageHelper;

import java.util.List;

public class RankingRecyclerViewAdapter extends RecyclerView.Adapter<RankingRecyclerViewAdapter.ViewHolder> {

    private final List<User> userList;
    private final OnItemClickListener listener;
    private final Context context;
    private ShowImageHelper showImageHelper;

    public RankingRecyclerViewAdapter(Context context, List<User> userList, OnItemClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final User user = userList.get(position);
        if (user != null){
            int follow = user.getFollows() != null ? user.getFollows().size() : 0;
            int followers = user.getFollowers() != null ? user.getFollowers().size() : 0;
            holder.user = user;

            holder.textNumber.setText(String.valueOf(position+1));
            holder.textName.setText(user.getName() != null ? user.getName() : user.getEmail());
            holder.textLevel.setText("Level: "+user.getLevel());
            holder.textFollow.setText(Helpers.getNumberCountFormat(follow));
            holder.textFollowers.setText(Helpers.getNumberCountFormat(followers));

            showImageHelper = new ShowImageHelper(context);
            showImageHelper.showImageProfile(user.getUid(), holder.imageProfile);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(holder.user);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView textNumber;
        public final TextView textName;
        public final TextView textLevel;
        public final TextView textFollow;
        public final TextView textFollowers;
        public final ImageView imageProfile;
        public User user;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            textNumber = (TextView) view.findViewById(R.id.text_item_number);
            textName = (TextView) view.findViewById(R.id.text_item_name);
            textLevel = (TextView) view.findViewById(R.id.text_item_level);
            textFollow = (TextView) view.findViewById(R.id.text_item_follows);
            textFollowers = (TextView) view.findViewById(R.id.text_item_followers);
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
