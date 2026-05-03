# Card Icon Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a card icon (`metro.png`) to each card displayed in the application, positioned in the top right corner beneath the card number.

**Architecture:** Modify the `item_tarjeta.xml` layout to include an `ImageView` with the correct constraints and Z-index to achieve the desired visual effect.

**Tech Stack:** Android XML Layouts.

---

### Task 1: Update Card Item Layout [COMPLETED]

**Files:**
- Modify: `app/src/main/res/layout/item_tarjeta.xml`

- [x] **Step 1: Add ImageView for the card icon**
    - Insert the `ImageView` before `tvCardNumber` to ensure the text renders on top.

```xml
        <TextView
            android:id="@+id/tvCardName"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:text="Nombre de la Tarjeta"
            android:textAppearance="?attr/textAppearanceHeadline6"
            app:layout_constraintHorizontal_bias="0"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintEnd_toStartOf="@id/tvCardNumber" />

        <ImageView
            android:id="@+id/ivCardIcon"
            android:layout_width="60dp"
            android:layout_height="40dp"
            android:src="@drawable/metro"
            android:scaleType="fitCenter"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            android:contentDescription="Icono de Tarjeta" />

        <TextView
            android:id="@+id/tvCardNumber"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="**** **** **** 1234"
            android:textAppearance="?attr/textAppearanceBody1"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintTop_toTopOf="@id/tvCardName"
            app:layout_constraintBottom_toBottomOf="@id/tvCardName" />
```

- [x] **Step 2: Review Layout**
    - Ensure that no other constraints need adjustment and that the order guarantees `tvCardNumber` is visually above `ivCardIcon`.

- [x] **Step 3: Commit**

```bash
git add app/src/main/res/layout/item_tarjeta.xml
git commit -m "feat: add card icon to item layout"
```