package com.example.lifetrack.domain.repository

import com.example.lifetrack.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {

    fun observeAll(): Flow<List<Achievement>>

    suspend fun getAll(): List<Achievement>

    suspend fun getById(id: Long): Achievement?

    suspend fun getByCode(code: String): Achievement?

    suspend fun insert(achievement: Achievement): Long

    suspend fun insertAll(achievements: List<Achievement>): List<Long>

    suspend fun update(achievement: Achievement)

    suspend fun delete(achievement: Achievement)

    suspend fun deleteById(id: Long)
}
