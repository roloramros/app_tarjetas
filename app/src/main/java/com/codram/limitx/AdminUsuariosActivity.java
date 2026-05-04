package com.codram.limitx;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.data.api.AdminStatsResponse;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.UsuarioResponse;
import com.google.android.material.navigation.NavigationView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsuariosActivity extends AppCompatActivity {

    private Spinner spinnerUsuarios;
    private LinearLayout layoutUserInfo;
    private TextView tvSuscripcionActiva, tvSuscripcionHasta, tvLastLogin, tvFechaCreacion, tvCantidadTarjetas, tvCantidadTransacciones;
    private TextView tvTotalUsuarios, tvTotalTarjetas, tvTotalTransacciones;
    private Button btnEliminarUsuario;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private List<UsuarioResponse> listaUsuarios = new ArrayList<>();
    private UsuarioResponse usuarioSeleccionado;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_usuarios);

        sessionManager = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Administración de Usuarios");
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Footer Logout
        View footerLogout = findViewById(R.id.nav_footer_logout);
        if (footerLogout != null) {
            footerLogout.setOnClickListener(v -> {
                sessionManager.clearSession();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add_card) {
                AddTarjetaBottomSheet bottomSheet = new AddTarjetaBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "AddTarjetaBottomSheet");
            } else if (id == R.id.nav_home) {
                finish();
            } else if (id == R.id.nav_admin_users) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        spinnerUsuarios = findViewById(R.id.spinnerUsuarios);
        layoutUserInfo = findViewById(R.id.layoutUserInfo);
        tvSuscripcionActiva = findViewById(R.id.tvSuscripcionActiva);
        tvSuscripcionHasta = findViewById(R.id.tvSuscripcionHasta);
        tvLastLogin = findViewById(R.id.tvLastLogin);
        tvFechaCreacion = findViewById(R.id.tvFechaCreacion);
        tvCantidadTarjetas = findViewById(R.id.tvCantidadTarjetas);
        tvCantidadTransacciones = findViewById(R.id.tvCantidadTransacciones);
        tvTotalUsuarios = findViewById(R.id.tvTotalUsuarios);
        tvTotalTarjetas = findViewById(R.id.tvTotalTarjetas);
        tvTotalTransacciones = findViewById(R.id.tvTotalTransacciones);
        btnEliminarUsuario = findViewById(R.id.btnEliminarUsuario);
        progressBar = findViewById(R.id.progressBar);

        cargarStatsGenerales();
        cargarUsuarios();

        spinnerUsuarios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    usuarioSeleccionado = listaUsuarios.get(position - 1);
                    mostrarInfoUsuario(usuarioSeleccionado);
                } else {
                    usuarioSeleccionado = null;
                    layoutUserInfo.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                layoutUserInfo.setVisibility(View.GONE);
            }
        });

        btnEliminarUsuario.setOnClickListener(v -> {
            if (usuarioSeleccionado != null) {
                confirmarEliminarUsuario();
            }
        });
    }

    private void cargarStatsGenerales() {
        String token = "Bearer " + sessionManager.getToken();
        ApiClient.getService().getAdminStats(token).enqueue(new Callback<AdminStatsResponse>() {
            @Override
            public void onResponse(Call<AdminStatsResponse> call, Response<AdminStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AdminStatsResponse stats = response.body();
                    tvTotalUsuarios.setText("Usuarios Totales: " + stats.getTotal_usuarios());
                    tvTotalTarjetas.setText("Tarjetas Totales: " + stats.getTotal_tarjetas());
                    tvTotalTransacciones.setText("Transacciones Totales: " + stats.getTotal_transacciones());
                }
            }

            @Override
            public void onFailure(Call<AdminStatsResponse> call, Throwable t) {
                // Silently fail or show minor error
            }
        });
    }

    private void cargarUsuarios() {
        progressBar.setVisibility(View.VISIBLE);
        String token = "Bearer " + sessionManager.getToken();
        ApiClient.getService().getUsuarios(token).enqueue(new Callback<List<UsuarioResponse>>() {
            @Override
            public void onResponse(Call<List<UsuarioResponse>> call, Response<List<UsuarioResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    listaUsuarios = response.body();
                    configurarSpinner();
                } else {
                    Toast.makeText(AdminUsuariosActivity.this, "Error al cargar usuarios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UsuarioResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminUsuariosActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarSpinner() {
        List<String> nombres = new ArrayList<>();
        nombres.add("Seleccione un usuario...");
        for (UsuarioResponse u : listaUsuarios) {
            nombres.add(u.getNombre());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUsuarios.setAdapter(adapter);
    }

    private void mostrarInfoUsuario(UsuarioResponse u) {
        layoutUserInfo.setVisibility(View.VISIBLE);
        tvSuscripcionActiva.setText("Suscripción Activa: " + (u.isSuscripcionActiva() ? "Sí" : "No"));

        String suscripcionHasta = formatBackendDate(u.getSuscripcionHasta());
        tvSuscripcionHasta.setText("Suscripción Hasta: " + (suscripcionHasta != null ? suscripcionHasta : "N/A"));

        String lastLogin = formatBackendDate(u.getLastLogin());
        tvLastLogin.setText("Último Login: " + (lastLogin != null ? lastLogin : "Nunca"));

        String fechaCreacion = formatBackendDate(u.getFechaCreacion());
        tvFechaCreacion.setText("Fecha Creación: " + (fechaCreacion != null ? fechaCreacion : "N/A"));

        tvCantidadTarjetas.setText("Tarjetas Registradas: " + u.getCantidadTarjetas());
        tvCantidadTransacciones.setText("Transacciones Realizadas: " + u.getCantidadTransacciones());
    }

    private String formatBackendDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) {
            return null;
        }
        try {
            // El backend devuelve ISO 8601: 2026-05-01T12:00:00
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = inputFormat.parse(rawDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "ES"));
            String formatted = outputFormat.format(date);

            // Capitalizar el mes (ej: "01 mayo 2026" -> "01 Mayo 2026")
            String[] parts = formatted.split(" ");
            if (parts.length >= 2) {
                String month = parts[1];
                if (month.length() > 0) {
                    parts[1] = month.substring(0, 1).toUpperCase() + month.substring(1);
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    sb.append(parts[i]);
                    if (i < parts.length - 1) sb.append(" ");
                }
                return sb.toString();
            }
            return formatted;
        } catch (Exception e) {
            e.printStackTrace();
            return rawDate;
        }
    }

    private void confirmarEliminarUsuario() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Usuario")
                .setMessage("¿Estás seguro de que deseas eliminar al usuario " + usuarioSeleccionado.getNombre() + "? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarUsuario())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarUsuario() {
        progressBar.setVisibility(View.VISIBLE);
        String token = "Bearer " + sessionManager.getToken();
        ApiClient.getService().eliminarUsuario(token, usuarioSeleccionado.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminUsuariosActivity.this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show();
                    cargarStatsGenerales();
                    cargarUsuarios(); // Recargar lista
                } else {
                    Toast.makeText(AdminUsuariosActivity.this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminUsuariosActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
