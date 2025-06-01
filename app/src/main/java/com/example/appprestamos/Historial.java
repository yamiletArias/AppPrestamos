// src/main/java/com/example/appprestamos/HistorialResumen.java
package com.example.appprestamos;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appprestamos.adaptadores.PrestamoResumenAdapter;
import com.example.appprestamos.entidades.PrestamoResumen;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Historial extends AppCompatActivity {

    private RecyclerView recyclerResumen;
    private ArrayList<PrestamoResumen> listaResumen;
    private PrestamoResumenAdapter adapter;

    private int idBeneficiario;
    private static final String BASE_URL = "http://192.168.18.87:3000/api/beneficiarios/historial/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        idBeneficiario = getIntent().getIntExtra("idBeneficiario", -1);
        if (idBeneficiario == -1) {
            Toast.makeText(this, "Falta el id del beneficiario", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerResumen = findViewById(R.id.recyclerResumen);
        recyclerResumen.setLayoutManager(new LinearLayoutManager(this));

        listaResumen = new ArrayList<>();
        adapter = new PrestamoResumenAdapter(this, listaResumen);
        recyclerResumen.setAdapter(adapter);

        cargarResumen();
    }

    private void cargarResumen() {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + idBeneficiario);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int code = connection.getResponseCode();
                if (code == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    int totalPrestamos = json.getInt("totalPrestamos");

                    JSONObject c = json.getJSONObject("contrato");
                    int idContrato    = c.getInt("idcontrato");
                    double monto      = c.getDouble("monto");
                    double interes    = c.getDouble("interes");
                    String fechaInicio= c.getString("fechainicio");
                    int numCuotas     = c.getInt("numcuotas");
                    int cuotasPagadas = c.getInt("cuotasPagadas");

                    listaResumen.clear();
                    listaResumen.add(new PrestamoResumen(
                            totalPrestamos,
                            idContrato,
                            monto,
                            interes,
                            fechaInicio,
                            numCuotas,
                            cuotasPagadas
                    ));

                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "No se encontró historial para este beneficiario", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error HTTP: " + code, Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }
}
