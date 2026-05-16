package com.codram.limitx.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.codram.limitx.data.api.TarjetaResponse;

@Entity(tableName = "tarjetas")
public class TarjetaEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String usuarioId;
    public String nombre;
    public String numero;
    public String banco;
    public String moneda;
    public double limiteMensual;
    public boolean activa;
    
    // Campos calculados que se guardan como caché
    public double saldoTarjeta;
    public double extraccionDisponible;
    public double depositoDisponible;

    public static TarjetaEntity fromResponse(TarjetaResponse r) {
        TarjetaEntity entity = new TarjetaEntity();
        entity.id = r.getId().toString();
        entity.usuarioId = r.getUsuarioId().toString();
        entity.nombre = r.getNombre();
        entity.numero = r.getNumero();
        entity.banco = r.getBanco();
        entity.moneda = r.getMoneda();
        entity.limiteMensual = r.getLimiteMensual();
        entity.activa = r.isActiva();
        entity.saldoTarjeta = r.getSaldo_tarjeta();
        entity.extraccionDisponible = r.getExtraccion_disponible();
        entity.depositoDisponible = r.getDeposito_disponible();
        return entity;
    }
}
