package com.example.guarden.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.guarden.R
import com.example.guarden.model.Plant
import com.example.guarden.ui.components.*
import com.example.guarden.ui.navigation.Screen
import com.example.guarden.ui.theme.GreenBackground
import com.example.guarden.ui.theme.GreenPrimary
import com.example.guarden.viewmodel.PlantViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: PlantViewModel = hiltViewModel()
) {
    val plants by viewModel.plants.collectAsState()
    val showPaywall by viewModel.showPaywall.collectAsState()
    val weatherState by viewModel.weatherState.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    val context = LocalContext.current

    // --- 1. הוספנו משתנה סטייט לניהול הפתיחה של הבועה ---
    var isChatOpen by remember { mutableStateOf(false) }

    var plantToDelete by remember { mutableStateOf<Plant?>(null) }

    if (plantToDelete != null) {
        AlertDialog(
            onDismissRequest = { plantToDelete = null },
            title = { Text(text = "Delete Plant") },
            text = {
                Text("Are you sure you want to delete ${plantToDelete?.name}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        plantToDelete?.let { viewModel.deletePlant(it) }
                        plantToDelete = null
                        Toast.makeText(context, "Plant deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { plantToDelete = null }) {
                    Text("No", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }

    // --- Location Permission ---
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.fetchWeatherByLocation(context)
        } else {
            Toast.makeText(context, "Location permission needed for weather", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Sorting Logic ---
    val sortedPlants = plants.sortedBy { plant ->
        val timeDiff = System.currentTimeMillis() - plant.lastWateringDate
        val daysPassed = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(timeDiff).toInt()
        plant.wateringFrequency - daysPassed
    }

    if (showPaywall) {
        PaywallDialog(
            onDismiss = { viewModel.onPaywallDismiss() },
            onConfirm = { viewModel.upgradeToPremium() }
        )
    }

    Scaffold(
        containerColor = GreenBackground,

        // --- TOP BAR ---
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "My Guarden",
                        style = MaterialTheme.typography.titleLarge,
                        color = GreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Surface(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clickable {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            },
                        color = Color.White,
                        shape = RoundedCornerShape(50),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = when {
                                weatherState.condition.contains("Rain", ignoreCase = true) -> Icons.Default.WaterDrop
                                weatherState.condition.contains("Cloud", ignoreCase = true) -> Icons.Default.Cloud
                                else -> Icons.Default.WbSunny
                            }

                            val iconColor = if (weatherState.condition.contains("Rain")) Color(0xFF2196F3) else Color(0xFFFFB300)

                            Icon(
                                imageVector = icon,
                                contentDescription = "Weather",
                                tint = iconColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = weatherState.temp,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.DarkGray
                            )
                        }
                    }
                },
                actions = {
                    // --- Settings Animation Logic ---
                    var isPlaying by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(10000)
                            isPlaying = true
                            delay(2000)
                            isPlaying = false
                        }
                    }

                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.setting_animation))
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        isPlaying = isPlaying,
                        iterations = 1
                    )

                    IconButton(onClick = {
                        navController.navigate(Screen.Settings.route)
                    }) {
                        if (composition != null) {
                            LottieAnimation(
                                composition = composition,
                                progress = { progress },
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = GreenBackground
                )
            )
        },

        bottomBar = {
            if (!isPremium) {
                AdBanner()
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- LIST OR EMPTY STATE ---
            if (plants.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 120.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedPlants) { plant ->
                        PlantItemCard(
                            plant = plant,
                            onDeleteClick = {
                                plantToDelete = plant
                            },
                            onWaterClick = {
                                viewModel.waterPlant(plant)
                                Toast.makeText(context, "Watered ${plant.name}! 🌱", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // --- BOTTOM BUTTONS AREA ---
            val buttonBottomPadding = 24.dp

            // 1. AI Agent Button (Bottom Left)
            val matchLottieGradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF63D066),
                    Color(0xFF30D7A7)
                )
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = buttonBottomPadding)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPremium) matchLottieGradient
                        else SolidColor(Color(0xFF424242))
                    )
                    .clickable {
                        if (isPremium) {
                            // --- 2. תיקון: במקום לנווט, אנחנו משנים את הסטייט ---
                            isChatOpen = true
                        } else {
                            Toast.makeText(context, "Agent is locked! Upgrade to Premium.", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isPremium) {
                    Image(
                        painter = painterResource(id = R.drawable.sparkles),
                        contentDescription = "AI Agent",
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.sparkles),
                            contentDescription = null,
                            alpha = 0.3f,
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.Fit
                        )
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp).offset(x = 10.dp, y = 10.dp)
                        )
                    }
                }
            }

            // 2. Add Plant Button (Bottom Center)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = buttonBottomPadding)
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable { navController.navigate(Screen.AddPlant.route) },
                contentAlignment = Alignment.Center
            ) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.add_button))
                val progress by animateLottieCompositionAsState(
                    composition = composition,
                    iterations = LottieConstants.IterateForever
                )

                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // --- 3. הוספת הבועה עצמה מעל הכל ---
            AnimatedVisibility(
                visible = isChatOpen,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f) // שים לב ל-Z Index הגבוה
            ) {
                // רקע כהה חצי שקוף
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isChatOpen = false // לחיצה בחוץ סוגרת
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // הכרטיס הלבן (הבועה)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(0.85f)
                            .clickable(enabled = false) {}, // לחיצה בפנים לא סוגרת
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ChatScreen()

                            IconButton(
                                onClick = { isChatOpen = false },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Chat",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}