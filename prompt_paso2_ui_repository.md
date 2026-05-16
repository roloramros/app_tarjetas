# Prompt — Paso 2: Conectar la UI al Repository (soporte offline completo)

## Contexto del proyecto

App Android **LimiTx**, Java puro, gestión de tarjetas y transacciones.
- Package: `com.codram.limitx`
- El Paso 1 ya está completado: existe `LimiTxRepository` con Room + cola de sync
- Actualmente todas las Activities/clases llaman directo a `ApiClient.getService()` — eso es lo que hay que reemplazar

---

## Estado actual — dónde se usa ApiClient (todo esto hay que migrar)

| Archivo | Llamadas actuales a ApiClient |
|---|---|
| `MainActivity.java` | `getTarjetas()`, `getMe()`, `getAppVersion()` |
| `HistorialActivity.java` | `getTarjetas()` (para saldo), `getTransaccionesMes()`, `actualizarTransaccion()`, `eliminarTransaccion()` |
| `AddTarjetaBottomSheet.java` | `createTarjeta()` |
| `TarjetasAdapter.java` | `eliminarTarjeta()` |
| `utils/TransactionDialogHelper.java` | `crearTransaccion()` |

---

## Tipos de datos — mapeo entre capas

El Repository trabaja con `TarjetaEntity` y `TransaccionEntity` (Room). La UI actualmente usa `TarjetaResponse` y `TransaccionResponse` (API). Hay que crear métodos de conversión o adaptar la UI para trabajar directamente con las entidades.

**Campos equivalentes:**

| TarjetaResponse | TarjetaEntity |
|---|---|
| `getId()` → UUID | `id` → String |
| `getUsuarioId()` → UUID | `usuarioId` → String |
| `getNombre()` | `nombre` |
| `getNumero()` | `numero` |
| `getBanco()` | `banco` |
| `getMoneda()` | `moneda` |
| `getLimiteMensual()` | `limiteMensual` |
| `isActiva()` | `activa` |
| `getSaldo_tarjeta()` | `saldoTarjeta` |
| `getExtraccion_disponible()` | `extraccionDisponible` |
| `getDeposito_disponible()` | `depositoDisponible` |

| TransaccionResponse | TransaccionEntity |
|---|---|
| `getId()` → UUID | `id` → String |
| `getTarjetaId()` → UUID | `tarjetaId` → String |
| `getTipo()` | `tipo` |
| `getMonto()` → BigDecimal | `monto` → String (convertir con `new BigDecimal(entity.monto)`) |
| `getDescripcion()` | `descripcion` |
| `getSubtipo()` | `subtipo` |
| `isAfectaLimite()` | `afecta_limite` |
| `getFecha()` | `fecha` |

---

## Tareas a realizar

### 1. Agregar banner de estado offline en `activity_main.xml`

Agregar encima del contenido principal (antes del ViewPager) una `TextView` con id `tvOfflineBanner`:
```xml
<TextView
    android:id="@+id/tvOfflineBanner"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="#FFC107"
    android:text="Sin conexión — mostrando datos guardados"
    android:textColor="#000000"
    android:gravity="center"
    android:padding="6dp"
    android:textSize="12sp"
    android:visibility="gone" />
```

### 2. Modificar `MainActivity.java`

**Cambios requeridos:**

a) Agregar campo `LimiTxRepository repository` y campo `TextView tvOfflineBanner`.

b) Cambiar el tipo de `listaTarjetasOriginal` de `List<TarjetaResponse>` a `List<TarjetaEntity>`.

c) En `onCreate`, inicializar el repository y el banner:
```java
repository = new LimiTxRepository(this);
tvOfflineBanner = findViewById(R.id.tvOfflineBanner);
```

d) Reemplazar el método `loadTarjetas()` completo:
```java
private void loadTarjetas() {
    showLoading(true);
    String token = "Bearer " + sessionManager.getToken();
    String usuarioId = sessionManager.getUsername(); // Nota: ver punto e)

    repository.getTarjetas(usuarioId, token, new LimiTxRepository.Callback<List<TarjetaEntity>>() {
        @Override
        public void onSuccess(List<TarjetaEntity> result) {
            showLoading(false);
            listaTarjetasOriginal = new ArrayList<>(result);
            String savedOrder = sessionManager.getSortOrder();
            ordenarTarjetas(savedOrder);
            actualizarBannerConectividad();
        }
        @Override
        public void onError(String mensaje) {
            showLoading(false);
        }
    });
}
```

