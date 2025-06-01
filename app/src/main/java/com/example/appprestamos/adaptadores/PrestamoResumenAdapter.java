// src/main/java/com/example/appprestamos/adaptadores/PrestamoResumenAdapter.java
package com.example.appprestamos.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appprestamos.R;
import com.example.appprestamos.entidades.PrestamoResumen;

import java.util.List;

public class PrestamoResumenAdapter extends RecyclerView.Adapter<PrestamoResumenAdapter.ViewHolder> {

    private Context context;
    private List<PrestamoResumen> listaResumen;  // Lista con un solo elemento

    public PrestamoResumenAdapter(Context context, List<PrestamoResumen> listaResumen) {
        this.context = context;
        this.listaResumen = listaResumen;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTotalPrestamos, tvMonto, tvInteres, tvFechaInicio, tvNumCuotas, tvCuotasPagadas;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTotalPrestamos     = itemView.findViewById(R.id.tvTotalPrestamos);
            tvMonto              = itemView.findViewById(R.id.tvMontoContrato);
            tvInteres            = itemView.findViewById(R.id.tvInteresContrato);
            tvFechaInicio        = itemView.findViewById(R.id.tvFechaInicioContrato);
            tvNumCuotas          = itemView.findViewById(R.id.tvNumCuotasContrato);
            tvCuotasPagadas      = itemView.findViewById(R.id.tvCuotasPagadas);
        }
    }

    @Override
    public PrestamoResumenAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_historial_resumen, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PrestamoResumenAdapter.ViewHolder holder, int position) {
        PrestamoResumen resumen = listaResumen.get(position);

        holder.tvTotalPrestamos.setText("Total de préstamos: " + resumen.getTotalPrestamos());
        holder.tvMonto.setText(String.format("Monto: S/ %.2f", resumen.getMonto()));
        holder.tvInteres.setText(String.format("Interés: %.2f%%", resumen.getInteres()));
        holder.tvFechaInicio.setText("Fecha de inicio: " + resumen.getFechaInicio());
        holder.tvNumCuotas.setText("Cuotas: " + resumen.getNumCuotas());
        holder.tvCuotasPagadas.setText("Cuotas pagadas: " + resumen.getCuotasPagadas());
    }

    @Override
    public int getItemCount() {
        return listaResumen.size();
    }
}
