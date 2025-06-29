package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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

    private Spinner spPrice, spRating, spSort;
    private MaterialButton btnAccount, btnNewPost, btnChats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1) Inicialización de Views
        rvPosts = findViewById(R.id.rvPosts);
        adapter = new PostAdapter(this, posts, false);
        svPosts = findViewById(R.id.svPosts);

        // Spinners para filtros
        spPrice = findViewById(R.id.spPriceRange);
        spRating = findViewById(R.id.spMinRating);
        spSort = findViewById(R.id.spSort);

        // Botones inferiores
        btnAccount = findViewById(R.id.btnAccount);
        btnNewPost = findViewById(R.id.btnNewPost);
        btnChats = findViewById(R.id.btnChats);

        // Configuración del RecyclerView
        rvPosts.setLayoutManager(new GridLayoutManager(this, 2));
        rvPosts.setAdapter(adapter);

        // 2) Carga inicial de posts
        loadPosts(null);

        // 3) Configuración del SearchView
        svPosts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadPosts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadPosts(newText);
                return true;
            }
        });

        // 4) Listeners para los spinners
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadPosts(svPosts.getQuery().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spPrice.setOnItemSelectedListener(spinnerListener);
        spRating.setOnItemSelectedListener(spinnerListener);
        spSort.setOnItemSelectedListener(spinnerListener);

        // 5) Listeners para los botones inferiores
        btnNewPost.setOnClickListener(v -> {
            startActivity(new Intent(this, CreatePostActivity.class));
        });

        btnAccount.setOnClickListener(v -> {
            startActivity(new Intent(this, AccountActivity.class));
        });

        btnChats.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatHistoryActivity.class));
        });

        // Verificar usuario actual
        ParseUser currentUser = ParseUser.getCurrentUser();
    }

    private void loadPosts(@Nullable String query) {
        String q = (query != null) ? query : "";

        // Crear consultas para búsqueda
        ParseQuery<Post> qTitle = ParseQuery.getQuery(Post.class)
                .whereMatches(Post.KEY_TITLE, "(?i).*" + q + ".*");
        ParseQuery<Post> qDesc = ParseQuery.getQuery(Post.class)
                .whereMatches(Post.KEY_DESC, "(?i).*" + q + ".*");
        ParseQuery<Post> qCat = ParseQuery.getQuery(Post.class)
                .whereMatches(Post.KEY_CATEGORY, "(?i).*" + q + ".*");

        ParseQuery<Post> mainQuery = ParseQuery.or(Arrays.asList(qTitle, qDesc, qCat));

        // --- APLICAR FILTROS ---

        // Filtro de precio
        String priceSel = spPrice.getSelectedItem().toString();
        switch (priceSel) {
            case "0 - 50":
                mainQuery.whereGreaterThanOrEqualTo("price", 0)
                        .whereLessThanOrEqualTo("price", 50);
                break;
            case "50 - 100":
                mainQuery.whereGreaterThanOrEqualTo("price", 50)
                        .whereLessThanOrEqualTo("price", 100);
                break;
            case "100+":
                mainQuery.whereGreaterThan("price", 100);
                break;
            // "Todos" no aplica filtro
        }

        // Filtro de rating mínimo
        String ratingSel = spRating.getSelectedItem().toString();
        if (!ratingSel.equals("Todos")) {
            int minRating = Integer.parseInt(ratingSel.substring(0, 1));
            mainQuery.whereGreaterThanOrEqualTo("rating", minRating);
        }

        // Ordenamiento
        String sortSel = spSort.getSelectedItem().toString();
        switch (sortSel) {
            case "Precio ↑":
                mainQuery.orderByAscending("price");
                break;
            case "Precio ↓":
                mainQuery.orderByDescending("price");
                break;
            case "Título A→Z":
                mainQuery.orderByAscending(Post.KEY_TITLE);
                break;
            case "Título Z→A":
                mainQuery.orderByDescending(Post.KEY_TITLE);
                break;
            default: // "Recientes"
                mainQuery.orderByDescending("createdAt");
        }

        // Ejecutar consulta
        mainQuery.findInBackground((list, e) -> {
            if (e == null) {
                posts.clear();
                posts.addAll(list);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error cargando posts: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}