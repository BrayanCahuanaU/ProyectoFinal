package com.example.proyectofinal;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.*;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private LinearLayout loadingPanel;
    private ChatAdapter adapter;
    private Button btnServiceDone, btnRate;
    private ImageView ivProfile;

    private String currentUserId, receiverId, postId;
    private ParseUser receiver;
    private Post currentPost;  // <-- ahora único Post

    private List<Message> messages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // UI
        ivProfile      = findViewById(R.id.ivProfile);
        rvMessages     = findViewById(R.id.rvMessages);
        etMessage      = findViewById(R.id.etMessage);
        btnSend        = findViewById(R.id.btnSend);
        loadingPanel   = findViewById(R.id.loadingPanel);
        btnServiceDone = findViewById(R.id.btnServiceDone);
        btnRate        = findViewById(R.id.btnRate);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnServiceDone.setVisibility(View.GONE);
        btnRate.setVisibility(View.GONE);

        adapter = new ChatAdapter(messages, ParseUser.getCurrentUser().getObjectId());
        rvMessages.setAdapter(adapter);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));

        // args
        currentUserId = ParseUser.getCurrentUser().getObjectId();
        receiverId    = getIntent().getStringExtra("receiverId");
        postId        = getIntent().getStringExtra("postId");

        loadOtherUserProfile();
        setupPostButtons();
        setupChat();
    }

    private void loadOtherUserProfile() {
        ParseUser.createWithoutData(ParseUser.class, receiverId)
                .fetchIfNeededInBackground((user, e) -> {
                    if (e==null) {
                        ParseFile pf = user.getParseFile("profileImage");
                        Glide.with(this)
                                .load(pf!=null?pf.getUrl():null)
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(R.drawable.cuenta)
                                .error(R.drawable.cuenta)
                                .into(ivProfile);
                    }
                });
    }

    private void setupPostButtons() {
        ParseQuery<Post> pq = ParseQuery.getQuery(Post.class);
        pq.getInBackground(postId, (post, e) -> {
            if (e != null) return;
            currentPost = post;  // ahora es realmente un Post

            boolean isProvider = post.getUser().getObjectId().equals(currentUserId);
            boolean done       = post.isServiceCompleted();

            if (isProvider && !done) {
                btnServiceDone.setVisibility(View.VISIBLE);
                btnServiceDone.setOnClickListener(v -> markServiceCompleted());
            }
            else if (!isProvider && done) {
                btnRate.setVisibility(View.VISIBLE);
                btnRate.setOnClickListener(v -> showRatingDialog());
            }
        });
    }


    private void markServiceCompleted() {
        btnServiceDone.setEnabled(false);
        currentPost.setServiceCompleted(true);
        currentPost.saveInBackground(e -> {
            if (e==null) {
                btnServiceDone.setVisibility(View.GONE);
                btnRate.setVisibility(View.VISIBLE);
                Toast.makeText(this,"Servicio completado",Toast.LENGTH_SHORT).show();
            } else {
                btnServiceDone.setEnabled(true);
            }
        });
    }

    private void setupChat() {
        btnSend.setOnClickListener(v -> sendMessage());
        loadReceiverAndMessages();
    }

    private void loadReceiverAndMessages() {
        ParseUser.getQuery().getInBackground(receiverId, (user, e) -> {
            if (e==null) {
                receiver = user;
                ((TextView)findViewById(R.id.tvChatTitle))
                        .setText("Chat con " + user.getUsername());
                loadMessages();
            } else finish();
        });
    }

    private void loadMessages() {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class)
                .whereEqualTo("post", ParseObject.createWithoutData(Post.class, postId));
        q.include("fromUser").include("toUser").orderByAscending("createdAt");
        q.findInBackground((list, e) -> {
            loadingPanel.setVisibility(View.GONE);
            if (e==null) {
                messages.clear();
                messages.addAll(list);
                adapter.notifyDataSetChanged();
                scrollToBottom();
            }
        });
    }

    private void sendMessage() {
        String txt = etMessage.getText().toString().trim();
        if (txt.isEmpty()) return;
        Message m = new Message();
        m.setFromUser(ParseUser.getCurrentUser());
        m.setToUser(receiver);
        m.setContent(txt);
        m.setPost(currentPost);
        messages.add(m);
        adapter.notifyItemInserted(messages.size()-1);
        etMessage.setText("");
        scrollToBottom();
        m.saveInBackground();
    }

    private void scrollToBottom() {
        rvMessages.post(() ->
                ((LinearLayoutManager)rvMessages.getLayoutManager())
                        .scrollToPositionWithOffset(messages.size()-1, 0)
        );
    }

    private void showRatingDialog() {
        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_rate, null);
        RatingBar rb = dialogView.findViewById(R.id.ratingBarDialog);

        new AlertDialog.Builder(this)
                .setTitle("Calificar servicio")
                .setView(dialogView)
                .setPositiveButton("Enviar", (d,w) -> {
                    int score = (int)rb.getRating();
                    currentPost.incrementRating(score);
                    currentPost.saveInBackground();
                    btnRate.setVisibility(View.GONE);
                    Toast.makeText(this,"Gracias por tu calificación",Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
