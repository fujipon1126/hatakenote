package com.example.hatakenote.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hatakenote.core.database.entity.CropEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropDao {
    @Query("SELECT * FROM crops ORDER BY name")
    fun getAll(): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE isActive = 1 ORDER BY name")
    fun getActiveOnly(): Flow<List<CropEntity>>

    @Query("SELECT * FROM crops WHERE id = :id")
    suspend fun getById(id: Long): CropEntity?

    @Query("SELECT * FROM crops WHERE familyId = :familyId ORDER BY name")
    fun getByFamilyId(familyId: Long): Flow<List<CropEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crop: CropEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crops: List<CropEntity>)

    @Update
    suspend fun update(crop: CropEntity)

    @Delete
    suspend fun delete(crop: CropEntity)
}
