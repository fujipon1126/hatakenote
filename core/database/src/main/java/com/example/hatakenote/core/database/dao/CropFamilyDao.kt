package com.example.hatakenote.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hatakenote.core.database.entity.CropFamilyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CropFamilyDao {
    @Query("SELECT * FROM crop_families ORDER BY name")
    fun getAll(): Flow<List<CropFamilyEntity>>

    @Query("SELECT * FROM crop_families WHERE id = :id")
    suspend fun getById(id: Long): CropFamilyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(family: CropFamilyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(families: List<CropFamilyEntity>)

    @Update
    suspend fun update(family: CropFamilyEntity)

    @Delete
    suspend fun delete(family: CropFamilyEntity)
}
