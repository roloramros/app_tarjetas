package com.codram.limitx.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.codram.limitx.R;
import com.codram.limitx.data.LimiTxRepository;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.TransaccionRequest;
import com.codram.limitx.data.local.entity.TransaccionEntity;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionDialogHelper {

    public interface OnTransactionAddedListener {
        void onTransactionAdded();
    }

    public static void showTransactionDialog(Context context, String tipo, UUID tarjetaId, String tarjetaNombre, OnTransactionAddedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_transaccion, null);
        TextInputEditText etMonto = dialogView.findViewById(R.id.etMonto);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        TextInputEditText etSubtipo = dialogView.findViewById(R.id.etSubtipo);
        Button btnFecha = dialogView.findViewById(R.id.btnFecha);
        SwitchMaterial swAfectaLimite = dialogView.findViewById(R.id.swAfectaLimite);

        if (tipo.equalsIgnoreCase("Depósito") || tipo.equalsIgnoreCase("Entrada")) {
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
            .setTitle(tipo + ": " + tarjetaNombre)
            .setView(dialogView)
            .setPositiveButton("Aceptar", (dialog, which) -> {
                String montoStr = etMonto.getText().toString();
                if (montoStr.isEmpty()) {
                    Toast.makeText(context, "El monto es obligatorio", Toast.LENGTH_SHORT).show();
                    return;
                }

                BigDecimal monto = new BigDecimal(montoStr);
                String tipoApi = (tipo.equalsIgnoreCase("Depósito") || tipo.equalsIgnoreCase("Entrada")) ? "entrada" : "salida";
                
                TransaccionRequest request = new TransaccionRequest(
                    tarjetaId, tipoApi, monto,
                    etDescripcion.getText().toString(), etSubtipo.getText().toString(),
                    swAfectaLimite.isChecked(), fechaSeleccionada[0]
                );

                String token = new SessionManager(context).getToken();
                LimiTxRepository repo = new LimiTxRepository(context);
                repo.crearTransaccion(request, "Bearer " + token, new LimiTxRepository.Callback<TransaccionEntity>() {
                    @Override
                    public void onSuccess(TransaccionEntity result) {
                        Toast.makeText(context, "Transacción guardada", Toast.LENGTH_SHORT).show();
                        if (listener != null) listener.onTransactionAdded();
                    }

                    @Override
                    public void onError(String mensaje) {
                        Toast.makeText(context, "Error al guardar transacción", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
}
