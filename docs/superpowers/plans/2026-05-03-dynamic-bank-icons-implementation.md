# Dynamic Bank Icons Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Show a dynamic bank icon for each card based on its database value (bpa, clasica, metro, bandec, tropical) and remove the redundant bank name text from the layout.

**Architecture:** 
1. Remove `tvBankName` from `item_tarjeta.xml` and update `ivCardIcon` to have slight transparency.
2. Update `TarjetasAdapter.java` to remove references to `tvBankName` and programmatically map the `tarjeta.getBanco()` string to the corresponding drawable resource for `ivCardIcon`.

**Tech Stack:** Android XML Layouts, Java.

---

### Task 1: Update Card Item Layout

**Files:**
- Modify: `app/src/main/res/layout/item_tarjeta.xml`

- [ ] **Step 1: Modify ImageView and Remove TextView**
    - Remove the `tvBankName` TextView.
    - Remove the hardcoded `android:src="@drawable/metro"` from `ivCardIcon`.
    - Add `android:alpha="0.4"` to `ivCardIcon` to ensure the card number remains readable.

```xml
        <ImageView
            android:id="@+id/ivCardIcon"
            android:layout_width="60dp"
            android:layout_height="40dp"
            android:scaleType="fitCenter"
            android:alpha="0.4"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            android:contentDescription="@string/card_icon_description" />
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/layout/item_tarjeta.xml
git commit -m "refactor: update card layout for dynamic icons and remove text"
```

---

### Task 2: Update TarjetasAdapter Logic

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/TarjetasAdapter.java`

- [ ] **Step 1: Update ViewHolder and bind method**
    - Remove `tvBankName` references from `TarjetaViewHolder`.
    - Add `ImageView ivCardIcon;` to `TarjetaViewHolder`.
    - Initialize `ivCardIcon = itemView.findViewById(R.id.ivCardIcon);`
    - In the `bind` method, map the `tarjeta.getBanco()` value to the corresponding drawable and set it to `ivCardIcon`.

```java
    static class TarjetaViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCardName, tvCardNumber, tvSaldoTarjeta, tvExtraccionDisponible, tvDepositoDisponible;
        private android.widget.ImageView ivCardIcon; // Add import if necessary or use fully qualified name

        public TarjetaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardName = itemView.findViewById(R.id.tvCardName);
            // tvBankName removed
            ivCardIcon = itemView.findViewById(R.id.ivCardIcon);
            tvCardNumber = itemView.findViewById(R.id.tvCardNumber);
            tvSaldoTarjeta = itemView.findViewById(R.id.tvSaldoTarjeta);
            tvExtraccionDisponible = itemView.findViewById(R.id.tvExtraccionDisponible);
            tvDepositoDisponible = itemView.findViewById(R.id.tvDepositoDisponible);
        }

        public void bind(TarjetaResponse tarjeta) {
            tvCardName.setText(tarjeta.getNombre());
            // tvBankName logic removed
            
            // Set dynamic icon
            String bancoStr = tarjeta.getBanco();
            int iconResId = R.drawable.clasica; // Default fallback
            if (bancoStr != null) {
                String bancoLower = bancoStr.toLowerCase().trim();
                switch (bancoLower) {
                    case "bpa":
                        iconResId = R.drawable.bpa;
                        break;
                    case "metro":
                        iconResId = R.drawable.metro;
                        break;
                    case "bandec":
                        iconResId = R.drawable.bandec;
                        break;
                    case "tropical":
                        iconResId = R.drawable.tropical;
                        break;
                    case "clasica":
                        iconResId = R.drawable.clasica;
                        break;
                    default:
                        iconResId = R.drawable.clasica;
                        break;
                }
            }
            ivCardIcon.setImageResource(iconResId);

            tvCardNumber.setText(obfuscateCardNumber(tarjeta.getNumero()));
            // ... rest of the existing bind logic remains unchanged
```

- [ ] **Step 2: Verify Compilation**

Run: `./gradlew compileDebugJavaWithJavac`
Expected: Build SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/codram/limitx/TarjetasAdapter.java
git commit -m "feat: implement dynamic bank icons mapping in adapter"
```