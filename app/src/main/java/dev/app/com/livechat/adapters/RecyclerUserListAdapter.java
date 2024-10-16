package dev.app.com.livechat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.User;
import dev.app.com.livechat.utils.ShowImageHelper;

public class RecyclerUserListAdapter extends RecyclerView.Adapter<RecyclerUserListAdapter.ViewHolder> {
    private ShowImageHelper showImageHelper;
    private AdapterView.OnItemClickListener onItemClickListener = null;
    private Context context;
    private List<User> listUser = new ArrayList<>();
    private StorageReference storageReference;

    class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private View container;
        private TextView textName;
        private TextView textMessage;
        private ImageView imageProfile;
        private ViewHolder(View view) {
            super(view);
            this.container = view.findViewById(R.id.container);
            this.textName = (TextView) view.findViewById(R.id.text_name);
            this.textMessage = (TextView) view.findViewById(R.id.text_message);
            this.imageProfile = (ImageView) view.findViewById(R.id.image_profile);
        }

        public View getContainer() {
            return container;
        }
    }

    public RecyclerUserListAdapter(Context context, List<User> listUser) {
        this.context = context;
        this.listUser = listUser;
        showImageHelper = new ShowImageHelper(context);
    }

    public RecyclerUserListAdapter(Context context, List<User> listUser, AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        this.context = context;
        this.listUser = listUser;
        showImageHelper = new ShowImageHelper(context);
    }

    @NonNull
    @Override
    public RecyclerUserListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.list_profile, viewGroup, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerUserListAdapter.ViewHolder viewHolder, final int position) {
        final User user = listUser.get(position);
        viewHolder.textName.setText(user.getName()+": ");
//        viewHolder.textMessage.setText(user.getMessage());
//        Log.d("listUser", "listUser onBindViewHolder:"+user.getName()+","+user.getMessage()+","+user.getTimer()+":"+position);
//        viewHolder.imageProfile.setText(user.setMessage(););
        if (user.getUid() != null){
            showImageHelper.showImageProfile(user.getUid(), viewHolder.imageProfile);
        }
        if (onItemClickListener != null){
            viewHolder.getContainer().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onItemClick(null, view, position, 0l);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listUser.size();
    }
}
