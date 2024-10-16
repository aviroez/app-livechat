package dev.app.com.livechat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.Tag;

public class CustomMenuProfileAdapter extends ArrayAdapter<Tag> {
    private TextView textTitle;
    private View layoutBelow;
    private ImageView itemImage;
    private List<Tag> list;

    public CustomMenuProfileAdapter(@NonNull Context context, int resource, @NonNull List<Tag> list) {
        super(context, resource, list);
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        return super.getView(position, convertView, parent);

        // Get the data item for this position
        Tag tag = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(R.layout.list_profile, parent, false);
        textTitle = (TextView) convertView.findViewById(R.id.list_item_title);
        itemImage = (ImageView) convertView.findViewById(R.id.list_item_image);
        layoutBelow = convertView.findViewById(R.id.layout_below);

        layoutBelow.setVisibility(View.GONE);

        textTitle.setText(tag.getText());
        itemImage.setImageResource(tag.getType());
        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public int getCount() {
//        return super.getCount();
        return list.size();
    }
}
