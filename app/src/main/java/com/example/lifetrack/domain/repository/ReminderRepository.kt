package com.example.lifetrack.domain.repository

import com.example.lifetrack.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {

    fun observeAll(): Flow<List<Reminder>>

    fun observeByHabitId(habitId: Long): Flow<Reminder?>

    suspend fun getAll(): List<Reminder>

    suspend fun getById(id: Long): Reminder?

    suspend fun getByHabitId(habitId: Long): Reminder?

    suspend fun insert(reminder: Reminder): Long

    suspend fun update(reminder: Reminder)

    suspend fun delete(reminder: Reminder)

    suspend fun deleteByHabitId(habitId: Long)
}
