package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHistoryActivity extends AppCompatActivity {

    public static class ChatConversation {
        private final ParseUser otherUser;
        private final Message lastMessage;

        public ChatConversation(ParseUser otherUser, Message lastMessage) {
            this.otherUser = otherUser;
            this.lastMessage = lastMessage;
        }

        public ParseUser getOtherUser() {
            return otherUser;
        }

        public Message getLastMessage() {
            return lastMessage;
        }
    }

    private RecyclerView rvChats;
    private ChatHistoryAdapter adapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        rvChats = findViewById(R.id.rvChats);

        // Obtener ID del usuario actual
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getObjectId() : "";

        adapter = new ChatHistoryAdapter(this::openChat, currentUserId);
        rvChats.setAdapter(adapter);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        loadChats();
    }

    private void loadChats() {
        if (currentUserId.isEmpty()) return;

        ParseQuery<Message> q1 = ParseQuery.getQuery(Message.class)
                .whereEqualTo("fromUser", ParseUser.createWithoutData(ParseUser.class, currentUserId));

        ParseQuery<Message> q2 = ParseQuery.getQuery(Message.class)
                .whereEqualTo("toUser", ParseUser.createWithoutData(ParseUser.class, currentUserId));

        List<ParseQuery<Message>> queries = new ArrayList<>();
        queries.add(q1);
        queries.add(q2);

        ParseQuery<Message> mainQuery = ParseQuery.or(queries);
        mainQuery.include("fromUser");
        mainQuery.include("toUser");
        mainQuery.orderByDescending("createdAt");

        mainQuery.findInBackground((messages, e) -> {
            if (e != null) {
                // Manejar error
                return;
            }

            Map<String, ChatConversation> conversationMap = new HashMap<>();
            List<String> otherUserIds = new ArrayList<>();

            for (Message message : messages) {
                String fromId = message.getFromUser().getObjectId();
                String toId = message.getToUser().getObjectId();
                String otherId = fromId.equals(currentUserId) ? toId : fromId;

                // Solo considerar usuarios diferentes al actual
                if (!otherId.equals(currentUserId)) {
                    if (!conversationMap.containsKey(otherId)) {
                        otherUserIds.add(otherId);
                        conversationMap.put(otherId, new ChatConversation(
                                fromId.equals(currentUserId) ? message.getToUser() : message.getFromUser(),
                                message
                        ));
                    } else {
                        // Actualizar si el mensaje es más reciente
                        ChatConversation existing = conversationMap.get(otherId);
                        if (message.getCreatedAt().after(existing.getLastMessage().getCreatedAt())) {
                            conversationMap.put(otherId, new ChatConversation(
                                    existing.getOtherUser(),
                                    message
                            ));
                        }
                    }
                }
            }

            // Si no hay conversaciones
            if (otherUserIds.isEmpty()) {
                adapter.setConversations(new ArrayList<>());
                return;
            }

            // Obtener detalles completos de los otros usuarios
            ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
            userQuery.whereContainedIn("objectId", otherUserIds);
            userQuery.findInBackground((users, ex) -> {
                if (ex == null && users != null) {
                    List<ChatConversation> conversations = new ArrayList<>();
                    for (ParseUser user : users) {
                        ChatConversation conv = conversationMap.get(user.getObjectId());
                        if (conv != null) {
                            conversations.add(conv);
                        }
                    }

                    // Ordenar por fecha del último mensaje (más reciente primero)
                    Collections.sort(conversations, (c1, c2) ->
                            c2.getLastMessage().getCreatedAt().compareTo(c1.getLastMessage().getCreatedAt()));

                    adapter.setConversations(conversations);
                }
            });
        });
    }

    private void openChat(ParseUser user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiverId", user.getObjectId());
        startActivity(intent);
    }
}