package com.example.lifetrack.data.repository

import com.example.lifetrack.data.local.db.dao.HabitCompletionDao
import com.example.lifetrack.data.mapper.toDomain
import com.example.lifetrack.data.mapper.toEntity
import com.example.lifetrack.domain.model.HabitCompletion
import com.example.lifetrack.domain.repository.HabitCompletionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitCompletionRepositoryImpl(
    private val habitCompletionDao: HabitCompletionDao
) : HabitCompletionRepository {

    override suspend fun getById(id: Long): HabitCompletion? =
        habitCompletionDao.getById(id)?.toDomain()

    override suspend fun getByHabitAndDate(habitId: Long, date: Long): HabitCompletion? =
        habitCompletionDao.getByHabitAndDate(habitId, date)?.toDomain()

    override suspend fun getByHabit(habitId: Long): List<HabitCompletion> =
        habitCompletionDao.getByHabit(habitId).map { it.toDomain() }

    override fun observeByHabit(habitId: Long): Flow<List<HabitCompletion>> =
        habitCompletionDao.observeByHabit(habitId).map { completions ->
            completions.map { it.toDomain() }
        }

    override fun observeByHabitBetween(
        habitId: Long,
        fromDate: Long,
        toDate: Long
    ): Flow<List<HabitCompletion>> =
        habitCompletionDao.observeByHabitBetween(habitId, fromDate, toDate).map { completions ->
            completions.map { it.toDomain() }
        }

    override suspend fun insert(completion: HabitCompletion): Long =
        habitCompletionDao.insert(completion.toEntity())

    override suspend fun update(completion: HabitCompletion) =
        habitCompletionDao.update(completion.toEntity())

    override suspend fun delete(completion: HabitCompletion) =
        habitCompletionDao.delete(completion.toEntity())

    override suspend fun deleteById(id: Long) = habitCompletionDao.deleteById(id)
}
