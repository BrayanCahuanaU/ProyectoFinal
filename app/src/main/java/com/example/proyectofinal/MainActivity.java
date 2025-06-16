package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvPosts;
    private PostAdapter adapter;
    private List<Post> posts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        rvPosts = findViewById(R.id.rvPosts);
        adapter = new PostAdapter(this, posts);

        // 2 columnas
        rvPosts.setLayoutManager(new GridLayoutManager(this, 2));
        rvPosts.setAdapter(adapter);

        loadPosts();

        Button btnNewPost = findViewById(R.id.btnNewPost);
        btnNewPost.setOnClickListener(v -> {
            startActivity(new Intent(this, CreatePostActivity.class));
        });

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvUserInfo = findViewById(R.id.tvUserInfo);
        Button btnLogout = findViewById(R.id.btnLogout);

        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser != null) {
            // Mostrar información del usuario
            String welcomeText = "¡Bienvenido, " + currentUser.getUsername() + "!";
            tvWelcome.setText(welcomeText);

            // Mostrar información adicional
            String userInfo = "Email: " + currentUser.getEmail() + "\n"
                    + "Fecha de creación: " + currentUser.getCreatedAt();
            tvUserInfo.setText(userInfo);
        } else {
            // Si no hay usuario, volver al login
            goToLoginActivity();
        }

        // Configurar el botón de cierre de sesión
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                goToLoginActivity();
            }
        });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // opcional: solo del usuario actual
        // query.whereEqualTo(Post.KEY_USER, ParseUser.getCurrentUser());
        query.orderByDescending("createdAt");
        query.findInBackground((list, e) -> {
            if (e == null) {
                posts.clear();
                posts.addAll(list);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error al cargar posts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}