package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.parse.*;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {
    private ViewPager vpImages;
    private TextView tvTitle, tvDesc, tvPrice, tvSchedules, tvCategory, tvLocations, tvUser;
    private RatingBar ratingBar;
    private Button btnContact;
    private ImageButton btnBack;
    private ImageView ivUserProfile;

    private Post currentPost;
    private String ownerId;
    private ParseUser ownerUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // 1) Vincular vistas
        vpImages      = findViewById(R.id.vpImages);
        tvTitle       = findViewById(R.id.tvDetailTitle);
        tvDesc        = findViewById(R.id.tvDetailDesc);
        tvPrice       = findViewById(R.id.tvDetailPrice);
        tvSchedules   = findViewById(R.id.tvDetailSchedules);
        tvCategory    = findViewById(R.id.tvDetailCategory);
        tvLocations   = findViewById(R.id.tvDetailLocations);
        ratingBar     = findViewById(R.id.ratingBarDetail);
        btnContact    = findViewById(R.id.btnContact);
        btnBack       = findViewById(R.id.btnBack);
        tvUser        = findViewById(R.id.tvUser);
        ivUserProfile = findViewById(R.id.ivUserProfile);

        // Cargar el post
        String postId = getIntent().getStringExtra("postId");
        loadPost(postId);

        // Botones
        btnBack.setOnClickListener(v -> finish());
        btnContact.setOnClickListener(v -> openChat());
    }

    private void loadPost(String id) {
        ParseQuery<Post> q = ParseQuery.getQuery(Post.class);
        q.include(Post.KEY_USER);
        q.getInBackground(id, (post, e) -> {
            if (e != null) {
                Toast.makeText(this, "No se encontró el post", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            currentPost = post;
            // Rellenar datos
            tvTitle.setText(post.getTitle());
            tvDesc.setText(post.getDescription());
            tvPrice.setText("$ " + post.getPrice());
            tvSchedules.setText("Horarios: " + String.join(", ", post.getSchedules()));
            tvCategory.setText("Categoría: " + post.getCategory());
            tvLocations.setText("Ubicaciones: " + String.join(", ", post.getLocations()));
            ratingBar.setRating(post.getRating().floatValue());

            // Imágenes del post
            List<String> urls = new ArrayList<>();
            if (post.getImages() != null) {
                for (ParseFile pf : post.getImages()) {
                    if (pf != null && pf.getUrl() != null) {
                        urls.add(pf.getUrl());
                    }
                }
            }
            vpImages.setAdapter(new ImagesPagerAdapter(this, urls));

            // Usuario dueño
            ParseUser direct = post.getUser();
            if (direct != null) {
                // include() funcionó
                ownerUser = direct;
                ownerId   = direct.getObjectId();
                fillOwnerInfo(direct);
            } else {
                // fallback
                ParseObject ptr = post.getParseObject(Post.KEY_USER);
                if (ptr != null) {
                    ownerId = ptr.getObjectId();
                    fetchOwnerManually(ownerId);
                } else {
                    tvUser.setText("Usuario desconocido");
                    ivUserProfile.setImageResource(R.drawable.cuenta);
                    ownerId = null;
                }
            }

            Log.d("PostDetail", "ownerId=" + ownerId + " ownerUser=" + ownerUser);
        });
    }

    private void fetchOwnerManually(String ownerId) {
        ParseQuery<ParseUser> uq = ParseUser.getQuery();
        uq.getInBackground(ownerId, (user, err) -> {
            if (err != null) {
                Log.e("PostDetail", "Error fetch owner", err);
                tvUser.setText("Usuario desconocido");
                ivUserProfile.setImageResource(R.drawable.cuenta);
                return;
            }
            ownerUser = user;
            fillOwnerInfo(user);
        });
    }

    /** Rellena nombre + foto de perfil (campo "profileImage") */
    private void fillOwnerInfo(ParseUser user) {
        tvUser.setText(user.getUsername());
        ParseFile pic = user.getParseFile("profileImage");
        if (pic != null && pic.getUrl() != null) {
            Glide.with(this)
                    .load(pic.getUrl())
                    .placeholder(R.drawable.cuenta)
                    .circleCrop()
                    .into(ivUserProfile);
        } else {
            ivUserProfile.setImageResource(R.drawable.cuenta);
        }
    }

    private void openChat() {
        ParseUser current = ParseUser.getCurrentUser();
        if (current == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ownerId == null) {
            Toast.makeText(this, "Usuario no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ownerId.equals(current.getObjectId())) {
            Toast.makeText(this, "No puedes chatear contigo mismo", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("postId", currentPost.getObjectId());
        i.putExtra("receiverId", ownerId);
        startActivity(i);
    }
}
