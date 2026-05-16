package com.codram.limitx;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codram.limitx.data.local.entity.TransaccionEntity;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.util.Locale;

public class TransaccionesAdapter extends RecyclerView.Adapter<TransaccionesAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(TransaccionEntity transaccion);
    }

    private List<TransaccionEntity> transacciones;
    private OnItemClickListener listener;
    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM");
    private final java.text.DecimalFormat numberFormat;

    public TransaccionesAdapter(List<TransaccionEntity> transacciones, OnItemClickListener listener) {
        this.transacciones = transacciones;
        this.listener = listener;
        
        java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(Locale.US);
        if (nf instanceof java.text.DecimalFormat) {
            this.numberFormat = (java.text.DecimalFormat) nf;
            java.text.DecimalFormatSymbols symbols = numberFormat.getDecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            numberFormat.setDecimalFormatSymbols(symbols);
            numberFormat.setMaximumFractionDigits(0);
        } else {
            // Fallback if not DecimalFormat
            this.numberFormat = null;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaccion_columna, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransaccionEntity t = transacciones.get(position);

        // Formatear fecha con posible asterisco
        String fechaFormateada = "";
        try {
            LocalDateTime dateTime = LocalDateTime.parse(t.fecha, inputFormatter);
            fechaFormateada = "(" + dateTime.format(outputFormatter) + ")";
            if (t.descripcion != null && !t.descripcion.isEmpty()) {
                fechaFormateada += "*";
            }
        } catch (Exception e) {
            fechaFormateada = "(??/??)";
        }

        // Formatear monto
        String montoFormateado;
        if (numberFormat != null) {
            montoFormateado = numberFormat.format(new BigDecimal(t.monto));
        } else {
            montoFormateado = String.format(Locale.US, "%.0f", new BigDecimal(t.monto).doubleValue());
        }

        holder.tvMonto.setText(montoFormateado);
        holder.tvFecha.setText(fechaFormateada);

        // Color para el tema claro (Gris oscuro mate)
        int colorTexto = Color.parseColor("#424242");
        holder.tvMonto.setTextColor(colorTexto);
        holder.tvFecha.setTextColor(colorTexto);

        // Lógica de clic para editar la transacción
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(t);
            }
        });
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
