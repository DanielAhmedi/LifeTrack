package com.example.lifetrack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lifetrack.data.local.db.entity.HabitCompletionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {

    @Query("SELECT * FROM habit_completions WHERE id = :id")
    suspend fun getById(id: Long): HabitCompletionEntity?

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId AND date = :date LIMIT 1")
    suspend fun getByHabitAndDate(habitId: Long, date: Long): HabitCompletionEntity?

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId ORDER BY date DESC")
    fun observeByHabit(habitId: Long): Flow<List<HabitCompletionEntity>>

    @Query(
        "SELECT * FROM habit_completions " +
            "WHERE habit_id = :habitId AND date BETWEEN :fromDate AND :toDate ORDER BY date ASC"
    )
    fun observeByHabitBetween(
        habitId: Long,
        fromDate: Long,
        toDate: Long
    ): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId ORDER BY date ASC")
    suspend fun getByHabit(habitId: Long): List<HabitCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: HabitCompletionEntity): Long

    @Update
    suspend fun update(completion: HabitCompletionEntity)

    @Delete
    suspend fun delete(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
