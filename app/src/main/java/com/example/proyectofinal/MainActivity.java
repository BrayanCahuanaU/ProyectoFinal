package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;

import android.widget.ImageButton;
import android.widget.Spinner;
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

    private Spinner spPrice, spRating, spSort;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 1) Views existentes
        rvPosts    = findViewById(R.id.rvPosts);
        adapter    = new PostAdapter(this, posts, false);
        svPosts    = findViewById(R.id.svPosts);

        // inicializa aquí tus spinners como campos
        spPrice  = findViewById(R.id.spPriceRange);
        spRating = findViewById(R.id.spMinRating);
        spSort   = findViewById(R.id.spSort);

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

        AdapterView.OnItemSelectedListener reloadListener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadPosts(svPosts.getQuery().toString());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        };

        spPrice.setOnItemSelectedListener(reloadListener);
        spRating.setOnItemSelectedListener(reloadListener);
        spSort.setOnItemSelectedListener(reloadListener);

        FloatingActionButton btnNewPost = findViewById(R.id.btnNewPost);
        btnNewPost.setOnClickListener(v -> {
            startActivity(new Intent(this, CreatePostActivity.class));
        });

        FloatingActionButton btnAccount = findViewById(R.id.btnAccount);
        btnAccount.setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class))
        );

        ImageButton btnChats = findViewById(R.id.btnChats);
        btnChats.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatHistoryActivity.class));
        });


        ParseUser currentUser = ParseUser.getCurrentUser();
    }

    private void loadPosts(@Nullable String query) {
        String q = query != null ? query : "";

        // Búsqueda por título/desc/categoría
        ParseQuery<Post> qTitle = ParseQuery.getQuery(Post.class)
                .whereMatches(Post.KEY_TITLE, "(?i).*" + q + ".*");
        ParseQuery<Post> qDesc = ParseQuery.getQuery(Post.class)
                .whereMatches(Post.KEY_DESC, "(?i).*" + q + ".*");
        ParseQuery<Post> qCat = ParseQuery.getQuery(Post.class)
                .whereMatches(Post.KEY_CATEGORY, "(?i).*" + q + ".*");

        ParseQuery<Post> mainQuery = ParseQuery.or(Arrays.asList(qTitle, qDesc, qCat));

        // --- FILTRO DE PRECIO ---
        String priceSel = spPrice.getSelectedItem().toString();
        switch (priceSel) {
            case "0 - 50":
                mainQuery.whereGreaterThanOrEqualTo("price", 0);
                mainQuery.whereLessThanOrEqualTo("price", 50);
                break;
            case "50 - 100":
                mainQuery.whereGreaterThanOrEqualTo("price", 50);
                mainQuery.whereLessThanOrEqualTo("price", 100);
                break;
            case "100+":
                mainQuery.whereGreaterThan("price", 100);
                break;
            default:
                // “Todos” no añade condición
        }

        // --- FILTRO DE RATING MÍNIMO ---
        String ratingSel = spRating.getSelectedItem().toString();
        if (!ratingSel.equals("Todos")) {
            int minRating = Integer.parseInt(ratingSel.substring(0,1));
            mainQuery.whereGreaterThanOrEqualTo("rating", minRating);
        }

        // --- ORDEN ---
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
            default:
                // “Recientes”
                mainQuery.orderByDescending("createdAt");
        }

        // Ejecutar
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