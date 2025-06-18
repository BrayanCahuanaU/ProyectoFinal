// ChatHistoryActivity.java
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
                .whereEqualTo("toUser",   ParseUser.createWithoutData(ParseUser.class, meId));

        // Unión de los dos
        List<ParseQuery<Message>> orQueries = Arrays.asList(q1, q2);
        ParseQuery<Message> mainQ = ParseQuery.or(orQueries);
        mainQ.findInBackground((msgs, e) -> {
            if (e != null) return;

            // 2) Extraer IDs únicos de “el otro” en cada mensaje
            Set<String> otherIds = new HashSet<>();
            for (Message m : msgs) {
                String fromId = m.getFromUser().getObjectId();
                String toId   = m.getToUser().getObjectId();
                if (!fromId.equals(meId)) otherIds.add(fromId);
                if (!toId.equals(meId))   otherIds.add(toId);
            }
            if (otherIds.isEmpty()) {
                // ningún chat
                adapter.setChats(Collections.emptyList());
                return;
            }

            // 3) Traer los ParseUser correspondientes
            ParseQuery<ParseUser> uq = ParseUser.getQuery();
            uq.whereContainedIn("objectId", otherIds);
            uq.findInBackground((users, ex) -> {
                if (ex == null) {
                    adapter.setChats(users);
                }
            });
        });
    }

    private void openChat(ParseUser user) {
        Intent intent = new Intent(this, ChatActivity.class);
        // no seteamos postId: así ChatActivity detecta modo “solo chat”
        intent.putExtra("receiverId", user.getObjectId());
        startActivity(intent);
    }
}