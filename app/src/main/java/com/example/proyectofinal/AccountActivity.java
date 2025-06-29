package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private TextView tvUser, tvEmail, tvCreatedAt, tvPostCount;
    private CircleImageView ivProfile;
    private FloatingActionButton btnAccLogout, btnBack;
    private MaterialButton btnEditAccount, btnDeleteAccount;
    private RecyclerView rvMyPosts;
    private PostAdapter adapter;
    private List<Post> myPosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Inicializar vistas
        tvUser = findViewById(R.id.tvAccUsername);
        tvEmail = findViewById(R.id.tvAccEmail);
        tvCreatedAt = findViewById(R.id.tvAccCreatedAt);
        tvPostCount = findViewById(R.id.tvAccPostCount);
        ivProfile = findViewById(R.id.ivProfile);
        btnAccLogout = findViewById(R.id.btnAccLogout);
        btnBack = findViewById(R.id.btnBack);
        btnEditAccount = findViewById(R.id.btnEditAccount);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        rvMyPosts = findViewById(R.id.rvMyPosts);

        // Configurar RecyclerView
        adapter = new PostAdapter(this, myPosts, true);
        rvMyPosts.setLayoutManager(new GridLayoutManager(this, 2));
        rvMyPosts.setAdapter(adapter);

        // Cargar datos del usuario y publicaciones
        loadUserData();
        loadMyPosts();

        // Acciones de botones
        btnBack.setOnClickListener(v -> finish());

        btnAccLogout.setOnClickListener(v -> {
            ParseUser.logOut();
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        btnEditAccount.setOnClickListener(v -> {
            // Lanzar actividad de edición de cuenta
            startActivity(new Intent(this, EditAccountActivity.class));
        });

        btnDeleteAccount.setOnClickListener(v -> confirmAndDeleteAccount());
    }

    private void loadUserData() {
        ParseUser u = ParseUser.getCurrentUser();
        if (u != null) {
            tvUser.setText(u.getUsername());
            tvEmail.setText(u.getEmail());

            Date createdAt = u.getCreatedAt();
            String pattern = "dd/MM/yyyy 'a las' HH:mm";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            tvCreatedAt.setText(sdf.format(createdAt));

            // Foto de perfil
            if (u.getParseFile("profileImage") != null) {
                Glide.with(this)
                        .load(u.getParseFile("profileImage").getUrl())
                        .placeholder(R.drawable.cuenta)
                        .into(ivProfile);
            }
        }
    }

    private void loadMyPosts() {
        ParseQuery<Post> q = ParseQuery.getQuery(Post.class);
        q.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        q.orderByDescending("createdAt");
        q.findInBackground((list, e) -> {
            if (e == null) {
                myPosts.clear();
                myPosts.addAll(list);
                adapter.notifyDataSetChanged();

                int total = list.size();
                tvPostCount.setText(total + " publicaciones");
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

        // Borrar posts
        ParseQuery<Post> postQuery = ParseQuery.getQuery(Post.class);
        postQuery.whereEqualTo(Post.KEY_USER, me);
        postQuery.findInBackground((posts, e1) -> {
            if (posts != null) {
                for (Post p : posts) p.deleteInBackground();
            }
            // Borrar mensajes
            ParseQuery<Message> m1 = ParseQuery.getQuery(Message.class)
                    .whereEqualTo("fromUser", me);
            ParseQuery<Message> m2 = ParseQuery.getQuery(Message.class)
                    .whereEqualTo("toUser", me);
            ParseQuery.or(Arrays.asList(m1, m2))
                    .findInBackground((msgs, e2) -> {
                        if (msgs != null) {
                            for (Message m : msgs) m.deleteInBackground();
                        }
                        // Borrar usuario
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
