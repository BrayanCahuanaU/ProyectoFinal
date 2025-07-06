package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends AppCompatActivity {
    // Cabecera
    private TextView titleText, tvUser, tvEmail, tvCreatedAt, tvPostCount;
    private FloatingActionButton btnBack, btnAccLogout;
    private MaterialButton btnEditAccount, btnDeleteAccount;

    // RecyclerView mis posts
    private RecyclerView rvMyPosts;
    private PostAdapter adapter;
    private List<Post> myPosts = new ArrayList<>();

    // Bottom nav
    private MaterialButton btnAccount, btnNewPost, btnChats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // --- 1) Vincular vistas de la cabecera ---
        titleText      = findViewById(R.id.title_text);
        btnBack        = findViewById(R.id.btnBack);
        titleText.setText("Mi Cuenta");
        btnBack.setOnClickListener(v -> finish());

        // --- 2) Vincular vistas de datos de usuario ---
        tvUser         = findViewById(R.id.tvAccUsername);
        tvEmail        = findViewById(R.id.tvAccEmail);
        tvCreatedAt    = findViewById(R.id.tvAccCreatedAt);
        tvPostCount    = findViewById(R.id.tvAccPostCount);
        CircleImageView ivProfile = findViewById(R.id.ivProfile);

        // --- 3) Vincular botones de acción sobre cuenta ---
        btnAccLogout   = findViewById(R.id.btnAccLogout);
        btnEditAccount = findViewById(R.id.btnEditAccount);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        btnAccLogout.setOnClickListener(v -> {
            ParseUser.logOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
        btnEditAccount.setOnClickListener(v ->
                startActivity(new Intent(this, EditAccountActivity.class))
        );
        btnDeleteAccount.setOnClickListener(v -> confirmAndDeleteAccount());

        // --- 4) RecyclerView de mis publicaciones ---
        rvMyPosts = findViewById(R.id.rvMyPosts);
        adapter = new PostAdapter(this, myPosts, true);
        rvMyPosts.setLayoutManager(new GridLayoutManager(this, 2));
        rvMyPosts.setAdapter(adapter);

        // --- 5) Vincular bottom nav ---
        btnAccount = findViewById(R.id.btnAccount);
        btnNewPost = findViewById(R.id.btnNewPost);
        btnChats = findViewById(R.id.btnChats);

        btnAccount.setOnClickListener(v -> {
            // Ya estamos en AccountActivity => opcional: refrescar
            recreate();
        });
        btnNewPost.setOnClickListener(v -> {
            startActivity(new Intent(this, CreatePostActivity.class));
        });
        btnChats.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatHistoryActivity.class));
        });

        // --- 6) Cargar datos ---
        loadUserData(ivProfile);
        loadMyPosts();
    }

    private void loadUserData(CircleImageView ivProfile) {
        ParseUser u = ParseUser.getCurrentUser();
        if (u != null) {
            tvUser.setText(u.getUsername());
            tvEmail.setText(u.getEmail());
            Date createdAt = u.getCreatedAt();
            String pattern = "dd/MM/yyyy 'a las' HH:mm";
            tvCreatedAt.setText(new SimpleDateFormat(pattern, Locale.getDefault())
                    .format(createdAt));

            if (u.getParseFile("profileImage") != null) {
                Glide.with(this)
                        .load(u.getParseFile("profileImage").getUrl())
                        .placeholder(R.drawable.cuenta)
                        .circleCrop()
                        .into(ivProfile);
            }
        }
    }

    private void loadMyPosts() {
        ParseQuery<Post> q = ParseQuery.getQuery(Post.class)
                .whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser())
                .orderByDescending("createdAt");
        q.findInBackground((list, e) -> {
            if (e == null) {
                myPosts.clear();
                myPosts.addAll(list);
                adapter.notifyDataSetChanged();
                tvPostCount.setText(list.size() + " publicaciones");
            } else {
                Toast.makeText(this, "Error cargando tus posts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmAndDeleteAccount() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Cuenta")
                .setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Se borrarán tus publicaciones y mensajes.")
                .setPositiveButton("Sí", (d, w) -> deleteAccountData())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAccountData() {
        ParseUser me = ParseUser.getCurrentUser();
        if (me == null) return;

        // 1) Eliminar posts
        ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class)
                .whereEqualTo(Post.KEY_USER, me);
        postQuery.findInBackground((posts, e1) -> {
            if (posts != null) {
                for (Post p : posts) p.deleteInBackground();
            }
            // 2) Eliminar mensajes
            ParseQuery<Message> m1 = ParseQuery.getQuery(Message.class)
                    .whereEqualTo("fromUser", me);
            ParseQuery<Message> m2 = ParseQuery.getQuery(Message.class)
                    .whereEqualTo("toUser", me);
            ParseQuery.or(Arrays.asList(m1, m2))
                    .findInBackground((msgs, e2) -> {
                        if (msgs != null) {
                            for (Message m : msgs) m.deleteInBackground();
                        }
                        // 3) Eliminar usuario
                        me.deleteInBackground(e3 -> {
                            if (e3 == null) {
                                ParseUser.logOut();
                                Intent i = new Intent(this, LoginActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                                finish();
                            } else {
                                Toast.makeText(this, "Error eliminando cuenta", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
        });
    }
}
