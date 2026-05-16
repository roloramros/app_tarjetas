# Transaction History FABs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add two Floating Action Buttons (FABs) to the transaction history screen for quick access to deposit and extraction actions (currently showing Toasts).

**Architecture:** Use `CoordinatorLayout` to position a `LinearLayout` containing two `FloatingActionButton`s in the bottom-right corner.

**Tech Stack:** Android (Java), Material Components.

---

### Task 1: Resources (Colors and Icons)

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/drawable/ic_add.xml`
- Create: `app/src/main/res/drawable/ic_remove.xml`

- [ ] **Step 1: Add colors to `colors.xml`**

```xml
    <color name="green_500">#4CAF50</color>
    <color name="red_500">#F44336</color>
```

- [ ] **Step 2: Create `ic_add.xml`**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#FFFFFF">
  <path
      android:fillColor="@android:color/white"
      android:pathData="M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2Z"/>
</vector>
```

- [ ] **Step 3: Create `ic_remove.xml`**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="#FFFFFF">
  <path
      android:fillColor="@android:color/white"
      android:pathData="M19,13H5v-2h14v2Z"/>
</vector>
```

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/values/colors.xml app/src/main/res/drawable/ic_add.xml app/src/main/res/drawable/ic_remove.xml
git commit -m "style: add colors and icons for transaction FABs"
```

### Task 2: Layout Update

**Files:**
- Modify: `app/src/main/res/layout/activity_historial.xml`

- [ ] **Step 1: Add FABs to `activity_historial.xml`**

Add the following block inside the `CoordinatorLayout`, as the last child:

```xml
    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_margin="16dp"
        android:orientation="vertical">

        <com.google.android.material.floatingactionbutton.FloatingActionButton
            android:id="@+id/fabAdd"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            android:src="@drawable/ic_add"
            app:backgroundTint="@color/green_500"
            app:tint="@android:color/white"
            android:contentDescription="Añadir depósito" />

        <com.google.android.material.floatingactionbutton.FloatingActionButton
            android:id="@+id/fabRemove"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:src="@drawable/ic_remove"
            app:backgroundTint="@color/red_500"
            app:tint="@android:color/white"
            android:contentDescription="Añadir extracción" />
    </LinearLayout>
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/res/layout/activity_historial.xml
git commit -m "layout: add stacked FABs to HistorialActivity"
```

### Task 3: Logic Implementation

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/HistorialActivity.java`

- [ ] **Step 1: Initialize FABs and set ClickListeners**

In `onCreate`, after setting up the RecyclerViews:

```java
        findViewById(R.id.fabAdd).setOnClickListener(v -> 
            Toast.makeText(this, "Añadir depósito", Toast.LENGTH_SHORT).show());
        
        findViewById(R.id.fabRemove).setOnClickListener(v -> 
            Toast.makeText(this, "Añadir extracción", Toast.LENGTH_SHORT).show());
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/codram/limitx/HistorialActivity.java
git commit -m "feat: implement Toast actions for transaction FABs"
```

### Task 4: Verification

- [ ] **Step 1: Manual Verification**
- Run the app and navigate to the transaction history.
- Confirm both buttons are visible in the bottom-right.
- Confirm Green button shows "Añadir depósito".
- Confirm Red button shows "Añadir extracción".
