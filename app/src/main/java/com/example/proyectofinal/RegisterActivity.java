package com.example.proyectofinal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.IOUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 100;

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
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

        //  Selección de imagen del perfil
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
        if (!password.equals(confirm)) {
            showError(etConfirmPassword, "Las contraseñas no coinciden");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // Paso 1: registro del usuario (sin imagen)
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        // ACL público
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(true);
        acl.setPublicWriteAccess(true);
        user.setACL(acl);

        user.signUpInBackground(e -> {
            if (e == null) {
                // Registro / signup exitoso
                if (imageUri != null) {
                    // Paso 2: subir la imagen al usuario autenticado
                    uploadProfileImage(user);
                } else {
                    // Paso 3: no había imagen, mostramos éxito
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                // Falla el registro
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this,
                        "Error registrando usuario: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.e("RegisterActivity", "signup failed", e);
            }
        });
    }

    private void uploadProfileImage(ParseUser user) {
        try {
            InputStream is = getContentResolver().openInputStream(imageUri);
            byte[] data = IOUtils.toByteArray(is);
            ParseFile profilePic = new ParseFile("profile.jpg", data);

            profilePic.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e1) {
                    progressBar.setVisibility(View.GONE);
                    if (e1 == null) {
                        // Imagen subida, asociarla al usuario
                        user.put("profileImage", profilePic);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e2) {
                                if (e2 == null) {
                                    // Paso 3: todo OK
                                    Toast.makeText(RegisterActivity.this,
                                            "¡Registro exitoso con foto de perfil!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    // La imagen subió, pero falló actualizar el user
                                    Toast.makeText(RegisterActivity.this,
                                            "Registro OK, pero no se guardó la foto. " +
                                                    "Pruébalo más tarde desde Ajustes.",
                                            Toast.LENGTH_LONG).show();
                                    Log.e("RegisterActivity",
                                            "user save after image failed", e2);
                                }
                                finish();
                            }
                        });
                    } else {
                        // Falló la subida de la imagen
                        Toast.makeText(RegisterActivity.this,
                                "Registro OK, pero fallo subir foto. " +
                                        "Pruébalo más tarde desde Ajustes.",
                                Toast.LENGTH_LONG).show();
                        Log.e("RegisterActivity", "file save error", e1);
                        finish();
                    }
                }
            });
        } catch (IOException ex) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this,
                    "Usuario registrado, pero error leyendo imagen. " +
                            "Pruébalo desde Ajustes.",
                    Toast.LENGTH_LONG).show();
            Log.e("RegisterActivity", "image read error", ex);
            finish();
        }
    }

    private void showError(TextInputEditText field, String message) {
        View parent = (View) field.getParent().getParent();
        if (parent instanceof TextInputLayout) {
            ((TextInputLayout) parent).setError(message);
        }
    }
}
