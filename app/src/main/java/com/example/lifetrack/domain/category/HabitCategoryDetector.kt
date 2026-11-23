package com.example.lifetrack.domain.category

import com.example.lifetrack.domain.model.HabitCategory

private val nonWordCharacters = Regex("[^\\p{L}\\p{Nd}]+")
private val repeatedSpaces = Regex("\\s+")

private val categoryKeywords: Map<String, List<String>> = mapOf(
    "здоровье" to listOf(
        "здоров", "вод", "пить", "сон", "спать", "витамин", "таблетк", "лекарств",
        "врач", "анализ", "осанк", "пита", "калори", "овощ", "фрукт", "сахар", "зуб",
        "кож", "растяжк", "душ", "массаж"
    ),
    "спорт" to listOf(
        "бег", "пробеж", "трениров", "зал", "спорт", "йог", "плава", "велик", "отжим",
        "присед", "турник", "фитнес", "кардио", "ходьб", "шаг", "лыж", "каток", "футбол",
        "баскетбол", "мяч"
    ),
    "обучение" to listOf(
        "чит", "чтен", "книг", "учеб", "учить", "язык", "английск", "курс", "лекци",
        "конспект", "задач", "прогр", "экзамен", "школ", "универ", "урок", "дуолинго"
    ),
    "работа" to listOf(
        "работ", "офис", "проект", "митинг", "встреч", "отчет", "дедлайн", "письм",
        "почт", "планер", "резюме", "портфолио", "созвон", "клиент", "собеседован"
    ),
    "дом" to listOf(
        "уборк", "пылесос", "посуд", "стирк", "готов", "ремонт", "полив", "цвет",
        "растен", "порядок", "разбор", "хлам", "мыть", "кухн", "глажк", "магазин"
    ),
    "отдых" to listOf(
        "отдых", "медитац", "дыхан", "дневник", "благодар", "музык", "гитар", "рисов",
        "рису", "хобби", "фильм", "игр", "сериал", "прогулк", "вязан", "фото"
    )
)

fun normalizeHabitText(value: String): String = value
    .lowercase()
    .replace('ё', 'е')
    .replace(nonWordCharacters, " ")
    .trim()
    .replace(repeatedSpaces, " ")

fun detectHabitCategory(name: String, categories: List<HabitCategory>): HabitCategory? {
    val words = normalizeHabitText(name).split(' ').filter { it.isNotEmpty() }
    if (words.isEmpty()) return null

    val matched = categories.filter { category ->
        val keywords = categoryKeywords[normalizeHabitText(category.name)].orEmpty()
        keywords.any { keyword -> words.any { word -> word.startsWith(keyword) } }
    }

    return matched.singleOrNull()
}
