package com.example.hatakenote.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hatakenote.core.database.entity.RotationIncompatibilityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RotationIncompatibilityDao {
    @Query("SELECT * FROM rotation_incompatibilities")
    fun getAll(): Flow<List<RotationIncompatibilityEntity>>

    @Query("SELECT * FROM rotation_incompatibilities WHERE familyId = :familyId")
    suspend fun getByFamilyId(familyId: Long): List<RotationIncompatibilityEntity>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM rotation_incompatibilities
            WHERE familyId = :familyId AND incompatibleFamilyId = :targetFamilyId
        )
    """)
    suspend fun isIncompatible(familyId: Long, targetFamilyId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(incompatibility: RotationIncompatibilityEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(incompatibilities: List<RotationIncompatibilityEntity>)

    @Delete
    suspend fun delete(incompatibility: RotationIncompatibilityEntity)
}
