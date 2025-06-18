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
import java.util.List;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ChatViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ParseUser user);
    }

    private List<ParseUser> chats;
    private OnChatClickListener listener;

    public ChatHistoryAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void setChats(List<ParseUser> chats) {
        this.chats = chats;
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
        ParseUser user = chats.get(position);
        holder.tvUsername.setText(user.getUsername());

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
        return chats != null ? chats.size() : 0;
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