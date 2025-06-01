package com.example.appprestamos;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appprestamos.adaptadores.PagoPendienteAdapter;
import com.example.appprestamos.entidades.PagoPendiente;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PagosPendientes extends AppCompatActivity {

    private RecyclerView recyclerPendientes;
    private TextView tvTotalPendiente;
    private List<PagoPendiente> listaPendientes;
    private PagoPendienteAdapter adapter;

    private int idContrato;
    private static final String BASE_URL = "http://192.168.18.87:3000/api/pagos/contrato/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pago_pendientes);

        idContrato = getIntent().getIntExtra("idContrato", -1);
        if (idContrato == -1) {
            Toast.makeText(this, "No se recibió el idContrato", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerPendientes = findViewById(R.id.recyclerPagosPendientes);
        tvTotalPendiente = findViewById(R.id.tvTotalPendiente);
        recyclerPendientes.setLayoutManager(new LinearLayoutManager(this));

        listaPendientes = new ArrayList<>();
        adapter = new PagoPendienteAdapter(this, listaPendientes);
        recyclerPendientes.setAdapter(adapter);

        cargarPagosPendientes();
    }

    private void cargarPagosPendientes() {
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
                    listaPendientes.clear();
                    double total = 0.0;

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        if (obj.isNull("fechapago")) {
                            int numCuota = obj.getInt("numcuota");
                            String fechaVenc = obj.getString("fechaestimada"); // ← cambiar aquí
                            double monto = obj.getDouble("monto");
                            total += monto;
                            listaPendientes.add(new PagoPendiente(numCuota, fechaVenc, monto));
                        }
                    }

                    double finalTotal = total;
                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        tvTotalPendiente.setText(String.format("Monto total pendiente: S/ %.2f", finalTotal));
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(PagosPendientes.this,
                            "Error código: " + responseCode,
                            Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(PagosPendientes.this,
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }
}
