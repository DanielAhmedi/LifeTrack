package com.example.lifetrack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lifetrack.data.local.db.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE habit_id = :habitId LIMIT 1")
    suspend fun getByHabitId(habitId: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE habit_id = :habitId LIMIT 1")
    fun observeByHabitId(habitId: Long): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders ORDER BY time_minutes ASC")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY time_minutes ASC")
    suspend fun getAll(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE habit_id = :habitId")
    suspend fun deleteByHabitId(habitId: Long)
}
