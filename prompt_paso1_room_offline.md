# Prompt — Paso 1: Capa offline con Room para LimiTx

## Contexto del proyecto

Estás trabajando en **LimiTx**, una app Android de gestión de tarjetas y transacciones.
- Lenguaje: **Java** (no Kotlin)
- Package: `com.codram.limitx`
- La app actualmente llama directo a una API REST con **Retrofit 2.9.0**
- No existe ninguna base de datos local todavía
- Gradle usa **Version Catalog** (`libs.versions.toml`)

### Estructura actual relevante
```
app/src/main/java/com/codram/limitx/
├── data/
│   ├── SessionManager.java          ← maneja token JWT en SharedPreferences
│   └── api/
│       ├── ApiClient.java           ← singleton Retrofit (BASE_URL hardcoded)
│       ├── ApiService.java          ← interfaz con todos los endpoints
│       ├── TarjetaResponse.java     ← campos: id, usuario_id, nombre, numero, banco, moneda, limite_mensual, activa, saldo_tarjeta, extraccion_disponible, deposito_disponible
│       ├── TarjetaRequest.java      ← campos: nombre, numero, banco, moneda, limite_mensual, activa
│       ├── TransaccionResponse.java ← campos: id, tarjeta_id, tipo, monto, descripcion, fecha
│       ├── TransaccionRequest.java  ← campos: tarjeta_id, tipo, monto, descripcion, subtipo, afecta_limite, fecha (ISO 8601)
│       └── ...otros modelos
├── MainActivity.java                ← llama ApiClient.getService().getTarjetas(token)
├── HistorialActivity.java           ← llama ApiClient.getService().getTransaccionesMes(token, tarjetaId)
├── TarjetasFragment.java
└── utils/TransactionDialogHelper.java
```

---

## Objetivo de este paso

Implementar la **capa de persistencia local con Room** para que la app funcione completamente offline. La UI debe poder mostrar tarjetas e historial completo de transacciones aunque no haya internet, usando datos previamente descargados y guardados en Room.

---

## Tareas a realizar

### 1. Agregar dependencias Room a `libs.versions.toml` y `app/build.gradle.kts`

Usar estas versiones exactas compatibles con `minSdk = 29` y `compileSdk 36`:
- `androidx.room:room-runtime:2.6.1`
- `androidx.room:room-compiler:2.6.1` (annotation processor con `annotationProcessor`, NO kapt — el proyecto es Java puro)
- `androidx.room:room-guava:2.6.1` (opcional, solo si se usan ListenableFuture)

Agregar también:
- `androidx.work:work-runtime:2.9.0` (para WorkManager, se usará en pasos siguientes)

### 2. Crear entidades Room

**`data/local/entity/TarjetaEntity.java`**
- `@Entity(tableName = "tarjetas")`
- Campos: `id` (String, @PrimaryKey), `usuarioId` (String), `nombre`, `numero`, `banco`, `moneda`, `limiteMensual` (double), `activa` (boolean)
- Campos calculados que se guardan como caché: `saldoTarjeta` (double), `extraccionDisponible` (double), `depositoDisponible` (double)
- Método estático `fromResponse(TarjetaResponse r)` que convierte el objeto de API a entidad local

**`data/local/entity/TransaccionEntity.java`**
- `@Entity(tableName = "transacciones")`
- Campos: `id` (String, @PrimaryKey), `tarjetaId` (String), `tipo`, `monto` (String — guardar BigDecimal como String para evitar pérdida de precisión), `descripcion`, `subtipo`, `afecta_limite` (boolean), `fecha` (String ISO 8601), `fechaCreacion` (String)
- Método estático `fromResponse(TransaccionResponse r)` que convierte de API a entidad local
- Método de instancia `toRequest()` que convierte la entidad a `TransaccionRequest` para enviar a la API

**`data/local/entity/SyncQueueEntity.java`**
- `@Entity(tableName = "sync_queue")`
- Campos:
  - `id` (long, @PrimaryKey autoGenerate = true)
  - `operacion` (String): `"CREATE_TARJETA"`, `"DELETE_TARJETA"`, `"CREATE_TRANSACCION"`, `"UPDATE_TRANSACCION"`, `"DELETE_TRANSACCION"`
  - `payload` (String): JSON con los datos necesarios para ejecutar la operación en la API
  - `localId` (String): UUID local del objeto afectado (para poder actualizar/eliminar después de sincronizar)
  - `estado` (String): `"PENDIENTE"`, `"ENVIANDO"`, `"ERROR"`
  - `intentos` (int): número de reintentos
  - `creadoEn` (long): timestamp en milisegundos

