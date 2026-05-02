package com.codram.limitx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.TarjetaResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvTarjetas;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        rvTarjetas = findViewById(R.id.rvTarjetas);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        
        rvTarjetas.setLayoutManager(new LinearLayoutManager(this));

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            AddTarjetaBottomSheet bottomSheet = new AddTarjetaBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "AddTarjetaBottomSheet");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTarjetas();
    }

    private void loadTarjetas() {
        showLoading(true);
        String token = "Bearer " + sessionManager.getToken();

        ApiClient.getService().getTarjetas(token).enqueue(new Callback<List<TarjetaResponse>>() {
            @Override
            public void onResponse(Call<List<TarjetaResponse>> call, Response<List<TarjetaResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        showEmptyState("Aún no tienes tarjetas añadidas.");
                    } else {
                        rvTarjetas.setVisibility(View.VISIBLE);
                        tvEmptyState.setVisibility(View.GONE);
                        rvTarjetas.setAdapter(new TarjetasAdapter(response.body()));
                    }
                } else {
                    showEmptyState("Error al cargar las tarjetas.");
                }
            }

            @Override
            public void onFailure(Call<List<TarjetaResponse>> call, Throwable t) {
                showLoading(false);
                showEmptyState("Error de red. Revisa tu conexión.");
            }
        });
    }
    
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        rvTarjetas.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        tvEmptyState.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        if(isLoading) tvEmptyState.setText("Cargando...");
    }

    private void showEmptyState(String message) {
        rvTarjetas.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
        tvEmptyState.setText(message);
    }
}
