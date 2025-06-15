package com.example.proyectofinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Button logoutBtn = findViewById(R.id.logoutBtn);
        TextView userEmail = findViewById(R.id.userEmail);

        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            userEmail.setText(user.getEmail());

            // Ejemplo: Guardar un post asociado al usuario
            saveUserPost("Mi primer post");
        }

        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void saveUserPost(String content) {
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null) {
            Post post = new Post(content);
            db.collection("users")
                    .document(user.getUid())
                    .collection("posts")
                    .add(post);
        }
    }
}