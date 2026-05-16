# Prompt — Paso 4: SyncWorker + Endpoint `/sync/batch` (sincronización offline → servidor)

## Contexto del proyecto

App Android **LimiTx**, Java puro. Gestión de tarjetas y transacciones con soporte offline completo.

- Package Android: `com.codram.limitx`
- API: **FastAPI + SQLAlchemy async + PostgreSQL**
- Los pasos 1–3 están completados:
  - ✅ Room con entidades `TarjetaEntity`, `TransaccionEntity`, `SyncQueueEntity`
  - ✅ DAOs: `TarjetaDao`, `TransaccionDao`, `SyncQueueDao`
  - ✅ `AppDatabase` (Room, version = 1)
  - ✅ `LimiTxRepository` con lógica online/offline y cola de sync
  - ✅ `ConnectivityHelper.isOnline(context)`
  - ✅ UI conectada al repository (Activities y Adapters usan `TarjetaEntity` / `TransaccionEntity`)
  - ✅ Banner offline visible en `MainActivity`

---

## Estado actual de la cola de sincronización

### `SyncQueueEntity` — campos disponibles

```java
public long id;          // autoGenerate
public String operacion; // "CREATE_TARJETA" | "DELETE_TARJETA" | "CREATE_TRANSACCION" | "DELETE_TRANSACCION" | "UPDATE_TRANSACCION"
public String payload;   // JSON serializado con Gson
public String localId;   // UUID local del objeto (String)
public String estado;    // "PENDIENTE" | "ENVIANDO" | "ERROR"
public int intentos;
public long creadoEn;    // System.currentTimeMillis()
```

### `SyncQueueDao` — métodos disponibles

```java
List<SyncQueueEntity> getPendientes();        // WHERE estado = 'PENDIENTE' ORDER BY creadoEn ASC
long insert(SyncQueueEntity item);
void update(SyncQueueEntity item);
void delete(SyncQueueEntity item);
void deleteByLocalId(String localId);
int countPendientes();
```

### Payloads que encola `LimiTxRepository` (Gson serializado)

| `operacion`           | `payload` contiene                             | `localId`           |
|-----------------------|------------------------------------------------|---------------------|
| `CREATE_TARJETA`      | `TarjetaRequest` serializado                   | UUID local generado |
| `DELETE_TARJETA`      | `tarjetaId` (String plano)                     | tarjetaId           |
| `CREATE_TRANSACCION`  | `TransaccionRequest` serializado               | UUID local generado |
| `DELETE_TRANSACCION`  | `transaccionId` (String plano)                 | transaccionId       |
| `UPDATE_TRANSACCION`  | *(reservado, aún no usado — ignorar por ahora)* | —                   |

### Clases de request existentes

```java
// TarjetaRequest
String nombre, numero, banco, moneda;
double limite_mensual;
boolean activa;

// TransaccionRequest
UUID tarjeta_id;
String tipo;          // "entrada" | "salida"
BigDecimal monto;
String descripcion, subtipo;
boolean afecta_limite;
String fecha;         // ISO 8601
```

---

## API FastAPI — estado actual

**Base URL:** `http://69.169.102.33:8003/`

**Endpoints existentes relevantes:**

| Método | Path | Descripción |
|--------|------|-------------|
| `POST` | `/tarjetas` | Crea una tarjeta (body: `TarjetaCreate`) |
| `DELETE` | `/tarjetas/{tarjeta_id}` | Elimina tarjeta |
| `POST` | `/transacciones` | Crea transacción (body: `TransaccionCreate`) |
| `DELETE` | `/transacciones/{transaccion_id}` | Elimina transacción |
| `PUT` | `/transacciones/{transaccion_id}` | Actualiza transacción |

**Auth:** todas las rutas protegidas usan `Authorization: Bearer <token>` via `oauth2_scheme`.

### Modelos SQLAlchemy relevantes (`models.py`)

```python
class Tarjeta(Base):
    __tablename__ = "tarjetas"
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    usuario_id = Column(UUID(as_uuid=True), ForeignKey("usuarios.id", ondelete="CASCADE"))
    nombre = Column(Text, nullable=False)
    numero = Column(Text, nullable=False)
    banco = Column(Text, nullable=True)
    moneda = Column(Text, nullable=False)
    limite_mensual = Column(Numeric(12, 2), nullable=False)
    activa = Column(Boolean, default=True)

class Transaccion(Base):
    __tablename__ = "transacciones"
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    tarjeta_id = Column(UUID(as_uuid=True), ForeignKey("tarjetas.id", ondelete="CASCADE"))
    tipo = Column(Text, nullable=False)   # CHECK: 'entrada' | 'salida'
    subtipo = Column(Text, nullable=True)
    monto = Column(Numeric(12, 2), nullable=False)  # CHECK: monto > 0
    descripcion = Column(Text, nullable=True)
    afecta_limite = Column(Boolean, default=True)
    fecha_creacion = Column(DateTime, server_default=func.now())
    fecha = Column(DateTime, nullable=False)
    fecha_actualizacion = Column(DateTime, onupdate=func.now())
```

