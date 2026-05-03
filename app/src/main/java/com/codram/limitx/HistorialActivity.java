package com.codram.limitx;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.ApiService;
import com.codram.limitx.data.api.TransaccionResponse;
import com.codram.limitx.data.SessionManager;
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

        String tarjetaId = getIntent().getStringExtra("TARJETA_ID");
        if (tarjetaId != null) {
            cargarTransacciones(UUID.fromString(tarjetaId));
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
                    rvEntradas.setAdapter(new TransaccionesAdapter(entradas));
                    rvSalidas.setAdapter(new TransaccionesAdapter(salidas));
                } else {
                    android.util.Log.e("LimiTxDebug", "Error en respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TransaccionResponse>> call, Throwable t) {
                android.util.Log.e("LimiTxDebug", "Fallo en la petición", t);
            }
        });
    }}
