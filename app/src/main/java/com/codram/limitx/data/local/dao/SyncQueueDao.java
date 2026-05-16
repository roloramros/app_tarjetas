package com.codram.limitx.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.codram.limitx.data.local.entity.SyncQueueEntity;

import java.util.List;

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
