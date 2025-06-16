package com.example.proyectofinal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CreatePostActivity extends AppCompatActivity {
    private static final int PICK_IMAGES_REQUEST = 1001;

    private Button btnPickImages, btnSave;
    private EditText etTitle, etDescription, etPrice, etSchedules;
    private Spinner spinnerCategory;
    private LinearLayout layoutLocations;

    private List<Uri> imageUris = new ArrayList<>();
    private List<ParseFile> parseFiles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Views
        btnPickImages   = findViewById(R.id.btnPickImages);
        btnSave         = findViewById(R.id.btnSavePost);
        etTitle         = findViewById(R.id.etTitle);
        etDescription   = findViewById(R.id.etDescription);
        etPrice         = findViewById(R.id.etPrice);
        etSchedules     = findViewById(R.id.etSchedules);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        layoutLocations = findViewById(R.id.layoutLocations);

        // Spinner: categorías (ejemplo estático)
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Comida", "Tecnología", "Arte", "Otro"});
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // Ubicaciones dinámicas (ejemplo)
        for (String loc : new String[]{"Arequipa", "Lima", "Cusco"}) {
            CheckBox cb = new CheckBox(this);
            cb.setText(loc);
            layoutLocations.addView(cb);
        }

        btnPickImages.setOnClickListener(v ->
                pickImagesFromGallery()
        );

        btnSave.setOnClickListener(v ->
                savePost()
        );
    }

    private void pickImagesFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(
                Intent.createChooser(intent, "Selecciona imágenes"),
                PICK_IMAGES_REQUEST
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUris.clear();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }
            Toast.makeText(this, imageUris.size() + " imágenes seleccionadas", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePost() {
        // Validaciones mínimas
        if (etTitle.getText().toString().isEmpty()) {
            etTitle.setError("Título requerido");
            return;
        }
        // Convertir URIs a ParseFile
        parseFiles.clear();
        for (Uri uri : imageUris) {
            try (InputStream is = getContentResolver().openInputStream(uri)) {
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                parseFiles.add(new ParseFile("image.jpg", bytes));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Crear objeto Post
        Post post = new Post();
        post.setUser(ParseUser.getCurrentUser());
        post.setImages(parseFiles);
        post.setTitle(etTitle.getText().toString());
        post.setDescription(etDescription.getText().toString());
        post.setPrice(Double.parseDouble(etPrice.getText().toString()));
        // Horarios como lista separada por comas
        List<String> schedules = List.of(etSchedules.getText().toString().split("\\s*,\\s*"));
        post.setSchedules(schedules);
        post.setCategory(spinnerCategory.getSelectedItem().toString());
        // Ubicaciones seleccionadas
        List<String> locs = new ArrayList<>();
        for (int i = 0; i < layoutLocations.getChildCount(); i++) {
            CheckBox cb = (CheckBox) layoutLocations.getChildAt(i);
            if (cb.isChecked()) locs.add(cb.getText().toString());
        }
        post.setLocations(locs);
        post.setRating(0); // inicial

        // Guardar en Parse
        post.saveInBackground((SaveCallback) e -> {
            if (e == null) {
                Toast.makeText(this, "Post creado", Toast.LENGTH_SHORT).show();
                finish();  // vuelve a la lista de posts
            } else {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
