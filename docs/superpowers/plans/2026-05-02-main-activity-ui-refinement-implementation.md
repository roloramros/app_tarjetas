# MainActivity UI Refinement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the `MainActivity` UI to implement an edge-to-edge display with a modern `CoordinatorLayout` and `Toolbar`.

**Architecture:** Use `CoordinatorLayout` as the root, with an `AppBarLayout` for the toolbar. Edge-to-edge will be enabled via window flags and insets will be handled with a listener.

**Tech Stack:** Java, Android SDK, Material Design Components (CoordinatorLayout, AppBarLayout, Toolbar).

---

### Task 1: Update Theme for Edge-to-Edge

**Files:**
- Modify: `app/src/main/res/values/themes.xml`
- Modify: `app/src/main/res/values-night/themes.xml`

- [ ] **Step 1: Update Day Theme (`values/themes.xml`)**
Make the status and navigation bars transparent.

```xml
<!-- ... existing style ... -->
        <!-- Status bar color. -->
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <!-- Customize your theme here. -->
    </style>
</resources>
```
*Self-correction: I will just replace `?attr/colorPrimaryVariant` with `@android:color/transparent` and add the navigation bar color.*

- [ ] **Step 2: Update Night Theme (`values-night/themes.xml`)**
Do the same for the night theme.

```xml
<!-- ... existing style ... -->
        <!-- Status bar color. -->
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <!-- Customize your theme here. -->
    </style>
</resources>
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/res/values*/themes.xml
git commit -m "style: make system bars transparent for edge-to-edge"
```

---

### Task 2: Refactor MainActivity Layout

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`

- [ ] **Step 1: Replace root with `CoordinatorLayout` and add `AppBar`**
This is a complete rewrite of the layout file to use the new structure.

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/coordinatorLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.AppBarLayout
        android:id="@+id/appBarLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <com.google.android.material.appbar.MaterialToolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="?attr/actionBarSize"
            app:title="Mis Tarjetas" />

    </com.google.android.material.appbar.AppBarLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvTarjetas"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone"
        app:layout_behavior="@string/appbar_scrolling_view_behavior" />

    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />

    <TextView
        android:id="@+id/tvEmptyState"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="Cargando..."
        android:visibility="gone" />

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/btnLogout"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|start"
        android:layout_margin="16dp"
        android:contentDescription="Salir"
        app:srcCompat="@android:drawable/ic_lock_power_off" />

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabAdd"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:contentDescription="Añadir tarjeta"
        app:srcCompat="@android:drawable/ic_input_add" />

</androidx.coordinatorlayout.widget.CoordinatorLayout>
```
*Note: The logout button is now a FAB and uses `layout_gravity` for positioning.*

- [ ] **Step 2: Commit**
```bash
git add app/src/main/res/layout/activity_main.xml
git commit -m "refactor(MainActivity): switch to CoordinatorLayout and add Toolbar"
```

---

### Task 3: Update MainActivity Logic

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/MainActivity.java`

- [ ] **Step 1: Implement Edge-to-Edge and Insets Handling**
Update the class to enable edge-to-edge and handle the window insets.

```java
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
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/codram/limitx/MainActivity.java
git commit -m "feat(MainActivity): implement edge-to-edge and insets handling"
```
