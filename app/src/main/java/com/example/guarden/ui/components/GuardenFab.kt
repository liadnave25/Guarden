package com.example.guarden.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.guarden.ui.theme.GreenPrimary

@Composable
fun GuardenFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = GreenPrimary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.size(64.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Plant",
            modifier = Modifier.size(32.dp)
        )
    }
}