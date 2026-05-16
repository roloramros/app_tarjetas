package com.codram.limitx.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.codram.limitx.data.local.entity.TarjetaEntity;

import java.util.List;

@Dao
public interface TarjetaDao {
    @Query("SELECT * FROM tarjetas WHERE usuarioId = :usuarioId")
    List<TarjetaEntity> getByUsuario(String usuarioId);

    @Query("SELECT * FROM tarjetas WHERE id = :id")
    TarjetaEntity getById(String id);

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
