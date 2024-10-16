package dev.app.com.livechat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dev.app.com.livechat.R;

public class LocationAdapter extends BaseAdapter {
    private OnItemClickListener listenerLocation;
    private Context context;
    private Set<String> stringLocation;
    private boolean hideCountry;

    public LocationAdapter(Context context, Set<String> stringLocation, OnItemClickListener listenerLocation) {
        this.context = context;
        this.stringLocation = stringLocation;
        this.listenerLocation = listenerLocation;
    }

    public boolean isHideCountry() {
        return hideCountry;
    }

    public void setHideCountry(boolean hideCountry) {
        this.hideCountry = hideCountry;
    }

    @Override
    public int getCount() {
        return stringLocation.size();
    }

    @Override
    public Object getItem(int i) {
        List<String> listString = new ArrayList<>(stringLocation);
        return listString.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        List<String> listString = new ArrayList<>(stringLocation);
        final String location = listString.get(position);

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_country, null);
        }

        final ImageView imageCountry = (ImageView)convertView.findViewById(R.id.image_country);
        final TextView textCountry = (TextView)convertView.findViewById(R.id.text_country);

        if (isHideCountry()){
            imageCountry.setVisibility(View.GONE);
        } else {
            imageCountry.setVisibility(View.VISIBLE);
        }
        textCountry.setText(location);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listenerLocation.onItemClick(location);
            }
        });

        return convertView;
    }

    public interface OnItemClickListener {
        void onItemClick(String string);
    }
}