e) **Problema con usuarioId**: El Repository necesita `usuarioId` (UUID del usuario) para filtrar tarjetas en Room, pero `SessionManager` solo guarda el `username` (texto). Hay dos opciones — implementar la **opción A**:

**Opción A (recomendada):** Guardar el `userId` en `SessionManager` al hacer login. Para esto:
- Agregar en `SessionManager.java` los métodos `saveUserId(String id)` y `getUserId()` (igual que `saveUsername`/`getUsername` pero con key `"user_id"`)
- En `LoginActivity.java`, después de recibir el token exitoso, llamar a `getMe` y guardar el `userId` con `sessionManager.saveUserId(response.body().getId().toString())`
- En `MainActivity`, usar `sessionManager.getUserId()` en vez del username

f) Reemplazar el método `checkSubscription()`:
- Si hay conexión: mantener la llamada a `getMe` como está (es para verificar suscripción, no tiene equivalente en Room)
- Si falla (offline): leer `sessionManager.isSubscriptionActive()` y aplicarlo al `pagerAdapter`
- Agregar manejo de `onFailure` para aplicar el valor guardado: `pagerAdapter.setSubscriptionActive(sessionManager.isSubscriptionActive())`

g) Agregar método `actualizarBannerConectividad()`:
```java
private void actualizarBannerConectividad() {
    boolean online = ConnectivityHelper.isOnline(this);
    tvOfflineBanner.setVisibility(online ? View.GONE : View.VISIBLE);
}
```

h) Reemplazar el método `ordenarTarjetas(String criterio)`:
- Actualmente ordena `List<TarjetaResponse>` — cambiarlo para ordenar `List<TarjetaEntity>`
- Los campos son directamente accesibles (públicos): `entity.nombre`, `entity.saldoTarjeta`, `entity.extraccionDisponible`, `entity.depositoDisponible`

i) Reemplazar el método `distribuirTarjetas()`:
- Actualmente distribuye `List<TarjetaResponse>` — cambiarlo para distribuir `List<TarjetaEntity>`
- La moneda está en `entity.moneda`

j) Reemplazar `actualizarSaldoTotal()`:
- Actualmente itera `TarjetaResponse` — cambiarlo para iterar `TarjetaEntity`
- Saldo: `entity.saldoTarjeta`; moneda: `entity.moneda`

### 3. Modificar `TarjetasAdapter.java`

Este adapter trabaja con `List<TarjetaResponse>`. Hay que migrarlo a `List<TarjetaEntity>`.

**Cambios:**

a) Cambiar el tipo del campo `tarjetas` de `List<TarjetaResponse>` a `List<TarjetaEntity>`.

b) Actualizar todos los constructores para recibir `List<TarjetaEntity>`.

c) En `onBindViewHolder`, cambiar `TarjetaResponse tarjeta` por `TarjetaEntity tarjeta` y actualizar accesos a campos (de getters a acceso directo: `tarjeta.nombre`, `tarjeta.saldoTarjeta`, etc.).

d) En el `OnClickListener` que lanza `HistorialActivity`, actualizar los extras:
```java
intent.putExtra("TARJETA_ID", tarjeta.id);  // ya es String
intent.putExtra("TARJETA_NOMBRE", tarjeta.nombre);
intent.putExtra("TARJETA_SALDO", tarjeta.saldoTarjeta);
intent.putExtra("TARJETA_MONEDA", tarjeta.moneda);
```

e) En la eliminación de tarjeta (donde actualmente llama `ApiClient.getService().eliminarTarjeta(...)`), reemplazar con:
```java
LimiTxRepository repo = new LimiTxRepository(context);
repo.eliminarTarjeta(tarjeta.id, "Bearer " + token, new LimiTxRepository.Callback<Void>() {
    @Override
    public void onSuccess(Void result) {
        Toast.makeText(context, "Tarjeta eliminada", Toast.LENGTH_SHORT).show();
        if (transactionListener != null) transactionListener.onTransactionAdded();
    }
    @Override
    public void onError(String mensaje) {
        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show();
    }
});
```

f) Actualizar el método `updateData` para recibir `List<TarjetaEntity>`.

### 4. Modificar `TarjetasFragment.java`

Actualmente recibe `List<TarjetaResponse>` en `updateData`. Cambiarlo a `List<TarjetaEntity>`.

