package com.example.guarden.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ManualRatingDialog(
    onDismiss: () -> Unit,
    onRated: (Int, String?) -> Unit,
    onNeverAskAgain: () -> Unit
) {
    var rating by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("") }
    val shouldShowTextField = rating in 1..3

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Enjoying Guarden? 🌱", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    (1..5).forEach { index ->
                        IconButton(onClick = { rating = index }, modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = if (index <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (index <= rating) Color(0xFFFFC107) else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                if (shouldShowTextField) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("What can we improve?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onRated(rating, if (shouldShowTextField) feedback else null) },
                    enabled = rating > 0,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Submit", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Maybe Later", color = Color(0xFF2E6B34))
                }
                TextButton(onClick = onNeverAskAgain, modifier = Modifier.fillMaxWidth()) {
                    Text("Don't show again", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }
        }
    }
}