# Transaction Dialog Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the "Add Transaction" dialog into a shared utility and use it in `HistorialActivity` FABs.

**Architecture:** Create `TransactionDialogHelper` with a static method to show the dialog and a shared callback interface.

**Tech Stack:** Android (Java), Material Components, Retrofit.

---

### Task 1: Create `TransactionDialogHelper`

**Files:**
- Create: `app/src/main/java/com/codram/limitx/utils/TransactionDialogHelper.java`

- [ ] **Step 1: Create the utility class and interface**

```java
package com.codram.limitx.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.codram.limitx.R;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.TransaccionRequest;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionDialogHelper {

    public interface OnTransactionAddedListener {
        void onTransactionAdded();
    }

    public static void showTransactionDialog(Context context, String tipo, UUID tarjetaId, String tarjetaNombre, OnTransactionAddedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_transaccion, null);
        TextInputEditText etMonto = dialogView.findViewById(R.id.etMonto);
        TextInputEditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        TextInputEditText etSubtipo = dialogView.findViewById(R.id.etSubtipo);
        Button btnFecha = dialogView.findViewById(R.id.btnFecha);
        SwitchMaterial swAfectaLimite = dialogView.findViewById(R.id.swAfectaLimite);

        if (tipo.equalsIgnoreCase("Depósito") || tipo.equalsIgnoreCase("Entrada")) {
            swAfectaLimite.setVisibility(View.GONE);
        }

        final String[] fechaSeleccionada = {LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)};

        btnFecha.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Seleccionar fecha")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                fechaSeleccionada[0] = LocalDateTime.of(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    0, 0
                ).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                btnFecha.setText(datePicker.getHeaderText());
            });
            datePicker.show(((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager(), "DATE_PICKER");
        });

        new MaterialAlertDialogBuilder(context)
            .setTitle(tipo + ": " + tarjetaNombre)
            .setView(dialogView)
            .setPositiveButton("Aceptar", (dialog, which) -> {
                String montoStr = etMonto.getText().toString();
                if (montoStr.isEmpty()) {
                    Toast.makeText(context, "El monto es obligatorio", Toast.LENGTH_SHORT).show();
                    return;
                }

                BigDecimal monto = new BigDecimal(montoStr);
                String tipoApi = (tipo.equalsIgnoreCase("Depósito") || tipo.equalsIgnoreCase("Entrada")) ? "entrada" : "salida";
                
                TransaccionRequest request = new TransaccionRequest(
                    tarjetaId, tipoApi, monto,
                    etDescripcion.getText().toString(), etSubtipo.getText().toString(),
                    swAfectaLimite.isChecked(), fechaSeleccionada[0]
                );

                String token = new SessionManager(context).getToken();
                ApiClient.getService().crearTransaccion("Bearer " + token, request).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "Transacción guardada", Toast.LENGTH_SHORT).show();
                            if (listener != null) listener.onTransactionAdded();
                        } else {
                            Toast.makeText(context, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/codram/limitx/utils/TransactionDialogHelper.java
git commit -m "feat: add TransactionDialogHelper for reusable transaction dialogs"
```

### Task 2: Refactor `TarjetasAdapter`

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/TarjetasAdapter.java`

- [ ] **Step 1: Update imports and remove old interface**

Remove:
```java
    public interface OnTransactionAddedListener {
        void onTransactionAdded();
    }
```

Add import:
```java
import com.codram.limitx.utils.TransactionDialogHelper;
```

Update class member type:
```java
    private TransactionDialogHelper.OnTransactionAddedListener transactionListener;
```

- [ ] **Step 2: Update constructors and navigation**

Update constructors to use `TransactionDialogHelper.OnTransactionAddedListener`.
Update Intent in `onBindViewHolder`:
```java
                android.content.Intent intent = new android.content.Intent(v.getContext(), HistorialActivity.class);
                intent.putExtra("TARJETA_ID", tarjeta.getId().toString());
                intent.putExtra("TARJETA_NOMBRE", tarjeta.getNombre()); // NEW
                v.getContext().startActivity(intent);
```

- [ ] **Step 3: Replace `mostrarDialogoTransaccion` with helper call**

In `onBindViewHolder` (LongPress):
```java
                    if (item.getTitle().equals("Añadir Depósito") || item.getTitle().equals("Añadir Extracción")) {
                        TransactionDialogHelper.showTransactionDialog(v.getContext(), item.getTitle().toString(), tarjeta.getId(), tarjeta.getNombre(), transactionListener);
                        return true;
                    }
```

Remove the private `mostrarDialogoTransaccion` method.

- [ ] **Step 4: Update `MainActivity` and other consumers**

Since the interface moved, `MainActivity`, `TarjetasFragment`, etc., need to update their imports.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/codram/limitx/TarjetasAdapter.java
git commit -m "refactor: use TransactionDialogHelper in TarjetasAdapter"
```

### Task 3: Integrate into `HistorialActivity`

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/HistorialActivity.java`

- [ ] **Step 1: Retrieve and store Intent extras**

Add fields:
```java
    private UUID tarjetaId;
    private String tarjetaNombre;
```

Update `onCreate`:
```java
        String idStr = getIntent().getStringExtra("TARJETA_ID");
        tarjetaNombre = getIntent().getStringExtra("TARJETA_NOMBRE");
        if (idStr != null) {
            tarjetaId = UUID.fromString(idStr);
            cargarTransacciones(tarjetaId);
        }
```

- [ ] **Step 2: Update FAB click listeners**

```java
        findViewById(R.id.fabAdd).setOnClickListener(v ->
                com.codram.limitx.utils.TransactionDialogHelper.showTransactionDialog(this, "Depósito", tarjetaId, tarjetaNombre, () -> cargarTransacciones(tarjetaId)));

        findViewById(R.id.fabRemove).setOnClickListener(v ->
                com.codram.limitx.utils.TransactionDialogHelper.showTransactionDialog(this, "Extracción", tarjetaId, tarjetaNombre, () -> cargarTransacciones(tarjetaId)));
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/codram/limitx/HistorialActivity.java
git commit -m "feat: enable transaction creation from HistorialActivity FABs"
```

### Task 4: Fix compile errors in other files

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/MainActivity.java`
- Modify: `app/src/main/java/com/codram/limitx/TarjetasFragment.java`
- Modify: `app/src/main/java/com/codram/limitx/TarjetasPagerAdapter.java`

- [ ] **Step 1: Update imports of `OnTransactionAddedListener`**

Change `com.codram.limitx.TarjetasAdapter.OnTransactionAddedListener` to `com.codram.limitx.utils.TransactionDialogHelper.OnTransactionAddedListener`.

- [ ] **Step 2: Commit**

```bash
git commit -a -m "fix: update references to OnTransactionAddedListener"
```
