package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
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
    private TextView titleText;
    private ImageButton btnBack;
    private MaterialButton btnAccount, btnNewPost, btnChats;
    private ChatHistoryAdapter adapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        // 1) Vincular vistas
        rvChats     = findViewById(R.id.rvChats);
        titleText   = findViewById(R.id.title_text);
        btnBack     = findViewById(R.id.btnBack);
        btnAccount  = findViewById(R.id.btnAccount);
        btnNewPost  = findViewById(R.id.btnNewPost);
        btnChats    = findViewById(R.id.btnChats);

        // 2) Texto del título y comportamiento del botón de volver
        titleText.setText("Chats");
        btnBack.setOnClickListener(v -> finish());

        // 3) Bottom navigation
        btnAccount.setOnClickListener(v -> {
            Intent i = new Intent(this, AccountActivity.class);
            startActivity(i);
        });
        btnNewPost.setOnClickListener(v -> {
            Intent i = new Intent(this, CreatePostActivity.class);
            startActivity(i);
        });
        btnChats.setOnClickListener(v -> {
            // Si queremos recargar la pantalla actual:
            recreate();
        });

        // 4) Preparar RecyclerView
        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getObjectId() : "";
        adapter = new ChatHistoryAdapter(this::openChat, currentUserId);
        rvChats.setAdapter(adapter);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        // 5) Cargar conversaciones
        loadChats();
    }

    private void loadChats() {
        if (currentUserId.isEmpty()) return;

        // Consultas “from” y “to” para el usuario actual
        ParseQuery<Message> q1 = ParseQuery.getQuery(Message.class)
                .whereEqualTo("fromUser", ParseUser.createWithoutData(ParseUser.class, currentUserId));
        ParseQuery<Message> q2 = ParseQuery.getQuery(Message.class)
                .whereEqualTo("toUser",   ParseUser.createWithoutData(ParseUser.class, currentUserId));

        ParseQuery.or(List.of(q1, q2))
                .include("fromUser")
                .include("toUser")
                .orderByDescending("createdAt")
                .findInBackground((messages, e) -> {
                    if (e != null) return;

                    Map<String, ChatConversation> convoMap = new HashMap<>();
                    List<String> otherIds = new ArrayList<>();

                    for (Message msg : messages) {
                        String fromId = msg.getFromUser().getObjectId();
                        String toId   = msg.getToUser().getObjectId();
                        String otherId = fromId.equals(currentUserId) ? toId : fromId;

                        if (!otherId.equals(currentUserId)) {
                            if (!convoMap.containsKey(otherId)) {
                                otherIds.add(otherId);
                                ParseUser otherUser = fromId.equals(currentUserId)
                                        ? msg.getToUser()
                                        : msg.getFromUser();
                                convoMap.put(otherId, new ChatConversation(otherUser, msg));
                            } else {
                                ChatConversation existing = convoMap.get(otherId);
                                if (msg.getCreatedAt().after(existing.getLastMessage().getCreatedAt())) {
                                    convoMap.put(otherId, new ChatConversation(
                                            existing.getOtherUser(), msg
                                    ));
                                }
                            }
                        }
                    }

                    if (otherIds.isEmpty()) {
                        adapter.setConversations(new ArrayList<>());
                        return;
                    }

                    // Obtener datos completos de cada otro usuario
                    ParseQuery<ParseUser> userQ = ParseUser.getQuery()
                            .whereContainedIn("objectId", otherIds);
                    userQ.findInBackground((users, ex) -> {
                        if (ex == null && users != null) {
                            List<ChatConversation> list = new ArrayList<>();
                            for (ParseUser u : users) {
                                ChatConversation conv = convoMap.get(u.getObjectId());
                                if (conv != null) list.add(conv);
                            }
                            // Ordenar por fecha de último mensaje
                            Collections.sort(list, (c1, c2) ->
                                    c2.getLastMessage().getCreatedAt()
                                            .compareTo(c1.getLastMessage().getCreatedAt()));
                            adapter.setConversations(list);
                        }
                    });
                });
    }

    private void openChat(ParseUser user, Message lastMessage) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiverId", user.getObjectId());
        intent.putExtra("postId", lastMessage.getParseObject("post").getObjectId());
        startActivity(intent);
    }
}
