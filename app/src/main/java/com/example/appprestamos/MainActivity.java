package com.example.appprestamos;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText edtDni;
    private TextView tvBienvenida;
    private Button btnEntrar;

    private static final String BASE_URL = "http://192.168.18.87:3000/api/beneficiarios/dni/";
    private int idBeneficiarioObtenido = -1;
    private int idContratoAsociado   = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadUI();
        setupListeners();
    }

    /**
     * Obtiene referencias a las Views definidas en activity_main.xml:
     *  - edtDni       (EditText para ingresar el DNI)
     *  - tvBienvenida (TextView para mostrar el saludo)
     *  - btnEntrar    (Button que abre MenuActivity; inicialmente deshabilitado)
     */
    private void loadUI() {
        edtDni       = findViewById(R.id.edtDni);
        tvBienvenida = findViewById(R.id.tvBienvenida);
        btnEntrar    = findViewById(R.id.btnEntrar);
    }

    /**
     * Configura:
     * 1) un OnEditorActionListener en edtDni para detectar cuando el usuario pulsa “Done” en el teclado.
     * 2) un OnClickListener en btnEntrar para abrir MenuActivity.
     */
    private void setupListeners() {
        // 1) Escucha la acción “Done” (IME_ACTION_DONE) en el EditText edtDni
        edtDni.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String dniIngresado = edtDni.getText().toString().trim();
                if (dniIngresado.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Por favor, ingresa un DNI", Toast.LENGTH_SHORT).show();
                } else {
                    consultarBeneficiarioPorDni(dniIngresado);
                }
                return true;
            }
            return false;
        });

        // 2) Al pulsar “Entrar”, abrimos MenuActivity
        btnEntrar.setOnClickListener(v -> {
            if (idContratoAsociado == -1) {
                Toast.makeText(MainActivity.this,
                        "Aún no se obtuvo contrato asociado",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, VistaBotones.class);
            intent.putExtra("idContrato", idContratoAsociado);
            intent.putExtra("idBeneficiario", idBeneficiarioObtenido);
            startActivity(intent);
        });
    }

    /**
     * Realiza la consulta HTTP GET a /api/beneficiarios/dni/{dni}.
     * Si existe el beneficiario, muestra “Bienvenido <nombres> <apellidos>” y habilita el botón Entrar.
     * Si no existe, muestra un Toast con mensaje de error.
     *
     * Esta operación se ejecuta en un Thread separado para no bloquear el hilo principal (UI Thread).
     */
    private void consultarBeneficiarioPorDni(String dni) {
        runOnUiThread(() -> {
            tvBienvenida.setVisibility(TextView.GONE);
            btnEntrar.setEnabled(false);
        });

        new Thread(() -> {
            HttpURLConnection conn1 = null;
            try {
                // 1) Obtener idbeneficiario, nombres y apellidos
                URL url1 = new URL(BASE_URL + dni); // /api/beneficiarios/dni/{dni}
                conn1 = (HttpURLConnection) url1.openConnection();
                conn1.setRequestMethod("GET");
                conn1.setConnectTimeout(5000);
                conn1.setReadTimeout(5000);

                int code1 = conn1.getResponseCode();
                if (code1 == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader1 = new BufferedReader(
                            new InputStreamReader(conn1.getInputStream()));
                    StringBuilder sb1 = new StringBuilder();
                    String line;
                    while ((line = reader1.readLine()) != null) {
                        sb1.append(line);
                    }
                    reader1.close();
                    JSONObject json1 = new JSONObject(sb1.toString());

                    String nombres   = json1.getString("nombres");
                    String apellidos = json1.getString("apellidos");
                    idBeneficiarioObtenido = json1.getInt("idbeneficiario");

                    // 2) Ahora pedimos el contrato activo de ese beneficiario
                    URL url2 = new URL("http://192.168.18.87:3000/api/contratos/activo/" + idBeneficiarioObtenido);
                    HttpURLConnection conn2 = (HttpURLConnection) url2.openConnection();
                    conn2.setRequestMethod("GET");
                    conn2.setConnectTimeout(5000);
                    conn2.setReadTimeout(5000);

                    int code2 = conn2.getResponseCode();
                    if (code2 == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader2 = new BufferedReader(
                                new InputStreamReader(conn2.getInputStream()));
                        StringBuilder sb2 = new StringBuilder();
                        while ((line = reader2.readLine()) != null) {
                            sb2.append(line);
                        }
                        reader2.close();
                        JSONObject json2 = new JSONObject(sb2.toString());
                        idContratoAsociado = json2.getInt("idcontrato");

                        // Finalmente, actualizamos la UI
                        runOnUiThread(() -> {
                            tvBienvenida.setText("Bienvenido\n" + nombres + " " + apellidos);
                            tvBienvenida.setVisibility(TextView.VISIBLE);
                            btnEntrar.setEnabled(true);
                        });
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "No tiene contrato activo (código " + code2 + ")",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                    conn2.disconnect();

                } else if (code1 == HttpURLConnection.HTTP_NOT_FOUND) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                "No se encontró beneficiario con DNI " + dni,
                                Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                "Error del servidor (código " + code1 + ")",
                                Toast.LENGTH_SHORT).show();
                    });
                }
                conn1.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Error al conectar: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (conn1 != null) {
                    conn1.disconnect();
                }
            }
        }).start();
    }
}