### 3. Crear DAOs

**`data/local/dao/TarjetaDao.java`**
```java
@Dao
public interface TarjetaDao {
    @Query("SELECT * FROM tarjetas WHERE usuarioId = :usuarioId")
    List<TarjetaEntity> getByUsuario(String usuarioId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(TarjetaEntity tarjeta);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TarjetaEntity> tarjetas);

    @Delete
    void delete(TarjetaEntity tarjeta);

    @Query("DELETE FROM tarjetas WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM tarjetas WHERE usuarioId = :usuarioId")
    void deleteAllByUsuario(String usuarioId);
}
```

**`data/local/dao/TransaccionDao.java`**
```java
@Dao
public interface TransaccionDao {
    @Query("SELECT * FROM transacciones WHERE tarjetaId = :tarjetaId ORDER BY fecha DESC")
    List<TransaccionEntity> getByTarjeta(String tarjetaId);

    @Query("SELECT * FROM transacciones WHERE tarjetaId = :tarjetaId AND fecha >= :desde ORDER BY fecha ASC")
    List<TransaccionEntity> getByTarjetaDesde(String tarjetaId, String desde);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(TransaccionEntity transaccion);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TransaccionEntity> transacciones);

    @Query("DELETE FROM transacciones WHERE id = :id")
    void deleteById(String id);

    @Query("DELETE FROM transacciones WHERE tarjetaId = :tarjetaId")
    void deleteByTarjeta(String tarjetaId);
}
```

**`data/local/dao/SyncQueueDao.java`**
```java
@Dao
public interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE estado = 'PENDIENTE' ORDER BY creadoEn ASC")
    List<SyncQueueEntity> getPendientes();

    @Insert
    long insert(SyncQueueEntity item);

    @Update
    void update(SyncQueueEntity item);

    @Delete
    void delete(SyncQueueEntity item);

    @Query("DELETE FROM sync_queue WHERE localId = :localId")
    void deleteByLocalId(String localId);

    @Query("SELECT COUNT(*) FROM sync_queue WHERE estado = 'PENDIENTE'")
    int countPendientes();
}
```

### 4. Crear la base de datos Room

**`data/local/AppDatabase.java`**
- `@Database(entities = {TarjetaEntity.class, TransaccionEntity.class, SyncQueueEntity.class}, version = 1)`
- Singleton thread-safe con `getInstance(Context context)`
- Exponer los tres DAOs

### 5. Crear el Repository

**`data/LimiTxRepository.java`**

Este es el componente central. La UI **nunca** habla directamente con `ApiClient` ni con `AppDatabase` — siempre pasa por el Repository.

Implementar los siguientes métodos. Todos deben ejecutarse en un hilo de background (usar `ExecutorService` con pool fijo de 3 threads). Los callbacks se devuelven en el hilo principal con `Handler(Looper.getMainLooper())`.

**Interfaz de callbacks genérica:**
```java
public interface Callback<T> {
    void onSuccess(T result);
    void onError(String mensaje);
}
```

**Métodos a implementar:**

```java
// Carga tarjetas: primero devuelve Room (inmediato), luego intenta actualizar desde API
void getTarjetas(String usuarioId, String token, Callback<List<TarjetaEntity>> callback);

// Carga TODAS las transacciones de una tarjeta (historial completo, sin filtro de fecha)
void getTransacciones(String tarjetaId, String token, Callback<List<TransaccionEntity>> callback);

// Carga transacciones del mes actual
void getTransaccionesMes(String tarjetaId, String token, Callback<List<TransaccionEntity>> callback);

// Crea transacción: guarda en Room con UUID temporal + encola en SyncQueue si offline
void crearTransaccion(TransaccionRequest request, String token, Callback<TransaccionEntity> callback);

// Elimina transacción: borra de Room + encola en SyncQueue si offline
void eliminarTransaccion(String transaccionId, String token, Callback<Void> callback);

// Crea tarjeta: guarda en Room + encola en SyncQueue si offline
void crearTarjeta(TarjetaRequest request, String usuarioId, String token, Callback<TarjetaEntity> callback);

// Elimina tarjeta: borra de Room + encola en SyncQueue si offline
void eliminarTarjeta(String tarjetaId, String token, Callback<Void> callback);

// Devuelve true si hay operaciones pendientes de sincronizar
boolean haySincronizacionPendiente();
```