- Cambiar el campo `tarjetas` a `List<TarjetaEntity>`
- Cambiar `pendingData` a `List<TarjetaEntity>`
- Actualizar la firma de `updateData(List<TarjetaEntity> nuevasTarjetas)`
- El adapter se inicializa con `new TarjetasAdapter(tarjetas, transactionListener, isSubscriptionActive)` — ya aceptará `TarjetaEntity` tras el cambio anterior

### 5. Modificar `TarjetasPagerAdapter.java`

Si tiene métodos que devuelven o reciben `List<TarjetaResponse>`, actualizarlos a `List<TarjetaEntity>`.

### 6. Modificar `AddTarjetaBottomSheet.java`

En el método `saveTarjeta()`, reemplazar la llamada a `ApiClient`:
```java
// ANTES:
ApiClient.getService().createTarjeta(token, request).enqueue(...)

// DESPUÉS:
LimiTxRepository repo = new LimiTxRepository(requireContext());
String usuarioId = sessionManager.getUserId(); // tras el cambio en SessionManager
repo.crearTarjeta(request, usuarioId, "Bearer " + token, new LimiTxRepository.Callback<TarjetaEntity>() {
    @Override
    public void onSuccess(TarjetaEntity result) {
        Toast.makeText(getContext(), "Tarjeta añadida con éxito", Toast.LENGTH_SHORT).show();
        if (listener != null) listener.onTarjetaAdded();
        dismiss();
    }
    @Override
    public void onError(String mensaje) {
        Toast.makeText(getContext(), "Error al añadir tarjeta", Toast.LENGTH_SHORT).show();
    }
});
```

### 7. Modificar `utils/TransactionDialogHelper.java`

En el método `showTransactionDialog`, reemplazar la llamada a `ApiClient`:
```java
// ANTES:
ApiClient.getService().crearTransaccion("Bearer " + token, request).enqueue(...)

// DESPUÉS:
LimiTxRepository repo = new LimiTxRepository(context);
repo.crearTransaccion(request, "Bearer " + token, new LimiTxRepository.Callback<TransaccionEntity>() {
    @Override
    public void onSuccess(TransaccionEntity result) {
        Toast.makeText(context, "Transacción guardada", Toast.LENGTH_SHORT).show();
        if (listener != null) listener.onTransactionAdded();
    }
    @Override
    public void onError(String mensaje) {
        Toast.makeText(context, "Error al guardar transacción", Toast.LENGTH_SHORT).show();
    }
});
```

Agregar el import de `TransaccionEntity`.

### 8. Modificar `HistorialActivity.java`

Este es el archivo más complejo porque usa tanto `TransaccionResponse` como `TarjetaResponse`.

a) Cambiar el tipo de `entradas` y `salidas` de `List<TransaccionResponse>` a `List<TransaccionEntity>`.

b) Agregar campo `LimiTxRepository repository` e inicializarlo en `onCreate`:
```java
repository = new LimiTxRepository(this);
```

c) Reemplazar `cargarTransacciones(UUID tarjetaId)` completo:
```java
private void cargarTransacciones(UUID tarjetaId) {
    String token = "Bearer " + new SessionManager(this).getToken();
    String tarjetaIdStr = tarjetaId.toString();

    repository.getTransacciones(tarjetaIdStr, token, new LimiTxRepository.Callback<List<TransaccionEntity>>() {
        @Override
        public void onSuccess(List<TransaccionEntity> result) {
            entradas.clear();
            salidas.clear();
            for (TransaccionEntity t : result) {
                if ("entrada".equals(t.tipo)) entradas.add(t);
                else salidas.add(t);
            }
            rvEntradas.setAdapter(new TransaccionesAdapter(entradas, t -> mostrarDialogoEdicion(t, tarjetaId)));
            rvSalidas.setAdapter(new TransaccionesAdapter(salidas, t -> mostrarDialogoEdicion(t, tarjetaId)));
        }
        @Override
        public void onError(String mensaje) {
            Toast.makeText(HistorialActivity.this, "Error cargando historial", Toast.LENGTH_SHORT).show();
        }
    });
}
```

d) Reemplazar `cargarSaldoTarjeta(UUID tarjetaId)`:
- En vez de llamar `getTarjetas` a la API, leer de Room directamente via repository
- Usar `repository.getTarjetas(usuarioId, token, callback)` y filtrar por id, o simplificar: el saldo ya viene en el Intent (`TARJETA_SALDO`), y se actualiza cuando `cargarTransacciones` trae datos frescos
- Opción más simple: eliminar `cargarSaldoTarjeta` y actualizar el saldo solo cuando hay conexión (el valor del Intent se usa como fallback)

