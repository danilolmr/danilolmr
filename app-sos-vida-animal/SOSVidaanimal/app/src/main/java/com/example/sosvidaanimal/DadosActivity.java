package com.example.sosvidaanimal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;

public class DadosActivity extends AppCompatActivity {

    private TextView tvDetalhes, tvDate, tvTime;
    private Button btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dados);

        // Inicializa as views
        tvDetalhes = findViewById(R.id.tvDetalhes);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Recupera dados do SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("ReportData", MODE_PRIVATE);
        String detalhes = sharedPreferences.getString("detalhes", "Detalhes não disponíveis");
        String date = sharedPreferences.getString("date", "Data não disponível");
        String time = sharedPreferences.getString("time", "Hora não disponível");

        // Exibe os dados
        tvDetalhes.setText(detalhes);
        tvDate.setText(date);
        tvTime.setText(time);

        // Define a ação do botão "Voltar"
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DadosActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
