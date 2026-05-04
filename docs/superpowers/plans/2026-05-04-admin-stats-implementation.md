# Admin Statistics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enhance the Admin view with global system statistics and user-specific transaction counts.

**Architecture:** 
- Add a new endpoint `GET /admin/stats` to the FastAPI backend for global totals.
- Update the backend user listing to count transactions per user.
- Update the Android UI to display these statistics in both a general overview and detailed user info.

**Tech Stack:** FastAPI (Python), SQLAlchemy, Retrofit (Android), Java.

---

### Task 1: Backend Schema Updates

**Files:**
- Modify: `api/schemas.py`

- [ ] **Step 1: Update schemas.py**
Add `AdminStatsResponse` and update `UsuarioResponse`.

```python
# api/schemas.py

# ... (after UsuarioResponse definition or near other responses)
class AdminStatsResponse(BaseModel):
    total_usuarios: int
    total_tarjetas: int
    total_transacciones: int
    model_config = ConfigDict(from_attributes=True)

# ... (Update UsuarioResponse)
class UsuarioResponse(UsuarioBase):
    id: uuid.UUID
    suscripcion_activa: bool
    suscripcion_hasta: Optional[datetime] = None
    last_login: Optional[datetime] = None
    fecha_creacion: Optional[datetime] = None
    cantidad_tarjetas: Optional[int] = 0
    cantidad_transacciones: Optional[int] = 0  # <--- Add this
    model_config = ConfigDict(from_attributes=True)
```

- [ ] **Step 2: Verify syntax**
Run: `python -m py_compile api/schemas.py`
Expected: Success

- [ ] **Step 3: Commit**
```bash
git add api/schemas.py
git commit -m "api: update schemas for admin stats"
```

---

### Task 2: Backend Endpoint Implementation

**Files:**
- Modify: `api/main.py`

- [ ] **Step 1: Implement GET /admin/stats**
Add the new endpoint and update the `listar_usuarios` query.

```python
# api/main.py

# ... (Update listar_usuarios)
@app.get("/usuarios", response_model=list[UsuarioResponse])
async def listar_usuarios(
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    # Join with Tarjeta and Transaccion to get counts
    # Subqueries are cleaner for multiple counts to avoid cartesian products if not careful
    query = select(
        Usuario,
        select(func.count(Tarjeta.id)).where(Tarjeta.usuario_id == Usuario.id).scalar_subquery().label("cantidad_tarjetas"),
        select(func.count(Transaccion.id)).where(Transaccion.tarjeta_id.in_(
            select(Tarjeta.id).where(Tarjeta.usuario_id == Usuario.id)
        )).scalar_subquery().label("cantidad_transacciones")
    ).order_by(Usuario.fecha_creacion.desc())
    
    result = await db.execute(query)
    usuarios_con_conteo = []
    for usuario, cant_tarjetas, cant_trans in result:
        u_dict = UsuarioResponse.model_validate(usuario).model_dump()
        u_dict["cantidad_tarjetas"] = cant_tarjetas
        u_dict["cantidad_transacciones"] = cant_trans
        usuarios_con_conteo.append(u_dict)
        
    return usuarios_con_conteo

# ... (Add /admin/stats)
@app.get("/admin/stats", response_model=AdminStatsResponse)
async def obtener_stats_admin(
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    # Simple check for admin role (Rolo)
    if current_user.nombre != "Rolo":
        raise HTTPException(status_code=403, detail="Acceso denegado")
        
    total_usuarios = await db.scalar(select(func.count(Usuario.id)))
    total_tarjetas = await db.scalar(select(func.count(Tarjeta.id)))
    total_transacciones = await db.scalar(select(func.count(Transaccion.id)))
    
    return AdminStatsResponse(
        total_usuarios=total_usuarios or 0,
        total_tarjetas=total_tarjetas or 0,
        total_transacciones=total_transacciones or 0
    )
```

- [ ] **Step 2: Verify endpoints**
(Assumes backend is running or can be tested via mock)
Run: `pytest tests/api/test_admin_stats.py` (Need to create this test first)

- [ ] **Step 3: Commit**
```bash
git add api/main.py
git commit -m "api: implement admin stats endpoints"
```

---

### Task 3: Android Layout Update

**Files:**
- Modify: `app/src/main/res/layout/activity_admin_usuarios.xml`

