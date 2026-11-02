package com.example.lifetrack.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lifetrack.data.local.db.dao.AchievementDao
import com.example.lifetrack.data.local.db.dao.HabitCategoryDao
import com.example.lifetrack.data.local.db.dao.HabitCompletionDao
import com.example.lifetrack.data.local.db.dao.HabitDao
import com.example.lifetrack.data.local.db.dao.ReminderDao
import com.example.lifetrack.data.local.db.entity.AchievementEntity
import com.example.lifetrack.data.local.db.entity.HabitCategoryEntity
import com.example.lifetrack.data.local.db.entity.HabitCompletionEntity
import com.example.lifetrack.data.local.db.entity.HabitEntity
import com.example.lifetrack.data.local.db.entity.ReminderEntity
import com.example.lifetrack.data.seed.DefaultData

@Database(
    entities = [
        HabitEntity::class,
        HabitCategoryEntity::class,
        HabitCompletionEntity::class,
        AchievementEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LifeTrackDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    abstract fun habitCategoryDao(): HabitCategoryDao

    abstract fun habitCompletionDao(): HabitCompletionDao

    abstract fun achievementDao(): AchievementDao

    abstract fun reminderDao(): ReminderDao

    companion object {
        private const val DATABASE_NAME = "lifetrack.db"

        fun create(context: Context): LifeTrackDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                LifeTrackDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(DefaultData.callback)
                .build()
    }
}
