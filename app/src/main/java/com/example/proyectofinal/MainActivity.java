package com.example.proyectofinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvUserInfo = findViewById(R.id.tvUserInfo);
        Button btnLogout = findViewById(R.id.btnLogout);

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

        // Configurar el botón de cierre de sesión
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                goToLoginActivity();
            }
        });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}