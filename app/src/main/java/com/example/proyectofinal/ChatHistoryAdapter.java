// ChatHistoryAdapter.java
package com.example.proyectofinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseUser;
import java.util.Collections;
import java.util.List;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ChatViewHolder> {

    public static class ChatInfo {
        public ParseUser user;
        public String postTitle;

        public ChatInfo(ParseUser user, String postTitle) {
            this.user = user;
            this.postTitle = postTitle;
        }
    }

    public interface OnChatClickListener {
        void onChatClick(ParseUser user);
    }

    private List<ChatInfo> chats;
    private OnChatClickListener listener;

    public ChatHistoryAdapter(OnChatClickListener listener) {
        this.listener = listener;
        this.chats = Collections.emptyList();
    }

    public void setChats(List<ChatInfo> chats) {
        this.chats = chats != null ? chats : Collections.emptyList();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatInfo chat = chats.get(position);
        ParseUser user = chat.user;
        String postTitle = chat.postTitle;

        // Mostrar "usuario | título del post" o solo usuario
        if (postTitle != null && !postTitle.isEmpty()) {
            holder.tvUsername.setText(user.getUsername() + " | " + postTitle);
        } else {
            holder.tvUsername.setText(user.getUsername());
        }

        ParseFile profilePic = user.getParseFile("profilePic");
        if (profilePic != null) {
            Glide.with(holder.itemView.getContext())
                    .load(profilePic.getUrl())
                    .into(holder.ivProfile);
        }

        holder.itemView.setOnClickListener(v -> listener.onChatClick(user));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvUsername;
        TextView tvLastMessage;

        ChatViewHolder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}