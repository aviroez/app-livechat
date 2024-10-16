package dev.app.com.livechat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.Chat;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.ShowImageHelper;

public class ListUserAdapter extends ArrayAdapter<User> {
    private final ShowImageHelper showImageHelper;
    private Context context;
    private int lastPosition = -1;

    public ListUserAdapter(@NonNull Context context, @NonNull List<User> listUser) {
        super(context, R.layout.list_profile, listUser);
        showImageHelper = new ShowImageHelper(context);
    }

    // View lookup cache
    private static class ViewHolder {
        TextView textTitle;
        TextView textDescription;
        TextView textTime;
        ImageView imageView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        return super.getView(position, convertView, parent);// Get the data item for this position
        User user = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_profile, parent, false);
            viewHolder.textTitle = (TextView) convertView.findViewById(R.id.list_item_title);
            viewHolder.textDescription = (TextView) convertView.findViewById(R.id.list_item_description);
            viewHolder.textTime = (TextView) convertView.findViewById(R.id.list_item_time);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.list_item_image);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

//        Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
//        result.startAnimation(animation);
        lastPosition = position;

        String title = user.getName();

        if (title == null || title.isEmpty()){
            title = user.getEmail();
        }

        viewHolder.textTitle.setText(title);
        Chat chat = user.getChat();
        if (chat != null){
            viewHolder.textDescription.setText(chat.getMessage());
            viewHolder.textTime.setText(Helpers.getTimeAgo(chat.getTimer()));
            viewHolder.textTime.setVisibility(View.VISIBLE);
        } else {
            viewHolder.textDescription.setVisibility(View.GONE);
            viewHolder.textTime.setVisibility(View.GONE);
            viewHolder.textTime.setVisibility(View.GONE);
        }

        showImageHelper.showImageProfile(user.getUid(), viewHolder.imageView);
//        convertView.setOnClickListener(this);
//        convertView.setTag(position);
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }
}
