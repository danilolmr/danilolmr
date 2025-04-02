package com.example.eventoqrcode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeReader;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ScannerActivity extends AppCompatActivity {

    private static final int SELECT_PHOTO_REQUEST_CODE = 100;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        db = FirebaseFirestore.getInstance();

        Button buttonOpenScanner = findViewById(R.id.buttonOpenScanner);
        buttonOpenScanner.setOnClickListener(v -> startQRScanner());

        Button buttonSelectPhoto = findViewById(R.id.buttonSelectPhoto);
        buttonSelectPhoto.setOnClickListener(v -> openGallery());
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escaneie o QR Code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PHOTO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                    scanQRCodeFromBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null && result.getContents() != null) {
                String qrContent = result.getContents();
                Toast.makeText(this, "QR Code escaneado: " + qrContent, Toast.LENGTH_LONG).show();
                validarPresencaNoFirebase(qrContent);
            } else {
                Toast.makeText(this, "Escaneamento cancelado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void scanQRCodeFromBitmap(Bitmap bitmap) {
        try {
            QRCodeReader reader = new QRCodeReader();
            int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            com.google.zxing.RGBLuminanceSource source = new com.google.zxing.RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = reader.decode(binaryBitmap);
            String qrContent = result.getText();
            Toast.makeText(this, "QR Code: " + qrContent, Toast.LENGTH_LONG).show();
            validarPresencaNoFirebase(qrContent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao escanear QR Code da imagem", Toast.LENGTH_SHORT).show();
        }
    }

    private void validarPresencaNoFirebase(String rgm) {
        DocumentReference documentRef = db.collection("presencas").document(rgm);
        documentRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Atualizar o status para "aprovado"
                Map<String, Object> updateStatus = new HashMap<>();
                updateStatus.put("status", "aprovado");
                documentRef.update(updateStatus).addOnSuccessListener(aVoid -> {
                    Toast.makeText(ScannerActivity.this, "Presença validada com sucesso!", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(ScannerActivity.this, "Erro ao atualizar presença: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(ScannerActivity.this, "Presença não encontrada!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(ScannerActivity.this, "Erro ao verificar presença: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
