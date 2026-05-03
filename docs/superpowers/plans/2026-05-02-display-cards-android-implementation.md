# Display Cards Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Display a list of the user's cards in `MainActivity` using a `RecyclerView`.

**Architecture:** Fetch data from the `GET /tarjetas` endpoint using Retrofit. Use a standard `RecyclerView.Adapter` to display the data. `MainActivity` will manage the state (loading, empty, error, success).

**Tech Stack:** Java, Android SDK, Retrofit, Material Design Components, RecyclerView.

---

### Task 1: Update API Service

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/data/api/ApiService.java`

- [ ] **Step 1: Add `getTarjetas` endpoint**
Add the `GET /tarjetas` method to the `ApiService` interface.

```java
package com.codram.limitx.data.api;

import java.util.List; // Import List
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET; // Import GET
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/login")
    Call<TokenResponse> login(@Body LoginRequest request);

    @POST("/usuarios")
    Call<UsuarioResponse> register(@Body UsuarioCreate request);

    @POST("/tarjetas")
    Call<TarjetaResponse> createTarjeta(
        @Header("Authorization") String token,
        @Body TarjetaRequest request
    );

    @GET("/tarjetas")
    Call<List<TarjetaResponse>> getTarjetas(@Header("Authorization") String token);
}
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/codram/limitx/data/api/ApiService.java
git commit -m "feat: add getTarjetas endpoint to ApiService"
```

---

### Task 2: Create Card Item Layout

**Files:**
- Create: `app/src/main/res/layout/item_tarjeta.xml`

- [ ] **Step 1: Define the card item layout**
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.google.android.material.card.MaterialCardView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardElevation="4dp"
    app:cardCornerRadius="8dp">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp">

        <TextView
            android:id="@+id/tvCardName"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="Nombre de la Tarjeta"
            android:textAppearance="?attr/textAppearanceHeadline6"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintEnd_toStartOf="@id/tvBankName" />

        <TextView
            android:id="@+id/tvBankName"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="BANCO"
            android:textAppearance="?attr/textAppearanceCaption"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="parent" />

        <TextView
            android:id="@+id/tvCardNumber"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="**** **** **** 1234"
            android:textAppearance="?attr/textAppearanceBody1"
            android:layout_marginTop="8dp"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toBottomOf="@id/tvCardName" />

        <TextView
            android:id="@+id/tvCardLimit"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="$10,000.00 CUP"
            android:textAppearance="?attr/textAppearanceBody2"
            android:textStyle="bold"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="@id/tvCardNumber"
            app:layout_constraintBottom_toBottomOf="@id/tvCardNumber" />

    </androidx.constraintlayout.widget.ConstraintLayout>
</com.google.android.material.card.MaterialCardView>
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/res/layout/item_tarjeta.xml
git commit -m "view: add layout for individual card item"
```

---

### Task 3: Implement TarjetasAdapter

**Files:**
- Create: `app/src/main/java/com/codram/limitx/TarjetasAdapter.java`

- [ ] **Step 1: Implement the RecyclerView Adapter**
```java
package com.codram.limitx;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.codram.limitx.data.api.TarjetaResponse;
import java.util.List;
import java.util.Locale;

public class TarjetasAdapter extends RecyclerView.Adapter<TarjetasAdapter.TarjetaViewHolder> {

    private List<TarjetaResponse> tarjetas;

    public TarjetasAdapter(List<TarjetaResponse> tarjetas) {
        this.tarjetas = tarjetas;
    }

    @NonNull
    @Override
    public TarjetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarjeta, parent, false);
        return new TarjetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarjetaViewHolder holder, int position) {
        TarjetaResponse tarjeta = tarjetas.get(position);
        holder.bind(tarjeta);
    }

    @Override
    public int getItemCount() {
        return tarjetas.size();
    }

    static class TarjetaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCardName, tvBankName, tvCardNumber, tvCardLimit;

        public TarjetaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardName = itemView.findViewById(R.id.tvCardName);
            tvBankName = itemView.findViewById(R.id.tvBankName);
            tvCardNumber = itemView.findViewById(R.id.tvCardNumber);
            tvCardLimit = itemView.findViewById(R.id.tvCardLimit);
        }

        public void bind(TarjetaResponse tarjeta) {
            tvCardName.setText(tarjeta.getNombre());
            tvBankName.setText(tarjeta.getBanco());
            tvCardNumber.setText(obfuscateCardNumber(tarjeta.getNumero()));
            String limitText = String.format(Locale.US, "$%,.2f %s", tarjeta.getLimiteMensual(), tarjeta.getMoneda());
            tvCardLimit.setText(limitText);
        }

        private String obfuscateCardNumber(String number) {
            if (number == null || number.length() <= 4) {
                return "****";
            }
            return "**** **** **** " + number.substring(number.length() - 4);
        }
    }
}
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/codram/limitx/TarjetasAdapter.java
git commit -m "feat: implement RecyclerView adapter for cards"
```

---

### Task 4: Update MainActivity to Display Cards

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/java/com/codram/limitx/MainActivity.java`

- [ ] **Step 1: Update activity_main.xml layout**
Replace the old content with a `RecyclerView`, `ProgressBar`, and `TextView` for empty/error states. Keep the `FloatingActionButton`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/rvTarjetas"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent" />

    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <TextView
        android:id="@+id/tvEmptyState"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Cargando..."
        android:visibility="gone"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />
        
    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnLogout"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:text="Salir"
        app:icon="@android:drawable/ic_lock_power_off"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fabAdd"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:contentDescription="Añadir tarjeta"
        app:srcCompat="@android:drawable/ic_input_add"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

- [ ] **Step 2: Update MainActivity.java logic**
Implement the data loading logic, adapter setup, and `onResume` refresh.

```java
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
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/res/layout/activity_main.xml app/src/main/java/com/codram/limitx/MainActivity.java
git commit -m "feat: display cards in MainActivity with RecyclerView"
```
