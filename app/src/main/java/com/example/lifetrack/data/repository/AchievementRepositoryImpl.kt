package com.example.lifetrack.data.repository

import com.example.lifetrack.data.local.db.dao.AchievementDao
import com.example.lifetrack.data.mapper.toDomain
import com.example.lifetrack.data.mapper.toEntity
import com.example.lifetrack.domain.model.Achievement
import com.example.lifetrack.domain.repository.AchievementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AchievementRepositoryImpl(
    private val achievementDao: AchievementDao
) : AchievementRepository {

    override fun observeAll(): Flow<List<Achievement>> =
        achievementDao.observeAll().map { achievements -> achievements.map { it.toDomain() } }

    override suspend fun getAll(): List<Achievement> =
        achievementDao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): Achievement? = achievementDao.getById(id)?.toDomain()

    override suspend fun getByCode(code: String): Achievement? =
        achievementDao.getByCode(code)?.toDomain()

    override suspend fun insert(achievement: Achievement): Long =
        achievementDao.insert(achievement.toEntity())

    override suspend fun insertAll(achievements: List<Achievement>): List<Long> =
        achievementDao.insertAll(achievements.map { it.toEntity() })

    override suspend fun update(achievement: Achievement) =
        achievementDao.update(achievement.toEntity())

    override suspend fun delete(achievement: Achievement) =
        achievementDao.delete(achievement.toEntity())

    override suspend fun deleteById(id: Long) = achievementDao.deleteById(id)
}
