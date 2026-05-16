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

import com.codram.limitx.data.LimiTxRepository;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.TransaccionResponse;
import com.codram.limitx.data.api.TransaccionUpdate;
import com.codram.limitx.data.local.entity.TarjetaEntity;
import com.codram.limitx.data.local.entity.TransaccionEntity;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.utils.TransactionDialogHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
    private android.widget.TextView tvSaldoActual;
    private List<TransaccionEntity> entradas = new ArrayList<>();
    private List<TransaccionEntity> salidas = new ArrayList<>();
    private LimiTxRepository repository;
    private UUID tarjetaId;
    private String tarjetaNombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        repository = new LimiTxRepository(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        View root = findViewById(R.id.rootLayout);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return WindowInsetsCompat.CONSUMED;
            });
        }
        
        rvEntradas = findViewById(R.id.rvEntradas);
        rvSalidas = findViewById(R.id.rvSalidas);
        tvSaldoActual = findViewById(R.id.tvSaldoActual);

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
            
            double saldoInicial = getIntent().getDoubleExtra("TARJETA_SALDO", 0.0);
            String monedaInicial = getIntent().getStringExtra("TARJETA_MONEDA");
            actualizarVistaSaldo(saldoInicial, monedaInicial);
            
            cargarTransacciones(tarjetaId);
        }
    }

    private void actualizarVistaSaldo(double saldo, String moneda) {
        if (tvSaldoActual == null) return;
        
        String monedaStr = (moneda != null ? moneda : "");
        try {
            java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance(Locale.US);
            if (nf instanceof java.text.DecimalFormat) {
                java.text.DecimalFormat df = (java.text.DecimalFormat) nf;
                java.text.DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
                symbols.setGroupingSeparator(' ');
                df.setDecimalFormatSymbols(symbols);
                df.setMaximumFractionDigits(0);
                String formatted = df.format(saldo);
                tvSaldoActual.setText(formatted + " " + monedaStr);
            } else {
                tvSaldoActual.setText(String.format(Locale.US, "%.0f %s", saldo, monedaStr));
            }
        } catch (Exception e) {
            tvSaldoActual.setText(String.format(Locale.US, "%.0f %s", saldo, monedaStr));
        }
    }

    private void cargarSaldoTarjeta(UUID tarjetaId) {
        String token = new SessionManager(this).getToken();
        String usuarioId = new SessionManager(this).getUserId();
        if (usuarioId == null) return;

        repository.getTarjetas(usuarioId, "Bearer " + token, new LimiTxRepository.Callback<List<TarjetaEntity>>() {
            @Override
            public void onSuccess(List<TarjetaEntity> result) {
                for (TarjetaEntity t : result) {
                    if (t.id.equals(tarjetaId.toString())) {
                        actualizarVistaSaldo(t.saldoTarjeta, t.moneda);
                        break;
                    }
                }
            }
            @Override
            public void onError(String mensaje) {}
        });
    }

    private void cargarTransacciones(UUID tarjetaId) {
        cargarSaldoTarjeta(tarjetaId);
        String token = new SessionManager(this).getToken();
        android.util.Log.d("LimiTxDebug", "Cargando transacciones para: " + tarjetaId);

        repository.getTransacciones(tarjetaId.toString(), "Bearer " + token, new LimiTxRepository.Callback<List<TransaccionEntity>>() {
            @Override
            public void onSuccess(List<TransaccionEntity> result) {
                android.util.Log.d("LimiTxDebug", "Transacciones recibidas: " + result.size());
                entradas.clear();
                salidas.clear();
                for (TransaccionEntity t : result) {
                    if ("entrada".equals(t.tipo)) {
                        entradas.add(t);
                    } else {
                        salidas.add(t);
                    }
                }
                android.util.Log.d("LimiTxDebug", "Entradas: " + entradas.size() + ", Salidas: " + salidas.size());
                rvEntradas.setAdapter(new TransaccionesAdapter(entradas, t -> mostrarDialogoEdicion(t, tarjetaId)));
                rvSalidas.setAdapter(new TransaccionesAdapter(salidas, t -> mostrarDialogoEdicion(t, tarjetaId)));
            }

            @Override
            public void onError(String mensaje) {
                android.util.Log.e("LimiTxDebug", "Error al cargar transacciones: " + mensaje);
            }
        });
    }

    private void mostrarDialogoEdicion(TransaccionEntity transaccion, UUID tarjetaId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_transaccion, null);
        TextInputEditText etMonto = dialogView.findViewById(R.id.etEditMonto);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etEditDescripcion);
        MaterialButton btnFecha = dialogView.findViewById(R.id.btnEditFecha);

        etMonto.setText(transaccion.monto);
        if (transaccion.descripcion != null) {
            etDescripcion.setText(transaccion.descripcion);
        }

        final String[] fechaSeleccionada = {transaccion.fecha};
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
                        repository.actualizarTransaccion(transaccion.id, update, "Bearer " + token,
                            new LimiTxRepository.Callback<TransaccionEntity>() {
                                @Override
                                public void onSuccess(TransaccionEntity result) {
                                    Toast.makeText(HistorialActivity.this, "Actualizado", Toast.LENGTH_SHORT).show();
                                    cargarTransacciones(tarjetaId); // Recargar
                                }

                                @Override
                                public void onError(String mensaje) {
                                    Toast.makeText(HistorialActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
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

    private void confirmarEliminacion(TransaccionEntity transaccion, UUID tarjetaId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Transacción")
                .setMessage("¿Estás seguro de que deseas eliminar esta transacción?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    String token = new SessionManager(this).getToken();
                    repository.eliminarTransaccion(transaccion.id, "Bearer " + token, new LimiTxRepository.Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Toast.makeText(HistorialActivity.this, "Eliminado", Toast.LENGTH_SHORT).show();
                            cargarTransacciones(tarjetaId);
                        }

                        @Override
                        public void onError(String mensaje) {
                            Toast.makeText(HistorialActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
