# Prompt — Paso 3: SyncWorker y sincronización automática al recuperar red

## Contexto del proyecto

App Android **LimiTx**, Java puro, package `com.codram.limitx`.
- Pasos 1 y 2 completados: Room operativo, UI leyendo de `LimiTxRepository`
- La `SyncQueue` ya se llena correctamente cuando el usuario opera offline
- WorkManager ya está como dependencia en `build.gradle.kts` (`libs.work.runtime`)
- Falta: el worker que procesa esa cola cuando vuelve la red

---

## Qué hace la SyncQueue hoy

Cuando el usuario opera sin conexión, el Repository encola operaciones en `sync_queue` con estos campos:

| Campo | Valores posibles |
|---|---|
| `operacion` | `CREATE_TARJETA`, `DELETE_TARJETA`, `CREATE_TRANSACCION`, `UPDATE_TRANSACCION`, `DELETE_TRANSACCION` |
| `payload` | JSON con los datos necesarios (serializado con Gson) |
| `localId` | UUID String del objeto afectado en Room |
| `estado` | `PENDIENTE` → `ENVIANDO` → eliminado (si éxito) o `ERROR` (si falla) |
| `intentos` | contador de reintentos |
| `creadoEn` | timestamp milisegundos |

---

## Tareas a realizar

### 1. Crear `SyncWorker.java`

**Ruta:** `app/src/main/java/com/codram/limitx/data/sync/SyncWorker.java`

Debe extender `Worker` (no `CoroutineWorker` — Java puro).

```java
public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Procesar la cola aquí
    }
}
```

**Lógica de `doWork()`:**

1. Obtener la instancia de `AppDatabase`
2. Obtener el token de `SessionManager` — si es null, retornar `Result.failure()` (no se puede sincronizar sin autenticar)
3. Obtener todas las operaciones pendientes: `db.syncQueueDao().getPendientes()`
4. Si la lista está vacía, retornar `Result.success()`
5. Crear una instancia de `ApiService` via `ApiClient.getService()`
6. Iterar cada `SyncQueueEntity` en orden (ya vienen ordenadas por `creadoEn ASC`):
   a. Marcar como `ENVIANDO` y actualizar en DB
   b. Ejecutar la operación correspondiente según `item.operacion` (ver tabla abajo)
   c. Si éxito: eliminar el item de la cola con `db.syncQueueDao().delete(item)`
   d. Si falla HTTP (código no 2xx): incrementar `item.intentos`, marcar como `PENDIENTE` de nuevo, actualizar en DB — continuar con el siguiente
   e. Si excepción de red (IOException): retornar `Result.retry()` — WorkManager reintentará con backoff
7. Al finalizar todas las operaciones, retornar `Result.success()`

**Tabla de operaciones a ejecutar (todas síncronas con `.execute()`):**

| `item.operacion` | Qué hacer |
|---|---|
| `CREATE_TARJETA` | Deserializar `item.payload` como `TarjetaRequest`. Llamar `api.createTarjeta("Bearer " + token, request).execute()`. Si éxito: reemplazar el registro local (con `item.localId`) por el que devuelve el servidor (con ID real): `db.tarjetaDao().deleteById(item.localId)` + `db.tarjetaDao().insertOrReplace(TarjetaEntity.fromResponse(response.body()))` |
| `DELETE_TARJETA` | El payload es el `tarjetaId` directamente. Llamar `api.eliminarTarjeta("Bearer " + token, UUID.fromString(item.payload)).execute()`. Si éxito: eliminar de la cola (ya fue borrado de Room en el momento de la acción) |
| `CREATE_TRANSACCION` | Deserializar `item.payload` como `TransaccionRequest`. Llamar `api.crearTransaccion("Bearer " + token, request).execute()`. Si éxito: eliminar de la cola (la transacción ya está en Room con ID temporal — no hay forma de actualizar el ID real porque el endpoint devuelve Void; se resuelve en el siguiente refresh de datos) |
| `UPDATE_TRANSACCION` | Deserializar `item.payload` como `TransaccionUpdatePayload` (ver punto 2). Llamar `api.actualizarTransaccion("Bearer " + token, UUID.fromString(item.localId), update).execute()`. Si éxito: actualizar el registro en Room con los datos que devuelve el servidor |
| `DELETE_TRANSACCION` | El payload es el `transaccionId`. Llamar `api.eliminarTransaccion("Bearer " + token, UUID.fromString(item.payload)).execute()` |

**Manejo de intentos máximos:** Si `item.intentos >= 5`, marcar el item como `ERROR` y continuar — no bloquear la cola por un item problemático.

### 2. Crear `TransaccionUpdatePayload.java`

**Ruta:** `app/src/main/java/com/codram/limitx/data/sync/TransaccionUpdatePayload.java`

Clase simple para serializar/deserializar el payload de `UPDATE_TRANSACCION`:

