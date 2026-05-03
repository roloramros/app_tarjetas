package com.codram.limitx;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.TarjetaResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TarjetasPagerAdapter pagerAdapter;
    private TextView tvSaldoTotal;
    private SessionManager sessionManager;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private List<TarjetaResponse> listaTarjetasOriginal = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sessionManager = new SessionManager(this);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        tvSaldoTotal = findViewById(R.id.tvSaldoTotal);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View footerLogout = findViewById(R.id.nav_footer_logout);
        if (footerLogout != null) {
            footerLogout.setOnClickListener(v -> {
                sessionManager.clearSession();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add_card) {
                AddTarjetaBottomSheet bottomSheet = new AddTarjetaBottomSheet();
                bottomSheet.setOnTarjetaAddedListener(this::loadTarjetas);
                bottomSheet.show(getSupportFragmentManager(), "AddTarjetaBottomSheet");
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        pagerAdapter = new TarjetasPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) tab.setText("CUP");
            else if (position == 1) tab.setText("USD");
            else tab.setText("MLC");
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                actualizarSaldoTotal();
            }
        });

        handleWindowInsets();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        checkSubscription();
        loadTarjetas();
    }

    private void checkSubscription() {
        String token = "Bearer " + sessionManager.getToken();
        ApiClient.getService().getMe(token).enqueue(new Callback<com.codram.limitx.data.api.UsuarioResponse>() {
            @Override
            public void onResponse(Call<com.codram.limitx.data.api.UsuarioResponse> call, Response<com.codram.limitx.data.api.UsuarioResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isActive = response.body().isSuscripcionActiva();
                    sessionManager.setSubscriptionActive(isActive);
                    if (pagerAdapter != null) {
                        pagerAdapter.setSubscriptionActive(isActive);
                    }
                }
            }
            @Override
            public void onFailure(Call<com.codram.limitx.data.api.UsuarioResponse> call, Throwable t) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sort_nombre) {
            ordenarTarjetas("nombre");
            return true;
        } else if (id == R.id.sort_saldo) {
            ordenarTarjetas("saldo");
            return true;
        } else if (id == R.id.sort_extraccion) {
            ordenarTarjetas("extraccion");
            return true;
        } else if (id == R.id.sort_deposito) {
            ordenarTarjetas("deposito");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void actualizarSaldoTotal() {
        int position = viewPager.getCurrentItem();
        String monedaFiltro;
        if (position == 0) monedaFiltro = "CUP";
        else if (position == 1) monedaFiltro = "USD";
        else monedaFiltro = "MLC";

        double total = 0;
        if (listaTarjetasOriginal != null) {
            for (TarjetaResponse tarjeta : listaTarjetasOriginal) {
                if (tarjeta != null && monedaFiltro.equals(tarjeta.getMoneda())) {
                    total += tarjeta.getSaldo_tarjeta();
                }
            }
        }

        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(symbols);
        df.setMaximumFractionDigits(0);

        tvSaldoTotal.setText("Total " + monedaFiltro + ": $" + df.format(total));
    }

    private void ordenarTarjetas(String criterio) {
        if (listaTarjetasOriginal == null) return;

        if (criterio != null && !listaTarjetasOriginal.isEmpty()) {
            Collections.sort(listaTarjetasOriginal, (t1, t2) -> {
                if (t1 == null && t2 == null) return 0;
                if (t1 == null) return 1;
                if (t2 == null) return -1;

                switch (criterio) {
                    case "nombre":
                        String n1 = t1.getNombre() != null ? t1.getNombre() : "";
                        String n2 = t2.getNombre() != null ? t2.getNombre() : "";
                        return n1.compareToIgnoreCase(n2);
                    case "saldo":
                        return Double.compare(t2.getSaldo_tarjeta(), t1.getSaldo_tarjeta());
                    case "extraccion":
                        return Double.compare(t2.getExtraccion_disponible(), t1.getExtraccion_disponible());
                    case "deposito":
                        return Double.compare(t2.getDeposito_disponible(), t1.getDeposito_disponible());
                    default:
                        return 0;
                }
            });
            sessionManager.saveSortOrder(criterio);
        }

        distribuirTarjetas();
        actualizarSaldoTotal();
    }

    private void loadTarjetas() {
        showLoading(true);
        String token = "Bearer " + sessionManager.getToken();

        ApiClient.getService().getTarjetas(token).enqueue(new Callback<List<TarjetaResponse>>() {
            @Override
            public void onResponse(Call<List<TarjetaResponse>> call, Response<List<TarjetaResponse>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    listaTarjetasOriginal = new ArrayList<>(response.body());
                    String savedOrder = sessionManager.getSortOrder();
                    ordenarTarjetas(savedOrder);
                }
            }
            @Override
            public void onFailure(Call<List<TarjetaResponse>> call, Throwable t) {
                showLoading(false);
            }
        });
    }

    private void distribuirTarjetas() {
        List<TarjetaResponse> cup = new ArrayList<>();
        List<TarjetaResponse> usd = new ArrayList<>();
        List<TarjetaResponse> mlc = new ArrayList<>();
        if (listaTarjetasOriginal != null) {
            for (TarjetaResponse t : listaTarjetasOriginal) {
                if (t == null) continue;
                if ("CUP".equals(t.getMoneda())) cup.add(t);
                else if ("USD".equals(t.getMoneda())) usd.add(t);
                else if ("MLC".equals(t.getMoneda())) mlc.add(t);
            }
        }
        
        // Configurar listeners si aún no se han configurado
        pagerAdapter.getCupFragment().setOnRefreshListener(this::loadTarjetas);
        pagerAdapter.getUsdFragment().setOnRefreshListener(this::loadTarjetas);
        pagerAdapter.getMlcFragment().setOnRefreshListener(this::loadTarjetas);
        
        pagerAdapter.getCupFragment().setTransactionListener(this::loadTarjetas);
        pagerAdapter.getUsdFragment().setTransactionListener(this::loadTarjetas);
        pagerAdapter.getMlcFragment().setTransactionListener(this::loadTarjetas);

        pagerAdapter.getCupFragment().updateData(cup);
        pagerAdapter.getUsdFragment().updateData(usd);
        pagerAdapter.getMlcFragment().updateData(mlc);
    }

    private void handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinatorLayout), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void showLoading(boolean isLoading) {
        pagerAdapter.getCupFragment().showLoading(isLoading);
        pagerAdapter.getUsdFragment().showLoading(isLoading);
        pagerAdapter.getMlcFragment().showLoading(isLoading);
    }
}
