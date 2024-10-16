package dev.app.com.livechat.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.Chat;
import dev.app.com.livechat.utils.ShowImageHelper;

public class RecyclerChatAdapter extends RecyclerView.Adapter<RecyclerChatAdapter.ViewHolder> {
    private final ShowImageHelper showImageHelper;
    private final FirebaseUser currentUser;
    private OnItemClickListener onItemClickListener = null;
    private Context context;
    private List<Chat> listChat = new ArrayList<>();
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

    public RecyclerChatAdapter(Context context, List<Chat> listChat) {
        this.context = context;
        this.listChat = listChat;
        showImageHelper = new ShowImageHelper(context);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public RecyclerChatAdapter(Context context, List<Chat> listChat, AdapterView.OnItemClickListener onItemClickListener) {
        this.context = context;
        this.listChat = listChat;
        this.onItemClickListener = onItemClickListener;
        showImageHelper = new ShowImageHelper(context);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public RecyclerChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.chat_layout, viewGroup, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerChatAdapter.ViewHolder viewHolder, final int position) {
        final Chat chat = listChat.get(position);
        viewHolder.textName.setText(chat.getName()+": ");
        viewHolder.textMessage.setText(chat.getMessage());
        Log.d("listChat", "listChat onBindViewHolder:"+chat.getName()+","+chat.getMessage()+","+chat.getTimer()+":"+position);
//        viewHolder.imageProfile.setText(chat.setMessage(););
        if (!chat.getUid().equals(currentUser.getUid())){
            showImageHelper.showImageProfile(chat.getUid(), viewHolder.imageProfile);
            viewHolder.container.setBackgroundResource(R.drawable.chat_box_black);
            viewHolder.textName.setTextColor(context.getResources().getColor(R.color.white));
            viewHolder.textMessage.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            viewHolder.imageProfile.setVisibility(View.GONE);
            viewHolder.container.setBackgroundResource(R.drawable.chat_box);
            viewHolder.textName.setTextColor(context.getResources().getColor(R.color.black));
            viewHolder.textMessage.setTextColor(context.getResources().getColor(R.color.black));
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
        return listChat.size();
    }
}