```java
public class TransaccionUpdatePayload {
    public String monto;       // BigDecimal como String
    public String descripcion;
    public String fecha;       // ISO 8601
}
```

### 3. Agregar método `actualizarTransaccion` al Repository

**En `LimiTxRepository.java`**, agregar:

```java
public void actualizarTransaccion(String transaccionId, TransaccionUpdate update, String token, Callback<TransaccionEntity> callback) {
    executor.execute(() -> {
        // Actualizar en Room inmediatamente (optimistic update)
        // Leer la entidad actual, actualizar campos, guardar
        // Campos a actualizar: monto (update.getMonto().toString()), descripcion (update.getDescripcion()), fecha (update.getFecha())
        
        if (ConnectivityHelper.isOnline(context)) {
            try {
                Response<TransaccionResponse> response = api.actualizarTransaccion(
                    token, UUID.fromString(transaccionId), update
                ).execute();
                if (response.isSuccessful() && response.body() != null) {
                    TransaccionEntity updated = TransaccionEntity.fromResponse(response.body());
                    db.transaccionDao().insertOrReplace(updated);
                    mainHandler.post(() -> callback.onSuccess(updated));
                } else {
                    // Encolar para sync posterior
                    TransaccionUpdatePayload payload = new TransaccionUpdatePayload();
                    payload.monto = update.getMonto().toString();
                    payload.descripcion = update.getDescripcion();
                    payload.fecha = update.getFecha();
                    queueSync("UPDATE_TRANSACCION", gson.toJson(payload), transaccionId);
                    mainHandler.post(() -> callback.onError("Error del servidor"));
                }
            } catch (IOException e) {
                // Encolar para sync posterior
                TransaccionUpdatePayload payload = new TransaccionUpdatePayload();
                payload.monto = update.getMonto().toString();
                payload.descripcion = update.getDescripcion();
                payload.fecha = update.getFecha();
                queueSync("UPDATE_TRANSACCION", gson.toJson(payload), transaccionId);
                mainHandler.post(() -> callback.onError("Sin conexión"));
            }
        } else {
            TransaccionUpdatePayload payload = new TransaccionUpdatePayload();
            payload.monto = update.getMonto().toString();
            payload.descripcion = update.getDescripcion();
            payload.fecha = update.getFecha();
            queueSync("UPDATE_TRANSACCION", gson.toJson(payload), transaccionId);
            mainHandler.post(() -> callback.onSuccess(null)); // Éxito local
        }
    });
}
```

Para el optimistic update en Room, agregar en `TransaccionDao`:
```java
@Query("UPDATE transacciones SET monto = :monto, descripcion = :descripcion, fecha = :fecha WHERE id = :id")
void actualizarCampos(String id, String monto, String descripcion, String fecha);
```

### 4. Migrar `actualizarTransaccion` en `HistorialActivity`

Reemplazar el bloque que llama directamente a `ApiClient.getService().actualizarTransaccion(...)` dentro de `mostrarDialogoEdicion`:

```java
// ANTES (directo a API):
ApiClient.getService().actualizarTransaccion("Bearer " + token, UUID.fromString(transaccion.id), update)
    .enqueue(new Callback<TransaccionResponse>() { ... });

// DESPUÉS (via Repository):
repository.actualizarTransaccion(transaccion.id, update, "Bearer " + token,
    new LimiTxRepository.Callback<TransaccionEntity>() {
        @Override
        public void onSuccess(TransaccionEntity result) {
            Toast.makeText(HistorialActivity.this, "Actualizado", Toast.LENGTH_SHORT).show();
            cargarTransacciones(tarjetaId);
        }
        @Override
        public void onError(String mensaje) {
            Toast.makeText(HistorialActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
        }
    });
```

### 5. Crear `SyncScheduler.java`

**Ruta:** `app/src/main/java/com/codram/limitx/data/sync/SyncScheduler.java`

Clase utilitaria que encapsula la programación del worker:

```java
public class SyncScheduler {

    private static final String SYNC_WORK_NAME = "limitx_sync";

    // Llamar cuando el usuario abre la app con red, o cuando se detecta que la red vuelve
    public static void scheduleSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP, // Si ya hay uno en cola, no duplicar
                syncRequest
        );
    }

    // Cancelar si es necesario (ej: logout)
    public static void cancelSync(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME);
    }
}
```

### 6. Registrar `SyncWorker` en `AndroidManifest.xml`

WorkManager con versión 2.9.0 no requiere registro manual en el Manifest — se registra automáticamente via `androidx.startup`. No hay nada que agregar al Manifest.

### 7. Disparar sync desde `MainActivity`

En `MainActivity.java`, en el método `actualizarBannerConectividad()`, ampliar para disparar la sync cuando hay red:

