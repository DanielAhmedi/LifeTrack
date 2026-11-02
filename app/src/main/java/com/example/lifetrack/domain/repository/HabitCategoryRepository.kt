package com.example.lifetrack.domain.repository

import com.example.lifetrack.domain.model.HabitCategory
import kotlinx.coroutines.flow.Flow

interface HabitCategoryRepository {

    fun observeAll(): Flow<List<HabitCategory>>

    suspend fun getAll(): List<HabitCategory>

    suspend fun getById(id: Long): HabitCategory?

    suspend fun insert(category: HabitCategory): Long

    suspend fun insertAll(categories: List<HabitCategory>): List<Long>

    suspend fun update(category: HabitCategory)

    suspend fun delete(category: HabitCategory)

    suspend fun deleteById(id: Long)
}
