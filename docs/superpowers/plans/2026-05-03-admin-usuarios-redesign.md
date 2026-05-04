# AdminUsuariosActivity Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enhance `AdminUsuariosActivity` with standardized navigation (Side Menu, Toolbar) and improve date readability by formatting them.

**Architecture:** Update the XML layout to include a `DrawerLayout`, `Toolbar`, and `NavigationView`. Modify the Java code to handle navigation drawer events and implement date formatting logic.

**Tech Stack:** Android, Java, Material Design Components, Retrofit.

---

### Task 1: Update Layout XML

**Files:**
- Modify: `app/src/main/res/layout/activity_admin_usuarios.xml`

- [ ] **Step 1: Wrap existing content in DrawerLayout and CoordinatorLayout**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/drawerLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">

    <androidx.coordinatorlayout.widget.CoordinatorLayout
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
                app:titleTextColor="?attr/colorOnPrimary" />

        </com.google.android.material.appbar.AppBarLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            android:padding="16dp"
            app:layout_behavior="@string/appbar_scrolling_view_behavior">

            <!-- Existing Content Here -->
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Seleccionar Usuario:"
                android:textSize="18sp"
                android:textStyle="bold"
                android:layout_marginBottom="8dp"/>

            <Spinner
                android:id="@+id/spinnerUsuarios"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginBottom="24dp"/>

            <LinearLayout
                android:id="@+id/layoutUserInfo"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:visibility="gone">

                <TextView
                    android:id="@+id/tvSuscripcionActiva"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Suscripción Activa: "
                    android:textSize="16sp"
                    android:layout_marginBottom="4dp"/>

                <TextView
                    android:id="@+id/tvSuscripcionHasta"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Suscripción Hasta: "
                    android:textSize="16sp"
                    android:layout_marginBottom="4dp"/>

                <TextView
                    android:id="@+id/tvLastLogin"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Último Login: "
                    android:textSize="16sp"
                    android:layout_marginBottom="4dp"/>

                <TextView
                    android:id="@+id/tvFechaCreacion"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Fecha Creación: "
                    android:textSize="16sp"
                    android:layout_marginBottom="4dp"/>

                <TextView
                    android:id="@+id/tvCantidadTarjetas"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Tarjetas Registradas: "
                    android:textSize="16sp"
                    android:layout_marginBottom="24dp"/>

                <Button
                    android:id="@+id/btnEliminarUsuario"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Eliminar Usuario"
                    android:backgroundTint="@color/design_default_color_error"
                    android:textColor="@android:color/white"/>

            </LinearLayout>

            <ProgressBar
                android:id="@+id/progressBar"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="center"
                android:visibility="gone"/>

        </LinearLayout>

    </androidx.coordinatorlayout.widget.CoordinatorLayout>

    <!-- Menú Lateral -->
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        android:background="?android:attr/windowBackground"
        android:fitsSystemWindows="true"
        android:orientation="vertical">

        <com.google.android.material.navigation.NavigationView
            android:id="@+id/navigationView"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            app:headerLayout="@layout/nav_header"
            app:menu="@menu/nav_menu" />

        <View
            android:layout_width="match_parent"
            android:layout_height="1dp"
            android:background="?android:attr/listDivider" />

        <include layout="@layout/nav_footer" />
    </LinearLayout>

</androidx.drawerlayout.widget.DrawerLayout>
```

- [ ] **Step 2: Commit layout changes**

```bash
git add app/src/main/res/layout/activity_admin_usuarios.xml
git commit -m "ui: add DrawerLayout and Toolbar to AdminUsuariosActivity"
```

---

### Task 2: Implement Side Menu and Toolbar in Java

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/AdminUsuariosActivity.java`

- [ ] **Step 1: Add necessary imports and fields**

```java
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import android.content.Intent;
```

- [ ] **Step 2: Initialize Toolbar and DrawerLayout in onCreate**

```java
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Administración de Usuarios");
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
```

- [ ] **Step 3: Implement Navigation Item Selection and Footer Actions**

```java
        View footerLogout = findViewById(R.id.nav_footer_logout);
        if (footerLogout != null) {
            footerLogout.setOnClickListener(v -> {
                sessionManager.clearSession();
                Intent intent = new Intent(AdminUsuariosActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_add_card) {
                // Since this requires a callback usually, we'll just open a simple version or handle it
                AddTarjetaBottomSheet bottomSheet = new AddTarjetaBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "AddTarjetaBottomSheet");
            } else if (id == R.id.nav_admin_users) {
                // Already here
            } else if (id == R.id.nav_home) {
                finish(); // Go back to MainActivity
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
```

- [ ] **Step 4: Update onBackPressed**

```java
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
```

- [ ] **Step 5: Commit Java navigation changes**

```bash
git add app/src/main/java/com/codram/limitx/AdminUsuariosActivity.java
git commit -m "feat: implement side menu and toolbar in AdminUsuariosActivity"
```

---

### Task 3: Implement Date Formatting Logic

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/AdminUsuariosActivity.java`

- [ ] **Step 1: Add imports for Date Formatting**

```java
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
```

- [ ] **Step 2: Add formatBackendDate helper method**

```java
    private String formatBackendDate(String rawDate) {
        if (rawDate == null || rawDate.isEmpty()) return "N/A";
        try {
            // ISO 8601 parser
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date date = inputFormat.parse(rawDate);
            // Desired format: 01 Enero 2026
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("es", "ES"));
            String formatted = outputFormat.format(date);
            // Capitalize month if necessary (optional, SimpleDateFormat usually handles it depending on locale)
            return formatted;
        } catch (Exception e) {
            return rawDate;
        }
    }
```

- [ ] **Step 3: Update mostrarInfoUsuario to use the helper**

```java
    private void mostrarInfoUsuario(UsuarioResponse u) {
        layoutUserInfo.setVisibility(View.VISIBLE);
        tvSuscripcionActiva.setText("Suscripción Activa: " + (u.isSuscripcionActiva() ? "Sí" : "No"));
        tvSuscripcionHasta.setText("Suscripción Hasta: " + formatBackendDate(u.getSuscripcionHasta()));
        tvLastLogin.setText("Último Login: " + (u.getLastLogin() != null ? formatBackendDate(u.getLastLogin()) : "Nunca"));
        tvFechaCreacion.setText("Fecha Creación: " + formatBackendDate(u.getFechaCreacion()));
        tvCantidadTarjetas.setText("Tarjetas Registradas: " + u.getCantidadTarjetas());
    }
```

- [ ] **Step 4: Commit formatting changes**

```bash
git add app/src/main/java/com/codram/limitx/AdminUsuariosActivity.java
git commit -m "feat: format dates in AdminUsuariosActivity to dd MMMM yyyy"
```

---

### Task 4: Final Validation

- [ ] **Step 1: Verify build**

Run: `./gradlew assembleDebug` (on Windows `gradlew assembleDebug`)

- [ ] **Step 2: Manual verification (simulated)**
- Check that the Toolbar shows "Administración de Usuarios".
- Check that the hamburger icon opens the Drawer.
- Check that dates like `2026-05-01T12:00:00` show as `01 mayo 2026`.