e) Actualizar `TransaccionesAdapter` — actualmente recibe `List<TransaccionResponse>`. Cambiar para que reciba `List<TransaccionEntity>`. Los campos se acceden directamente: `entity.monto` (String, convertir a BigDecimal para mostrar), `entity.descripcion`, `entity.fecha`, `entity.tipo`.

f) Actualizar `mostrarDialogoEdicion(TransaccionResponse transaccion, UUID tarjetaId)` → `mostrarDialogoEdicion(TransaccionEntity transaccion, UUID tarjetaId)`:
- Acceder a campos directamente: `transaccion.monto`, `transaccion.descripcion`, `transaccion.fecha`
- La llamada a `actualizarTransaccion` puede seguir usando ApiClient por ahora (el update offline se trabaja en el Paso 3)
- La eliminación: reemplazar con `repository.eliminarTransaccion(transaccion.id, token, callback)`

g) Actualizar `confirmarEliminacion(TransaccionResponse, UUID)` → `confirmarEliminacion(TransaccionEntity, UUID)`:
```java
repository.eliminarTransaccion(transaccion.id, "Bearer " + token,
    new LimiTxRepository.Callback<Void>() {
        @Override
        public void onSuccess(Void result) {
            Toast.makeText(HistorialActivity.this, "Eliminado", Toast.LENGTH_SHORT).show();
            cargarTransacciones(tarjetaId);
        }
        @Override
        public void onError(String mensaje) {
            Toast.makeText(HistorialActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show();
        }
    });
```

### 9. Modificar `TransaccionesAdapter.java`

Cambiar de `List<TransaccionResponse>` a `List<TransaccionEntity>`:
- Cambiar el tipo del campo interno
- Actualizar constructor
- En `onBindViewHolder`: acceder a `entity.tipo`, `entity.monto` (convertir: `new BigDecimal(entity.monto)`), `entity.descripcion`, `entity.fecha`
- El listener de click que devuelve la transacción: cambiar de `TransaccionResponse` a `TransaccionEntity`

---

## Restricciones importantes

- **No eliminar ni modificar `ApiClient`** — `checkVersion()` y `checkSubscription()` en `MainActivity` pueden seguir usándolo (son llamadas que no afectan datos offline)
- **No usar `allowMainThreadQueries()`** — el Repository ya maneja los hilos correctamente
- **Java puro** — sin Kotlin, sin coroutines, sin LiveData
- El banner offline debe aparecer/desaparecer automáticamente cada vez que se cargan tarjetas, no solo al inicio
- Si `getUserId()` retorna null (usuario antiguo sin ID guardado), hacer fallback: llamar `getMe` una vez para obtenerlo y guardarlo

---

## Archivos a modificar

- `app/src/main/res/layout/activity_main.xml` — agregar `tvOfflineBanner`
- `app/src/main/java/com/codram/limitx/data/SessionManager.java` — agregar `saveUserId` / `getUserId`
- `app/src/main/java/com/codram/limitx/MainActivity.java`
- `app/src/main/java/com/codram/limitx/TarjetasAdapter.java`
- `app/src/main/java/com/codram/limitx/TarjetasFragment.java`
- `app/src/main/java/com/codram/limitx/TarjetasPagerAdapter.java`
- `app/src/main/java/com/codram/limitx/AddTarjetaBottomSheet.java`
- `app/src/main/java/com/codram/limitx/HistorialActivity.java`
- `app/src/main/java/com/codram/limitx/TransaccionesAdapter.java`
- `app/src/main/java/com/codram/limitx/utils/TransactionDialogHelper.java`
- `app/src/main/java/com/codram/limitx/LoginActivity.java` — guardar userId tras login

## Archivos que NO tocar

- `LimiTxRepository.java`
- `AppDatabase.java`
- Entidades y DAOs en `data/local/`
- `ApiClient.java`, `ApiService.java`
- `ConnectivityHelper.java`

---

## Resultado esperado

Al finalizar este paso:
1. La app compila sin errores
2. Al abrir sin internet, se muestran todas las tarjetas e historial guardado en Room
3. Aparece el banner amarillo "Sin conexión" cuando no hay red
4. Al crear una transacción sin internet, se guarda localmente y se muestra de inmediato
5. Al crear una tarjeta sin internet, aparece en la lista de inmediato
6. Al eliminar (tarjeta o transacción) sin internet, desaparece de la UI de inmediato
7. Al recuperar la red, los datos se actualizan silenciosamente en segundo plano
