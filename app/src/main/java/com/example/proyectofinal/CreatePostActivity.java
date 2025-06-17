package com.example.proyectofinal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CreatePostActivity extends AppCompatActivity {
    private static final int PICK_IMAGES_REQUEST = 1001;
    private static final int MAX_IMAGES = 5;

    private Button btnSave;
    private EditText etTitle, etDescription, etPrice, etSchedules;
    private Spinner spinnerCategory;
    private LinearLayout layoutLocations;
    private LinearLayout btnPickImages;
    private TextView tvImageCount;
    private ProgressBar progressBar;

    private List<Uri> imageUris = new ArrayList<>();
    private List<ParseFile> parseFiles = new ArrayList<>();
    private Post postToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Views
        btnPickImages   = findViewById(R.id.btnPickImages);
        tvImageCount = findViewById(R.id.tvImageCount);
        btnSave         = findViewById(R.id.btnSavePost);
        etTitle         = findViewById(R.id.etTitle);
        etDescription   = findViewById(R.id.etDescription);
        etPrice         = findViewById(R.id.etPrice);
        etSchedules     = findViewById(R.id.etSchedules);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        layoutLocations = findViewById(R.id.layoutLocations);
        progressBar     = findViewById(R.id.progressBar);

        // Actualizar texto del botón
        updateImageButtonText();

        // Spinner categorías
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Comida", "Tecnología", "Arte", "Otro"}
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // Ubicaciones dinámicas
        for (String loc : new String[]{"Arequipa", "Lima", "Cusco"}) {
            CheckBox cb = new CheckBox(this);
            cb.setText(loc);
            layoutLocations.addView(cb);
        }

        // Modo edición
        String editId = getIntent().getStringExtra("editPostId");
        if (editId != null) {
            progressBar.setVisibility(View.VISIBLE);
            ParseQuery<Post> q = ParseQuery.getQuery(Post.class);
            q.getInBackground(editId, (post, e) -> {
                progressBar.setVisibility(View.GONE);
                if (e == null) {
                    postToEdit = post;
                    populateForEdit(post);
                } else {
                    Toast.makeText(this, "Error cargando post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }

        btnPickImages.setOnClickListener(v -> pickImagesFromGallery());
        btnSave.setOnClickListener(v -> savePost());
    }

    private void updateImageButtonText() {
        int count = imageUris.size();
        tvImageCount.setText("Seleccionar Imágenes (" + count + "/5)");
    }

    private void populateForEdit(Post post) {
        etTitle.setText(post.getTitle());
        etDescription.setText(post.getDescription());
        etPrice.setText(String.valueOf(post.getPrice()));
        etSchedules.setText(String.join(",", post.getSchedules()));

        // Categoría
        String category = post.getCategory();
        int pos = ((ArrayAdapter<String>) spinnerCategory.getAdapter()).getPosition(category);
        if (pos >= 0) spinnerCategory.setSelection(pos);

        // Ubicaciones seleccionadas
        List<String> locs = post.getLocations();
        for (int i = 0; i < layoutLocations.getChildCount(); i++) {
            CheckBox cb = (CheckBox) layoutLocations.getChildAt(i);
            cb.setChecked(locs.contains(cb.getText().toString()));
        }

        // Imágenes existentes
        List<ParseFile> imgs = post.getImages();
        if (imgs != null) parseFiles.addAll(imgs);
    }

    private void pickImagesFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Selecciona imágenes"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUris.clear();

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < Math.min(count, MAX_IMAGES); i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }

                if (count > MAX_IMAGES) {
                    Toast.makeText(this, "Máximo " + MAX_IMAGES + " imágenes permitidas", Toast.LENGTH_SHORT).show();
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }

            updateImageButtonText();
            Toast.makeText(this, imageUris.size() + " imágenes seleccionadas", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePost() {
        if (etTitle.getText().toString().isEmpty()) {
            etTitle.setError("Título requerido");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        // Nuevas imágenes
        if (!imageUris.isEmpty()) {
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
        }

        // Crear o editar
        Post post = postToEdit != null ? postToEdit : new Post();
        if (postToEdit == null) {
            post.setUser(ParseUser.getCurrentUser());
            post.setRating(0);
        }
        post.setImages(parseFiles);
        post.setTitle(etTitle.getText().toString());
        post.setDescription(etDescription.getText().toString());
        post.setPrice(Double.parseDouble(etPrice.getText().toString()));
        post.setSchedules(List.of(etSchedules.getText().toString().split("\\s*,\\s*")));
        post.setCategory(spinnerCategory.getSelectedItem().toString());

        List<String> locs = new ArrayList<>();
        for (int i = 0; i < layoutLocations.getChildCount(); i++) {
            CheckBox cb = (CheckBox) layoutLocations.getChildAt(i);
            if (cb.isChecked()) locs.add(cb.getText().toString());
        }
        post.setLocations(locs);

        post.saveInBackground((SaveCallback) e -> {
            progressBar.setVisibility(View.GONE);
            if (e == null) {
                String msg = postToEdit != null ? "Post actualizado" : "Post creado";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}