**Lógica de `getTarjetas`:**
1. Leer de Room y llamar `callback.onSuccess()` inmediatamente con lo que haya (puede ser lista vacía)
2. Verificar conectividad con `ConnectivityManager`
3. Si hay red: llamar a la API, al recibir respuesta guardar en Room con `insertAll` y volver a llamar `callback.onSuccess()` con los datos actualizados
4. Si no hay red: no hacer nada más (los datos de Room ya fueron entregados)

**Lógica de `getTransacciones`:**
- Igual que getTarjetas pero para el historial **completo** de la tarjeta (endpoint `/tarjetas/{id}/transacciones`, no el de mes)
- Guardar todo en Room con `insertAll`
- La UI siempre mostrará lo que haya en Room

**Lógica de `crearTransaccion`:**
1. Generar UUID local con `UUID.randomUUID().toString()`
2. Crear `TransaccionEntity` con ese UUID y estado local
3. Guardar en Room con `insertOrReplace`
4. Si hay red: llamar a la API directamente, si éxito actualizar el registro en Room con el ID real del servidor
5. Si no hay red: encolar en `SyncQueue` con `operacion = "CREATE_TRANSACCION"` y `payload = Gson().toJson(request)`
6. Llamar `callback.onSuccess()` con la entidad local en ambos casos

### 6. Crear `ConnectivityHelper`

**`utils/ConnectivityHelper.java`**
```java
public class ConnectivityHelper {
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
}
```

---

## Restricciones importantes

- **Java puro**, sin Kotlin, sin coroutines, sin LiveData, sin ViewModel — la app no usa arquitectura MVVM
- **No usar** `allowMainThreadQueries()` — todas las operaciones de Room deben ir en background
- **No romper** ninguna Activity ni Fragment existente en este paso — el Repository es nuevo código; la integración con la UI es el paso siguiente
- Los UUIDs se manejan como `String` en Room (Room no soporta `UUID` nativo en Java sin TypeConverter; usar `String` es más simple)
- Usar `Gson` para serializar/deserializar el campo `payload` de `SyncQueueEntity` (Gson ya está en el proyecto como dependencia de Retrofit)
- Agregar el permiso `ACCESS_NETWORK_STATE` en `AndroidManifest.xml` si no está

---

## Archivos a crear/modificar

**Crear:**
- `app/src/main/java/com/codram/limitx/data/local/entity/TarjetaEntity.java`
- `app/src/main/java/com/codram/limitx/data/local/entity/TransaccionEntity.java`
- `app/src/main/java/com/codram/limitx/data/local/entity/SyncQueueEntity.java`
- `app/src/main/java/com/codram/limitx/data/local/dao/TarjetaDao.java`
- `app/src/main/java/com/codram/limitx/data/local/dao/TransaccionDao.java`
- `app/src/main/java/com/codram/limitx/data/local/dao/SyncQueueDao.java`
- `app/src/main/java/com/codram/limitx/data/local/AppDatabase.java`
- `app/src/main/java/com/codram/limitx/data/LimiTxRepository.java`
- `app/src/main/java/com/codram/limitx/utils/ConnectivityHelper.java`

**Modificar:**
- `gradle/libs.versions.toml` — agregar versiones de Room y WorkManager
- `app/build.gradle.kts` — agregar dependencias Room (con `annotationProcessor`) y WorkManager
- `app/src/main/AndroidManifest.xml` — agregar permiso `ACCESS_NETWORK_STATE` si falta

**No tocar en este paso:**
- Ninguna Activity ni Fragment existente
- `ApiClient.java`, `ApiService.java`
- `SessionManager.java`

---

## Entregable esperado

Al finalizar este paso, el proyecto debe compilar sin errores. No se espera ningún cambio visible en la UI todavía. La integración de la UI con el Repository es el **Paso 2**.
