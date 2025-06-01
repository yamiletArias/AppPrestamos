package com.example.appprestamos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class VistaBotones extends AppCompatActivity {

    private Button btnPagar, btnPagosRealizados, btnPagosPendientes, btnHistorial;
    private int idContrato;
    private int idBeneficiario; // ← Declaramos esta variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vista_botones);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            return insets;
        });

        // 1) Leemos ambos extras desde el Intent: idContrato e idBeneficiario
        idContrato     = getIntent().getIntExtra("idContrato", -1);
        idBeneficiario = getIntent().getIntExtra("idBeneficiario", -1);

        if (idContrato == -1 || idBeneficiario == -1) {
            Toast.makeText(this, "Faltan datos (idContrato o idBeneficiario)", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUI();

        // 2) Listeners para cada botón: reenviamos los extras necesarios
        btnPagar.setOnClickListener(v -> {
            Intent intent = new Intent(VistaBotones.this, Pagar.class);
            intent.putExtra("idContrato", idContrato);
            startActivity(intent);
        });

        btnPagosRealizados.setOnClickListener(v -> {
            Intent intent = new Intent(VistaBotones.this, PagosRealizado.class);
            intent.putExtra("idContrato", idContrato);
            startActivity(intent);
        });

        btnPagosPendientes.setOnClickListener(v -> {
            Intent intent = new Intent(VistaBotones.this, PagosPendientes.class);
            intent.putExtra("idContrato", idContrato);
            startActivity(intent);
        });

        btnHistorial.setOnClickListener(v -> {
            // 3) Enviamos ahora correctamente el idBeneficiario
            Intent intent = new Intent(VistaBotones.this, Historial.class);
            intent.putExtra("idBeneficiario", idBeneficiario);
            startActivity(intent);
        });
    }

    private void loadUI() {
        btnPagar           = findViewById(R.id.btnPagar);
        btnPagosRealizados = findViewById(R.id.btnPagosRealizados);
        btnPagosPendientes = findViewById(R.id.btnPagosPendientes);
        btnHistorial       = findViewById(R.id.btnHistorial);
    }
}
