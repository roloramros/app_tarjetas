package com.codram.limitx.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_queue")
public class SyncQueueEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String operacion; // "CREATE_TARJETA", "DELETE_TARJETA", "CREATE_TRANSACCION", "UPDATE_TRANSACCION", "DELETE_TRANSACCION"
    public String payload; // JSON con los datos necesarios
    public String localId; // UUID local del objeto afectado
    public String estado; // "PENDIENTE", "ENVIANDO", "ERROR"
    public int intentos;
    public long creadoEn; // timestamp en milisegundos

    public SyncQueueEntity() {}

    public SyncQueueEntity(String operacion, String payload, String localId) {
        this.operacion = operacion;
        this.payload = payload;
        this.localId = localId;
        this.estado = "PENDIENTE";
        this.intentos = 0;
        this.creadoEn = System.currentTimeMillis();
    }
}
