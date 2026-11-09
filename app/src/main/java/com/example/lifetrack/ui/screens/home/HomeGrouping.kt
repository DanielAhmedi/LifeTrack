package com.example.lifetrack.ui.screens.home

import com.example.lifetrack.domain.model.Habit
import com.example.lifetrack.domain.model.HabitCategory

private val habitOrder = compareBy<Habit>({ !it.isActive }, { it.name.lowercase() })

internal fun groupHabits(habits: List<Habit>, categories: List<HabitCategory>): List<HabitGroup> {
    val sortedCategories = categories.sortedBy { it.name.lowercase() }
    val groups = mutableListOf<HabitGroup>()
    sortedCategories.forEach { category ->
        val items = habits.filter { it.categoryId == category.id }.sortedWith(habitOrder)
        if (items.isNotEmpty()) {
            groups += HabitGroup(category = category, habits = items)
        }
    }
    val knownIds = sortedCategories.map { it.id }.toSet()
    val withoutCategory = habits
        .filter { it.categoryId == null || it.categoryId !in knownIds }
        .sortedWith(habitOrder)
    if (withoutCategory.isNotEmpty()) {
        groups += HabitGroup(category = null, habits = withoutCategory)
    }
    return groups
}