### Schemas Pydantic relevantes (`schemas.py`)

```python
class TarjetaCreate(BaseModel):
    nombre: str
    numero: str
    banco: Optional[str] = None
    moneda: str
    limite_mensual: Decimal
    activa: bool = True

class TransaccionCreate(BaseModel):
    tarjeta_id: uuid.UUID
    tipo: str
    monto: Decimal
    descripcion: Optional[str] = None
    subtipo: Optional[str] = None
    afecta_limite: bool = True
    fecha: datetime

class TarjetaResponse(BaseModel):
    id: uuid.UUID
    usuario_id: uuid.UUID
    nombre: str; numero: str; banco: Optional[str]; moneda: str
    limite_mensual: Decimal; activa: bool
    saldo_tarjeta: Optional[Decimal] = Decimal('0.00')
    extraccion_disponible: Optional[Decimal] = Decimal('0.00')
    deposito_disponible: Optional[Decimal] = Decimal('0.00')
    model_config = ConfigDict(from_attributes=True)

class TransaccionResponse(BaseModel):
    id: uuid.UUID; tarjeta_id: uuid.UUID; tipo: str; monto: Decimal
    descripcion: Optional[str]; subtipo: Optional[str]
    afecta_limite: bool; fecha: datetime
    fecha_creacion: datetime; fecha_actualizacion: Optional[datetime]
    model_config = ConfigDict(from_attributes=True)
```

---

## Objetivo de este paso

Implementar la **sincronización automática** de operaciones pendientes cuando el dispositivo recupera la conexión a internet.

El flujo es:
1. El usuario opera offline → las acciones se guardan en `SyncQueue` con estado `PENDIENTE`
2. Al recuperar red → un **WorkManager worker** lee la cola y las envía al servidor **una a una** (no batch por ahora)
3. Cada operación exitosa se elimina de la cola; las fallidas se marcan como `ERROR` y se reintenta hasta 3 veces
4. El servidor no necesita un endpoint `/sync/batch` — el worker reutiliza los endpoints REST existentes

---

## Tareas a realizar

### PARTE A — Android: `SyncWorker.java`

Crear `app/src/main/java/com/codram/limitx/sync/SyncWorker.java`

**Clase:** `public class SyncWorker extends Worker`  
**Constructor:** `public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params)`

**Lógica del método `doWork()`:**

```
1. Leer de Room todos los items de SyncQueue con estado = "PENDIENTE" (en orden por creadoEn ASC)
2. Para cada item:
   a. Marcar estado = "ENVIANDO" y guardar en Room
   b. Ejecutar la operación correspondiente según `item.operacion`:
      - "CREATE_TARJETA"     → POST /tarjetas         (deserializar payload como TarjetaRequest con Gson)
      - "DELETE_TARJETA"     → DELETE /tarjetas/{id}  (payload es el UUID directo)
      - "CREATE_TRANSACCION" → POST /transacciones     (deserializar payload como TransaccionRequest con Gson)
      - "DELETE_TRANSACCION" → DELETE /transacciones/{id} (payload es el UUID directo)
   c. Si éxito (HTTP 2xx): eliminar el item de Room con `syncQueueDao.delete(item)`
   d. Si error o excepción:
      - item.intentos++
      - Si item.intentos >= 3: item.estado = "ERROR"
      - Sino: item.estado = "PENDIENTE"   ← se reintentará en la próxima ejecución
      - Guardar con `syncQueueDao.update(item)`
3. Si todos los items procesados sin excepción fatal → retornar Result.success()
4. Si hubo algún fallo → retornar Result.retry()
```

**Cómo obtener el token dentro del Worker:**  
`new SessionManager(getApplicationContext()).getToken()`  
→ usarlo como `"Bearer " + token` en el header `Authorization`

**Dependencias internas a usar:**
- `AppDatabase.getInstance(context)` → para acceder a `syncQueueDao()`
- `ApiClient.getService()` → para llamadas a la API (ya es un singleton Retrofit)
- `new Gson()` → para deserializar el `payload`

**Llamadas Retrofit deben ser síncronas** (`.execute()` no `.enqueue()`) porque `doWork()` ya corre en un hilo de background de WorkManager.

