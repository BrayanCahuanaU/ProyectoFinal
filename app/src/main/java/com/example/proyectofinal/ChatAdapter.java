package com.example.proyectofinal;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.parse.ParseUser;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messages;
    private String currentUserId;

    public ChatAdapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        DisplayMetrics metrics = holder.itemView.getContext().getResources().getDisplayMetrics();
        int maxWidth = (int) (metrics.widthPixels * 0.75); // 75% del ancho de pantalla

        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.tvMessage.setText(message.getContent());
            sentHolder.tvTime.setText(formatTime(message.getCreatedAt()));

            // Establecer ancho máximo
            sentHolder.tvMessage.setMaxWidth(maxWidth);
        } else {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.tvMessage.setText(message.getContent());
            receivedHolder.tvTime.setText(formatTime(message.getCreatedAt()));
            receivedHolder.tvSender.setText(message.getFromUser().getUsername());

            // Establecer ancho máximo
            receivedHolder.tvMessage.setMaxWidth(maxWidth);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ParseUser fromUser = messages.get(position).getFromUser();
        return fromUser.getObjectId().equals(currentUserId) ?
                VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        void bind(Message message) {
            tvMessage.setText(message.getContent());
            tvTime.setText(formatTime(message.getCreatedAt()));
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvSender;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvSender = itemView.findViewById(R.id.tvSender);
        }

        void bind(Message message) {
            tvMessage.setText(message.getContent());
            tvTime.setText(formatTime(message.getCreatedAt()));
            tvSender.setText(message.getFromUser().getUsername());
        }
    }

    private static String formatTime(Date date) {
        if (date == null) return "Enviando...";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}