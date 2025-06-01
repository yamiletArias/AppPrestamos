package com.example.appprestamos.adaptadores;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appprestamos.R;
import com.example.appprestamos.entidades.PagoPendiente;

import java.util.List;

public class PagoPendienteAdapter extends RecyclerView.Adapter<PagoPendienteAdapter.ViewHolder> {

    private Context context;
    private List<PagoPendiente> listaPagosPendientes;

    public PagoPendienteAdapter(Context context, List<PagoPendiente> listaPagosPendientes) {
        this.context = context;
        this.listaPagosPendientes = listaPagosPendientes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pago_pendiente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PagoPendiente pago = listaPagosPendientes.get(position);

        holder.tvNumCuota.setText("Cuota #" + pago.numCuota);
        holder.tvFechaVencimiento.setText(pago.fechaVencimiento.substring(0, 10));
        holder.tvMontoPendiente.setText("S/ " + String.format("%.2f", pago.monto));
    }

    @Override
    public int getItemCount() {
        return listaPagosPendientes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumCuota, tvFechaVencimiento, tvMontoPendiente;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumCuota = itemView.findViewById(R.id.tvNumCuotaPendiente);
            tvFechaVencimiento = itemView.findViewById(R.id.tvFechaVencimiento);
            tvMontoPendiente = itemView.findViewById(R.id.tvMontoPendiente);
        }
    }
}
