package com.example.appprestamos.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appprestamos.R;
import com.example.appprestamos.entidades.PagoRealizado;

import java.util.List;

public class PagoRealizadoAdapter extends RecyclerView.Adapter<PagoRealizadoAdapter.PagoRealizadoViewHolder> {

    private Context context;
    private List<PagoRealizado> listaPagos;

    public PagoRealizadoAdapter(Context context, List<PagoRealizado> listaPagos) {
        this.context = context;
        this.listaPagos = listaPagos;
    }

    @NonNull
    @Override
    public PagoRealizadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pago_realizado, parent, false);
        return new PagoRealizadoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PagoRealizadoViewHolder holder, int position) {
        PagoRealizado pago = listaPagos.get(position);

        // 1) Mostrar “Cuota #X”
        holder.tvNumCuota.setText("Cuota #" + pago.numCuota);

        // 2) Extraer los primeros 10 caracteres de la fecha ("YYYY-MM-DD")
        String fechaFormateada;
        if (pago.fechaPago != null && pago.fechaPago.length() >= 10) {
            fechaFormateada = pago.fechaPago.substring(0, 10);
        } else {
            fechaFormateada = pago.fechaPago;
        }
        holder.tvFechaPago.setText(fechaFormateada);

        // 3) Mostrar monto
        holder.tvMontoPago.setText("S/ " + String.format("%.2f", pago.monto));
    }

    @Override
    public int getItemCount() {
        return listaPagos.size();
    }

    static class PagoRealizadoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumCuota, tvFechaPago, tvMontoPago;

        public PagoRealizadoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumCuota  = itemView.findViewById(R.id.tvNumCuotaPagoItem);
            tvFechaPago = itemView.findViewById(R.id.tvFechaPagoItem);
            tvMontoPago = itemView.findViewById(R.id.tvMontoPagoItem);
        }
    }
}
