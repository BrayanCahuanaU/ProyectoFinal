package com.example.proyectofinal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.IOUtils;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.IOException;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private ImageView ivProfile;
    private ProgressBar progressBar;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername        = findViewById(R.id.etUsername);
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivProfile         = findViewById(R.id.ivProfile);
        progressBar       = findViewById(R.id.progressBar);
        Button btnRegister= findViewById(R.id.btnRegister);
        TextView tvLogin  = findViewById(R.id.tvLogin);

        ivProfile.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pick, PICK_IMAGE);
        });

        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_IMAGE && res == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            ivProfile.setImageURI(imageUri);
        }
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm  = etConfirmPassword.getText().toString();

        if (username.isEmpty()) {
            etUsername.setError("Nombre de usuario requerido");
            return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email inválido");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            return;
        }
        if (!password.equals(confirm)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(true);
        acl.setPublicWriteAccess(true);
        user.setACL(acl);

        user.signUpInBackground(e -> {
            if (e == null) {
                if (imageUri != null) {
                    uploadProfileImage(user);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error registrando usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("RegisterActivity", "signup failed", e);
            }
        });
    }

    private void uploadProfileImage(ParseUser user) {
        try {
            InputStream is = getContentResolver().openInputStream(imageUri);
            byte[] data = IOUtils.toByteArray(is);
            ParseFile profilePic = new ParseFile("profile.jpg", data);

            profilePic.saveInBackground((ProgressCallback) e1 -> {
                progressBar.setVisibility(View.GONE);
                if (e1 == null) {
                    user.put("profileImage", profilePic);
                    user.saveInBackground(e2 -> {
                        if (e2 == null) {
                            Toast.makeText(RegisterActivity.this,
                                    "¡Registro exitoso con foto de perfil!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Registro OK, pero no se guardó la foto.",
                                    Toast.LENGTH_LONG).show();
                            Log.e("RegisterActivity", "user save after image failed", e2);
                        }
                        finish();
                    });
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Registro OK, pero fallo subir foto.",
                            Toast.LENGTH_LONG).show();
                    Log.e("RegisterActivity", "file save error");
                    finish();
                }
            });
        } catch (IOException ex) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this,
                    "Usuario registrado, pero error leyendo imagen.",
                    Toast.LENGTH_LONG).show();
            Log.e("RegisterActivity", "image read error", ex);
            finish();
        }
    }
}
