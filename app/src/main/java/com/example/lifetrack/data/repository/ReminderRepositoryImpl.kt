package com.example.lifetrack.data.repository

import com.example.lifetrack.data.local.db.dao.ReminderDao
import com.example.lifetrack.data.mapper.toDomain
import com.example.lifetrack.data.mapper.toEntity
import com.example.lifetrack.domain.model.Reminder
import com.example.lifetrack.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl(private val reminderDao: ReminderDao) : ReminderRepository {

    override fun observeAll(): Flow<List<Reminder>> =
        reminderDao.observeAll().map { reminders -> reminders.map { it.toDomain() } }

    override fun observeByHabitId(habitId: Long): Flow<Reminder?> =
        reminderDao.observeByHabitId(habitId).map { reminder -> reminder?.toDomain() }

    override suspend fun getAll(): List<Reminder> = reminderDao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): Reminder? = reminderDao.getById(id)?.toDomain()

    override suspend fun getByHabitId(habitId: Long): Reminder? =
        reminderDao.getByHabitId(habitId)?.toDomain()

    override suspend fun insert(reminder: Reminder): Long = reminderDao.insert(reminder.toEntity())

    override suspend fun update(reminder: Reminder) = reminderDao.update(reminder.toEntity())

    override suspend fun delete(reminder: Reminder) = reminderDao.delete(reminder.toEntity())

    override suspend fun deleteByHabitId(habitId: Long) = reminderDao.deleteByHabitId(habitId)
}
