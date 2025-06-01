package com.example.appprestamos;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appprestamos.adaptadores.PagoAdapter;
import com.example.appprestamos.entidades.Pago;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Pagar extends AppCompatActivity {

    private RecyclerView recyclerPagos;
    private PagoAdapter adapter;
    private List<Pago> listaPagos;
    private int idContrato;

    private static final String BASE_URL = "http://192.168.18.87:3000/api/pagos/contrato/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagar);

        // 1) Obtener idContrato que vino por Intent
        idContrato = getIntent().getIntExtra("idContrato", -1);
        if (idContrato == -1) {
            Toast.makeText(this, "No se recibió el idContrato", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2) Configurar RecyclerView
        recyclerPagos = findViewById(R.id.recyclerPagos);
        recyclerPagos.setLayoutManager(new LinearLayoutManager(this));

        // 3) Inicializar lista y adapter
        listaPagos = new ArrayList<>();
        adapter = new PagoAdapter(this, listaPagos, idContrato);
        recyclerPagos.setAdapter(adapter);

        // 4) Llamar al método que hace GET para llenar la lista
        cargarPagos();
    }

    private void cargarPagos() {
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

                    // 5) Parsear el arreglo JSON
                    JSONArray jsonArray = new JSONArray(sb.toString());
                    listaPagos.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject pagoJson = jsonArray.getJSONObject(i);
                        int numCuota   = pagoJson.getInt("numcuota");
                        double monto   = pagoJson.getDouble("monto");
                        boolean pagado = !pagoJson.isNull("fechapago");

                        // Nuevo: leer fechaestimada (string formato "YYYY-MM-DD")
                        String fechaEstim = pagoJson.getString("fechaestimada");

                        // Nuevo: leer penalidad que ya venga de la BD (por defecto es 0)
                        double penalidadBD = pagoJson.getDouble("penalidad");

                        listaPagos.add(new Pago(numCuota, monto, pagado, fechaEstim, penalidadBD));
                    }

                    // 6) Notificar al adapter en el hilo de UI
                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } else {
                    runOnUiThread(() -> Toast.makeText(Pagar.this,
                            "Error al cargar pagos. Código: " + responseCode,
                            Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(Pagar.this,
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
