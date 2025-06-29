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
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import java.io.InputStream;

public class EditAccountActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 200;

    private EditText etUsername, etEmail;
    private ImageView ivProfile;
    private ProgressBar progressBar;
    private Uri imageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account);

        etUsername = findViewById(R.id.etEditUsername);
        etEmail = findViewById(R.id.etEditEmail);
        ivProfile = findViewById(R.id.ivEditProfile);
        progressBar = findViewById(R.id.pbEditProgress);
        Button btnSave = findViewById(R.id.btnSaveAccount);
        Button btnChangePhoto = findViewById(R.id.btnChangePhoto);

        // Cargar datos actuales
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            etUsername.setText(user.getUsername());
            etEmail.setText(user.getEmail());
            ParseFile pf = user.getParseFile("profileImage");
            if (pf != null) {
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
        String newEmail = etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(newUsername) || TextUtils.isEmpty(newEmail)) {
            Toast.makeText(this, "Username y Email son requeridos", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        ParseUser user = ParseUser.getCurrentUser();
        user.setUsername(newUsername);
        user.setEmail(newEmail);
        if (imageUri != null) {
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                byte[] data = new byte[is.available()];
                is.read(data);
                ParseFile file = new ParseFile("profile.jpg", data);
                file.saveInBackground((ProgressCallback) e -> {
                    if (e == null) {
                        user.put("profileImage", file);
                        user.saveInBackground(saveCallback);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception ex) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Error leyendo imagen", Toast.LENGTH_SHORT).show();
            }
        } else {
            user.saveInBackground(saveCallback);
        }
    }

    private final SaveCallback saveCallback = e -> {
        progressBar.setVisibility(View.GONE);
        if (e == null) {
            Toast.makeText(this, "Cuenta actualizada", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error actualizando cuenta", Toast.LENGTH_SHORT).show();
        }
    };
}
