# TCP Card Option Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a "TCP" option to the "Add Card" dialog that sets the card's monthly limit to 0 when enabled.

**Architecture:** Add a `SwitchMaterial` to `layout_add_tarjeta.xml`. In `AddTarjetaBottomSheet.java`, read the state of this switch before saving the card; if checked, force the `limite_mensual` to 0.

**Tech Stack:** Android XML Layouts, Java.

---

### Task 1: Update Add Card Layout

**Files:**
- Modify: `app/src/main/res/layout/layout_add_tarjeta.xml`

- [ ] **Step 1: Add SwitchMaterial for TCP option**
    - Insert a `com.google.android.material.switchmaterial.SwitchMaterial` below the `spinnerMoneda`.

```xml
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

    <com.google.android.material.switchmaterial.SwitchMaterial
        android:id="@+id/swTcp"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Es tarjeta TCP"
        android:layout_marginBottom="24dp" />
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/layout/layout_add_tarjeta.xml
git commit -m "feat: add TCP switch to add card layout"
```

---

### Task 2: Implement Logic in AddTarjetaBottomSheet

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/AddTarjetaBottomSheet.java`

- [ ] **Step 1: Initialize SwitchMaterial**
    - Add import: `com.google.android.material.switchmaterial.SwitchMaterial`
    - Declare field: `private SwitchMaterial swTcp;`
    - Initialize it in `onCreateDialog`.

```java
        spinnerBanco = view.findViewById(R.id.spinnerBanco);
        spinnerMoneda = view.findViewById(R.id.spinnerMoneda);
        swTcp = view.findViewById(R.id.swTcp); // Add this line
```

- [ ] **Step 2: Update saveTarjeta logic**
    - Read `swTcp.isChecked()` and set `limite` to 0 if true.

```java
    private void saveTarjeta() {
        String nombre = etNombre.getText().toString().trim();
        String numero = etNumero.getText().toString().trim();
        String banco = spinnerBanco.getSelectedItem().toString();
        String moneda = spinnerMoneda.getSelectedItem().toString();

        if (nombre.isEmpty() || numero.isEmpty()) {
            Toast.makeText(getContext(), "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double limite = 0.0;
        if (!swTcp.isChecked()) {
            limite = moneda.equals("CUP") ? 120000.0 : 5000.0;
        }

        TarjetaRequest request = new TarjetaRequest(nombre, numero, banco, moneda, limite, true);
```

- [ ] **Step 3: Verify Compilation**

Run: `./gradlew compileDebugJavaWithJavac`
Expected: Build SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/codram/limitx/AddTarjetaBottomSheet.java
git commit -m "feat: apply 0 limit when TCP option is selected"
```