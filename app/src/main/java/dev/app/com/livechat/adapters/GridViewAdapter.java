package dev.app.com.livechat.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.Tag;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.entities.Stream;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.PreferenceHelper;
import dev.app.com.livechat.utils.ShowImageHelper;

public class GridViewAdapter extends BaseAdapter {

    private Context context;
    private List<User> listUser = new ArrayList<>();
    private HashMap<String, String> mapfiles = new HashMap<>();
    private ShowImageHelper showImageHelper;

    public GridViewAdapter(Context context, List<User> listUser) {
        this.context = context;
        this.listUser = listUser;
        Log.d("listUser", listUser.size()+".");
    }

    public void setListUser(List<User> listUser) {
//        this.listUser.clear();
        this.listUser = listUser;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return this.listUser.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // LayoutInflator to call external grid_item.xml file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridView;

        gridView = inflater.inflate( R.layout.grid_layout, null);
        // set value into textview
        ImageView imageProfileGrid = (ImageView) gridView.findViewById(R.id.image_profile_grid);
        ImageView imageProfile = (ImageView) gridView.findViewById(R.id.image_profile);
        TextView textViewCount = (TextView) gridView.findViewById(R.id.text_view_count);
        TextView textNameGrid = (TextView) gridView.findViewById(R.id.text_name_grid);
        TextView textTitleGrid = (TextView) gridView.findViewById(R.id.text_title_grid);
        TextView textDistance = (TextView) gridView.findViewById(R.id.text_distance);
        ImageView imageNotif = (ImageView) gridView.findViewById(R.id.image_notif);
        View layoutNotif = gridView.findViewById(R.id.layout_notif);
        TextView textNotif = (TextView) gridView.findViewById(R.id.text_notif);

        final User user = listUser.get(i);
        if (user != null){
            textViewCount.setText(String.valueOf(user.getCount()));
            if (user.getName() != null){
                textNameGrid.setText(user.getName());
            } else {
                textNameGrid.setText(user.getEmail());
            }

            if (user.getStream() != null){
                textTitleGrid.setText(user.getStream().getTitle());

                if (user.getStream().getWatchCount() > 0){
                    textViewCount.setText(String.valueOf(user.getStream().getWatchCount()));
                }
            }
            if (showImageHelper == null){
                showImageHelper = new ShowImageHelper(context);
            }
            showImageHelper.showImageGrid(user, imageProfileGrid);
            showImageHelper.showImageProfile(user.getUid(), imageProfile);
            String distanceString = Helpers.getDistance(user.getDistance());
            textDistance.setText(distanceString);

            Stream stream  = user.getStream();
            if (stream != null){
                float minute = Helpers.getMinuteCount(user.getLast_seen());
                if (minute < 5){
                    imageNotif.setVisibility(View.GONE);
                }

                switch (stream.getStatus()){
                    case 0: textNotif.setText("Start");
                        layoutNotif.setBackground(context.getResources().getDrawable(R.drawable.shape_rectangular_primary));
                        break;
                    case 1: textNotif.setText("Live");
                        layoutNotif.setBackground(context.getResources().getDrawable(R.drawable.shape_rectangular_accent));
                        break;
                    case 2: textNotif.setText("End");
                        layoutNotif.setBackground(context.getResources().getDrawable(R.drawable.shape_rectangular_primary));
                        break;
                }
            }
        }

        return gridView;
    }

    public void search(String text){
        List<User> listSearch = new ArrayList<>();
        Log.d(GridViewAdapter.class.getSimpleName(), "listUser:"+listUser.size());
        if (listUser.size() > 0){
            for (User u: listUser){
                boolean isNameAvailable = isNameAvailable(u, text);
                boolean isTitleAvailable = isTitleAvailable(u, text);
                boolean isAddressAvailable = isAddressAvailable(u, text);
                boolean isTagAvailable = isTagAvailable(u, text);
                if (isNameAvailable || isTitleAvailable || isAddressAvailable || isTagAvailable){
                    listSearch.add(u);
                }
            }
        }
        listUser = listSearch;
        notifyDataSetChanged();
        Log.d(GridViewAdapter.class.getSimpleName(), text+":"+listUser.size());
    }

    private boolean isNameAvailable(User u, String text){
        return u.getName() != null && u.getName().toLowerCase().contains(text.toLowerCase());
    }

    private boolean isTitleAvailable(User u, String text){
        return u.getStream() != null && u.getStream().getTitle() != null
                && u.getStream().getTitle().toLowerCase().contains(text.toLowerCase());
    }

    private boolean isAddressAvailable(User u, String text){
        return u.getLocation() != null && u.getLocation().toLowerCase().contains(text.toLowerCase());
    }

    private boolean isTagAvailable(User u, String text){
        if (u != null && u.getStream() != null){
            List<String> tags = u.getStream().getTags();
            if (tags != null && tags.size() > 0){
                for (String s: tags){
                    if (s.toLowerCase().equals(text.toLowerCase())){
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
