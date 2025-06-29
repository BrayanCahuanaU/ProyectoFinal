package com.example.proyectofinal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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
    private Button btnServiceDone, btnRate;

    private List<Message> messages = new ArrayList<>();
    private String postId;
    private String receiverId;
    private ParseUser receiver;
    private Post currentPost;
    private boolean isOwner = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        postId      = getIntent().getStringExtra("postId");
        receiverId  = getIntent().getStringExtra("receiverId");

        rvMessages     = findViewById(R.id.rvMessages);
        etMessage      = findViewById(R.id.etMessage);
        btnSend        = findViewById(R.id.btnSend);
        loadingPanel   = findViewById(R.id.loadingPanel);
        btnServiceDone = findViewById(R.id.btnServiceDone);
        btnRate        = findViewById(R.id.btnRate);
        btnServiceDone.setVisibility(View.GONE);

        btnRate.setVisibility(View.GONE);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        adapter = new ChatAdapter(messages, ParseUser.getCurrentUser() != null ? ParseUser.getCurrentUser().getObjectId() : "");
        rvMessages.setAdapter(adapter);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        setupChat();
    }

    private void setupChat() {
        btnSend.setOnClickListener(v -> sendMessage());
        loadReceiver();
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
        ParseQuery<Message> query;
        if (postId != null) {
            query = ParseQuery.getQuery(Message.class)
                    .whereEqualTo("post", ParseObject.createWithoutData("Post", postId));
        } else {
            ParseUser me   = ParseUser.getCurrentUser();
            ParseUser them = ParseUser.createWithoutData(ParseUser.class, receiverId);
            ParseQuery<Message> q1 = ParseQuery.getQuery(Message.class)
                    .whereEqualTo("fromUser", me)
                    .whereEqualTo("toUser", them);
            ParseQuery<Message> q2 = ParseQuery.getQuery(Message.class)
                    .whereEqualTo("fromUser", them)
                    .whereEqualTo("toUser", me);
            query = ParseQuery.or(Arrays.asList(q1, q2));
        }
        query.include("fromUser");
        query.include("toUser");
        query.include("post");
        query.orderByAscending("createdAt");
        query.findInBackground((messagesList, e) -> {
            loadingPanel.setVisibility(View.GONE);
            if (e == null) {
                messages.clear();
                messages.addAll(messagesList);
                adapter.notifyDataSetChanged();
                scrollToBottom();

                // Forzar recarga del post si ya existe
                if (currentPost != null) {
                    currentPost.fetchInBackground((obj, e2) -> {
                        inferPostAndLoad();
                    });
                } else {
                    inferPostAndLoad();
                }
            } else {
                Toast.makeText(this, "Error cargando mensajes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void inferPostAndLoad() {
        if (messages.isEmpty()) {
            afterMessagesLoaded(); // Asegurarse de llamar esto siempre
            return;
        }

        // Intentar obtener el post del primer mensaje
        if (postId == null) {
            Message first = messages.get(0);
            ParseObject postPtr = first.getParseObject("post");
            if (postPtr != null) {
                postId = postPtr.getObjectId();
                postPtr.fetchInBackground((o, fe) -> {
                    if (fe == null && o instanceof Post) {
                        currentPost = (Post) o;
                    }
                    afterMessagesLoaded();
                });
                return;
            }
        }

        // Cargar post si tenemos ID pero no el objeto
        if (postId != null && currentPost == null) {
            ParseQuery<Post> pq = ParseQuery.getQuery(Post.class);
            pq.getInBackground(postId, (post, pe) -> {
                if (pe == null) currentPost = post;
                afterMessagesLoaded();
            });
        } else {
            afterMessagesLoaded();
        }
    }

    private void afterMessagesLoaded() {
        // Restablecer visibilidad
        btnServiceDone.setVisibility(View.GONE);
        btnRate.setVisibility(View.GONE);

        if (currentPost == null || messages.isEmpty() || ParseUser.getCurrentUser() == null) {
            return;
        }

        ParseUser me = ParseUser.getCurrentUser();
        String meId = me.getObjectId();

        // Verificar si el usuario actual es el dueño del post
        isOwner = currentPost.getUser() != null &&
                currentPost.getUser().getObjectId().equals(meId);

        if (isOwner) {
            // Dueño del post - mostrar botón de completado servicio
            btnServiceDone.setVisibility(currentPost.isServiceCompleted() ? View.GONE : View.VISIBLE);
            btnServiceDone.setOnClickListener(v -> markServiceDone());
        } else {
            // No es dueño - verificar condiciones para calificación
            Message first = messages.get(0);
            if (first.getFromUser() == null) return;

            String initiatorId = first.getFromUser().getObjectId();
            Date convoStart = first.getCreatedAt();
            boolean serviceDone = currentPost.isServiceCompleted();

            // Verificar si el usuario actual inició la conversación
            if (initiatorId.equals(meId) && serviceDone) {
                int myCount = 0;
                for (Message m : messages) {
                    if (m.getFromUser() != null && meId.equals(m.getFromUser().getObjectId())) {
                        myCount++;
                    }
                }

                // Calcular diferencia de tiempo correctamente
                long timeDifference = System.currentTimeMillis() - convoStart.getTime();
                boolean oneDayPassed = timeDifference >= 24L * 3600L * 1000L;

                if (myCount >= 4 && oneDayPassed) {
                    btnRate.setVisibility(View.VISIBLE);
                    btnRate.setOnClickListener(v -> showRatingDialog());
                }
            }
        }
    }

    private void markServiceDone() {
        btnServiceDone.setEnabled(false);
        currentPost.setServiceCompleted(true);
        currentPost.saveInBackground(e -> {
            if (e == null) {
                Toast.makeText(this, "Servicio completado", Toast.LENGTH_SHORT).show();
                btnServiceDone.setVisibility(View.GONE);

                // Actualizar UI para posible calificación
                if (!isOwner) {
                    afterMessagesLoaded();
                }
            } else {
                btnServiceDone.setEnabled(true);
                Toast.makeText(this, "No se pudo marcar servicio", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        if (ParseUser.getCurrentUser() == null || receiver == null) return;
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        Message message = new Message();
        message.setFromUser(ParseUser.getCurrentUser());
        message.setToUser(receiver);
        message.setContent(content);
        if (postId != null) message.setPost(ParseObject.createWithoutData("Post", postId));

        messages.add(message);
        adapter.notifyItemInserted(messages.size() - 1);
        etMessage.setText("");
        scrollToBottom();

        message.saveInBackground(e -> {
            if (e != null) Toast.makeText(this, "Error enviando mensaje", Toast.LENGTH_SHORT).show();
        });
    }

    private void scrollToBottom() {
        rvMessages.post(() -> {
            if (rvMessages.getLayoutManager() instanceof LinearLayoutManager) {
                ((LinearLayoutManager) rvMessages.getLayoutManager())
                        .scrollToPositionWithOffset(messages.size() - 1, 0);
            }
        });
    }

    private void showRatingDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_rate, null);
        RatingBar rb = dialogView.findViewById(R.id.ratingBarDialog);
        new AlertDialog.Builder(this)
                .setTitle("Calificar servicio")
                .setView(dialogView)
                .setPositiveButton("Enviar", (d,i) -> {
                    int score = (int) rb.getRating();
                    currentPost.incrementRating(score);
                    currentPost.saveInBackground(e -> {
                        if (e == null) {
                            Toast.makeText(this, "Gracias por tu calificación", Toast.LENGTH_SHORT).show();
                            btnRate.setVisibility(View.GONE);
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
