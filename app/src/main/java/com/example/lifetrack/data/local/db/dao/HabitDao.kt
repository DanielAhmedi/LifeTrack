package com.example.lifetrack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lifetrack.data.local.db.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): HabitEntity?

    @Query("SELECT * FROM habits WHERE id = :id")
    fun observeById(id: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM habits ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE is_active = 1 ORDER BY name COLLATE NOCASE ASC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY created_at ASC")
    suspend fun getAll(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(habit: HabitEntity): Long

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Long)
}
