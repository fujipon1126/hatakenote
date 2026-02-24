package com.example.hatakenote.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hatakenote.core.database.entity.PlantingPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantingPhotoDao {
    @Query("SELECT * FROM planting_photos WHERE plantingId = :plantingId ORDER BY takenDate DESC")
    fun getByPlantingId(plantingId: Long): Flow<List<PlantingPhotoEntity>>

    @Query("SELECT * FROM planting_photos WHERE id = :id")
    suspend fun getById(id: Long): PlantingPhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: PlantingPhotoEntity): Long

    @Delete
    suspend fun delete(photo: PlantingPhotoEntity)

    @Query("DELETE FROM planting_photos WHERE plantingId = :plantingId")
    suspend fun deleteByPlantingId(plantingId: Long)
}