**Imports necesarios:**
```java
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.Result;
import com.codram.limitx.data.SessionManager;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.TarjetaRequest;
import com.codram.limitx.data.api.TransaccionRequest;
import com.codram.limitx.data.local.AppDatabase;
import com.codram.limitx.data.local.dao.SyncQueueDao;
import com.codram.limitx.data.local.entity.SyncQueueEntity;
import com.google.gson.Gson;
import java.util.List;
import java.util.UUID;
```

---

### PARTE B — Android: Programar el Worker cuando vuelve la red

En `MainActivity.java`, al final del método `actualizarBannerConectividad()`, agregar la programación del worker **solo si hay conexión y hay items pendientes**:

```java
private void actualizarBannerConectividad() {
    boolean online = ConnectivityHelper.isOnline(this);
    tvOfflineBanner.setVisibility(online ? View.GONE : View.VISIBLE);

    // Si hay conexión, disparar sincronización si hay pendientes
    if (online) {
        dispararSyncSiHayPendientes();
    }
}

private void dispararSyncSiHayPendientes() {
    // Correr en background para no bloquear UI
    new Thread(() -> {
        boolean hayPendientes = repository.haySincronizacionPendiente();
        if (hayPendientes) {
            OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(SyncWorker.class)
                    .addTag("sync_queue")
                    .build();
            WorkManager.getInstance(getApplicationContext())
                    .enqueueUniqueWork("sync_pendientes", ExistingWorkPolicy.KEEP, syncWork);
        }
    }).start();
}
```

**Imports a agregar en `MainActivity.java`:**
```java
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.codram.limitx.sync.SyncWorker;
```

---

### PARTE C — Android: `libs.versions.toml` y `app/build.gradle.kts`

> ⚠️ WorkManager `work-runtime:2.9.0` **ya está declarado** en `libs.versions.toml` e incluido en `build.gradle.kts`. **No agregues nada** — solo verifica que está presente antes de continuar.

Verificar en `libs.versions.toml`:
```toml
[versions]
work = "2.9.0"

[libraries]
work-runtime = { group = "androidx.work", name = "work-runtime", version.ref = "work" }
```

Verificar en `app/build.gradle.kts`:
```kotlin
implementation(libs.work.runtime)
```

---

### PARTE D — API FastAPI: No se requiere endpoint nuevo

El worker reutiliza los endpoints REST individuales ya existentes. **No hay que modificar `main.py`** en este paso.

Lo único que puede necesitar ajuste es manejar el caso en que `CREATE_TARJETA` intente crear una tarjeta que ya existe en el servidor (porque el usuario volvió a abrir la app con red y el item no se había limpiado). La API ya retorna `201` en creación exitosa; si retorna `4xx` por duplicado, el worker lo contará como error y eventualmente marcará como `ERROR` tras 3 intentos — comportamiento aceptable.

---

## Restricciones

- **Java puro** — sin Kotlin, sin coroutines
- El worker extiende `Worker` (síncrono), **no** `CoroutineWorker` ni `ListenableWorker`
- Todas las llamadas Retrofit dentro del worker son **síncronas** (`.execute()`)
- No usar `allowMainThreadQueries()` — el worker ya corre en background
- No crear un endpoint `/sync/batch` — el diseño procesa operación por operación
- El `WorkManager` solo se programa una vez con `enqueueUniqueWork(..., KEEP, ...)` para evitar ejecuciones duplicadas si se llama varias veces
- Máximo 3 intentos por operación antes de marcarla como `ERROR` definitivo
- Si el token es `null` (sesión expirada), el worker debe retornar `Result.failure()` inmediatamente sin procesar nada

---

## Archivos a crear

- `app/src/main/java/com/codram/limitx/sync/SyncWorker.java` ← **nuevo**

## Archivos a modificar

- `app/src/main/java/com/codram/limitx/MainActivity.java` — agregar `dispararSyncSiHayPendientes()` y su llamada desde `actualizarBannerConectividad()`

## Archivos que NO tocar

- `LimiTxRepository.java`
- `AppDatabase.java`
- Entidades y DAOs en `data/local/`
- `ApiClient.java`, `ApiService.java`
- `ConnectivityHelper.java`
- `api/main.py`, `api/models.py`, `api/schemas.py`

---

## Resultado esperado

Al finalizar este paso:

1. El proyecto compila sin errores
2. Al crear/eliminar tarjetas o transacciones offline, las operaciones se encolan en Room
3. Al recuperar conexión a internet (al cargar tarjetas en `MainActivity`), el worker se dispara automáticamente
4. El worker procesa cada operación pendiente en orden cronológico usando los endpoints REST existentes
5. Las operaciones exitosas desaparecen de la cola; las fallidas se reintentán hasta 3 veces
6. Si todas las operaciones fallan, el worker devuelve `Result.retry()` y WorkManager lo reintentará según su política de backoff
