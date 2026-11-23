package com.example.lifetrack.domain.category

import com.example.lifetrack.domain.model.HabitCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HabitCategoryDetectorTest {

    private val health = HabitCategory(id = 1L, name = "Здоровье", icon = "heart", color = 0)
    private val sport = HabitCategory(id = 2L, name = "Спорт", icon = "sport", color = 1)
    private val study = HabitCategory(id = 3L, name = "Обучение", icon = "book", color = 2)
    private val work = HabitCategory(id = 4L, name = "Работа", icon = "work", color = 3)
    private val home = HabitCategory(id = 5L, name = "Дом", icon = "home", color = 4)
    private val rest = HabitCategory(id = 6L, name = "Отдых", icon = "meditation", color = 5)

    private val categories = listOf(health, sport, study, work, home, rest)

    @Test
    fun detectsHealthByKeyword() {
        assertEquals(health, detectHabitCategory("Пить воду каждый день", categories))
    }

    @Test
    fun detectsSportByKeyword() {
        assertEquals(sport, detectHabitCategory("Утренняя пробежка", categories))
    }

    @Test
    fun detectsStudyIgnoringCaseAndPunctuation() {
        assertEquals(study, detectHabitCategory("ЧИТАТЬ книгу 30 минут!", categories))
    }

    @Test
    fun detectsStudyWithYoLetter() {
        assertEquals(study, detectHabitCategory("Учёба по английскому", categories))
    }

    @Test
    fun detectsWorkByWordPrefix() {
        assertEquals(work, detectHabitCategory("Созвон с клиентом", categories))
    }

    @Test
    fun detectsHomeByKeyword() {
        assertEquals(home, detectHabitCategory("Помыть посуду вечером", categories))
    }

    @Test
    fun detectsRestByKeyword() {
        assertEquals(rest, detectHabitCategory("Медитация вечером", categories))
    }

    @Test
    fun keywordInsideLongerWordIsNotMatched() {
        assertNull(detectHabitCategory("Персонал", categories))
    }

    @Test
    fun ambiguousNameIsNotCategorized() {
        assertNull(detectHabitCategory("Читать и бегать", categories))
    }

    @Test
    fun unknownNameIsNotCategorized() {
        assertNull(detectHabitCategory("Смотреть в окно", categories))
    }

    @Test
    fun blankNameIsNotCategorized() {
        assertNull(detectHabitCategory("", categories))
        assertNull(detectHabitCategory("   ", categories))
        assertNull(detectHabitCategory("...", categories))
    }

    @Test
    fun categoriesWithoutDictionaryEntryAreIgnored() {
        val finance = HabitCategory(id = 7L, name = "Финансы", icon = "star", color = 6)

        assertNull(detectHabitCategory("Копить деньги", listOf(finance)))
        assertEquals(health, detectHabitCategory("Пить воду", categories + finance))
    }

    @Test
    fun normalizationLowercasesAndStripsPunctuation() {
        assertEquals("пить воду", normalizeHabitText("  Пить, ВОДУ!  "))
        assertEquals("учеба", normalizeHabitText("Учёба"))
    }
}
