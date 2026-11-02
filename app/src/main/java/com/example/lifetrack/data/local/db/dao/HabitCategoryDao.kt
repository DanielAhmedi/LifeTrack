package com.example.lifetrack.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.lifetrack.data.local.db.entity.HabitCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCategoryDao {

    @Query("SELECT * FROM habit_categories WHERE id = :id")
    suspend fun getById(id: Long): HabitCategoryEntity?

    @Query("SELECT * FROM habit_categories ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<HabitCategoryEntity>>

    @Query("SELECT * FROM habit_categories ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<HabitCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: HabitCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<HabitCategoryEntity>): List<Long>

    @Update
    suspend fun update(category: HabitCategoryEntity)

    @Delete
    suspend fun delete(category: HabitCategoryEntity)

    @Query("DELETE FROM habit_categories WHERE id = :id")
    suspend fun deleteById(id: Long)
}
