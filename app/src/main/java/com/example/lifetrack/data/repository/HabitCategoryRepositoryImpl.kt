package com.example.lifetrack.data.repository

import com.example.lifetrack.data.local.db.dao.HabitCategoryDao
import com.example.lifetrack.data.mapper.toDomain
import com.example.lifetrack.data.mapper.toEntity
import com.example.lifetrack.domain.model.HabitCategory
import com.example.lifetrack.domain.repository.HabitCategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitCategoryRepositoryImpl(
    private val habitCategoryDao: HabitCategoryDao
) : HabitCategoryRepository {

    override fun observeAll(): Flow<List<HabitCategory>> =
        habitCategoryDao.observeAll().map { categories -> categories.map { it.toDomain() } }

    override suspend fun getAll(): List<HabitCategory> =
        habitCategoryDao.getAll().map { it.toDomain() }

    override suspend fun getById(id: Long): HabitCategory? =
        habitCategoryDao.getById(id)?.toDomain()

    override suspend fun insert(category: HabitCategory): Long =
        habitCategoryDao.insert(category.toEntity())

    override suspend fun insertAll(categories: List<HabitCategory>): List<Long> =
        habitCategoryDao.insertAll(categories.map { it.toEntity() })

    override suspend fun update(category: HabitCategory) =
        habitCategoryDao.update(category.toEntity())

    override suspend fun delete(category: HabitCategory) =
        habitCategoryDao.delete(category.toEntity())

    override suspend fun deleteById(id: Long) = habitCategoryDao.deleteById(id)
}
