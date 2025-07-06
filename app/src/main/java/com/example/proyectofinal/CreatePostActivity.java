package com.example.proyectofinal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreatePostActivity extends AppCompatActivity {
    private static final int PICK_IMAGES_REQUEST = 1001;
    private static final int MAX_IMAGES = 5;

    // Cabecera
    private TextView titleText;
    private ImageButton btnBack;

    // Formulario
    private LinearLayout btnPickImages;
    private TextView tvImageCount;
    private EditText etTitle, etDescription, etPrice;
    private Spinner spinnerRegion, spinnerCategory, spinnerSchedules;
    private LinearLayout layoutLocations;
    private ProgressBar progressBar;
    private Button btnSave;

    // Bottom nav
    private Button btnAccount, btnNewPost, btnChats;

    // Manejo de imágenes y edición
    private List<Uri> imageUris = new ArrayList<>();
    private List<ParseFile> parseFiles = new ArrayList<>();
    private Post postToEdit;

    // Todos los departamentos del Perú
    private static final String[] DEPARTAMENTOS = {
            "Amazonas","Áncash","Apurímac","Arequipa","Ayacucho","Cajamarca",
            "Callao","Cusco","Huancavelica","Huánuco","Ica","Junín",
            "La Libertad","Lambayeque","Lima","Loreto","Madre de Dios",
            "Moquegua","Pasco","Piura","Puno","San Martín","Tacna","Tumbes","Ucayali"
    };

    // Listas de sur y norte
    private static final Set<String> SUR = Set.of(
            "Arequipa","Apurímac","Ayacucho","Cusco","Huancavelica",
            "Madre de Dios","Moquegua","Puno","Tacna"
    );
    private static final Set<String> NORTE = Set.of(
            "Amazonas","Áncash","Cajamarca","La Libertad","Lambayeque",
            "Piura","Tumbes","San Martín"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // 1) Cabecera
        titleText = findViewById(R.id.title_text);
        titleText.setText("Nuevo Post");
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // 2) Formulario
        btnPickImages    = findViewById(R.id.btnPickImages);
        tvImageCount     = findViewById(R.id.tvImageCount);
        btnSave          = findViewById(R.id.btnSavePost);
        etTitle          = findViewById(R.id.etTitle);
        etDescription    = findViewById(R.id.etDescription);
        etPrice          = findViewById(R.id.etPrice);
        spinnerRegion    = findViewById(R.id.spinnerRegion);
        spinnerCategory  = findViewById(R.id.spinnerCategory);
        spinnerSchedules = findViewById(R.id.spinnerSchedules);
        layoutLocations  = findViewById(R.id.layoutLocations);
        progressBar      = findViewById(R.id.progressBar);

        // Imágenes
        updateImageButtonText();
        btnPickImages.setOnClickListener(v -> pickImagesFromGallery());
        btnSave.setOnClickListener(v -> savePost());

        // Spinner Región
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Todas","Sur","Norte","Personalizado"}
        );
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRegion.setAdapter(regionAdapter);

        // Spinner Categoría
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Comida","Tecnología","Arte","Otro"}
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // Spinner Horarios
        ArrayAdapter<String> schedAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Variable","Lunes a Viernes","Fin de Semana","Todos los Días","Personalizado"}
        );
        schedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSchedules.setAdapter(schedAdapter);

        // Inicializar checkboxes de departamentos (siempre listos)
        for (String dept : DEPARTAMENTOS) {
            CheckBox cb = new CheckBox(this);
            cb.setText(dept);
            layoutLocations.addView(cb);
        }
        layoutLocations.setVisibility(View.GONE);

        // Listener región
        spinnerRegion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String sel = (String) parent.getItemAtPosition(pos);
                switch (sel) {
                    case "Todas":
                        selectAllLocations();
                        layoutLocations.setVisibility(View.GONE);
                        break;
                    case "Sur":
                        selectRegionLocations(SUR);
                        layoutLocations.setVisibility(View.GONE);
                        break;
                    case "Norte":
                        selectRegionLocations(NORTE);
                        layoutLocations.setVisibility(View.GONE);
                        break;
                    case "Personalizado":
                        clearAllLocations();
                        layoutLocations.setVisibility(View.VISIBLE);
                        break;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        // 3) Bottom navigation
        btnAccount = findViewById(R.id.btnAccount);
        btnNewPost = findViewById(R.id.btnNewPost);
        btnChats   = findViewById(R.id.btnChats);

        btnAccount .setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
        btnNewPost .setOnClickListener(v -> startActivity(new Intent(this, CreatePostActivity.class)));
        btnChats   .setOnClickListener(v -> startActivity(new Intent(this, ChatHistoryActivity.class)));

        // 4) Modo edición
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
                    Toast.makeText(this, "Error cargando post: "+e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }

    private void selectAllLocations() {
        for (int i = 0; i < layoutLocations.getChildCount(); i++) {
            ((CheckBox) layoutLocations.getChildAt(i)).setChecked(true);
        }
    }

    private void clearAllLocations() {
        for (int i = 0; i < layoutLocations.getChildCount(); i++) {
            ((CheckBox) layoutLocations.getChildAt(i)).setChecked(false);
        }
    }

    private void selectRegionLocations(Set<String> regionSet) {
        for (int i = 0; i < layoutLocations.getChildCount(); i++) {
            CheckBox cb = (CheckBox) layoutLocations.getChildAt(i);
            cb.setChecked(regionSet.contains(cb.getText().toString()));
        }
    }

    private void updateImageButtonText() {
        int count = imageUris.size();
        tvImageCount.setText("Seleccionar Imágenes (" + count + "/5)");
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
        if (requestCode == PICK_IMAGES_REQUEST &&
                resultCode == Activity.RESULT_OK && data != null) {

            imageUris.clear();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < Math.min(count, MAX_IMAGES); i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }
                if (count > MAX_IMAGES) {
                    Toast.makeText(this,
                            "Máximo " + MAX_IMAGES + " imágenes permitidas",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }
            updateImageButtonText();
            Toast.makeText(this,
                    imageUris.size() + " imágenes seleccionadas",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void populateForEdit(Post post) {
        etTitle.setText(post.getTitle());
        etDescription.setText(post.getDescription());
        etPrice.setText(String.valueOf(post.getPrice()));

        // Región
        String prevReg = post.getLocations().isEmpty()
                ? "Personalizado"
                : determineRegionFor(post.getLocations());
        int rpos = ((ArrayAdapter<String>) spinnerRegion.getAdapter())
                .getPosition(prevReg);
        spinnerRegion.setSelection(rpos);

        // Categoría
        int cpos = ((ArrayAdapter<String>) spinnerCategory.getAdapter())
                .getPosition(post.getCategory());
        spinnerCategory.setSelection(cpos);

        // Horarios
        String prevSched = post.getSchedules().isEmpty()
                ? "Variable" : post.getSchedules().get(0);
        int spos = ((ArrayAdapter<String>) spinnerSchedules.getAdapter())
                .getPosition(prevSched);
        spinnerSchedules.setSelection(spos);

        // Ubicaciones
        layoutLocations.setVisibility(prevReg.equals("Personalizado")
                ? View.VISIBLE : View.GONE);
        selectRegionLocations(new HashSet<>(post.getLocations()));

        // Imágenes existentes
        if (post.getImages() != null) {
            parseFiles.addAll(post.getImages());
            updateImageButtonText();
        }
    }

    private String determineRegionFor(List<String> locs) {
        Set<String> set = new HashSet<>(locs);
        if (set.containsAll(Arrays.asList(DEPARTAMENTOS))) return "Todas";
        if (SUR.containsAll(set)) return "Sur";
        if (NORTE.containsAll(set)) return "Norte";
        return "Personalizado";
    }

    private void savePost() {
        if (etTitle.getText().toString().isEmpty()) {
            etTitle.setError("Título requerido");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

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

        Post post = postToEdit != null ? postToEdit : new Post();
        if (postToEdit == null) {
            post.setUser(ParseUser.getCurrentUser());
            post.setRating(0);
        }
        post.setImages(parseFiles);
        post.setTitle(etTitle.getText().toString());
        post.setDescription(etDescription.getText().toString());
        post.setPrice(Double.parseDouble(etPrice.getText().toString()));
        post.setSchedules(List.of(spinnerSchedules.getSelectedItem().toString()));
        post.setCategory(spinnerCategory.getSelectedItem().toString());

        // Ubicaciones según selección
        String region = (String) spinnerRegion.getSelectedItem();
        List<String> locs = new ArrayList<>();
        switch (region) {
            case "Todas":
                locs.addAll(Arrays.asList(DEPARTAMENTOS));
                break;
            case "Sur":
                locs.addAll(SUR);
                break;
            case "Norte":
                locs.addAll(NORTE);
                break;
            case "Personalizado":
                for (int i = 0; i < layoutLocations.getChildCount(); i++) {
                    CheckBox cb = (CheckBox) layoutLocations.getChildAt(i);
                    if (cb.isChecked()) locs.add(cb.getText().toString());
                }
                break;
        }
        post.setLocations(locs);

        post.saveInBackground((SaveCallback) e -> {
            progressBar.setVisibility(View.GONE);
            if (e == null) {
                Toast.makeText(
                        this,
                        postToEdit != null ? "Post actualizado" : "Post creado",
                        Toast.LENGTH_SHORT
                ).show();
                finish();
            } else {
                Toast.makeText(
                        this,
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}