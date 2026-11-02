package com.example.lifetrack.di

import android.content.Context
import com.example.lifetrack.data.local.db.LifeTrackDatabase
import com.example.lifetrack.data.local.datastore.SettingsDataStore
import com.example.lifetrack.data.repository.AchievementRepositoryImpl
import com.example.lifetrack.data.repository.HabitCategoryRepositoryImpl
import com.example.lifetrack.data.repository.HabitCompletionRepositoryImpl
import com.example.lifetrack.data.repository.HabitRepositoryImpl
import com.example.lifetrack.data.repository.ReminderRepositoryImpl
import com.example.lifetrack.data.repository.SettingsRepositoryImpl
import com.example.lifetrack.domain.repository.AchievementRepository
import com.example.lifetrack.domain.repository.HabitCategoryRepository
import com.example.lifetrack.domain.repository.HabitCompletionRepository
import com.example.lifetrack.domain.repository.HabitRepository
import com.example.lifetrack.domain.repository.ReminderRepository
import com.example.lifetrack.domain.repository.SettingsRepository

class AppContainer(context: Context) {

    private val database: LifeTrackDatabase = LifeTrackDatabase.create(context)

    private val settingsDataStore = SettingsDataStore.create(context)

    val habitRepository: HabitRepository = HabitRepositoryImpl(database.habitDao())

    val habitCategoryRepository: HabitCategoryRepository =
        HabitCategoryRepositoryImpl(database.habitCategoryDao())

    val habitCompletionRepository: HabitCompletionRepository =
        HabitCompletionRepositoryImpl(database.habitCompletionDao())

    val achievementRepository: AchievementRepository =
        AchievementRepositoryImpl(database.achievementDao())

    val reminderRepository: ReminderRepository = ReminderRepositoryImpl(database.reminderDao())

    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(settingsDataStore)
}
