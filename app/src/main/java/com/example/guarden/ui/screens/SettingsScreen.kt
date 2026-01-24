package com.example.guarden.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.example.guarden.R
import com.example.guarden.ui.theme.*
import com.example.guarden.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.userPreferences.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showBuyPlantsDialog by remember { mutableStateOf(false) }
    var showBuyPremiumDialog by remember { mutableStateOf(false) }
    var showCancelPremiumDialog by remember { mutableStateOf(false) }

    var isNotificationPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else true

                isNotificationPermissionGranted = currentStatus
                viewModel.toggleNotifications(currentStatus)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    // --- Dialogs ---
    if (showBuyPlantsDialog) {
        AlertDialog(
            onDismissRequest = { showBuyPlantsDialog = false },
            icon = { Icon(Icons.Default.Grass, null, tint = GreenPrimary) },
            title = { Text("Expand Your Garden") },
            text = { Text("Pay $5 one-time to add 5 more plants to your collection.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.buyPlantPack()
                    showBuyPlantsDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) { Text("Buy Now") }
            },
            dismissButton = { TextButton(onClick = { showBuyPlantsDialog = false }) { Text("Cancel") } }
        )
    }

    if (showBuyPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showBuyPremiumDialog = false },
            icon = { Icon(Icons.Default.Diamond, null, tint = GreenPrimary) },
            title = { Text("Go Premium") },
            text = { Text("Unlock the AI Assistant and remove all ads for $10/month.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.buyPremiumSubscription()
                    showBuyPremiumDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)) { Text("Subscribe") }
            },
            dismissButton = { TextButton(onClick = { showBuyPremiumDialog = false }) { Text("Maybe Later") } }
        )
    }

    if (showCancelPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showCancelPremiumDialog = false },
            title = { Text("Cancel Subscription?") },
            text = { Text("You will lose access to the AI Assistant and ads will return.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.downgradeToFree()
                    showCancelPremiumDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("Confirm Cancel") }
            },
            dismissButton = { TextButton(onClick = { showCancelPremiumDialog = false }) { Text("Keep It") } }
        )
    }

    Scaffold(
        containerColor = GreenBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = GreenDark) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = GreenDark)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = GreenBackground)
            )
        },
    ) { paddingValues ->
        if (prefs == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MEMBERSHIP & PLANS",
                style = MaterialTheme.typography.labelLarge,
                color = GreenPrimary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
            )

            PlanCardModern(
                title = "Free Plan",
                subtitle = "Limit: 7 Plants",
                isActive = !prefs!!.isPremium,
                icon = Icons.Default.Spa,
                onClick = { if (prefs!!.isPremium) showCancelPremiumDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PlanCardModern(
                title = "Add Capacity",
                subtitle = "One-time: +5 Plants ($5)",
                isActive = false,
                icon = Icons.Default.Grass,
                badge = "Current Limit: ${prefs!!.plantLimit}",
                onClick = { showBuyPlantsDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PlanCardModern(
                title = "Guarden Premium",
                subtitle = "AI Agent + No Ads ($10/mo)",
                isActive = prefs!!.isPremium,
                icon = Icons.Default.Diamond,
                isPremiumStyle = true,
                onClick = { if (!prefs!!.isPremium) showBuyPremiumDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "PREFERENCES",
                style = MaterialTheme.typography.labelLarge,
                color = GreenPrimary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isNotificationPermissionGranted) GreenSoft.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isNotificationPermissionGranted) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = if (isNotificationPermissionGranted) GreenDark else Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Notifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (isNotificationPermissionGranted) "Status: Active" else "Status: Blocked",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isNotificationPermissionGranted) GreenPrimary else ErrorRed
                            )
                        }
                    }
                    TextButton(onClick = { openAppSettings() }) {
                        Text("Manage", fontWeight = FontWeight.Bold, color = GreenPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "LEGAL",
                style = MaterialTheme.typography.labelLarge,
                color = GreenPrimary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    LegalRowItem(
                        title = "Privacy Policy",
                        icon = Icons.Default.Lock,
                        onClick = { openUrl("https://sites.google.com/view/guarden-privacy-policy") }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = Color.LightGray.copy(alpha = 0.4f)
                    )
                    LegalRowItem(
                        title = "Terms & Conditions",
                        icon = Icons.Default.Description,
                        onClick = { openUrl("https://sites.google.com/view/guarden-termsconditions") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Animation
            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.walking_petos))
                val progress by animateLottieCompositionAsState(composition = composition, iterations = LottieConstants.IterateForever)
                LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.fillMaxSize())
            }

            Text(text = "Version 1.0.0", style = MaterialTheme.typography.labelSmall, color = Color.Gray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun LegalRowItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = TextDark)
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun PlanCardModern(
    title: String,
    subtitle: String,
    isActive: Boolean,
    icon: ImageVector,
    isPremiumStyle: Boolean = false,
    badge: String? = null,
    onClick: () -> Unit
) {
    val backgroundColor = if (isActive) GreenSoft.copy(alpha = 0.15f) else Color.White
    val borderColor = if (isActive) GreenPrimary else Color.Transparent
    val elevation = if (isActive) 0.dp else 2.dp

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = if (isActive) BorderStroke(2.dp, borderColor) else null
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isActive || isPremiumStyle) GreenPrimary else Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = if (isActive || isPremiumStyle) Color.White else Color.Gray, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (isActive) GreenDark else TextDark)
                    if (isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Check, null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextGray)
                if (badge != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(color = GreenSoft.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp)) {
                        Text(text = badge, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = GreenDark)
                    }
                }
            }
            if (isPremiumStyle && !isActive) {
                Icon(Icons.Default.Lock, null, tint = Color.Gray.copy(alpha = 0.4f))
            }
        }
    }
}