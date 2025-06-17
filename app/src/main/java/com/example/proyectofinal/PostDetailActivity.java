// PostDetailActivity.java
package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.parse.*;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private ViewPager vpImages;
    private TextView tvTitle, tvDesc, tvPrice, tvSchedules, tvCategory, tvLocations;
    private RatingBar ratingBar;
    private Button btnContact;
    private ImageButton btnBack;
    private TextView tvUser;
    private ImageView ivUserProfile;
    private Post currentPost;
    private String postUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Views
        vpImages     = findViewById(R.id.vpImages);
        tvTitle      = findViewById(R.id.tvDetailTitle);
        tvDesc       = findViewById(R.id.tvDetailDesc);
        tvPrice      = findViewById(R.id.tvDetailPrice);
        tvSchedules  = findViewById(R.id.tvDetailSchedules);
        tvCategory   = findViewById(R.id.tvDetailCategory);
        tvLocations  = findViewById(R.id.tvDetailLocations);
        ratingBar    = findViewById(R.id.ratingBarDetail);
        btnContact   = findViewById(R.id.btnContact);
        btnBack = findViewById(R.id.btnBack);
        tvUser = findViewById(R.id.tvUser);
        ivUserProfile = findViewById(R.id.ivUserProfile);


        String postId = getIntent().getStringExtra("postId");
        loadPost(postId);

        // Set back button click listener
        btnBack.setOnClickListener(v -> finish());

        btnContact.setOnClickListener(v -> {
            if (currentPost != null) {
                openChat(currentPost);
            }
        });
    }

    private void loadPost(String id) {
        ParseQuery<Post> q = ParseQuery.getQuery(Post.class);
        q.include("user");

        q.getInBackground(id, (post, e) -> {
            if (e == null) {
                currentPost = post;

                tvTitle.setText(post.getTitle());
                tvDesc.setText(post.getDescription());
                tvPrice.setText("$ " + post.getPrice());
                tvSchedules.setText("Horarios: " + String.join(", ", post.getSchedules()));
                tvCategory.setText("Categoría: " + post.getCategory());
                tvLocations.setText("Ubicaciones: " + String.join(", ", post.getLocations()));
                ratingBar.setRating(post.getRating().floatValue());

                // Imágenes con ViewPager + adapter sencillo
                List<String> urls = new ArrayList<>();
                for (ParseFile pf : post.getImages()) urls.add(pf.getUrl());
                vpImages.setAdapter(new ImagesPagerAdapter(this, urls));

                // Cargar información del usuario
                ParseUser user = post.getUser();
                if (user != null) {
                    tvUser.setText(user.getUsername());
                    postUserId = user.getObjectId(); // Almacenar el ID del usuario

                    // Cargar foto de perfil si existe
                    ParseFile profilePic = user.getParseFile("profilePic");
                    if (profilePic != null) {
                        Glide.with(this).load(profilePic.getUrl()).into(ivUserProfile);
                    } else {
                        // Imagen por defecto si no hay foto
                        ivUserProfile.setImageResource(R.drawable.ic_launcher_foreground);
                    }


                } else {
                    tvUser.setText("Usuario desconocido");
                    postUserId = null;
                }

            } else {
                Toast.makeText(this, "No se encontró el post", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void openChat(Post post) {
        ParseUser currentUser = ParseUser.getCurrentUser();

        // Verificar si el usuario está autenticado
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para contactar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Usar el ID almacenado en lugar del objeto ParseUser
        if (postUserId == null || postUserId.isEmpty()) {
            Toast.makeText(this, "Error: Usuario no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = currentUser.getObjectId();

        // Comparar IDs de forma segura
        if (currentUserId.equals(postUserId)) {
            Toast.makeText(this, "No puedes chatear contigo mismo", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("postId", post.getObjectId());
            intent.putExtra("receiverId", postUserId);
            startActivity(intent);
        }
    }
}
