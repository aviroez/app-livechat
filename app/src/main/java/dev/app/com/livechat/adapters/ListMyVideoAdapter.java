package dev.app.com.livechat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.utils.Helpers;

public class ListMyVideoAdapter extends ArrayAdapter<Stream> {
    List<Stream> list = new ArrayList<>();

    public ListMyVideoAdapter(@NonNull Context context, @NonNull List<Stream> list) {
        super(context, R.layout.list_video, list);
        this.list = list;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView textTitle;
        TextView textDescription;
        TextView textTime;
        ImageView imageView;
        ImageButton buttonDownload;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Stream stream = getItem(position);
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_video, parent, false);
            viewHolder.textTitle = (TextView) convertView.findViewById(R.id.list_item_title);
            viewHolder.textDescription = (TextView) convertView.findViewById(R.id.list_item_description);
            viewHolder.textTime = (TextView) convertView.findViewById(R.id.list_item_time);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.list_item_image);
            viewHolder.buttonDownload = (ImageButton) convertView.findViewById(R.id.button_download);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        viewHolder.textTitle.setText(stream.getTitle());
        viewHolder.textDescription.setText("");
        viewHolder.textTime.setText(Helpers.getTimeAgo(stream.getStartStream()));

        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
