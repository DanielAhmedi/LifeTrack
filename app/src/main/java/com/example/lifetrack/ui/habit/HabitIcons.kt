package com.example.lifetrack.ui.habit

import androidx.annotation.DrawableRes
import com.example.lifetrack.R

object HabitIcons {

    const val DEFAULT_KEY = "star"

    val keys: List<String> = listOf(
        "star",
        "water",
        "sport",
        "run",
        "book",
        "study",
        "sleep",
        "food",
        "apple",
        "coffee",
        "work",
        "home",
        "clean",
        "meditation",
        "walk",
        "music",
        "art",
        "code",
        "phone_off",
        "savings",
        "heart",
        "sun",
        "moon",
        "leaf"
    )

    @DrawableRes
    fun drawableRes(key: String): Int = when (key) {
        "water" -> R.drawable.ic_habit_water
        "sport" -> R.drawable.ic_habit_sport
        "run" -> R.drawable.ic_habit_run
        "book" -> R.drawable.ic_habit_book
        "study" -> R.drawable.ic_habit_study
        "sleep" -> R.drawable.ic_habit_sleep
        "food" -> R.drawable.ic_habit_food
        "apple" -> R.drawable.ic_habit_apple
        "coffee" -> R.drawable.ic_habit_coffee
        "work" -> R.drawable.ic_habit_work
        "home" -> R.drawable.ic_habit_home
        "clean" -> R.drawable.ic_habit_clean
        "meditation" -> R.drawable.ic_habit_meditation
        "walk" -> R.drawable.ic_habit_walk
        "music" -> R.drawable.ic_habit_music
        "art" -> R.drawable.ic_habit_art
        "code" -> R.drawable.ic_habit_code
        "phone_off" -> R.drawable.ic_habit_phone_off
        "savings" -> R.drawable.ic_habit_savings
        "heart" -> R.drawable.ic_habit_heart
        "sun" -> R.drawable.ic_habit_sun
        "moon" -> R.drawable.ic_habit_moon
        "leaf" -> R.drawable.ic_habit_leaf
        else -> R.drawable.ic_habit_star
    }
}
