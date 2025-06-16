// AccountActivity.java
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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class AccountActivity extends AppCompatActivity {
    private TextView tvUser, tvEmail;
    private Button btnLogout;
    private RecyclerView rvMyPosts;
    private PostAdapter adapter;
    private List<Post> myPosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        tvUser    = findViewById(R.id.tvAccUsername);
        tvEmail   = findViewById(R.id.tvAccEmail);
        btnLogout = findViewById(R.id.btnAccLogout);
        rvMyPosts = findViewById(R.id.rvMyPosts);

        // Datos de usuario
        ParseUser u = ParseUser.getCurrentUser();
        if (u != null) {
            tvUser.setText("Usuario: " + u.getUsername());
            tvEmail.setText("Email: " + u.getEmail());
        }

        // Cerrar sesión
        btnLogout.setOnClickListener(v -> {
            ParseUser.logOut();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });

        // RecyclerView de mis posts
        adapter = new PostAdapter(this, myPosts, true);
        rvMyPosts.setLayoutManager(new GridLayoutManager(this, 2));
        rvMyPosts.setAdapter(adapter);

        loadMyPosts();
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
            } else {
                Toast.makeText(this, "Error cargando tus posts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
