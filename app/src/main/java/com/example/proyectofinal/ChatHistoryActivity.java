// ChatHistoryActivity.java
package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatHistoryActivity extends AppCompatActivity {

    private RecyclerView rvChats;
    private ChatHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        rvChats = findViewById(R.id.rvChats);
        adapter = new ChatHistoryAdapter(this::openChat);
        rvChats.setAdapter(adapter);
        rvChats.setLayoutManager(new LinearLayoutManager(this));

        loadChats();
    }

    private void loadChats() {
        String meId = ParseUser.getCurrentUser().getObjectId();

        // 1) Traer todos los mensajes donde yo soy emisor o receptor
        ParseQuery<Message> q1 = ParseQuery.getQuery(Message.class)
                .whereEqualTo("fromUser", ParseUser.createWithoutData(ParseUser.class, meId));
        ParseQuery<Message> q2 = ParseQuery.getQuery(Message.class)
                .whereEqualTo("toUser", ParseUser.createWithoutData(ParseUser.class, meId));

        // Unión de los dos
        List<ParseQuery<Message>> orQueries = new ArrayList<>();
        orQueries.add(q1);
        orQueries.add(q2);
        ParseQuery<Message> mainQ = ParseQuery.or(orQueries);
        mainQ.include("post"); // Incluir datos del post
        mainQ.findInBackground((msgs, e) -> {
            if (e != null) return;

            // Map para almacenar títulos de posts (postId -> título)
            Map<String, String> postTitles = new HashMap<>();
            Set<String> postIds = new HashSet<>();
            Set<String> otherIds = new HashSet<>();

            // 2) Extraer IDs únicos de "el otro" en cada mensaje y IDs de posts
            for (Message m : msgs) {
                String fromId = m.getFromUser().getObjectId();
                String toId = m.getToUser().getObjectId();
                if (!fromId.equals(meId)) otherIds.add(fromId);
                if (!toId.equals(meId)) otherIds.add(toId);

                // Recoger IDs de posts
                ParseObject post = m.getPost();
                if (post != null) {
                    postIds.add(post.getObjectId());
                }
            }

            if (otherIds.isEmpty()) {
                // ningún chat
                adapter.setChats(Collections.emptyList());
                return;
            }

            // Buscar títulos de posts
            ParseQuery<ParseObject> postQuery = ParseQuery.getQuery("Post");
            postQuery.whereContainedIn("objectId", postIds);
            postQuery.findInBackground((posts, exPosts) -> {
                if (exPosts == null) {
                    for (ParseObject post : posts) {
                        postTitles.put(post.getObjectId(), post.getString("title"));
                    }
                }

                // 3) Traer los ParseUser correspondientes
                ParseQuery<ParseUser> uq = ParseUser.getQuery();
                uq.whereContainedIn("objectId", otherIds);
                uq.findInBackground((users, ex) -> {
                    if (ex == null) {
                        // Crear lista de chats con títulos de posts
                        List<ChatHistoryAdapter.ChatInfo> chatList = new ArrayList<>();
                        for (ParseUser user : users) {
                            String userId = user.getObjectId();
                            String postTitle = "";

                            // Buscar título de post asociado a este usuario
                            for (Message m : msgs) {
                                ParseObject post = m.getPost();
                                if (post != null && postTitles.containsKey(post.getObjectId())) {
                                    String fromId = m.getFromUser().getObjectId();
                                    String toId = m.getToUser().getObjectId();

                                    if (fromId.equals(userId) || toId.equals(userId)) {
                                        postTitle = postTitles.get(post.getObjectId());
                                        break;
                                    }
                                }
                            }
                            chatList.add(new ChatHistoryAdapter.ChatInfo(user, postTitle));
                        }
                        adapter.setChats(chatList);
                    }
                });
            });
        });
    }

    private void openChat(ParseUser user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiverId", user.getObjectId());
        startActivity(intent);
    }
}