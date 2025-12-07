package com.example.guarden.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.guarden.model.Plant
import com.example.guarden.ui.theme.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

@Composable
fun PlantItemCard(
    plant: Plant,
    onDeleteClick: () -> Unit,
    onWaterClick: () -> Unit
) {
    val currentTime = System.currentTimeMillis()
    val timeDiff = currentTime - plant.lastWateringDate
    val daysPassed = TimeUnit.MILLISECONDS.toDays(timeDiff).toInt()

    val daysUntilWatering = plant.wateringFrequency - daysPassed

    val isThirsty = daysUntilWatering <= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(12.dp),
                color = GreenBackground
            ) {
                if (plant.imageUri != null) {
                    AsyncImage(
                        model = plant.imageUri,
                        contentDescription = "Plant",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocalFlorist, null, tint = GreenPrimary, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                if (isThirsty) {
                    Text(
                        text = "Water Me Today!",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Next in $daysUntilWatering days",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextGray
                    )
                }
            }

            IconButton(
                onClick = onWaterClick,
                modifier = Modifier
                    .background(
                        if (isThirsty) Color(0xFFE3F2FD) else Color.Transparent,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Water",
                    tint = if (isThirsty) Color(0xFF2196F3) else Color.LightGray
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}