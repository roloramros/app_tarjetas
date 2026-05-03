package com.codram.limitx;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codram.limitx.data.api.TransaccionResponse;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.util.Locale;

public class TransaccionesAdapter extends RecyclerView.Adapter<TransaccionesAdapter.ViewHolder> {
    private List<TransaccionResponse> transacciones;
    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private final java.text.DecimalFormat numberFormat;

    public TransaccionesAdapter(List<TransaccionResponse> transacciones) {
        this.transacciones = transacciones;
        this.numberFormat = (java.text.DecimalFormat) java.text.NumberFormat.getNumberInstance(Locale.US);
        java.text.DecimalFormatSymbols symbols = numberFormat.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        numberFormat.setDecimalFormatSymbols(symbols);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaccion_columna, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransaccionResponse t = transacciones.get(position);

        // Formatear fecha con posible asterisco
        String fechaFormateada = "";
        try {
            LocalDateTime dateTime = LocalDateTime.parse(t.getFecha(), inputFormatter);
            fechaFormateada = "(" + dateTime.format(outputFormatter) + ")";
            if (t.getDescripcion() != null && !t.getDescripcion().isEmpty()) {
                fechaFormateada += "*";
            }
        } catch (Exception e) {
            fechaFormateada = "(??/??)";
        }

        // Formatear monto
        String montoFormateado = numberFormat.format(t.getMonto());

        holder.tvMonto.setText(montoFormateado);
        holder.tvFecha.setText(fechaFormateada);

        // Color para el tema claro (Gris oscuro mate)
        int colorTexto = Color.parseColor("#424242");
        holder.tvMonto.setTextColor(colorTexto);
        holder.tvFecha.setTextColor(colorTexto);

        // Lógica de clic para mostrar descripción
        if (t.getDescripcion() != null && !t.getDescripcion().isEmpty()) {
            holder.itemView.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Descripción")
                    .setMessage(t.getDescripcion())
                    .setPositiveButton("OK", null)
                    .show();
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return transacciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMonto, tvFecha;
        public ViewHolder(View itemView) {
            super(itemView);
            tvMonto = itemView.findViewById(R.id.tvMonto);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}
