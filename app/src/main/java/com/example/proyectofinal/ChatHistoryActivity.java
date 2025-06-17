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
import java.util.List;

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
        // Aquí deberías cargar las conversaciones existentes
        // Esto es un ejemplo simplificado
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.findInBackground((users, e) -> {
            if (e == null) {
                adapter.setChats(users);
            }
        });
    }

    private void openChat(ParseUser user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("receiverId", user.getObjectId());
        startActivity(intent);
    }
}