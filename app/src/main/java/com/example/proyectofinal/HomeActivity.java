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

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // VERIFICACIÓN PRIMERO: Si no hay usuario, regresar inmediatamente
        if (user == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return; // Importante: salir del método
        }

        // Inicializar vistas DESPUÉS de verificar usuario
        TextView userEmail = findViewById(R.id.userEmail);
        Button logoutBtn = findViewById(R.id.logoutBtn);

        userEmail.setText(user.getEmail());

        // Configurar botón de logout
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