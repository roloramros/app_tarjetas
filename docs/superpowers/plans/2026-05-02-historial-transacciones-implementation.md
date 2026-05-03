# Historial de Transacciones (Vista Tabla) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar una nueva Activity que muestra el historial de transacciones de una tarjeta en formato tabla (dos columnas: Entradas y Salidas).

**Architecture:** 
- `HistorialActivity` cargará las transacciones mediante un nuevo endpoint.
- Layout: dos `RecyclerView` (o `LinearLayout` dinámico) dispuestos horizontalmente, sincronizados mediante `OnScrollListener`.
- Filtrado en la API: se filtrarán las transacciones por el mes en curso.

**Tech Stack:** Android (Java), Retrofit, SQLAlchemy (API).

---

### Task 1: API Endpoint para Listar Transacciones por Mes

**Files:**
- Modify: `api/main.py`

- [ ] **Step 1: Añadir endpoint GET `/tarjetas/{tarjeta_id}/transacciones/mes`**

```python
@app.get("/tarjetas/{tarjeta_id}/transacciones/mes", response_model=list[TransaccionResponse])
async def listar_transacciones_mes(
    tarjeta_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
    current_user: Usuario = Depends(get_current_user)
):
    # Validar acceso
    result = await db.execute(select(Tarjeta).where(Tarjeta.id == tarjeta_id, Tarjeta.usuario_id == current_user.id))
    if not result.scalars().first():
        raise HTTPException(status_code=404, detail="Tarjeta no encontrada")
        
    # Filtrar por mes actual
    now = datetime.now()
    start_of_month = datetime(now.year, now.month, 1)
    
    result = await db.execute(
        select(Transaccion)
        .where(Transaccion.tarjeta_id == tarjeta_id, Transaccion.fecha >= start_of_month)
        .order_by(Transaccion.fecha.desc())
    )
    return result.scalars().all()
```

### Task 2: UI del Historial (`activity_historial.xml`)

**Files:**
- Create: `app/src/main/res/layout/activity_historial.xml`
- Create: `app/src/main/res/layout/item_transaccion_columna.xml`

- [ ] **Step 1: Layout de la Activity (`activity_historial.xml`)**

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        <TextView android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="Entradas" android:gravity="center" android:padding="8dp"/>
        <TextView android:layout_width="0dp" android:layout_weight="1" android:layout_height="wrap_content" android:text="Salidas" android:gravity="center" android:padding="8dp"/>
    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="horizontal">
        <androidx.recyclerview.widget.RecyclerView android:id="@+id/rvEntradas" android:layout_width="0dp" android:layout_weight="1" android:layout_height="match_parent"/>
        <androidx.recyclerview.widget.RecyclerView android:id="@+id/rvSalidas" android:layout_width="0dp" android:layout_weight="1" android:layout_height="match_parent"/>
    </LinearLayout>
</LinearLayout>
```

### Task 3: Lógica en `HistorialActivity.java`

**Files:**
- Create: `app/src/main/java/com/codram/limitx/HistorialActivity.java`

- [ ] **Step 1: Implementar lógica de scroll sincronizado y carga de datos**

```java
// Sincronización de scroll
rvEntradas.addOnScrollListener(new RecyclerView.OnScrollListener() {
    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        rvSalidas.scrollBy(dx, dy);
    }
});
```

### Task 4: Integración en `TarjetasAdapter`

- [ ] **Step 1: Conectar opción "Ver Historial"**

En `TarjetasAdapter`, modificar el listener de "Ver Historial":
```java
Intent intent = new Intent(v.getContext(), HistorialActivity.class);
intent.putExtra("TARJETA_ID", tarjeta.getId().toString());
v.getContext().startActivity(intent);
```
