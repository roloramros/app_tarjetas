package com.codram.limitx.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.codram.limitx.data.local.dao.SyncQueueDao;
import com.codram.limitx.data.local.dao.TarjetaDao;
import com.codram.limitx.data.local.dao.TransaccionDao;
import com.codram.limitx.data.local.entity.SyncQueueEntity;
import com.codram.limitx.data.local.entity.TarjetaEntity;
import com.codram.limitx.data.local.entity.TransaccionEntity;

@Database(entities = {TarjetaEntity.class, TransaccionEntity.class, SyncQueueEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract TarjetaDao tarjetaDao();
    public abstract TransaccionDao transaccionDao();
    public abstract SyncQueueDao syncQueueDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "limitx_db")
                            .build();
                }
            }
        }
        return instance;
    }
}
