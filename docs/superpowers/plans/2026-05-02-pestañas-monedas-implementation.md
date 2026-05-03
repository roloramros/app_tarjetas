# Pestañas de Moneda (CUP/USD) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar una interfaz de pestañas (Tabs) en la pantalla principal para separar las tarjetas por moneda (CUP y USD), con totales independientes.

**Architecture:** 
- `MainActivity` descargará los datos y los distribuirá a dos instancias de `TarjetasFragment`.
- Se utilizará `ViewPager2` con un `FragmentStateAdapter` para la navegación entre pestañas.
- El `TabLayout` se sincronizará con el `ViewPager2`.
- El saldo total en la Toolbar se actualizará dinámicamente según la pestaña seleccionada.

**Tech Stack:** Android (Java), ViewPager2, TabLayout, Material Components.

---

### Task 1: Añadir dependencia de ViewPager2

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Añadir a libs.versions.toml**
```toml
# En [versions]
viewpager2 = "1.1.0"

# En [libraries]
viewpager2 = { group = "androidx.viewpager2", name = "viewpager2", version.ref = "viewpager2" }
```

- [ ] **Step 2: Añadir a app/build.gradle.kts**
```kotlin
dependencies {
    // ...
    implementation(libs.viewpager2)
}
```

- [ ] **Step 3: Commit**
```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "chore: añadir dependencia de ViewPager2"
```

### Task 2: Crear TarjetasFragment

**Files:**
- Create: `app/src/main/res/layout/fragment_tarjetas.xml`
- Create: `app/src/main/java/com/codram/limitx/TarjetasFragment.java`

- [ ] **Step 1: Crear layout fragment_tarjetas.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/swipeRefresh"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rvTarjetas"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:visibility="gone" />

        <TextView
            android:id="@+id/tvEmptyState"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="No hay tarjetas"
            android:visibility="gone" />

        <ProgressBar
            android:id="@+id/progressBar"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:visibility="gone" />

    </FrameLayout>

</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

- [ ] **Step 2: Crear clase TarjetasFragment.java**
```java
package com.codram.limitx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.codram.limitx.data.api.TarjetaResponse;
import java.util.ArrayList;
import java.util.List;

public class TarjetasFragment extends Fragment {

    private RecyclerView rvTarjetas;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TarjetasAdapter adapter;
    private List<TarjetaResponse> tarjetas = new ArrayList<>();
    private Runnable onRefreshListener;
    private TarjetasAdapter.OnTransactionAddedListener transactionListener;

    public static TarjetasFragment newInstance() {
        return new TarjetasFragment();
    }

    public void setOnRefreshListener(Runnable listener) {
        this.onRefreshListener = listener;
    }

    public void setTransactionListener(TarjetasAdapter.OnTransactionAddedListener listener) {
        this.transactionListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tarjetas, container, false);
        rvTarjetas = view.findViewById(R.id.rvTarjetas);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        rvTarjetas.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefresh.setOnRefreshListener(() -> {
            if (onRefreshListener != null) onRefreshListener.run();
        });

        return view;
    }

    public void updateData(List<TarjetaResponse> nuevasTarjetas) {
        this.tarjetas = nuevasTarjetas;
        if (adapter == null) {
            adapter = new TarjetasAdapter(tarjetas, transactionListener);
            rvTarjetas.setAdapter(adapter);
        } else {
            adapter.updateData(tarjetas);
        }
        
        showLoading(false);
        if (tarjetas.isEmpty()) {
            rvTarjetas.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvTarjetas.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    public void showLoading(boolean isLoading) {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(isLoading);
        if (progressBar != null) progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}
```

### Task 3: Crear ViewPagerAdapter

**Files:**
- Create: `app/src/main/java/com/codram/limitx/TarjetasPagerAdapter.java`

- [ ] **Step 1: Implementar TarjetasPagerAdapter**
```java
package com.codram.limitx;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TarjetasPagerAdapter extends FragmentStateAdapter {
    private final TarjetasFragment cupFragment;
    private final TarjetasFragment usdFragment;

    public TarjetasPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        cupFragment = TarjetasFragment.newInstance();
        usdFragment = TarjetasFragment.newInstance();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? cupFragment : usdFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public TarjetasFragment getCupFragment() { return cupFragment; }
    public TarjetasFragment getUsdFragment() { return usdFragment; }
}
```

### Task 4: Actualizar layout de MainActivity

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`

- [ ] **Step 1: Añadir TabLayout y ViewPager2**
```xml
<!-- Reemplazar el SwipeRefreshLayout actual -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    app:layout_behavior="@string/appbar_scrolling_view_behavior">

    <com.google.android.material.tabs.TabLayout
        android:id="@+id/tabLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:tabMode="fixed"
        app:tabGravity="fill" />

    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</LinearLayout>
```

### Task 5: Refactorizar MainActivity para soportar Pestañas

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/MainActivity.java`

- [ ] **Step 1: Integrar TabLayout, ViewPager2 y lógica de totales**
```java
// ... (imports)
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TarjetasPagerAdapter pagerAdapter;
    // ...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ... (toolbar, sessionManager init)
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        
        pagerAdapter = new TarjetasPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "CUP" : "USD");
        }).attach();

        // Configurar listeners en fragmentos
        pagerAdapter.getCupFragment().setOnRefreshListener(this::loadTarjetas);
        pagerAdapter.getUsdFragment().setOnRefreshListener(this::loadTarjetas);
        pagerAdapter.getCupFragment().setTransactionListener(this::loadTarjetas);
        pagerAdapter.getUsdFragment().setTransactionListener(this::loadTarjetas);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                actualizarSaldoTotal();
            }
        });
        
        // ... (fab, logout)
    }

    private void actualizarSaldoTotal() {
        int position = viewPager.getCurrentItem();
        String monedaFiltro = (position == 0) ? "CUP" : "USD";
        
        double total = 0;
        for (TarjetaResponse tarjeta : listaTarjetasOriginal) {
            if (tarjeta.getMoneda().equals(monedaFiltro)) {
                total += tarjeta.getSaldo_tarjeta();
            }
        }
        // ... (formatear y setear en tvSaldoTotal)
    }

    private void loadTarjetas() {
        pagerAdapter.getCupFragment().showLoading(true);
        pagerAdapter.getUsdFragment().showLoading(true);
        // ... (ApiClient call)
        // En onResponse exitoso:
        listaTarjetasOriginal = response.body();
        distribuirTarjetas();
        actualizarSaldoTotal();
    }

    private void distribuirTarjetas() {
        List<TarjetaResponse> cup = new ArrayList<>();
        List<TarjetaResponse> usd = new ArrayList<>();
        for (TarjetaResponse t : listaTarjetasOriginal) {
            if ("CUP".equals(t.getMoneda())) cup.add(t);
            else usd.add(t);
        }
        pagerAdapter.getCupFragment().updateData(cup);
        pagerAdapter.getUsdFragment().updateData(usd);
    }
}
```

### Task 6: Verificación Final

- [ ] **Step 1: Compilar y probar**
Verificar el deslizamiento entre CUP y USD, el refresco en ambas pestañas y la actualización del saldo total superior.