- [ ] **Step 1: Add Global Stats Section**
Add a `MaterialCardView` at the top of the `LinearLayout` (inside the `CoordinatorLayout`).

```xml
<!-- Add before "Seleccionar Usuario" TextView -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/cardGlobalStats"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="24dp"
    app:cardCornerRadius="8dp"
    app:cardElevation="2dp"
    app:strokeWidth="1dp"
    app:strokeColor="?attr/colorOutline">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Resumen General"
            android:textSize="18sp"
            android:textStyle="bold"
            android:layout_marginBottom="12dp"
            android:textColor="?attr/colorPrimary"/>

        <TextView
            android:id="@+id/tvTotalUsuarios"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Usuarios Totales: --"
            android:textSize="16sp"
            android:layout_marginBottom="4dp"/>

        <TextView
            android:id="@+id/tvTotalTarjetas"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Tarjetas Totales: --"
            android:textSize="16sp"
            android:layout_marginBottom="4dp"/>

        <TextView
            android:id="@+id/tvTotalTransacciones"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Transacciones Totales: --"
            android:textSize="16sp"/>

    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

- [ ] **Step 2: Add User Transaction Count Field**
Add a `TextView` in `layoutUserInfo`.

```xml
<!-- Inside layoutUserInfo, before the Delete button -->
<TextView
    android:id="@+id/tvCantidadTransacciones"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Transacciones Realizadas: "
    android:textSize="16sp"
    android:layout_marginBottom="24dp"/>
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/res/layout/activity_admin_usuarios.xml
git commit -m "android: update admin usuarios layout with stats"
```

---

### Task 4: Android Activity Logic

**Files:**
- Modify: `app/src/main/java/com/codram/limitx/AdminUsuariosActivity.java`

- [ ] **Step 1: Initialize views and call API**
Update `onCreate` and implement `cargarStatsGenerales()`.

```java
// AdminUsuariosActivity.java

// ... (Fields)
private TextView tvTotalUsuarios, tvTotalTarjetas, tvTotalTransacciones, tvCantidadTransacciones;

// ... (in onCreate)
tvTotalUsuarios = findViewById(R.id.tvTotalUsuarios);
tvTotalTarjetas = findViewById(R.id.tvTotalTarjetas);
tvTotalTransacciones = findViewById(R.id.tvTotalTransacciones);
tvCantidadTransacciones = findViewById(R.id.tvCantidadTransacciones);

cargarStatsGenerales();
cargarUsuarios();

// ... (Method implementation)
private void cargarStatsGenerales() {
    String token = "Bearer " + sessionManager.getToken();
    ApiClient.getService().getAdminStats(token).enqueue(new Callback<AdminStatsResponse>() {
        @Override
        public void onResponse(Call<AdminStatsResponse> call, Response<AdminStatsResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                AdminStatsResponse stats = response.body();
                tvTotalUsuarios.setText("Usuarios Totales: " + stats.getTotal_usuarios());
                tvTotalTarjetas.setText("Tarjetas Totales: " + stats.getTotal_tarjetas());
                tvTotalTransacciones.setText("Transacciones Totales: " + stats.getTotal_transacciones());
            }
        }

        @Override
        public void onFailure(Call<AdminStatsResponse> call, Throwable t) {
            // Silently fail or show minor error
        }
    });
}

// ... (Update mostrarInfoUsuario)
private void mostrarInfoUsuario(UsuarioResponse u) {
    // ... (existing)
    tvCantidadTarjetas.setText("Tarjetas Registradas: " + u.getCantidadTarjetas());
    tvCantidadTransacciones.setText("Transacciones Realizadas: " + u.getCantidadTransacciones()); // <--- Add this
}
```

- [ ] **Step 2: Update refresh logic**
Ensure global stats are refreshed when a user is deleted.

```java
// In eliminarUsuario success callback
Toast.makeText(AdminUsuariosActivity.this, "Usuario eliminado correctamente", Toast.LENGTH_SHORT).show();
cargarStatsGenerales(); // <--- Add this
cargarUsuarios();
```

- [ ] **Step 3: Commit**
```bash
git add app/src/main/java/com/codram/limitx/AdminUsuariosActivity.java
git commit -m "android: implement admin stats logic"
```
