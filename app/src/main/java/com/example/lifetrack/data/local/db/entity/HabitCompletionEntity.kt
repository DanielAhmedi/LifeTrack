package com.example.lifetrack.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.lifetrack.domain.model.CompletionStatus

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habit_id", "date"], unique = true),
        Index(value = ["date"])
    ]
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "habit_id")
    val habitId: Long,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "status")
    val status: CompletionStatus
)
