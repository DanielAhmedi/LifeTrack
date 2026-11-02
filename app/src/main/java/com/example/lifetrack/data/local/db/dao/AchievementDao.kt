package com.example.lifetrack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lifetrack.data.local.db.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getById(id: Long): AchievementEntity?

    @Query("SELECT * FROM achievements WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): AchievementEntity?

    @Query("SELECT * FROM achievements ORDER BY id ASC")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements ORDER BY id ASC")
    suspend fun getAll(): List<AchievementEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(achievement: AchievementEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(achievements: List<AchievementEntity>): List<Long>

    @Update
    suspend fun update(achievement: AchievementEntity)

    @Delete
    suspend fun delete(achievement: AchievementEntity)

    @Query("DELETE FROM achievements WHERE id = :id")
    suspend fun deleteById(id: Long)
}
