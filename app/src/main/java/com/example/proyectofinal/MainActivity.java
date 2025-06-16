package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvPosts;
    private PostAdapter adapter;
    private List<Post> posts = new ArrayList<>();
    private SearchView svPosts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 1) Views existentes
        rvPosts    = findViewById(R.id.rvPosts);
        adapter    = new PostAdapter(this, posts, false);
        svPosts    = findViewById(R.id.svPosts);

        // 2 columnas
        rvPosts.setLayoutManager(new GridLayoutManager(this, 2));
        rvPosts.setAdapter(adapter);

        // 2) Carga inicial
        loadPosts(null);

        // 3) Listener de búsqueda
        svPosts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadPosts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Búsqueda en tiempo real opcional:
                loadPosts(newText);
                return true;
            }
        });

        FloatingActionButton btnNewPost = findViewById(R.id.btnNewPost);
        btnNewPost.setOnClickListener(v -> {
            startActivity(new Intent(this, CreatePostActivity.class));
        });

        FloatingActionButton btnAccount = findViewById(R.id.btnAccount);
        btnAccount.setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class))
        );

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvUserInfo = findViewById(R.id.tvUserInfo);

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

    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void loadPosts(@Nullable String query) {
        ParseQuery<Post> qTitle = ParseQuery.getQuery(Post.class)
                .whereMatches(Post.KEY_TITLE, "(?i).*" + (query != null ? query : "") + ".*");
        ParseQuery<Post> qDesc = ParseQuery.getQuery(Post.class)
                .whereContains(Post.KEY_DESC, query != null ? query : "");
        ParseQuery<Post> qCat  = ParseQuery.getQuery(Post.class)
                .whereMatches(Post.KEY_CATEGORY, "(?i).*" + (query != null ? query : "") + ".*");

        List<ParseQuery<Post>> orQueries = Arrays.asList(qTitle, qDesc, qCat);
        ParseQuery<Post> mainQuery = ParseQuery.or(orQueries);
        mainQuery.orderByDescending("createdAt");
        mainQuery.findInBackground((list, e) -> {
            if (e == null) {
                posts.clear();
                posts.addAll(list);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error cargando posts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}