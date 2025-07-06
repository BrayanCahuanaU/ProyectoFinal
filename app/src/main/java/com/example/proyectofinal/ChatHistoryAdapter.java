package com.example.proyectofinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ChatViewHolder> {



    public interface OnChatClickListener {
        void onChatClick(ParseUser user, Message lastMessage);
    }

    private List<ChatHistoryActivity.ChatConversation> conversations;
    private final OnChatClickListener listener;
    private final String currentUserId;

    public ChatHistoryAdapter(OnChatClickListener listener, String currentUserId) {
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    public void setConversations(List<ChatHistoryActivity.ChatConversation> conversations) {
        this.conversations = conversations;
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
        ChatHistoryActivity.ChatConversation conversation = conversations.get(position);
        ParseUser user = conversation.getOtherUser();
        Message lastMessage = conversation.getLastMessage();

        holder.tvUsername.setText(user.getUsername());
        holder.tvLastMessage.setText(lastMessage.getContent());

        // Listener que pasa user y lastMessage
        holder.itemView.setOnClickListener(v -> listener.onChatClick(user, lastMessage));

        // Mostrar "Tú:" si el último mensaje es del usuario actual
        if (lastMessage.getFromUser().getObjectId().equals(currentUserId)) {
            holder.tvSenderIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.tvSenderIndicator.setVisibility(View.GONE);
        }

        // Cargar imagen de perfil con Glide (transformación circular)
        ParseFile profilePic = user.getParseFile("profileImage");
        if (profilePic != null && profilePic.getUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(profilePic.getUrl())
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .placeholder(R.drawable.cuenta)
                    .error(R.drawable.cuenta)
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.cuenta);
        }

        // --- NUEVO: cargar título del Post ---
        if (lastMessage.getParseObject("post") != null) {
            lastMessage.getParseObject("post").fetchIfNeededInBackground((obj, e) -> {
                if (e == null && obj instanceof Post) {
                    String title = ((Post)obj).getTitle();
                    holder.tvPostTitle.setText(title);
                }
            });
        } else {
            holder.tvPostTitle.setText("Sin Post");
        }

        // --- NUEVO: formatear timestamp ---
        Date created = lastMessage.getCreatedAt();
        String stamp = formatTimestamp(created);
        holder.tvTimestamp.setText(stamp);

    }

    private String formatTimestamp(Date d) {
        Calendar msgCal = Calendar.getInstance();
        msgCal.setTime(d);

        Calendar now = Calendar.getInstance();
        boolean sameDay = now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR);

        if (sameDay) {
            // solo hora, e.g. "14:35"
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(d);
        } else {
            // fecha, e.g. "23/06/2025"
            return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d);
        }
    }

    @Override
    public int getItemCount() {
        return conversations != null ? conversations.size() : 0;
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvUsername;
        TextView tvLastMessage;
        TextView tvSenderIndicator;
        TextView tvPostTitle;
        TextView tvTimestamp;

        ChatViewHolder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvPostTitle = itemView.findViewById(R.id.tvPostTitle);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvSenderIndicator = itemView.findViewById(R.id.tvSenderIndicator);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
