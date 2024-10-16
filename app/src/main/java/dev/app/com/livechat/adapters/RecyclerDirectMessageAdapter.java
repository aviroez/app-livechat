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

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;

import java.util.List;

import dev.app.com.livechat.R;
import dev.app.com.livechat.entities.Chat;
import dev.app.com.livechat.utils.Helpers;
import dev.app.com.livechat.utils.ShowImageHelper;

import static dev.app.com.livechat.entities.Chat.STATUS_READ;
import static dev.app.com.livechat.entities.Chat.STATUS_SENT;

public class RecyclerDirectMessageAdapter extends RecyclerView.Adapter<RecyclerDirectMessageAdapter.ViewHolder> {
    private Context context;
    private String uid;
    private List<Chat> listChat;
    private AdapterView.OnItemClickListener onItemClickListener = null;

    class ViewHolder extends RecyclerView.ViewHolder {
        private View leftLayout;
        private TextView textChat;
        private TextView textTime;
        private ImageView imageStatus;

        private View rightLayout;
        private TextView textChatRight;
        private TextView textTimeRight;
        private ImageView imageStatusRight;
        // each data item is just a string in this case

        public ViewHolder(@NonNull View view) {
            super(view);

            this.leftLayout = view.findViewById(R.id.left_layout);
            this.textChat = view.findViewById(R.id.list_item_chat);
            this.textTime = view.findViewById(R.id.list_item_time);
            this.imageStatus = view.findViewById(R.id.image_status);

            this.rightLayout = view.findViewById(R.id.right_layout);
            this.textChatRight = view.findViewById(R.id.list_item_chat_right);
            this.textTimeRight = view.findViewById(R.id.list_item_time_right);
            this.imageStatusRight = view.findViewById(R.id.image_status_right);
        }
    }

    public RecyclerDirectMessageAdapter(Context context, String uid, List<Chat> listChat) {
        this.context = context;
        this.uid = uid;
        this.listChat = listChat;
    }

    public RecyclerDirectMessageAdapter(Context context, String uid, List<Chat> listChat, AdapterView.OnItemClickListener onItemClickListener) {
        this.context = context;
        this.uid = uid;
        this.listChat = listChat;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerDirectMessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.list_chat, viewGroup, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerDirectMessageAdapter.ViewHolder viewHolder, final int position) {
        Chat chat = listChat.get(position);
        Log.d("onBindViewHolder", "chat:"+new Gson().toJson(chat));
        Log.d("onBindViewHolder", "uid:"+uid);
        Log.d("onBindViewHolder", "currentUid:"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
        String timeValue = Helpers.getTimeAgo(chat.getTimer());
        int idIcon = R.drawable.ic_sync;
        switch (chat.getStatus()){
            case STATUS_SENT: idIcon = R.drawable.ic_check; break;
            case STATUS_READ: idIcon = R.drawable.ic_check_circle; break;
        }
        if (chat.getUid() != null && chat.getUid().equals(uid)){
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.rightLayout.setVisibility(View.GONE);

            viewHolder.textTime.setText(timeValue);
            viewHolder.textChat.setText(chat.getMessage());
            viewHolder.imageStatus.setImageResource(idIcon);
        } else {
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightLayout.setVisibility(View.VISIBLE);

            viewHolder.textTimeRight.setText(timeValue);
            viewHolder.textChatRight.setText(chat.getMessage());
            viewHolder.imageStatusRight.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listChat.size();
    }
}
