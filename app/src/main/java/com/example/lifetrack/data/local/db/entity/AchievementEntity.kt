package com.example.lifetrack.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.lifetrack.domain.model.AchievementConditionType

@Entity(
    tableName = "achievements",
    indices = [Index(value = ["code"], unique = true)]
)
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "code")
    val code: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "icon")
    val icon: String,
    @ColumnInfo(name = "condition_type")
    val conditionType: AchievementConditionType,
    @ColumnInfo(name = "target_value")
    val targetValue: Int,
    @ColumnInfo(name = "is_unlocked")
    val isUnlocked: Boolean = false,
    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Long? = null
)
