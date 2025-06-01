package com.example.appprestamos;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appprestamos.adaptadores.PagoRealizadoAdapter;
import com.example.appprestamos.entidades.PagoRealizado;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PagosRealizado extends AppCompatActivity {

    private RecyclerView recyclerPagosRealizados;
    private PagoRealizadoAdapter adapter;
    private List<PagoRealizado> listaPagosRealizados;
    private TextView tvTotalPagos;

    private int idContrato;
    private static final String BASE_URL = "http://192.168.18.87:3000/api/pagos/contrato/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago_realizado);

        // 1) Obtener idContrato del Intent
        idContrato = getIntent().getIntExtra("idContrato", -1);
        if (idContrato == -1) {
            Toast.makeText(this, "No se recibió el idContrato", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2) Referencias a vistas
        recyclerPagosRealizados = findViewById(R.id.recyclerPagosRealizados);
        tvTotalPagos = findViewById(R.id.tvTotalPagos);

        recyclerPagosRealizados.setLayoutManager(new LinearLayoutManager(this));
        listaPagosRealizados = new ArrayList<>();
        adapter = new PagoRealizadoAdapter(this, listaPagosRealizados);
        recyclerPagosRealizados.setAdapter(adapter);

        // 3) Cargar datos desde el backend
        cargarPagosRealizados();
    }

    private void cargarPagosRealizados() {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + idContrato);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONArray jsonArray = new JSONArray(sb.toString());
                    listaPagosRealizados.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject pagoJson = jsonArray.getJSONObject(i);
                        if (!pagoJson.isNull("fechapago")) {
                            int numCuota = pagoJson.getInt("numcuota");
                            String fechaPago = pagoJson.getString("fechapago");
                            double monto = pagoJson.getDouble("monto");
                            listaPagosRealizados.add(new PagoRealizado(numCuota, fechaPago, monto));
                        }
                    }

                    // 4) Calcular la suma total de montos
                    double sumaTotal = 0.0;
                    for (PagoRealizado pr : listaPagosRealizados) {
                        sumaTotal += pr.monto;
                    }
                    String textoTotal = String.format("Monto total pagado: S/ %.2f", sumaTotal);

                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        tvTotalPagos.setText(textoTotal);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(PagosRealizado.this,
                            "Error al cargar pagos. Código: " + responseCode,
                            Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(PagosRealizado.this,
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
