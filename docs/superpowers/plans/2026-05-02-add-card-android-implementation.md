# Add Card Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the "Add Card" feature in the Android application, including a FAB in `MainActivity`, a `BottomSheetDialogFragment` form with bank/currency logic, and API integration.

**Architecture:** Use `FloatingActionButton` for the entry point, `BottomSheetDialogFragment` for the modal form, and Retrofit for the backend communication.

**Tech Stack:** Java, Android SDK, Retrofit, Material Design Components.

---

### Task 1: Update API Models and Service

**Files:**
- Create: `app/src/main/java/com/codram/limitx/data/api/TarjetaRequest.java`
- Create: `app/src/main/java/com/codram/limitx/data/api/TarjetaResponse.java`
- Modify: `app/src/main/java/com/codram/limitx/data/api/ApiService.java`

- [ ] **Step 1: Create TarjetaRequest.java**
```java
package com.codram.limitx.data.api;

public class TarjetaRequest {
    private String nombre;
    private String numero;
    private String banco;
    private String moneda;
    private double limite_mensual;
    private boolean activa;

    public TarjetaRequest(String nombre, String numero, String banco, String moneda, double limite_mensual, boolean activa) {
        this.nombre = nombre;
        this.numero = numero;
        this.banco = banco;
        this.moneda = moneda;
        this.limite_mensual = limite_mensual;
        this.activa = activa;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getNumero() { return numero; }
    public String getBanco() { return banco; }
    public String getMoneda() { return moneda; }
    public double getLimiteMensual() { return limite_mensual; }
    public boolean isActiva() { return activa; }
}
```

- [ ] **Step 2: Create TarjetaResponse.java**
```java
package com.codram.limitx.data.api;

import java.util.UUID;

public class TarjetaResponse {
    private UUID id;
    private UUID usuario_id;
    private String nombre;
    private String numero;
    private String banco;
    private String moneda;
    private double limite_mensual;
    private boolean activa;

    // Getters
    public UUID getId() { return id; }
    public UUID getUsuarioId() { return usuario_id; }
    public String getNombre() { return nombre; }
    public String getNumero() { return numero; }
    public String getBanco() { return banco; }
    public String getMoneda() { return moneda; }
    public double getLimiteMensual() { return limite_mensual; }
    public boolean isActiva() { return activa; }
}
```

- [ ] **Step 3: Update ApiService.java**
Add the `createTarjeta` method.
```java
package com.codram.limitx.data.api;

import retrofit2.Call;
import retrofit2.http.Body;
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
}
```

- [ ] **Step 4: Commit**
```bash
git add app/src/main/java/com/codram/limitx/data/api/*.java
git commit -m "feat: add card api models and endpoint"
```

---

### Task 2: Create BottomSheet Layout

**Files:**
- Create: `app/src/main/res/layout/layout_add_tarjeta.xml`

- [ ] **Step 1: Define the layout**
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="24dp">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Añadir Nueva Tarjeta"
        android:textSize="20sp"
        android:textStyle="bold"
        android:layout_marginBottom="24dp" />

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/tilNombre"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Nombre de la tarjeta"
        android:layout_marginBottom="16dp">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etNombre"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/tilNumero"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Número de tarjeta"
        android:layout_marginBottom="16dp">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etNumero"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="number" />
    </com.google.android.material.textfield.TextInputLayout>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Banco"
        android:layout_marginBottom="4dp" />
    <Spinner
        android:id="@+id/spinnerBanco"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Moneda"
        android:layout_marginBottom="4dp" />
    <Spinner
        android:id="@+id/spinnerMoneda"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="16dp" />

    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/tilLimite"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Límite mensual"
        android:layout_marginBottom="24dp">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etLimite"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="numberDecimal" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnSave"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="GUARDAR TARJETA"
        android:padding="12dp" />

</LinearLayout>
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/res/layout/layout_add_tarjeta.xml
git commit -m "view: add layout for card creation modal"
```

---

### Task 3: Implement AddTarjetaBottomSheet Class

**Files:**
- Create: `app/src/main/java/com/codram/limitx/AddTarjetaBottomSheet.java`

- [ ] **Step 1: Implement the class with logic**
```java
package com.codram.limitx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.TarjetaRequest;
import com.codram.limitx.data.api.TarjetaResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTarjetaBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText etNombre, etNumero, etLimite;
    private Spinner spinnerBanco, spinnerMoneda;
    private String[] bancos = {"BPA", "BANDEC", "METRO", "CLASICA", "TROPICAL"};
    private String[] monedas = {"CUP", "USD"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_tarjeta, container, false);

        etNombre = view.findViewById(R.id.etNombre);
        etNumero = view.findViewById(R.id.etNumero);
        etLimite = view.findViewById(R.id.etLimite);
        spinnerBanco = view.findViewById(R.id.spinnerBanco);
        spinnerMoneda = view.findViewById(R.id.spinnerMoneda);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);

        setupSpinners();

        btnSave.setOnClickListener(v -> saveTarjeta());

        return view;
    }

    private void setupSpinners() {
        ArrayAdapter<String> bancoAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, bancos);
        bancoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBanco.setAdapter(bancoAdapter);

        ArrayAdapter<String> monedaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, monedas);
        monedaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMoneda.setAdapter(monedaAdapter);

        spinnerBanco.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBanco = bancos[position];
                if (selectedBanco.equals("CLASICA") || selectedBanco.equals("TROPICAL")) {
                    spinnerMoneda.setSelection(1); // USD
                    spinnerMoneda.setEnabled(false);
                } else {
                    spinnerMoneda.setEnabled(true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void saveTarjeta() {
        String nombre = etNombre.getText().toString().trim();
        String numero = etNumero.getText().toString().trim();
        String banco = spinnerBanco.getSelectedItem().toString();
        String moneda = spinnerMoneda.getSelectedItem().toString();
        String limiteStr = etLimite.getText().toString().trim();

        if (nombre.isEmpty() || numero.isEmpty() || limiteStr.isEmpty()) {
            Toast.makeText(getContext(), "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double limite = Double.parseDouble(limiteStr);
        TarjetaRequest request = new TarjetaRequest(nombre, numero, banco, moneda, limite, true);
        
        SessionManager sessionManager = new SessionManager(getContext());
        String token = "Bearer " + sessionManager.getToken();

        ApiClient.getService().createTarjeta(token, request).enqueue(new Callback<TarjetaResponse>() {
            @Override
            public void onResponse(Call<TarjetaResponse> call, Response<TarjetaResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Tarjeta añadida con éxito", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Error al añadir tarjeta: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TarjetaResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/codram/limitx/AddTarjetaBottomSheet.java
git commit -m "feat: implement logic for card creation modal"
```

---

### Task 4: Update MainActivity with FAB

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/java/com/codram/limitx/MainActivity.java`

- [ ] **Step 1: Add FAB to activity_main.xml**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:padding="24dp">

    <TextView
        android:id="@+id/tvWelcome"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="¡Bienvenido a LimiTx!"
        android:textSize="24sp"
        app:layout_constraintBottom_toTopOf="@id/btnLogout"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintVertical_chainStyle="packed" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnLogout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:text="Cerrar Sesión"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintTop_toBottomOf="@id/tvWelcome" />

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

- [ ] **Step 2: Setup FAB in MainActivity.java**
```java
package com.codram.limitx;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.codram.limitx.data.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            new SessionManager(this).clearSession();
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
}
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/res/layout/activity_main.xml app/src/main/java/com/codram/limitx/MainActivity.java
git commit -m "feat: add FAB to MainActivity to open card modal"
```
