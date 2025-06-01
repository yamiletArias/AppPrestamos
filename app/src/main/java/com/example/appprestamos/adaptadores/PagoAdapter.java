package com.example.appprestamos.adaptadores;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appprestamos.R;
import com.example.appprestamos.entidades.Pago;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PagoAdapter extends RecyclerView.Adapter<PagoAdapter.PagoViewHolder> {

    private Context context;
    private List<Pago> listaPagos;
    private int idContrato;

    public PagoAdapter(Context context, List<Pago> listaPagos, int idContrato) {
        this.context = context;
        this.listaPagos = listaPagos;
        this.idContrato = idContrato;
    }

    @NonNull
    @Override
    public PagoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pago, parent, false);
        return new PagoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagoViewHolder holder, int position) {
        Pago pago = listaPagos.get(position);

        holder.txtCuota.setText("Cuota #" + pago.numCuota);
        holder.txtMonto.setText("S/ " + String.format("%.2f", pago.monto));

        if (pago.pagado) {
            holder.btnPagar.setEnabled(false);
            holder.btnPagar.setText("Pagado");
        } else {
            holder.btnPagar.setEnabled(true);
            holder.btnPagar.setText("Pagar");

            final int pos = position;
            holder.btnPagar.setOnClickListener(v -> {
                // Inflar diálogo
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.dialog_pagar, null);

                TextView tvNumCuotaDialog   = dialogView.findViewById(R.id.tvNumCuotaDialog);
                TextView tvMontoDialog      = dialogView.findViewById(R.id.tvMontoDialog);
                TextView tvFechaDialog      = dialogView.findViewById(R.id.tvFechaDialog);
                TextView tvPenalidadDialog  = dialogView.findViewById(R.id.tvPenalidadDialog);
                RadioGroup rgMedio          = dialogView.findViewById(R.id.rgMedio);
                RadioButton rbEfectivo      = dialogView.findViewById(R.id.rbEfectivo);
                RadioButton rbDeposito      = dialogView.findViewById(R.id.rbDeposito);
                Button btnConfirmar         = dialogView.findViewById(R.id.btnConfirmarPagoDialog);

                // 1) Llenar datos básicos
                tvNumCuotaDialog.setText("Cuota #" + pago.numCuota);
                tvMontoDialog.setText("Monto: S/ " + String.format("%.2f", pago.monto));

                // 2) Fecha de hoy
                LocalDate fechaHoy = LocalDate.now();
                String fechaHoyStr = fechaHoy.format(DateTimeFormatter.ISO_DATE);
                tvFechaDialog.setText("Fecha: " + fechaHoyStr);

                // 3) Calcular penalidad: si hoy > fechaEstim → 10% del monto; sino 0
                double penalidadCalculadaTemp = 0.0;
                try {
                    LocalDate fechaVenc = LocalDate.parse(pago.fechaEstim, DateTimeFormatter.ISO_DATE);
                    if (fechaHoy.isAfter(fechaVenc)) {
                        penalidadCalculadaTemp = pago.monto * 0.10; // 10 %
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // Convertimos a final para poder usarlo más adelante
                final double penalidadCalculada = penalidadCalculadaTemp;

                tvPenalidadDialog.setText("Penalidad: S/ " + String.format("%.2f", penalidadCalculada));

                // 4) Mostrar el AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(dialogView);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

                // 5) Confirmar pago
                btnConfirmar.setOnClickListener(dialogV -> {
                    int seleccionado = rgMedio.getCheckedRadioButtonId();
                    if (seleccionado == -1) {
                        Toast.makeText(context, "Elige Efectivo o Depósito", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String medioElegido = rbEfectivo.isChecked() ? "EFC" : "DEP";

                    // Hacer POST a /api/pagos/pagar
                    new Thread(() -> {
                        HttpURLConnection conn = null;
                        try {
                            URL url = new URL("http://192.168.18.87:3000/api/pagos/pagar");
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                            conn.setDoOutput(true);

                            JSONObject body = new JSONObject();
                            body.put("idContrato", idContrato);
                            body.put("numCuota", pago.numCuota);
                            body.put("medio", medioElegido);
                            // Aquí uso directamente la variable final penalidadCalculada
                            body.put("penalidad", penalidadCalculada);

                            byte[] out = body.toString().getBytes("UTF-8");
                            conn.getOutputStream().write(out);

                            int responseCode = conn.getResponseCode();
                            conn.disconnect();

                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                // Marcar como pagado localmente
                                pago.pagado = true;
                                ((AppCompatActivity) context).runOnUiThread(() -> {
                                    notifyItemChanged(pos);
                                    alertDialog.dismiss();
                                });
                            } else {
                                ((AppCompatActivity) context).runOnUiThread(() -> {
                                    Toast.makeText(context,
                                            "Error al pagar cuota. Código: " + responseCode,
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            ((AppCompatActivity) context).runOnUiThread(() -> {
                                Toast.makeText(context,
                                        "Exception: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                        } finally {
                            if (conn != null) {
                                conn.disconnect();
                            }
                        }
                    }).start();
                });
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaPagos.size();
    }

    static class PagoViewHolder extends RecyclerView.ViewHolder {
        TextView txtCuota, txtMonto;
        Button btnPagar;

        public PagoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCuota = itemView.findViewById(R.id.txtCuota);
            txtMonto  = itemView.findViewById(R.id.txtMonto);
            btnPagar  = itemView.findViewById(R.id.btnPagar);
        }
    }
}
