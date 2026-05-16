package com.codram.limitx.data.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.codram.limitx.data.SessionManager;
import com.codram.limitx.data.api.ApiClient;
import com.codram.limitx.data.api.ApiService;
import com.codram.limitx.data.api.TarjetaRequest;
import com.codram.limitx.data.api.TarjetaResponse;
import com.codram.limitx.data.api.TransaccionRequest;
import com.codram.limitx.data.api.TransaccionResponse;
import com.codram.limitx.data.api.TransaccionUpdate;
import com.codram.limitx.data.local.AppDatabase;
import com.codram.limitx.data.local.entity.SyncQueueEntity;
import com.codram.limitx.data.local.entity.TarjetaEntity;
import com.codram.limitx.data.local.entity.TransaccionEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import retrofit2.Response;

public class SyncWorker extends Worker {

    private final Gson gson;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.gson = new GsonBuilder()
                .registerTypeAdapter(UUID.class, (JsonDeserializer<UUID>)
                        (json, type, ctx) -> UUID.fromString(json.getAsString()))
                .create();
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        String token = sessionManager.getToken();

        if (token == null) {
            return Result.failure();
        }

        List<SyncQueueEntity> pendientes = db.syncQueueDao().getPendientes();
        if (pendientes.isEmpty()) {
            return Result.success();
        }

        ApiService api = ApiClient.getService();
        String authHeader = "Bearer " + token;

        for (SyncQueueEntity item : pendientes) {
            item.estado = "ENVIANDO";
            db.syncQueueDao().update(item);

            try {
                boolean success = false;
                switch (item.operacion) {
                    case "CREATE_TARJETA":
                        TarjetaRequest tReq = gson.fromJson(item.payload, TarjetaRequest.class);
                        Response<TarjetaResponse> tRes = api.createTarjeta(authHeader, tReq).execute();
                        if (tRes.isSuccessful() && tRes.body() != null) {
                            db.tarjetaDao().deleteById(item.localId);
                            db.tarjetaDao().insertOrReplace(TarjetaEntity.fromResponse(tRes.body()));
                            success = true;
                        }
                        break;

                    case "DELETE_TARJETA":
                        Response<Void> dtRes = api.eliminarTarjeta(authHeader, UUID.fromString(item.payload)).execute();
                        if (dtRes.isSuccessful()) {
                            success = true;
                        }
                        break;

                    case "CREATE_TRANSACCION":
                        TransaccionRequest trReq = gson.fromJson(item.payload, TransaccionRequest.class);
                        Response<Void> trRes = api.crearTransaccion(authHeader, trReq).execute();
                        if (trRes.isSuccessful()) {
                            success = true;
                        }
                        break;

                    case "UPDATE_TRANSACCION":
                        TransaccionUpdatePayload p = gson.fromJson(item.payload, TransaccionUpdatePayload.class);
                        TransaccionUpdate u = new TransaccionUpdate(new BigDecimal(p.monto), p.descripcion, p.fecha);
                        Response<TransaccionResponse> uRes = api.actualizarTransaccion(authHeader, UUID.fromString(item.localId), u).execute();
                        if (uRes.isSuccessful() && uRes.body() != null) {
                            db.transaccionDao().insertOrReplace(TransaccionEntity.fromResponse(uRes.body()));
                            success = true;
                        }
                        break;

                    case "DELETE_TRANSACCION":
                        Response<Void> dtrRes = api.eliminarTransaccion(authHeader, UUID.fromString(item.payload)).execute();
                        if (dtrRes.isSuccessful()) {
                            success = true;
                        }
                        break;
                }

                if (success) {
                    db.syncQueueDao().delete(item);
                } else {
                    item.intentos++;
                    if (item.intentos >= 5) {
                        item.estado = "ERROR";
                    } else {
                        item.estado = "PENDIENTE";
                    }
                    db.syncQueueDao().update(item);
                }

            } catch (IOException e) {
                // Error de red, reintentar con el worker completo
                item.estado = "PENDIENTE";
                db.syncQueueDao().update(item);
                return Result.retry();
            } catch (Exception e) {
                // Error fatal en este item
                item.intentos++;
                item.estado = (item.intentos >= 5) ? "ERROR" : "PENDIENTE";
                db.syncQueueDao().update(item);
            }
        }

        return Result.success();
    }
}
