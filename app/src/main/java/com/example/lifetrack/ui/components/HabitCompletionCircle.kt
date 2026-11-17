package com.example.lifetrack.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.lifetrack.R
import com.example.lifetrack.domain.model.CompletionStatus

@Composable
fun HabitCompletionCircle(
    status: CompletionStatus?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val fillColor = when (status) {
        CompletionStatus.DONE -> MaterialTheme.colorScheme.primary
        CompletionStatus.SKIPPED -> MaterialTheme.colorScheme.errorContainer
        null -> Color.Transparent
    }
    val border = if (status == null) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
    } else {
        null
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(fillColor)
                .then(if (border != null) Modifier.border(border, CircleShape) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            when (status) {
                CompletionStatus.DONE -> Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )

                CompletionStatus.SKIPPED -> Icon(
                    painter = painterResource(id = R.drawable.ic_minus),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(22.dp)
                )

                null -> Unit
            }
        }
    }
}
