package com.example.lifetrack.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.lifetrack.domain.model.FrequencyType

@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = HabitCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["is_active"])
    ]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String? = null,
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,
    @ColumnInfo(name = "icon")
    val icon: String,
    @ColumnInfo(name = "color")
    val color: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "frequency_type")
    val frequencyType: FrequencyType,
    @ColumnInfo(name = "week_days_mask")
    val weekDaysMask: Int? = null,
    @ColumnInfo(name = "interval_days")
    val intervalDays: Int? = null,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)
