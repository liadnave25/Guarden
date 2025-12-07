package com.example.guarden.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun PaywallDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
        },
        title = {
            Text(text = "Limit Reached")
        },
        text = {
            Text(text = "You have reached the limit of 5 plants.\nUpgrade to Premium to grow a full jungle!")
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Upgrade (Free)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No Thanks")
            }
        }
    )
}