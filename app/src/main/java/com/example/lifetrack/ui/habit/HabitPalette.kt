package com.example.lifetrack.ui.habit

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val HabitPaletteColors: List<Int> = listOf(
    0xFF3F6FE0,
    0xFF2196C9,
    0xFF12A594,
    0xFF3D9A50,
    0xFF7CA82B,
    0xFFD6A419,
    0xFFE07A2E,
    0xFFD64545,
    0xFFD4518C,
    0xFF8A5CD6,
    0xFF5C6BC0,
    0xFF8D6E63
).map { it.toInt() }

val DefaultHabitColor: Int = HabitPaletteColors.first()

fun habitColor(argb: Int): Color = Color(argb)

fun habitContainerColor(argb: Int): Color = Color(argb).copy(alpha = 0.12f)

fun habitContentColor(argb: Int): Color =
    if (Color(argb).luminance() > 0.5f) Color(0xFF1A1C1B) else Color.White
