# Design Doc - User Administration Statistics

Enhancing the User Administration window with global system stats and user-specific transaction counts.

## Problem Statement
The current User Administration window only shows basic user info and the number of cards. The administrator needs a higher-level overview of the entire system (total users, cards, and transactions) and more detail per user (total transactions).

## Proposed Solution

### 1. Backend (FastAPI / PostgreSQL)
- **Schema:** 
    - Create `AdminStatsResponse` in `schemas.py` for global stats.
    - Update `UsuarioResponse` in `schemas.py` to include `cantidad_transacciones`.
- **Endpoints:**
    - New `GET /admin/stats` (Restricted to 'Rolo'): Returns total users, total cards, and total transactions.
    - Update `GET /usuarios`: Update the query to also join with `Transaccion` (or use a subquery) to count total transactions per user.

### 2. Android (Java)
- **Models:** 
    - Create `AdminStatsResponse.java`.
    - Update `UsuarioResponse.java` to include `cantidadTransacciones`.
- **API Service:** 
    - Add `getAdminStats()` to `ApiService.java`.
- **UI (Activity & Layout):**
    - **Global Stats:** Add a new section at the top of `activity_admin_usuarios.xml` (e.g., inside a `CardView` or a styled `LinearLayout`) to display:
        - "Usuarios Totales: X"
        - "Tarjetas Totales: Y"
        - "Transacciones Totales: Z"
    - **User Info:** Add a new `TextView` in the user details section to show:
        - "Transacciones Realizadas: W"
- **Logic:**
    - Call `getAdminStats()` in `onCreate` of `AdminUsuariosActivity` and update the global stats section.
    - Update `mostrarInfoUsuario()` to display the new transaction count from the `UsuarioResponse` object.

## Detailed Design

### Global Stats Section (Layout)
A simple `CardView` at the top of the scrollable content with three `TextView`s.
```xml
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardGlobalStats"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="24dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="4dp">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        <TextView android:id="@+id/tvTotalUsuarios" ... text="Usuarios Totales: --" />
        <TextView android:id="@+id/tvTotalTarjetas" ... text="Tarjetas Totales: --" />
        <TextView android:id="@+id/tvTotalTransacciones" ... text="Transacciones Totales: --" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

### Backend Endpoint Logic
The `GET /admin/stats` will use a single query with scalar subqueries for efficiency:
```python
stmt = select(
    select(func.count()).select_from(Usuario).scalar_subquery().label("total_usuarios"),
    select(func.count()).select_from(Tarjeta).scalar_subquery().label("total_tarjetas"),
    select(func.count()).select_from(Transaccion).scalar_subquery().label("total_transacciones")
)
```

## Testing Plan
- **Backend:** Verify `/admin/stats` returns correct counts. Verify `/usuarios` includes the correct `cantidad_transacciones` for each user.
- **Android:** 
    - Verify global stats load correctly on activity start.
    - Verify selecting a user updates the "Transacciones Realizadas" field.
    - Verify deleting a user refreshes both global and user-specific stats.
