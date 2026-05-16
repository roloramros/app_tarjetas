package com.codram.limitx.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.codram.limitx.data.api.TransaccionRequest;
import com.codram.limitx.data.api.TransaccionResponse;

import java.math.BigDecimal;
import java.util.UUID;

@Entity(tableName = "transacciones")
public class TransaccionEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String tarjetaId;
    public String tipo;
    public String monto; // Guardar BigDecimal como String para evitar pérdida de precisión
    public String descripcion;
    public String subtipo;
    public boolean afecta_limite;
    public String fecha; // ISO 8601
    public String fechaCreacion;

    public static TransaccionEntity fromResponse(TransaccionResponse r) {
        TransaccionEntity entity = new TransaccionEntity();
        entity.id = r.getId().toString();
        entity.tarjetaId = r.getTarjetaId().toString();
        entity.tipo = r.getTipo();
        entity.monto = r.getMonto().toString();
        entity.descripcion = r.getDescripcion();
        entity.fecha = r.getFecha();
        // subtipo, afecta_limite y fechaCreacion pueden no venir en TransaccionResponse según el archivo actual, 
        // pero se incluyen en la entidad según el prompt.
        return entity;
    }

    public TransaccionRequest toRequest() {
        return new TransaccionRequest(
                UUID.fromString(tarjetaId),
                tipo,
                new BigDecimal(monto),
                descripcion,
                subtipo,
                afecta_limite,
                fecha
        );
    }
}
