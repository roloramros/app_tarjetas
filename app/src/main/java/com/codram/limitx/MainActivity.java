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
import com.codram.limitx.data.LimiTxRepository;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.AppVersionResponse;
import com.codram.limitx.data.local.entity.TarjetaEntity;
import com.codram.limitx.utils.ConnectivityHelper;
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
    private TextView tvOfflineBanner;
    private SessionManager sessionManager;
    private LimiTxRepository repository;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private List<TarjetaEntity> listaTarjetasOriginal = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        checkVersion();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sessionManager = new SessionManager(this);
        repository = new LimiTxRepository(this);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        tvSaldoTotal = findViewById(R.id.tvSaldoTotal);
        tvOfflineBanner = findViewById(R.id.tvOfflineBanner);
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
            } else if (id == R.id.nav_admin_users) {
                Intent intent = new Intent(MainActivity.this, AdminUsuariosActivity.class);
                startActivity(intent);
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
        setupAdminMenu();
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
            public void onFailure(Call<com.codram.limitx.data.api.UsuarioResponse> call, Throwable t) {
                if (pagerAdapter != null) {
                    pagerAdapter.setSubscriptionActive(sessionManager.isSubscriptionActive());
                }
            }
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
            for (TarjetaEntity tarjeta : listaTarjetasOriginal) {
                if (tarjeta != null && monedaFiltro.equals(tarjeta.moneda)) {
                    total += tarjeta.saldoTarjeta;
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
                        String n1 = t1.nombre != null ? t1.nombre : "";
                        String n2 = t2.nombre != null ? t2.nombre : "";
                        return n1.compareToIgnoreCase(n2);
                    case "saldo":
                        return Double.compare(t2.saldoTarjeta, t1.saldoTarjeta);
                    case "extraccion":
                        return Double.compare(t2.extraccionDisponible, t1.extraccionDisponible);
                    case "deposito":
                        return Double.compare(t2.depositoDisponible, t1.depositoDisponible);
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
        String usuarioId = sessionManager.getUserId();

        // Fallback para usuarios antiguos sin ID
        if (usuarioId == null) {
            ApiClient.getService().getMe(token).enqueue(new Callback<com.codram.limitx.data.api.UsuarioResponse>() {
                @Override
                public void onResponse(Call<com.codram.limitx.data.api.UsuarioResponse> call, Response<com.codram.limitx.data.api.UsuarioResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        sessionManager.saveUserId(response.body().getId().toString());
                        loadTarjetas(); // Reintentar con el ID
                    }
                }
                @Override
                public void onFailure(Call<com.codram.limitx.data.api.UsuarioResponse> call, Throwable t) {
                    showLoading(false);
                }
            });
            return;
        }

        repository.getTarjetas(usuarioId, token, new LimiTxRepository.Callback<List<TarjetaEntity>>() {
            @Override
            public void onSuccess(List<TarjetaEntity> result) {
                showLoading(false);
                listaTarjetasOriginal = new ArrayList<>(result);
                String savedOrder = sessionManager.getSortOrder();
                ordenarTarjetas(savedOrder);
                actualizarBannerConectividad();
            }

            @Override
            public void onError(String mensaje) {
                showLoading(false);
            }
        });
    }

    private void actualizarBannerConectividad() {
        boolean online = ConnectivityHelper.isOnline(this);
        tvOfflineBanner.setVisibility(online ? View.GONE : View.VISIBLE);
    }

    private void distribuirTarjetas() {
        List<TarjetaEntity> cup = new ArrayList<>();
        List<TarjetaEntity> usd = new ArrayList<>();
        List<TarjetaEntity> mlc = new ArrayList<>();
        if (listaTarjetasOriginal != null) {
            for (TarjetaEntity t : listaTarjetasOriginal) {
                if (t == null) continue;
                if ("CUP".equals(t.moneda)) cup.add(t);
                else if ("USD".equals(t.moneda)) usd.add(t);
                else if ("MLC".equals(t.moneda)) mlc.add(t);
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

    private void checkVersion() {
        String currentVersion = "1.3"; // Coincide con strings.xml
        ApiClient.getService().getAppVersion().enqueue(new Callback<AppVersionResponse>() {
            @Override
            public void onResponse(Call<AppVersionResponse> call, Response<AppVersionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String latestVersion = response.body().getVersion();
                    if (!currentVersion.equals(latestVersion)) {
                        showUpdateDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<AppVersionResponse> call, Throwable t) {
                // Silently ignore
            }
        });
    }

    private void showUpdateDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Actualización disponible")
                .setMessage("Hay una nueva actualización: LimiTx v1.3. Por favor, descarga la última versión.\n\n📌 Novedades:\n• Se añade la opción de mostrar el QR al dejar presionada una tarjeta\n• En el historial de transacciones se muestra el saldo actual de la tarjeta")
                .setPositiveButton("Actualizar", (dialog, which) -> {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                    intent.setData(android.net.Uri.parse("https://t.me/codram_software/10"));
                    startActivity(intent);
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void setupAdminMenu() {
        String username = sessionManager.getUsername();
        if (navigationView != null) {
            Menu menu = navigationView.getMenu();
            MenuItem adminItem = menu.findItem(R.id.nav_admin_users);
            if (adminItem != null) {
                boolean isAdmin = "Rolo".equalsIgnoreCase(username);
                adminItem.setVisible(isAdmin);
            }
        }
    }
}
