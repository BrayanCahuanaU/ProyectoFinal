package com.example.proyectofinal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.io.InputStream;

public class EditAccountActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 200;

    private EditText etUsername, etEmail;
    private EditText etCurrentPass, etNewPass, etConfirmPass;
    private ImageView ivProfile;
    private ProgressBar progressBar;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        etUsername     = findViewById(R.id.etEditUsername);
        etEmail        = findViewById(R.id.etEditEmail);
        etCurrentPass  = findViewById(R.id.etCurrentPassword);
        etNewPass      = findViewById(R.id.etNewPassword);
        etConfirmPass  = findViewById(R.id.etConfirmPassword);
        ivProfile      = findViewById(R.id.ivEditProfile);
        progressBar    = findViewById(R.id.pbEditProgress);
        Button btnSave = findViewById(R.id.btnSaveAccount);
        Button btnChangePhoto = findViewById(R.id.btnChangePhoto);

        // Cargar datos actuales
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            etUsername.setText(user.getUsername());
            etEmail.setText(user.getEmail());
            ParseFile pf = user.getParseFile("profileImage");
            if (pf != null && pf.getUrl() != null) {
                Glide.with(this).load(pf.getUrl()).into(ivProfile);
            }
        }

        btnChangePhoto.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, PICK_IMAGE);
        });

        btnSave.setOnClickListener(v -> saveChanges());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            ivProfile.setImageURI(imageUri);
        }
    }

    private void saveChanges() {
        String newUsername = etUsername.getText().toString().trim();
        String newEmail    = etEmail.getText().toString().trim();
        String currentPass = etCurrentPass.getText().toString();
        String newPass     = etNewPass.getText().toString();
        String confirmPass = etConfirmPass.getText().toString();

        if (TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(newEmail)) {
            Toast.makeText(this, "Username y Email son requeridos", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean changingPass = !TextUtils.isEmpty(currentPass)
                || !TextUtils.isEmpty(newPass) || !TextUtils.isEmpty(confirmPass);
        if (changingPass) {
            if (TextUtils.isEmpty(currentPass) || TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
                Toast.makeText(this, "Completa todos los campos de contraseña", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "La nueva contraseña y confirmación no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        progressBar.setVisibility(View.VISIBLE);
        ParseUser user = ParseUser.getCurrentUser();
        user.setUsername(newUsername);
        user.setEmail(newEmail);

        // Si hay nueva foto, subirla primero
        if (imageUri != null) {
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                byte[] data = new byte[is.available()];
                is.read(data);
                is.close();
                ParseFile file = new ParseFile("profile.jpg", data);
                file.saveInBackground((ParseException fileErr) -> {
                    if (fileErr == null) {
                        user.put("profileImage", file);
                        continueSave(user, changingPass, currentPass, newPass);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException ex) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error leyendo imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            continueSave(user, changingPass, currentPass, newPass);
        }
    }

    private void continueSave(ParseUser user, boolean changingPass,
                              String currentPass, String newPass) {
        if (changingPass) {
            // Reautenticar con contraseña actual
            String username = user.getUsername();
            ParseUser.logInInBackground(username, currentPass, (u, loginErr) -> {
                if (loginErr == null) {
                    u.setPassword(newPass);
                    u.saveInBackground(saveCallback);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            user.saveInBackground(saveCallback);
        }
    }

    private final SaveCallback saveCallback = new SaveCallback() {
        @Override
        public void done(ParseException e) {
            progressBar.setVisibility(View.GONE);
            if (e == null) {
                Toast.makeText(EditAccountActivity.this,
                        "Cuenta actualizada correctamente", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditAccountActivity.this,
                        "Error actualizando cuenta: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    };
}

