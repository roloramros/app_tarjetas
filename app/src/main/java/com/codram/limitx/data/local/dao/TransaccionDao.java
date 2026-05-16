package com.codram.limitx.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.codram.limitx.data.local.entity.TransaccionEntity;

import java.util.List;

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

    @Query("UPDATE transacciones SET monto = :monto, descripcion = :descripcion, fecha = :fecha WHERE id = :id")
    void actualizarCampos(String id, String monto, String descripcion, String fecha);
}
