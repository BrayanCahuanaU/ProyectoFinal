package com.example.proyectofinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class FeedActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DocumentSnapshot lastVisible;
    private boolean isLastItemReached;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        // Configurar RecyclerView
        adapter = new PostAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Configurar SwipeRefresh
        swipeRefreshLayout.setOnRefreshListener(this::loadInitialPosts);

        // Cargar primeros posts
        loadInitialPosts();

        // Configurar scroll infinito
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLastItemReached && !swipeRefreshLayout.isRefreshing() &&
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0) {
                    loadMorePosts();
                }
            }
        });
    }

    private void loadInitialPosts() {
        swipeRefreshLayout.setRefreshing(true);

        db.collectionGroup("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(6)
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        List<Post> posts = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Post post = doc.toObject(Post.class);
                            posts.add(post);
                        }
                        adapter = new PostAdapter(posts);
                        recyclerView.setAdapter(adapter);

                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                        isLastItemReached = task.getResult().size() < 6;
                    } else {
                        Log.e("FeedActivity", "Error loading posts: ", task.getException());
                    }
                });
    }

    private void loadMorePosts() {
        if (lastVisible == null) return;

        swipeRefreshLayout.setRefreshing(true);

        db.collectionGroup("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(6)
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefreshLayout.setRefreshing(false);
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        List<Post> newPosts = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Post post = doc.toObject(Post.class);
                            newPosts.add(post);
                        }
                        adapter.addPosts(newPosts);

                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                        isLastItemReached = task.getResult().size() < 6;
                    } else if (task.isSuccessful()) {
                        isLastItemReached = true;
                    } else {
                        Log.e("FeedActivity", "Error loading more posts: ", task.getException());
                    }
                });
    }
}