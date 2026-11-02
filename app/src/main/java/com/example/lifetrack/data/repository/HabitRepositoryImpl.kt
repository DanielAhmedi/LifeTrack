package com.example.lifetrack.data.repository

import com.example.lifetrack.data.local.db.dao.HabitDao
import com.example.lifetrack.data.mapper.toDomain
import com.example.lifetrack.data.mapper.toEntity
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepositoryImpl(private val habitDao: HabitDao) : HabitRepository {

    override fun observeAll(): Flow<List<Habit>> =
        habitDao.observeAll().map { habits -> habits.map { it.toDomain() } }

    override fun observeActive(): Flow<List<Habit>> =
        habitDao.observeActive().map { habits -> habits.map { it.toDomain() } }

    override suspend fun getAll(): List<Habit> = habitDao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): Habit? = habitDao.getById(id)?.toDomain()

    override suspend fun insert(habit: Habit): Long = habitDao.insert(habit.toEntity())

    override suspend fun update(habit: Habit) = habitDao.update(habit.toEntity())

    override suspend fun delete(habit: Habit) = habitDao.delete(habit.toEntity())

    override suspend fun deleteById(id: Long) = habitDao.deleteById(id)
}
