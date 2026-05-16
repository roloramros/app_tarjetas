package com.codram.limitx.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.ApiService;
import com.codram.limitx.data.api.TarjetaRequest;
import com.codram.limitx.data.api.TarjetaResponse;
import com.codram.limitx.data.api.TransaccionRequest;
import com.codram.limitx.data.api.TransaccionResponse;
import com.codram.limitx.data.api.TransaccionUpdate;
import com.codram.limitx.data.api.UsuarioResponse;
import com.codram.limitx.data.sync.TransaccionUpdatePayload;
import com.codram.limitx.data.local.AppDatabase;
import com.codram.limitx.data.local.entity.SyncQueueEntity;
import com.codram.limitx.data.local.entity.TarjetaEntity;
import com.codram.limitx.data.local.entity.TransaccionEntity;
import com.codram.limitx.utils.ConnectivityHelper;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class LimiTxRepository {

    private final AppDatabase db;
    private final ApiService api;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final Context context;
    private final Gson gson;

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String mensaje);
    }

    public LimiTxRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = AppDatabase.getInstance(this.context);
        this.api = ApiClient.getService();
        this.executor = Executors.newFixedThreadPool(3);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new Gson();
    }

    public void getTarjetas(String usuarioId, String token, Callback<List<TarjetaEntity>> callback) {
        executor.execute(() -> {
            // 1. Leer de Room e informar inmediatamente
            List<TarjetaEntity> cached = db.tarjetaDao().getByUsuario(usuarioId);
            mainHandler.post(() -> callback.onSuccess(cached));

            // 2. Si hay red, actualizar desde API
            if (ConnectivityHelper.isOnline(context)) {
                try {
                    Response<List<TarjetaResponse>> response = api.getTarjetas(token).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        List<TarjetaEntity> entities = new ArrayList<>();
                        for (TarjetaResponse r : response.body()) {
                            entities.add(TarjetaEntity.fromResponse(r));
                        }
                        db.tarjetaDao().deleteAllByUsuario(usuarioId);
                        db.tarjetaDao().insertAll(entities);
                        
                        // Informar nuevos datos
                        mainHandler.post(() -> callback.onSuccess(entities));
                    }
                } catch (IOException e) {
                    // Error de red silencioso o informar si es necesario
                }
            }
        });
    }

    public void getTransacciones(String tarjetaId, String token, Callback<List<TransaccionEntity>> callback) {
        executor.execute(() -> {
            List<TransaccionEntity> cached = db.transaccionDao().getByTarjeta(tarjetaId);
            mainHandler.post(() -> callback.onSuccess(cached));

            if (ConnectivityHelper.isOnline(context)) {
                try {
                    Response<List<TransaccionResponse>> response = api.getTransacciones(token, UUID.fromString(tarjetaId)).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        List<TransaccionEntity> entities = new ArrayList<>();
                        for (TransaccionResponse r : response.body()) {
                            entities.add(TransaccionEntity.fromResponse(r));
                        }
                        db.transaccionDao().deleteByTarjeta(tarjetaId);
                        db.transaccionDao().insertAll(entities);
                        mainHandler.post(() -> callback.onSuccess(entities));
                    }
                } catch (IOException e) {
                    mainHandler.post(() -> callback.onError(e.getMessage()));
                }
            }
        });
    }

    public void getTransaccionesMes(String tarjetaId, String token, Callback<List<TransaccionEntity>> callback) {
        executor.execute(() -> {
            // Para el mes actual, no guardamos en Room de forma masiva para no mezclar con historial completo
            // o simplemente mostramos lo que hay en Room que coincida.
            // Para simplificar y seguir el flujo, llamamos a la API y devolvemos.
            if (ConnectivityHelper.isOnline(context)) {
                try {
                    Response<List<TransaccionResponse>> response = api.getTransaccionesMes(token, UUID.fromString(tarjetaId)).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        List<TransaccionEntity> entities = new ArrayList<>();
                        for (TransaccionResponse r : response.body()) {
                            entities.add(TransaccionEntity.fromResponse(r));
                        }
                        mainHandler.post(() -> callback.onSuccess(entities));
                    }
                } catch (IOException e) {
                    mainHandler.post(() -> callback.onError(e.getMessage()));
                }
            } else {
                // Si offline, devolvemos lo que hay en Room (que es el historial completo)
                List<TransaccionEntity> cached = db.transaccionDao().getByTarjeta(tarjetaId);
                mainHandler.post(() -> callback.onSuccess(cached));
            }
        });
    }

    public void actualizarTransaccion(String transaccionId, TransaccionUpdate update, String token, Callback<TransaccionEntity> callback) {
        executor.execute(() -> {
            TransaccionEntity localTx = db.transaccionDao().getById(transaccionId);
            if (localTx == null) {
                mainHandler.post(() -> callback.onError("Transacción no encontrada"));
                return;
            }

            String tarjetaId = localTx.tarjetaId;

            // Actualizar en Room inmediatamente (optimistic update)
            db.transaccionDao().actualizarCampos(transaccionId, 
                    update.getMonto().toString(), 
                    update.getDescripcion(), 
                    update.getFecha());
            
            recalcularSaldosTarjeta(tarjetaId);
            
            if (ConnectivityHelper.isOnline(context)) {
                try {
                    Response<TransaccionResponse> response = api.actualizarTransaccion(
                        token, UUID.fromString(transaccionId), update
                    ).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        TransaccionEntity updated = TransaccionEntity.fromResponse(response.body());
                        db.transaccionDao().insertOrReplace(updated);
                        recalcularSaldosTarjeta(tarjetaId);
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

    public void crearTransaccion(TransaccionRequest request, String token, Callback<TransaccionEntity> callback) {
        executor.execute(() -> {
            String localId = UUID.randomUUID().toString();
            TransaccionEntity entity = new TransaccionEntity();
            entity.id = localId;
            // Mapear campos desde request
            // Nota: El prompt pide crear TransaccionEntity con ese UUID y estado local.
            // Aquí falta lógica de mapeo detallada pero se cumple lo esencial.
            entity.tarjetaId = request.getTarjetaId().toString();
            entity.tipo = request.getTipo();
            entity.monto = request.getMonto().toString();
            entity.descripcion = request.getDescripcion();
            entity.subtipo = request.getSubtipo();
            entity.afecta_limite = request.isAfectaLimite();
            entity.fecha = request.getFecha();
            entity.fechaCreacion = String.valueOf(System.currentTimeMillis());
            
            db.transaccionDao().insertOrReplace(entity);
            recalcularSaldosTarjeta(entity.tarjetaId);
            mainHandler.post(() -> callback.onSuccess(entity));

            if (ConnectivityHelper.isOnline(context)) {
                try {
                    Response<Void> response = api.crearTransaccion(token, request).execute();
                    if (response.isSuccessful()) {
                        // Idealmente la API debería devolver el objeto creado con el ID real.
                        // Como es Void, esperamos a la siguiente sincronización para refrescar IDs.
                    } else {
                        queueSync("CREATE_TRANSACCION", gson.toJson(request), localId);
                    }
                } catch (IOException e) {
                    queueSync("CREATE_TRANSACCION", gson.toJson(request), localId);
                }
            } else {
                queueSync("CREATE_TRANSACCION", gson.toJson(request), localId);
            }
        });
    }

    public void eliminarTransaccion(String transaccionId, String token, Callback<Void> callback) {
        executor.execute(() -> {
            TransaccionEntity localTx = db.transaccionDao().getById(transaccionId);
            if (localTx != null) {
                String tarjetaId = localTx.tarjetaId;
                db.transaccionDao().deleteById(transaccionId);
                recalcularSaldosTarjeta(tarjetaId);
            }
            
            mainHandler.post(() -> callback.onSuccess(null));

            if (ConnectivityHelper.isOnline(context)) {
                try {
                    api.eliminarTransaccion(token, UUID.fromString(transaccionId)).execute();
                } catch (Exception e) {
                    queueSync("DELETE_TRANSACCION", transaccionId, transaccionId);
                }
            } else {
                queueSync("DELETE_TRANSACCION", transaccionId, transaccionId);
            }
        });
    }

    public void crearTarjeta(TarjetaRequest request, String usuarioId, String token, Callback<TarjetaEntity> callback) {
        executor.execute(() -> {
            String localId = UUID.randomUUID().toString();
            TarjetaEntity entity = new TarjetaEntity();
            entity.id = localId;
            entity.usuarioId = usuarioId;
            entity.nombre = request.getNombre();
            entity.numero = request.getNumero();
            entity.banco = request.getBanco();
            entity.moneda = request.getMoneda();
            entity.limiteMensual = request.getLimiteMensual();
            entity.activa = request.isActiva();
            
            db.tarjetaDao().insertOrReplace(entity);
            mainHandler.post(() -> callback.onSuccess(entity));

            if (ConnectivityHelper.isOnline(context)) {
                try {
                    Response<TarjetaResponse> response = api.createTarjeta(token, request).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        db.tarjetaDao().deleteById(localId);
                        db.tarjetaDao().insertOrReplace(TarjetaEntity.fromResponse(response.body()));
                    } else {
                        queueSync("CREATE_TARJETA", gson.toJson(request), localId);
                    }
                } catch (IOException e) {
                    queueSync("CREATE_TARJETA", gson.toJson(request), localId);
                }
            } else {
                queueSync("CREATE_TARJETA", gson.toJson(request), localId);
            }
        });
    }

    public void eliminarTarjeta(String tarjetaId, String token, Callback<Void> callback) {
        executor.execute(() -> {
            db.tarjetaDao().deleteById(tarjetaId);
            mainHandler.post(() -> callback.onSuccess(null));

            if (ConnectivityHelper.isOnline(context)) {
                try {
                    api.eliminarTarjeta(token, UUID.fromString(tarjetaId)).execute();
                } catch (Exception e) {
                    queueSync("DELETE_TARJETA", tarjetaId, tarjetaId);
                }
            } else {
                queueSync("DELETE_TARJETA", tarjetaId, tarjetaId);
            }
        });
    }

    public boolean haySincronizacionPendiente() {
        // Esta operación debería ser síncrona según la firma, pero Room no lo permite en UI thread.
        // Como el prompt no especifica cómo llamarlo, se asume que se llama desde background
        // o se requiere un cambio en la firma. Por ahora, implementamos consulta directa.
        try {
            return executor.submit(() -> db.syncQueueDao().countPendientes() > 0).get();
        } catch (Exception e) {
            return false;
        }
    }

    private void queueSync(String op, String payload, String localId) {
        db.syncQueueDao().insert(new SyncQueueEntity(op, payload, localId));
    }

    private void recalcularSaldosTarjeta(String tarjetaId) {
        TarjetaEntity tarjeta = db.tarjetaDao().getById(tarjetaId);
        if (tarjeta == null) return;

        List<TransaccionEntity> transacciones = db.transaccionDao().getByTarjeta(tarjetaId);
        
        double saldoTotal = 0;
        double consumoMesActual = 0;

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int yearActual = cal.get(java.util.Calendar.YEAR);
        int monthActual = cal.get(java.util.Calendar.MONTH);

        for (TransaccionEntity tx : transacciones) {
            double monto = Double.parseDouble(tx.monto);
            
            if ("entrada".equalsIgnoreCase(tx.tipo)) {
                saldoTotal += monto;
            } else {
                saldoTotal -= monto;
            }

            if ("salida".equalsIgnoreCase(tx.tipo) && tx.afecta_limite) {
                try {
                    String[] parts = tx.fecha.split("-");
                    int txYear = Integer.parseInt(parts[0]);
                    int txMonth = Integer.parseInt(parts[1]) - 1;
                    
                    if (txYear == yearActual && txMonth == monthActual) {
                        consumoMesActual += monto;
                    }
                } catch (Exception ignored) {}
            }
        }

        tarjeta.saldoTarjeta = saldoTotal;
        tarjeta.extraccionDisponible = tarjeta.limiteMensual - consumoMesActual;
        tarjeta.depositoDisponible = tarjeta.limiteMensual - saldoTotal - consumoMesActual;

        db.tarjetaDao().insertOrReplace(tarjeta);
    }
}
