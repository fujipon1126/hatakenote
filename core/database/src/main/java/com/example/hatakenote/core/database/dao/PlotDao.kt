package com.example.hatakenote.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hatakenote.core.database.entity.PlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlotDao {
    @Query("SELECT * FROM plots ORDER BY side, number")
    fun getAll(): Flow<List<PlotEntity>>

    @Query("SELECT * FROM plots WHERE id = :id")
    suspend fun getById(id: Long): PlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plot: PlotEntity): Long

    @Update
    suspend fun update(plot: PlotEntity)

    @Delete
    suspend fun delete(plot: PlotEntity)

    @Query("SELECT COALESCE(MAX(number), 0) FROM plots")
    suspend fun getMaxNumber(): Int
}
