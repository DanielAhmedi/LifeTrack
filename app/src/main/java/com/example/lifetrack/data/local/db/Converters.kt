package com.example.lifetrack.data.local.db

import androidx.room.TypeConverter
import com.example.lifetrack.domain.model.AchievementConditionType
import com.example.lifetrack.domain.model.CompletionStatus
import com.example.lifetrack.domain.model.FrequencyType

class Converters {

    @TypeConverter
    fun fromFrequencyType(value: FrequencyType): String = value.name

    @TypeConverter
    fun toFrequencyType(value: String): FrequencyType = FrequencyType.valueOf(value)

    @TypeConverter
    fun fromCompletionStatus(value: CompletionStatus): String = value.name

    @TypeConverter
    fun toCompletionStatus(value: String): CompletionStatus = CompletionStatus.valueOf(value)

    @TypeConverter
    fun fromAchievementConditionType(value: AchievementConditionType): String = value.name

    @TypeConverter
    fun toAchievementConditionType(value: String): AchievementConditionType =
        AchievementConditionType.valueOf(value)
}
