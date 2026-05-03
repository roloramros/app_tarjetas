# Implementación de Añadir Transacciones (Long Press en Tarjeta)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar un menú contextual (Long Press) en las tarjetas de la pantalla principal que lance un `AlertDialog` para registrar una nueva transacción.

**Architecture:** Se utilizará un `OnLongClickListener` en el `TarjetasAdapter` que invocará un `PopupMenu`, y luego, tras la selección del tipo, lanzará un `AlertDialog` con el formulario de transacción.

**Tech Stack:** Android (Java), Material Components (AlertDialog, MaterialDatePicker, SwitchMaterial).

---

### Task 1: Crear layout del AlertDialog (`dialog_transaccion.xml`)

**Files:**
- Create: `app/src/main/res/layout/dialog_transaccion.xml`

- [ ] **Step 1: Crear el archivo de layout**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Monto">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etMonto"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:inputType="numberDecimal" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Descripción">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etDescripcion"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
    </com.google.android.material.textfield.TextInputLayout>

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:hint="Subtipo">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etSubtipo"
            android:layout_width="match_parent"
            android:layout_height="wrap_content" />
    </com.google.android.material.textfield.TextInputLayout>

    <Button
        android:id="@+id/btnFecha"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Seleccionar Fecha"
        style="?attr/materialButtonOutlinedStyle" />

    <com.google.android.material.switchmaterial.SwitchMaterial
        android:id="@+id/swAfectaLimite"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Afecta Límite"
        android:checked="true"
        android:layout_marginTop="8dp" />

</LinearLayout>
```

### Task 2: Modificar `TarjetasAdapter` para manejar el Long Press

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/TarjetasAdapter.java`

- [ ] **Step 1: Implementar el LongClick listener en el ViewHolder**

```java
// Dentro del método onBindViewHolder
holder.itemView.setOnLongClickListener(v -> {
    PopupMenu popup = new PopupMenu(v.getContext(), v);
    popup.getMenu().add("Entrada");
    popup.getMenu().add("Salida");
    popup.setOnMenuItemClickListener(item -> {
        mostrarDialogoTransaccion(v.getContext(), item.getTitle().toString(), tarjetas.get(holder.getAdapterPosition()));
        return true;
    });
    popup.show();
    return true;
});
```

- [ ] **Step 2: Implementar el método `mostrarDialogoTransaccion` dentro de `TarjetasAdapter`**

```java
private void mostrarDialogoTransaccion(Context context, String tipo, TarjetaResponse tarjeta) {
    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_transaccion, null);
    TextInputEditText etMonto = dialogView.findViewById(R.id.etMonto);
    TextInputEditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
    TextInputEditText etSubtipo = dialogView.findViewById(R.id.etSubtipo);
    Button btnFecha = dialogView.findViewById(R.id.btnFecha);
    SwitchMaterial swAfectaLimite = dialogView.findViewById(R.id.swAfectaLimite);

    new MaterialAlertDialogBuilder(context)
        .setTitle(tipo + ": " + tarjeta.getNombre())
        .setView(dialogView)
        .setPositiveButton("Aceptar", (dialog, which) -> {
            String resumen = tipo + " de " + etMonto.getText().toString();
            Toast.makeText(context, resumen, Toast.LENGTH_SHORT).show();
        })
        .setNegativeButton("Cancelar", null)
        .show();
}
```

### Task 3: Verificación e integración

- [ ] **Step 1: Compilar y verificar**
Ejecutar la app y realizar un clic largo sobre una tarjeta para abrir el popup y luego el diálogo. Asegurarse de que el `Toast` aparece al dar "Aceptar".

- [ ] **Step 2: Commit**
```bash
git add app/src/main/res/layout/dialog_transaccion.xml app/src/main/java/com/codram/limitx/TarjetasAdapter.java
git commit -m "feat: implementar menú contextual y modal para transacciones"
```
