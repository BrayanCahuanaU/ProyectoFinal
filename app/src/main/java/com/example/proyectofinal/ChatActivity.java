// ChatActivity.java
package com.example.proyectofinal;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.parse.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private LinearLayout loadingPanel;
    private ChatAdapter adapter;

    private List<Message> messages = new ArrayList<>();
    private String postId;
    private String receiverId;
    private ParseUser receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Obtener datos del intent
        postId = getIntent().getStringExtra("postId");
        receiverId = getIntent().getStringExtra("receiverId");

        // Configurar views
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        loadingPanel = findViewById(R.id.loadingPanel);

        TextView tvTitle = findViewById(R.id.tvChatTitle);
        ImageButton btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // Configurar RecyclerView
        adapter = new ChatAdapter(messages, ParseUser.getCurrentUser().getObjectId());
        rvMessages.setAdapter(adapter);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        // Cargar información del receptor
        loadReceiver();

        // Configurar botón de enviar
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadReceiver() {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(receiverId, (user, e) -> {
            if (e == null) {
                receiver = user;
                TextView tvChatTitle = findViewById(R.id.tvChatTitle);
                tvChatTitle.setText("Chat con " + user.getUsername());
                loadMessages();
            } else {
                Toast.makeText(this, "Error al cargar usuario", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadMessages() {
        // Si tengo postId, filtro por post (chat asociado a una publicación)
        // Si no, chat “libre” uno a uno
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);

        if (postId != null) {
            query.whereEqualTo("post", ParseObject.createWithoutData("Post", postId));
        } else {
            // chat directo: mensajes entre current y receiver
            ParseUser me = ParseUser.getCurrentUser();
            ParseUser them = ParseUser.createWithoutData(ParseUser.class, receiverId);

            ParseQuery<Message> q1 = ParseQuery.getQuery(Message.class)
                    .whereEqualTo("fromUser", me)
                    .whereEqualTo("toUser",   them);
            ParseQuery<Message> q2 = ParseQuery.getQuery(Message.class)
                    .whereEqualTo("fromUser", them)
                    .whereEqualTo("toUser",   me);

            query = ParseQuery.or(Arrays.asList(q1, q2));
        }

        query.include("fromUser");
        query.include("toUser");
        query.orderByAscending("createdAt");
        query.findInBackground((messagesList, e) -> {
            loadingPanel.setVisibility(View.GONE);
            if (e == null) {
                messages.clear();
                messages.addAll(messagesList);
                adapter.notifyDataSetChanged();
                scrollToBottom();
            } else {
                Toast.makeText(this, "Error cargando mensajes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        if (receiver == null) {
            Toast.makeText(this, "Esperando información del receptor...", Toast.LENGTH_SHORT).show();
            return;
        }

        Message message = new Message();
        message.setFromUser(ParseUser.getCurrentUser());
        message.setToUser(receiver);
        message.setContent(content);
        message.setPost(ParseObject.createWithoutData("Post", postId));

        // Mostrar mensaje localmente inmediatamente
        messages.add(message);
        adapter.notifyItemInserted(messages.size() - 1);
        etMessage.setText("");
        scrollToBottom();

        // Guardar en Parse
        message.saveInBackground(e -> {
            runOnUiThread(() -> {
                if (e != null) {
                    Toast.makeText(this, "Error enviando mensaje", Toast.LENGTH_SHORT).show();
                    int position = messages.indexOf(message);
                    if (position != -1) {
                        messages.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                }
            });
        });
    }

    private void scrollToBottom() {
        if (rvMessages.getAdapter() != null && rvMessages.getAdapter().getItemCount() > 0) {
            rvMessages.post(() -> {
                LinearLayoutManager layoutManager = (LinearLayoutManager) rvMessages.getLayoutManager();
                if (layoutManager != null) {
                    layoutManager.scrollToPositionWithOffset(messages.size() - 1, 0);
                }
            });
        }
    }
}