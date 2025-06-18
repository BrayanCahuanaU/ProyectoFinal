package com.example.proyectofinal;

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
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ParseException;
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
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin   = findViewById(R.id.tvLogin);

        ivProfile.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pick, PICK_IMAGE);
        });

        btnRegister.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
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

        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);

        // ACL público
        ParseACL acl = new ParseACL();
        acl.setPublicReadAccess(true);
        acl.setPublicWriteAccess(true);
        user.setACL(acl);

        // Si no hay imagen, registra directamente
        if (imageUri == null) {
            signUp(user);
            return;
        }

        // Si hay imagen, súbela primero
        try {
            InputStream is = getContentResolver().openInputStream(imageUri);
            byte[] data = IOUtils.toByteArray(is);
            ParseFile pic = new ParseFile("profile.jpg", data);

            pic.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e1) {
                    if (e1 == null) {
                        // La imagen ya se subió
                        user.put("profileImage", pic);
                        signUp(user);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterActivity.this,
                                "Error subiendo imagen: " + e1.getMessage(),
                                Toast.LENGTH_LONG).show();
                        Log.e("RegisterActivity", "file save error", e1);
                    }
                }
            });
        } catch (IOException ex) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this,
                    "No se pudo leer la imagen: " + ex.getMessage(),
                    Toast.LENGTH_LONG).show();
            Log.e("RegisterActivity", "image read error", ex);
        }
    }

    private void signUp(ParseUser user) {
        user.signUpInBackground(e -> {
            progressBar.setVisibility(View.GONE);
            if (e == null) {
                Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this,
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.e("RegisterActivity", "signup failed", e);
            }
        });
    }

    private void showError(TextInputEditText field, String message) {
        View parent = (View) field.getParent().getParent();
        if (parent instanceof TextInputLayout) {
            ((TextInputLayout) parent).setError(message);
        }
    }
}