```java
private void actualizarBannerConectividad() {
    boolean online = ConnectivityHelper.isOnline(this);
    tvOfflineBanner.setVisibility(online ? View.GONE : View.VISIBLE);
    
    if (online && repository.haySincronizacionPendiente()) {
        // Mostrar indicador de sincronización mientras trabaja el worker
        tvOfflineBanner.setVisibility(View.VISIBLE);
        tvOfflineBanner.setText("Sincronizando datos pendientes...");
        tvOfflineBanner.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")); // Verde
        
        SyncScheduler.scheduleSync(this);
        
        // Observar el estado del trabajo para ocultar el banner al terminar
        WorkManager.getInstance(this)
            .getWorkInfosForUniqueWorkLiveData("limitx_sync")
            .observe(this, workInfoList -> {
                if (workInfoList == null || workInfoList.isEmpty()) return;
                WorkInfo info = workInfoList.get(0);
                if (info.getState() == WorkInfo.State.SUCCEEDED) {
                    tvOfflineBanner.setVisibility(View.GONE);
                    tvOfflineBanner.setText("Sin conexión — mostrando datos guardados");
                    tvOfflineBanner.setBackgroundColor(android.graphics.Color.parseColor("#FFC107"));
                    loadTarjetas(); // Refrescar UI con datos sincronizados
                } else if (info.getState() == WorkInfo.State.FAILED) {
                    tvOfflineBanner.setVisibility(View.GONE);
                }
            });
    }
}
```

Agregar los imports necesarios:
```java
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.codram.limitx.data.sync.SyncScheduler;
```

### 8. Disparar sync desde `LoginActivity`

Después de un login exitoso, si hay operaciones pendientes de una sesión anterior, disparar sync. Al final de `goToMain()`, agregar:
```java
SyncScheduler.scheduleSync(this);
```

---

## Detalles de implementación importantes

**Deserialización del payload en SyncWorker:**
```java
Gson gson = new Gson();

// Para TarjetaRequest:
TarjetaRequest request = gson.fromJson(item.payload, TarjetaRequest.class);

// Para TransaccionRequest:
TransaccionRequest request = gson.fromJson(item.payload, TransaccionRequest.class);

// Para TransaccionUpdatePayload:
TransaccionUpdatePayload payload = gson.fromJson(item.payload, TransaccionUpdatePayload.class);
TransaccionUpdate update = new TransaccionUpdate(
    new BigDecimal(payload.monto), payload.descripcion, payload.fecha
);
```

**Problema con UUID en Gson:** `TransaccionRequest` tiene `tarjeta_id` como `UUID`. Gson puede no deserializarlo correctamente. Usar este adaptador en el SyncWorker:
```java
Gson gson = new GsonBuilder()
    .registerTypeAdapter(UUID.class, (JsonDeserializer<UUID>) 
        (json, type, ctx) -> UUID.fromString(json.getAsString()))
    .create();
```

**El `doWork()` se ejecuta en un hilo de background** — no usar `mainHandler`, no actualizar UI directamente. Room se puede usar de forma síncrona aquí sin problemas.

**No crear un nuevo `ExecutorService`** dentro del worker — usar llamadas síncronas de Retrofit (`.execute()`) directamente en `doWork()`.

---

## Archivos a crear

- `app/src/main/java/com/codram/limitx/data/sync/SyncWorker.java`
- `app/src/main/java/com/codram/limitx/data/sync/SyncScheduler.java`
- `app/src/main/java/com/codram/limitx/data/sync/TransaccionUpdatePayload.java`

## Archivos a modificar

- `app/src/main/java/com/codram/limitx/data/LimiTxRepository.java` — agregar `actualizarTransaccion`
- `app/src/main/java/com/codram/limitx/data/local/dao/TransaccionDao.java` — agregar `actualizarCampos`
- `app/src/main/java/com/codram/limitx/HistorialActivity.java` — usar repository en `actualizarTransaccion`
- `app/src/main/java/com/codram/limitx/MainActivity.java` — ampliar `actualizarBannerConectividad` y disparar sync
- `app/src/main/java/com/codram/limitx/LoginActivity.java` — disparar sync tras login

## Archivos que NO tocar

- Entidades Room, `AppDatabase`, `SyncQueueDao`, `TarjetaDao` (salvo el nuevo método en `TransaccionDao`)
- `ApiClient.java`, `ApiService.java`
- `AdminUsuariosActivity.java`, `RegisterActivity.java` — no tienen sentido offline, quedan con ApiClient

---

## Resultado esperado

Al finalizar este paso:
1. El proyecto compila sin errores
2. Si el usuario opera offline (crea/elimina tarjetas o transacciones), al recuperar la red la app sincroniza automáticamente sin intervención del usuario
3. Mientras sincroniza, el banner se pone verde con "Sincronizando..."
4. Al terminar, el banner desaparece y la UI se refresca
5. Editar transacciones también funciona offline (queda encolado y se sincroniza)
6. Si un item falla 5 veces, se marca como ERROR y no bloquea el resto de la cola
