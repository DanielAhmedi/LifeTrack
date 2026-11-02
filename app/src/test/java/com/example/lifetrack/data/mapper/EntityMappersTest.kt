package com.example.lifetrack.data.mapper

import com.example.lifetrack.data.local.db.entity.AchievementEntity
import com.example.lifetrack.data.local.db.entity.HabitCategoryEntity
import com.example.lifetrack.data.local.db.entity.HabitCompletionEntity
import com.example.lifetrack.data.local.db.entity.HabitEntity
import com.example.lifetrack.data.local.db.entity.ReminderEntity
import com.example.lifetrack.domain.model.Achievement
import com.example.lifetrack.domain.model.AchievementConditionType
import com.example.lifetrack.domain.model.CompletionStatus
import com.example.lifetrack.domain.model.FrequencyType
import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCategory
import com.example.lifetrack.domain.model.HabitCompletion
import com.example.lifetrack.domain.model.Reminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EntityMappersTest {

    @Test
    fun habitMapsToEntityAndBack() {
        val habit = Habit(
            id = 7L,
            name = "Пить воду",
            description = "Два литра в день",
            categoryId = 1L,
            icon = "water",
            color = 0xFF2196C9.toInt(),
            createdAt = 1_793_000_000_000L,
            frequencyType = FrequencyType.WEEKLY,
            weekDaysMask = 0b0010101,
            intervalDays = null,
            isActive = false
        )

        val entity = habit.toEntity()

        assertEquals(
            HabitEntity(
                id = 7L,
                name = "Пить воду",
                description = "Два литра в день",
                categoryId = 1L,
                icon = "water",
                color = 0xFF2196C9.toInt(),
                createdAt = 1_793_000_000_000L,
                frequencyType = FrequencyType.WEEKLY,
                weekDaysMask = 0b0010101,
                intervalDays = null,
                isActive = false
            ),
            entity
        )
        assertEquals(habit, entity.toDomain())
    }

    @Test
    fun habitKeepsNullableFields() {
        val entity = HabitEntity(
            id = 0L,
            name = "Медитация",
            description = null,
            categoryId = null,
            icon = "meditation",
            color = 0xFF8A5CD6.toInt(),
            createdAt = 1L,
            frequencyType = FrequencyType.INTERVAL,
            weekDaysMask = null,
            intervalDays = 3,
            isActive = true
        )

        val habit = entity.toDomain()

        assertNull(habit.description)
        assertNull(habit.categoryId)
        assertNull(habit.weekDaysMask)
        assertEquals(3, habit.intervalDays)
        assertEquals(entity, habit.toEntity())
    }

    @Test
    fun categoryMapsToEntityAndBack() {
        val category = HabitCategory(
            id = 2L,
            name = "Спорт",
            icon = "sport",
            color = 0xFF12A594.toInt()
        )

        val entity = category.toEntity()

        assertEquals(
            HabitCategoryEntity(
                id = 2L,
                name = "Спорт",
                icon = "sport",
                color = 0xFF12A594.toInt()
            ),
            entity
        )
        assertEquals(category, entity.toDomain())
    }

    @Test
    fun completionMapsToEntityAndBack() {
        val completion = HabitCompletion(
            id = 11L,
            habitId = 7L,
            date = 20_393L,
            status = CompletionStatus.SKIPPED
        )

        val entity = completion.toEntity()

        assertEquals(
            HabitCompletionEntity(
                id = 11L,
                habitId = 7L,
                date = 20_393L,
                status = CompletionStatus.SKIPPED
            ),
            entity
        )
        assertEquals(completion, entity.toDomain())
    }

    @Test
    fun achievementMapsToEntityAndBack() {
        val achievement = Achievement(
            id = 6L,
            code = "STREAK_7",
            name = "Неделя силы",
            description = "Достигните серии 7 дней подряд",
            icon = "leaf",
            conditionType = AchievementConditionType.CURRENT_STREAK_DAYS,
            targetValue = 7,
            isUnlocked = true,
            unlockedAt = 1_793_000_500_000L
        )

        val entity = achievement.toEntity()

        assertEquals(
            AchievementEntity(
                id = 6L,
                code = "STREAK_7",
                name = "Неделя силы",
                description = "Достигните серии 7 дней подряд",
                icon = "leaf",
                conditionType = AchievementConditionType.CURRENT_STREAK_DAYS,
                targetValue = 7,
                isUnlocked = true,
                unlockedAt = 1_793_000_500_000L
            ),
            entity
        )
        assertEquals(achievement, entity.toDomain())
    }

    @Test
    fun lockedAchievementHasNoUnlockTime() {
        val achievement = Achievement(
            code = "COMPLETIONS_10",
            name = "Разгон",
            description = "Выполните привычки 10 раз",
            icon = "run",
            conditionType = AchievementConditionType.TOTAL_COMPLETIONS,
            targetValue = 10
        )

        assertNull(achievement.unlockedAt)
        assertNull(achievement.toEntity().toDomain().unlockedAt)
    }

    @Test
    fun reminderMapsToEntityAndBack() {
        val reminder = Reminder(
            id = 3L,
            habitId = 7L,
            timeMinutes = 480,
            isActive = false
        )

        val entity = reminder.toEntity()

        assertEquals(
            ReminderEntity(
                id = 3L,
                habitId = 7L,
                timeMinutes = 480,
                isActive = false
            ),
            entity
        )
        assertEquals(reminder, entity.toDomain())
    }
}
