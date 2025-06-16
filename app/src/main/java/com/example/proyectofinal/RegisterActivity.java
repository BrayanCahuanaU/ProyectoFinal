package com.example.proyectofinal;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        progressBar = findViewById(R.id.progressBar);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validaciones
        if (username.isEmpty()) {
            showError(etUsername, "Nombre de usuario requerido");
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, "Email inválido");
            return;
        }

        if (password.length() < 6) {
            showError(etPassword, "Mínimo 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError(etConfirmPassword, "Las contraseñas no coinciden");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        user.signUpInBackground(e -> {
            progressBar.setVisibility(View.GONE);
            if (e == null) {
                Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showError(TextInputEditText field, String message) {
        if (field.getParent().getParent() instanceof TextInputLayout) {
            ((TextInputLayout) field.getParent().getParent()).setError(message);
        }
    }
}