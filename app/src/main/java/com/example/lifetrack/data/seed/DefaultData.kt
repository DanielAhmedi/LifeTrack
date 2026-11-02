package com.example.lifetrack.data.seed

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lifetrack.domain.model.Achievement
import com.example.lifetrack.domain.model.AchievementConditionType
import com.example.lifetrack.domain.model.HabitCategory

object DefaultData {

    val categories: List<HabitCategory> = listOf(
        HabitCategory(id = 1L, name = "Здоровье", icon = "heart", color = 0xFF3D9A50.toInt()),
        HabitCategory(id = 2L, name = "Спорт", icon = "sport", color = 0xFF12A594.toInt()),
        HabitCategory(id = 3L, name = "Обучение", icon = "book", color = 0xFF3F6FE0.toInt()),
        HabitCategory(id = 4L, name = "Работа", icon = "work", color = 0xFF5C6BC0.toInt()),
        HabitCategory(id = 5L, name = "Дом", icon = "home", color = 0xFFE07A2E.toInt()),
        HabitCategory(id = 6L, name = "Отдых", icon = "meditation", color = 0xFF8A5CD6.toInt())
    )

    val achievements: List<Achievement> = listOf(
        Achievement(
            id = 1L,
            code = "FIRST_HABIT",
            name = "Первый шаг",
            description = "Создайте первую привычку",
            icon = "star",
            conditionType = AchievementConditionType.FIRST_HABIT_CREATED,
            targetValue = 1
        ),
        Achievement(
            id = 2L,
            code = "FIRST_COMPLETION",
            name = "Есть результат",
            description = "Выполните привычку впервые",
            icon = "heart",
            conditionType = AchievementConditionType.TOTAL_COMPLETIONS,
            targetValue = 1
        ),
        Achievement(
            id = 3L,
            code = "COMPLETIONS_10",
            name = "Разгон",
            description = "Выполните привычки 10 раз",
            icon = "run",
            conditionType = AchievementConditionType.TOTAL_COMPLETIONS,
            targetValue = 10
        ),
        Achievement(
            id = 4L,
            code = "COMPLETIONS_50",
            name = "В ритме",
            description = "Выполните привычки 50 раз",
            icon = "walk",
            conditionType = AchievementConditionType.TOTAL_COMPLETIONS,
            targetValue = 50
        ),
        Achievement(
            id = 5L,
            code = "COMPLETIONS_100",
            name = "Сотня",
            description = "Выполните привычки 100 раз",
            icon = "sun",
            conditionType = AchievementConditionType.TOTAL_COMPLETIONS,
            targetValue = 100
        ),
        Achievement(
            id = 6L,
            code = "STREAK_7",
            name = "Неделя силы",
            description = "Достигните серии 7 дней подряд",
            icon = "leaf",
            conditionType = AchievementConditionType.CURRENT_STREAK_DAYS,
            targetValue = 7
        ),
        Achievement(
            id = 7L,
            code = "STREAK_30",
            name = "Месяц дисциплины",
            description = "Достигните серии 30 дней подряд",
            icon = "moon",
            conditionType = AchievementConditionType.CURRENT_STREAK_DAYS,
            targetValue = 30
        ),
        Achievement(
            id = 8L,
            code = "HABITS_5",
            name = "Коллекционер",
            description = "Ведите 5 активных привычек одновременно",
            icon = "savings",
            conditionType = AchievementConditionType.ACTIVE_HABITS_COUNT,
            targetValue = 5
        ),
        Achievement(
            id = 9L,
            code = "CATEGORIES_3",
            name = "Разносторонний",
            description = "Создайте привычки в трёх разных категориях",
            icon = "art",
            conditionType = AchievementConditionType.CATEGORIES_USED,
            targetValue = 3
        ),
        Achievement(
            id = 10L,
            code = "PERFECT_WEEK",
            name = "Идеальная неделя",
            description = "Выполните все запланированные привычки за неделю без пропусков",
            icon = "meditation",
            conditionType = AchievementConditionType.PERFECT_WEEK,
            targetValue = 1
        )
    )

    val callback: RoomDatabase.Callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            categories.forEach { category ->
                db.execSQL(
                    "INSERT OR IGNORE INTO habit_categories (id, name, icon, color) VALUES (?, ?, ?, ?)",
                    arrayOf<Any?>(category.id, category.name, category.icon, category.color)
                )
            }
            achievements.forEach { achievement ->
                db.execSQL(
                    "INSERT OR IGNORE INTO achievements " +
                        "(id, code, name, description, icon, condition_type, target_value, is_unlocked, " +
                        "unlocked_at) VALUES (?, ?, ?, ?, ?, ?, ?, 0, NULL)",
                    arrayOf<Any?>(
                        achievement.id,
                        achievement.code,
                        achievement.name,
                        achievement.description,
                        achievement.icon,
                        achievement.conditionType.name,
                        achievement.targetValue
                    )
                )
            }
        }
    }
}
