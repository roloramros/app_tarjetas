package com.codram.limitx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.TarjetaResponse;
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
    private FloatingActionButton btnLogout, fabAdd; // Changed btnLogout type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Step 1: Enable Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Step 2: Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Step 3: Initialize views
        sessionManager = new SessionManager(this);
        rvTarjetas = findViewById(R.id.rvTarjetas);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnLogout = findViewById(R.id.btnLogout); // Now a FAB
        fabAdd = findViewById(R.id.fabAdd);
        
        rvTarjetas.setLayoutManager(new LinearLayoutManager(this));

        // Step 4: Handle Insets
        handleWindowInsets();

        // Step 5: Setup Listeners
        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        fabAdd.setOnClickListener(v -> {
            AddTarjetaBottomSheet bottomSheet = new AddTarjetaBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "AddTarjetaBottomSheet");
        });
    }
    
    private void handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinatorLayout), (v, insets) -> {
            WindowInsetsCompat systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            
            // Apply padding to the toolbar and recyclerview to avoid overlap
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            rvTarjetas.setPadding(0, 0, 0, systemBars.bottom);

            // Adjust FAB margins
            updateFabMargin(btnLogout, systemBars.bottom);
            updateFabMargin(fabAdd, systemBars.bottom);

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void updateFabMargin(FloatingActionButton fab, int bottomInset) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) fab.getLayoutParams();
        // Default margin is 16dp, we add the bottom inset to it
        params.bottomMargin = (int) (16 * getResources().getDisplayMetrics().density) + bottomInset;
        fab.setLayoutParams(params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTarjetas();
    }

    private void loadTarjetas() {
        // This method remains the same as before
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
