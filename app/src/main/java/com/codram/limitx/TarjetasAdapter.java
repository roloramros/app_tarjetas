package com.codram.limitx;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.TarjetaResponse;
import com.codram.limitx.data.api.TransaccionRequest;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TarjetasAdapter extends RecyclerView.Adapter<TarjetasAdapter.TarjetaViewHolder> {

    public interface OnTransactionAddedListener {
        void onTransactionAdded();
    }

    private List<TarjetaResponse> tarjetas;
    private OnTransactionAddedListener transactionListener;
    private boolean isSubscriptionActive = true;

    public TarjetasAdapter(List<TarjetaResponse> tarjetas, OnTransactionAddedListener transactionListener) {
        this(tarjetas, transactionListener, true);
    }

    public TarjetasAdapter(List<TarjetaResponse> tarjetas, OnTransactionAddedListener transactionListener, boolean isSubscriptionActive) {
        this.tarjetas = tarjetas;
        this.transactionListener = transactionListener;
        this.isSubscriptionActive = isSubscriptionActive;
    }

    public void setSubscriptionActive(boolean isActive) {
        this.isSubscriptionActive = isActive;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TarjetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarjeta, parent, false);
        return new TarjetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarjetaViewHolder holder, int position) {
        TarjetaResponse tarjeta = tarjetas.get(position);
        holder.bind(tarjeta);

        boolean isDisabled = !isSubscriptionActive && position > 0;

        if (isDisabled) {
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setOnClickListener(v -> Toast.makeText(v.getContext(), "Requiere suscripción activa", Toast.LENGTH_SHORT).show());
            holder.itemView.setOnLongClickListener(v -> {
                Toast.makeText(v.getContext(), "Requiere suscripción activa", Toast.LENGTH_SHORT).show();
                return true;
            });
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenu().add("Depósito");
                popup.getMenu().add("Extracción");
                popup.setOnMenuItemClickListener(item -> {
                    mostrarDialogoTransaccion(v.getContext(), item.getTitle().toString(), tarjeta);
                    return true;
                });
                popup.show();
            });

            holder.itemView.setOnLongClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenu().add("Ver Historial");
                popup.getMenu().add("Ajustar Saldo");
                popup.getMenu().add("Eliminar");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Ver Historial")) {
                        android.content.Intent intent = new android.content.Intent(v.getContext(), HistorialActivity.class);
                        intent.putExtra("TARJETA_ID", tarjeta.getId().toString());
                        v.getContext().startActivity(intent);
                    } else if (item.getTitle().equals("Eliminar")) {
                        new MaterialAlertDialogBuilder(v.getContext())
                            .setTitle("Confirmar eliminación")
                            .setMessage("¿Estás seguro de que deseas eliminar esta tarjeta? Esta operación es irreversible y se perderá toda la información relacionada con esta tarjeta del historial del sistema.")
                            .setPositiveButton("Eliminar", (dialog, which) -> {
                                String token = new SessionManager(v.getContext()).getToken();
                                ApiClient.getService().eliminarTarjeta("Bearer " + token, tarjeta.getId()).enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if (response.isSuccessful()) {
                                            Toast.makeText(v.getContext(), "Tarjeta eliminada", Toast.LENGTH_SHORT).show();
                                            tarjetas.remove(holder.getAdapterPosition());
                                            notifyItemRemoved(holder.getAdapterPosition());
                                            if (transactionListener != null) transactionListener.onTransactionAdded(); // Refrescar saldo total
                                        } else {
                                            Toast.makeText(v.getContext(), "Error al eliminar", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        Toast.makeText(v.getContext(), "Error de red", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                    } else {
                        Toast.makeText(v.getContext(), item.getTitle() + " seleccionado", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
                popup.show();
                return true;
            });
        }
    }

    private void mostrarDialogoTransaccion(Context context, String tipo, TarjetaResponse tarjeta) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_transaccion, null);
        TextInputEditText etMonto = dialogView.findViewById(R.id.etMonto);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        TextInputEditText etSubtipo = dialogView.findViewById(R.id.etSubtipo);
        Button btnFecha = dialogView.findViewById(R.id.btnFecha);
        SwitchMaterial swAfectaLimite = dialogView.findViewById(R.id.swAfectaLimite);

        if (tipo.equalsIgnoreCase("Depósito")) {
            swAfectaLimite.setVisibility(View.GONE);
        }

        final String[] fechaSeleccionada = {LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)};

        btnFecha.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Seleccionar fecha")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                fechaSeleccionada[0] = LocalDateTime.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    0, 0
                ).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                btnFecha.setText(datePicker.getHeaderText());
            });
            datePicker.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "DATE_PICKER");
        });

        new MaterialAlertDialogBuilder(context)
            .setTitle(tipo + ": " + tarjeta.getNombre())
            .setView(dialogView)
            .setPositiveButton("Aceptar", (dialog, which) -> {
                String montoStr = etMonto.getText().toString();
                if (montoStr.isEmpty()) {
                    Toast.makeText(context, "El monto es obligatorio", Toast.LENGTH_SHORT).show();
                    return;
                }

                BigDecimal monto = new BigDecimal(montoStr);
                if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                    Toast.makeText(context, "El monto debe ser mayor a cero", Toast.LENGTH_SHORT).show();
                    return;
                }
                String tipoApi = tipo.equalsIgnoreCase("Depósito") ? "entrada" : "salida";
                
                TransaccionRequest request = new TransaccionRequest(
                    tarjeta.getId(), tipoApi, monto,
                    etDescripcion.getText().toString(), etSubtipo.getText().toString(),
                    swAfectaLimite.isChecked(), fechaSeleccionada[0]
                );

                String token = new SessionManager(context).getToken();
                ApiClient.getService().crearTransaccion("Bearer " + token, request).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "Transacción guardada", Toast.LENGTH_SHORT).show();
                            if (transactionListener != null) transactionListener.onTransactionAdded();
                        } else {
                            Toast.makeText(context, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    public void updateData(List<TarjetaResponse> nuevasTarjetas) {
        this.tarjetas = nuevasTarjetas;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return tarjetas.size();
    }

    static class TarjetaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCardName, tvCardNumber, tvSaldoTarjeta, tvExtraccionDisponible, tvDepositoDisponible;
        private ImageView ivCardIcon;

        public TarjetaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardName = itemView.findViewById(R.id.tvCardName);
            ivCardIcon = itemView.findViewById(R.id.ivCardIcon);
            tvCardNumber = itemView.findViewById(R.id.tvCardNumber);
            tvSaldoTarjeta = itemView.findViewById(R.id.tvSaldoTarjeta);
            tvExtraccionDisponible = itemView.findViewById(R.id.tvExtraccionDisponible);
            tvDepositoDisponible = itemView.findViewById(R.id.tvDepositoDisponible);
        }

        public void bind(TarjetaResponse tarjeta) {
            tvCardName.setText(tarjeta.getNombre());
            
            // Set dynamic icon
            String bancoStr = tarjeta.getBanco();
            int iconResId = R.drawable.clasica; // Default fallback
            if (bancoStr != null) {
                String bancoLower = bancoStr.toLowerCase().trim();
                switch (bancoLower) {
                    case "bpa":
                        iconResId = R.drawable.bpa;
                        break;
                    case "metro":
                        iconResId = R.drawable.metro;
                        break;
                    case "bandec":
                        iconResId = R.drawable.bandec;
                        break;
                    case "tropical":
                        iconResId = R.drawable.tropical;
                        break;
                    case "clasica":
                        iconResId = R.drawable.clasica;
                        break;
                    default:
                        iconResId = R.drawable.clasica;
                        break;
                }
            }
            ivCardIcon.setImageResource(iconResId);

            tvCardNumber.setText(obfuscateCardNumber(tarjeta.getNumero()));

            java.text.DecimalFormat df = (java.text.DecimalFormat) java.text.NumberFormat.getNumberInstance(Locale.US);
            java.text.DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
            symbols.setGroupingSeparator(' ');
            df.setDecimalFormatSymbols(symbols);
            df.setMaximumFractionDigits(0);

            String moneda = tarjeta.getMoneda();
            Context context = itemView.getContext();
            tvSaldoTarjeta.setText(context.getString(R.string.saldo_tarjeta_label) + df.format(tarjeta.getSaldo_tarjeta()) + " " + moneda);
            tvSaldoTarjeta.setTextColor(Color.BLACK);

            double extraccion = tarjeta.getExtraccion_disponible();
            double deposito = tarjeta.getDeposito_disponible();

            String extraccionText = df.format(extraccion);
            String depositoText = df.format(deposito);

            if (deposito < 0) {
                // Si el depósito es negativo, mostramos 0 en depósito y el excedente en extracción
                extraccionText += " (" + df.format(Math.abs(deposito)) + ")";
                depositoText = "0";
            }

            tvExtraccionDisponible.setText(context.getString(R.string.extraccion_disponible_label) + extraccionText + " " + moneda);
            tvExtraccionDisponible.setTextColor(Color.DKGRAY);
            tvDepositoDisponible.setText(context.getString(R.string.deposito_disponible_label) + depositoText + " " + moneda);
            tvDepositoDisponible.setTextColor(Color.DKGRAY);
            }

        private String obfuscateCardNumber(String number) {
            if (number == null || number.length() <= 4) {
                return "****";
            }
            return "**** **** **** " + number.substring(number.length() - 4);
        }
    }
}
