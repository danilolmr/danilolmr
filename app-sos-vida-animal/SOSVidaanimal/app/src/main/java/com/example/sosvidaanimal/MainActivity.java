package com.example.sosvidaanimal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int MAP_REQUEST_CODE = 2;

    private EditText etDetalhes, edtDate, edtTime;
    private Button btnReport, btnSelectImage, btnSelectLocation, buttonSaibaMais;
    private ImageView imageView, imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setListeners();
    }

    private void initViews() {
        etDetalhes = findViewById(R.id.etDetalhes);
        edtDate = findViewById(R.id.edtDate);
        edtTime = findViewById(R.id.edtTime);
        btnReport = findViewById(R.id.btnReport);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSelectLocation = findViewById(R.id.btnSelectLocation);
        imageView = findViewById(R.id.imageView);
        buttonSaibaMais = findViewById(R.id.buttonSaibaMais);

        imageView2 = findViewById(R.id.imageView2);

        // Adicionando TextWatcher para formatação automática
        edtDate.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private final String ddmmyyyy = "DDMMYYYY";
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdating) {
                    String clean = s.toString().replaceAll("[^\\d]", "");
                    int cl = clean.length();

                    if (cl < 8) {
                        clean = clean + ddmmyyyy.substring(cl);
                    } else {
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int month = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        if (month > 12) month = 12;
                        day = Math.min(day, 31);
                        clean = String.format("%02d%02d%04d", day, month, year);
                    }

                    // Formatando a data
                    String formatted = String.format("%s/%s/%s", clean.substring(0, 2), clean.substring(2, 4), clean.substring(4, 8));

                    isUpdating = true;  // Previne chamada recursiva
                    edtDate.setText(formatted);
                    edtDate.setSelection(Math.min(formatted.length(), cl + (cl > 2 ? 1 : 0) + (cl > 4 ? 1 : 0))); // Atualiza o cursor
                    isUpdating = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        edtTime.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private final String hhmm = "HHMM";
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdating) {
                    String clean = s.toString().replaceAll("[^\\d]", "");
                    int cl = clean.length();

                    if (cl < 4) {
                        clean = clean + hhmm.substring(cl);
                    } else {
                        int hour = Integer.parseInt(clean.substring(0, 2));
                        int min = Integer.parseInt(clean.substring(2, 4));

                        if (hour > 23) hour = 23;
                        if (min > 59) min = 59;
                        clean = String.format("%02d%02d", hour, min);
                    }

                    // Formatando a hora
                    String formatted = String.format("%s:%s", clean.substring(0, 2), clean.substring(2, 4));

                    isUpdating = true;  // Previne chamada recursiva
                    edtTime.setText(formatted);
                    edtTime.setSelection(Math.min(formatted.length(), cl + (cl > 2 ? 1 : 0))); // Atualiza o cursor
                    isUpdating = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setListeners() {
        imageView2.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DadosActivity.class);
            startActivity(intent);
        });

        btnSelectImage.setOnClickListener(v -> openImageChooser());
        btnSelectLocation.setOnClickListener(v -> openMap());
        btnReport.setOnClickListener(v -> handleReport());
        buttonSaibaMais.setOnClickListener(v -> {Intent intent = new Intent(MainActivity.this, SaibaMaisActivity.class);
            startActivity(intent);
        });
    }



    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecionar Imagem"), PICK_IMAGE_REQUEST);
    }

    private void openMap() {
        Intent intent = new Intent(MainActivity.this, MapSelectionActivity.class);
        startActivityForResult(intent, MAP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            imageView.setImageURI(imageUri);
            imageView.setVisibility(View.VISIBLE);
        } else if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            Toast.makeText(this, "Localização selecionada: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleReport() {
        String detalhes = etDetalhes.getText().toString().trim();
        String date = edtDate.getText().toString().trim();
        String time = edtTime.getText().toString().trim();

        if (TextUtils.isEmpty(detalhes)) {
            etDetalhes.setError("Detalhes são obrigatórios");
            return;
        }

        if (TextUtils.isEmpty(date)) {
            edtDate.setError("Data é obrigatória");
            return;
        }

        if (TextUtils.isEmpty(time)) {
            edtTime.setError("Hora é obrigatória");
            return;
        }

        Toast.makeText(this, "Denúncia enviado com sucesso!", Toast.LENGTH_SHORT).show();

        saveReportData(detalhes, date, time);
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveReportData(String detalhes, String date, String time) {
        SharedPreferences sharedPreferences = getSharedPreferences("ReportData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("detalhes", detalhes);
        editor.putString("date", date);
        editor.putString("time", time);
        editor.apply();
    }
}
