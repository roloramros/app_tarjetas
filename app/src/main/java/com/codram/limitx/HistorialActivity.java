package com.codram.limitx;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.ApiService;
import com.codram.limitx.data.api.TransaccionResponse;
import com.codram.limitx.data.api.TransaccionUpdate;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.utils.TransactionDialogHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class HistorialActivity extends AppCompatActivity {
    private RecyclerView rvEntradas, rvSalidas;
    private List<TransaccionResponse> entradas = new ArrayList<>();
    private List<TransaccionResponse> salidas = new ArrayList<>();
    private UUID tarjetaId;
    private String tarjetaNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        
        rvEntradas = findViewById(R.id.rvEntradas);
        rvSalidas = findViewById(R.id.rvSalidas);

        rvEntradas.setLayoutManager(new LinearLayoutManager(this));
        rvSalidas.setLayoutManager(new LinearLayoutManager(this));

        // Sincronización de scroll
        rvEntradas.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                rvSalidas.scrollBy(dx, dy);
            }
        });
        
        rvSalidas.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                rvEntradas.scrollBy(dx, dy);
            }
        });

        findViewById(R.id.fabAdd).setOnClickListener(v ->
                TransactionDialogHelper.showTransactionDialog(this, "Depósito", tarjetaId, tarjetaNombre, () -> cargarTransacciones(tarjetaId)));

        findViewById(R.id.fabRemove).setOnClickListener(v ->
                TransactionDialogHelper.showTransactionDialog(this, "Extracción", tarjetaId, tarjetaNombre, () -> cargarTransacciones(tarjetaId)));

        String idStr = getIntent().getStringExtra("TARJETA_ID");
        if (idStr != null) {
            tarjetaId = UUID.fromString(idStr);
            tarjetaNombre = getIntent().getStringExtra("TARJETA_NOMBRE");
            cargarTransacciones(tarjetaId);
        }
    }

    private void cargarTransacciones(UUID tarjetaId) {
        String token = new SessionManager(this).getToken();
        android.util.Log.d("LimiTxDebug", "Cargando transacciones para: " + tarjetaId);

        ApiClient.getService().getTransaccionesMes("Bearer " + token, tarjetaId).enqueue(new Callback<List<TransaccionResponse>>() {
            @Override
            public void onResponse(Call<List<TransaccionResponse>> call, Response<List<TransaccionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("LimiTxDebug", "Transacciones recibidas: " + response.body().size());
                    entradas.clear();
                    salidas.clear();
                    for (TransaccionResponse t : response.body()) {
                        if ("entrada".equals(t.getTipo())) {
                            entradas.add(t);
                        } else {
                            salidas.add(t);
                        }
                    }
                    android.util.Log.d("LimiTxDebug", "Entradas: " + entradas.size() + ", Salidas: " + salidas.size());
                    // Configurar adaptadores
                    rvEntradas.setAdapter(new TransaccionesAdapter(entradas, t -> mostrarDialogoEdicion(t, tarjetaId)));
                    rvSalidas.setAdapter(new TransaccionesAdapter(salidas, t -> mostrarDialogoEdicion(t, tarjetaId)));
                } else {
                    android.util.Log.e("LimiTxDebug", "Error en respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TransaccionResponse>> call, Throwable t) {
                android.util.Log.e("LimiTxDebug", "Fallo en la petición", t);
            }
        });
    }

    private void mostrarDialogoEdicion(TransaccionResponse transaccion, UUID tarjetaId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_transaccion, null);
        TextInputEditText etMonto = dialogView.findViewById(R.id.etEditMonto);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etEditDescripcion);
        MaterialButton btnFecha = dialogView.findViewById(R.id.btnEditFecha);

        etMonto.setText(transaccion.getMonto().toString());
        if (transaccion.getDescripcion() != null) {
            etDescripcion.setText(transaccion.getDescripcion());
        }

        final String[] fechaSeleccionada = {transaccion.getFecha()};
        // Mostrar fecha formateada en el botón
        try {
            LocalDateTime dateTime = LocalDateTime.parse(fechaSeleccionada[0], java.time.format.DateTimeFormatter.ISO_DATE_TIME);
            btnFecha.setText(dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        } catch (Exception ignored) {}

        btnFecha.setOnClickListener(v -> {
            LocalDateTime current = LocalDateTime.now();
            try {
                current = LocalDateTime.parse(fechaSeleccionada[0], java.time.format.DateTimeFormatter.ISO_DATE_TIME);
            } catch (Exception ignored) {}
            LocalDateTime finalCurrent = current;
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
                    LocalDateTime nuevaFecha = LocalDateTime.of(year, month + 1, dayOfMonth, hourOfDay, minute);
                    fechaSeleccionada[0] = nuevaFecha.format(java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                    btnFecha.setText(nuevaFecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }, finalCurrent.getHour(), finalCurrent.getMinute(), true).show();
            }, finalCurrent.getYear(), finalCurrent.getMonthValue() - 1, finalCurrent.getDayOfMonth()).show();
        });

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Editar Transacción")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    try {
                        BigDecimal monto = new BigDecimal(etMonto.getText().toString().trim());
                        String descripcion = etDescripcion.getText().toString().trim();
                        TransaccionUpdate update = new TransaccionUpdate(monto, descripcion, fechaSeleccionada[0]);
                        
                        String token = new SessionManager(this).getToken();
                        ApiClient.getService().actualizarTransaccion("Bearer " + token, transaccion.getId(), update)
                            .enqueue(new Callback<TransaccionResponse>() {
                                @Override
                                public void onResponse(Call<TransaccionResponse> call, Response<TransaccionResponse> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(HistorialActivity.this, "Actualizado", Toast.LENGTH_SHORT).show();
                                        cargarTransacciones(tarjetaId); // Recargar
                                    } else {
                                        Toast.makeText(HistorialActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFailure(Call<TransaccionResponse> call, Throwable t) {
                                    Toast.makeText(HistorialActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                                }
                            });
                    } catch (Exception e) {
                        Toast.makeText(this, "Monto inválido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Eliminar", (dialog, which) -> confirmarEliminacion(transaccion, tarjetaId))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarEliminacion(TransaccionResponse transaccion, UUID tarjetaId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Transacción")
                .setMessage("¿Estás seguro de que deseas eliminar esta transacción?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    String token = new SessionManager(this).getToken();
                    ApiClient.getService().eliminarTransaccion("Bearer " + token, transaccion.getId())
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(HistorialActivity.this, "Eliminado", Toast.LENGTH_SHORT).show();
                                    cargarTransacciones(tarjetaId); // Recargar
                                } else {
                                    Toast.makeText(HistorialActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(HistorialActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                            }
                        });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
