# Card Background Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Customize the card item UI in `MainActivity` to show `clasica.png` as the background for cards of type 'CLASICA'.

**Architecture:** Use an `ImageView` in `item_tarjeta.xml` to hold the background image. Add a semi-transparent view overlay for readability. Dynamically update this background in `TarjetasAdapter`.

**Tech Stack:** Java, Android SDK.

---

### Task 1: Update Card Item Layout

**Files:**
- Modify: `app/src/main/res/layout/item_tarjeta.xml`

- [ ] **Step 1: Add ImageView and Overlay**
Insert the `ImageView` and a `View` (overlay) inside the `ConstraintLayout` of `item_tarjeta.xml`.

```xml
<!-- Inside the ConstraintLayout of item_tarjeta.xml -->

<ImageView
    android:id="@+id/ivCardBackground"
    android:layout_width="0dp"
    android:layout_height="0dp"
    android:scaleType="centerCrop"
    android:visibility="gone"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    tools:src="@drawable/clasica" />

<View
    android:id="@+id/vOverlay"
    android:layout_width="0dp"
    android:layout_height="0dp"
    android:background="#80000000"
    android:visibility="gone"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/res/layout/item_tarjeta.xml
git commit -m "view: add imageview and overlay to item_tarjeta layout"
```

---

### Task 2: Update TarjetasAdapter Logic

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/TarjetasAdapter.java`

- [ ] **Step 1: Update ViewHolder and bind method**
Update `TarjetaViewHolder` to find and control the `ImageView` and overlay.

```java
// Inside TarjetaViewHolder class
private ImageView ivCardBackground;
private View vOverlay;

public TarjetaViewHolder(@NonNull View itemView) {
    super(itemView);
    // ... existing initializations
    ivCardBackground = itemView.findViewById(R.id.ivCardBackground);
    vOverlay = itemView.findViewById(R.id.vOverlay);
}

public void bind(TarjetaResponse tarjeta) {
    // ... existing bindings
    
    if ("CLASICA".equalsIgnoreCase(tarjeta.getBanco())) {
        ivCardBackground.setVisibility(View.VISIBLE);
        vOverlay.setVisibility(View.VISIBLE);
        ivCardBackground.setImageResource(R.drawable.clasica);
    } else {
        ivCardBackground.setVisibility(View.GONE);
        vOverlay.setVisibility(View.GONE);
    }
}
```

- [ ] **Step 2: Commit**
```bash
git add app/src/main/java/com/codram/limitx/TarjetasAdapter.java
git commit -m "feat: implement dynamic card background based on type"
```
