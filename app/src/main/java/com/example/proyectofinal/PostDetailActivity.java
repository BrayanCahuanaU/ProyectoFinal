// PostDetailActivity.java
package com.example.proyectofinal;

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

        // Aún sin funcionalidad real:
        btnContact.setOnClickListener(v ->
                Toast.makeText(this, "Función de chat próximamente", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadPost(String id) {
        ParseQuery<Post> q = ParseQuery.getQuery(Post.class);
        q.include("user");

        q.getInBackground(id, (post, e) -> {
            if (e == null) {
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
                }

            } else {
                Toast.makeText(this, "No se encontró el post", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
