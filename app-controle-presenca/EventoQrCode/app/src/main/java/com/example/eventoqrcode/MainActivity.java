package com.example.eventoqrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.util.Map;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private EditText editTextFullName, editTextRGM, editTextConfirmRGM;
    private Button buttonGenerateQR;
    private ImageView imageViewQRCode;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextFullName = findViewById(R.id.editText_fullName);
        editTextRGM = findViewById(R.id.editText_rgm);
        editTextConfirmRGM = findViewById(R.id.editText_confirmRgm); // Campo para confirmar o RGM
        buttonGenerateQR = findViewById(R.id.button_generateQR);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        imageViewQRCode = findViewById(R.id.imageView_qrCode);

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            verificarEExibirQRCode(userId); // Chama a função para verificar e exibir o QR Code do usuário
        }

        buttonGenerateQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = editTextFullName.getText().toString().trim();
                String rgm = editTextRGM.getText().toString().trim();
                String confirmRgm = editTextConfirmRGM.getText().toString().trim();

                if (fullName.isEmpty()) {
                    editTextFullName.setError("Por favor, insira o nome completo");
                    return;
                }
                if (rgm.isEmpty()) {
                    editTextRGM.setError("Por favor, insira o RGM");
                    return;
                }
                if (!rgm.equals(confirmRgm)) {
                    editTextConfirmRGM.setError("O RGM não corresponde");
                    return;
                }

                if (user != null) {
                    verificarERegistrarPresenca(fullName, rgm);
                } else {
                    Toast.makeText(MainActivity.this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void generateQRCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
            for (int x = 0; x < 512; x++) {
                for (int y = 0; y < 512; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            imageViewQRCode.setImageBitmap(bitmap);
            imageViewQRCode.setVisibility(View.VISIBLE);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void verificarERegistrarPresenca(String nome, String rgm) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("presencas").document(rgm).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Toast.makeText(MainActivity.this, "RGM já cadastrado. Escolha um RGM diferente.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Adiciona os campos "status" e "dataHora"
                        Map<String, Object> presenca = new HashMap<>();
                        presenca.put("id_usuario", userId);
                        presenca.put("nome", nome);
                        presenca.put("rgm", rgm);
                        presenca.put("status", "pendente");
                        presenca.put("dataHora", FieldValue.serverTimestamp());

                        db.collection("presencas").document(rgm)
                                .set(presenca)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MainActivity.this, "Presença registrada com sucesso", Toast.LENGTH_SHORT).show();
                                    generateQRCode(rgm);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Erro ao registrar presença: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Erro ao verificar RGM: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void verificarEExibirQRCode(String userId) {
        db.collection("presencas").whereEqualTo("id_usuario", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String rgm = document.getString("rgm");

                        if (rgm != null) {
                            generateQRCode(rgm);
                        }
                    } else {
                        Toast.makeText(this, "QR Code não encontrado para o usuário.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao verificar QR Code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}