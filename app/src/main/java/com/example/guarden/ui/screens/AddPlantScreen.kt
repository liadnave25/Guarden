package com.example.guarden.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.guarden.ui.components.AdBanner
import com.example.guarden.ui.theme.*
import com.example.guarden.viewmodel.PlantViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    navController: NavController,
    viewModel: PlantViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // State Variables
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var waterFreqFloat by remember { mutableFloatStateOf(7f) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // משתנה להצגת הבוטום שיט לבחירת מקור תמונה
    var showImageSourceOption by remember { mutableStateOf(false) }
    // משתנה זמני לשמירת ה-URI של המצלמה לפני הצילום
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val commonPlaces = listOf("Living Room", "Balcony", "Kitchen", "Bedroom", "Office", "Garden")

    // --- 1. גלריה: משגר בחירת תמונה ---
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) selectedImageUri = uri
            showImageSourceOption = false
        }
    )

    // --- 2. מצלמה: משגר צילום תמונה ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                selectedImageUri = tempCameraUri
            }
            showImageSourceOption = false
        }
    )

    // --- 3. הרשאות: משגר בקשת הרשאה למצלמה ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // אם אושר -> צור קובץ ופתח מצלמה
            val uri = context.createImageFile()
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val isFormValid = name.isNotEmpty() && type.isNotEmpty()

    fun onSave() {
        if (isFormValid) {
            viewModel.addPlant(
                name = name,
                type = type,
                waterFreq = waterFreqFloat.toInt(),
                imageUri = selectedImageUri?.toString()
            )
            navController.popBackStack()
        }
    }

    // --- UI של הבוטום שיט (בחירה בין מצלמה לגלריה) ---
    if (showImageSourceOption) {
        ModalBottomSheet(
            onDismissRequest = { showImageSourceOption = false },
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(16.dp).padding(bottom = 24.dp)) {
                Text(
                    "Add Photo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // אפשרות 1: צילום תמונה
                ListItem(
                    headlineContent = { Text("Take Photo") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, null, tint = GreenPrimary) },
                    modifier = Modifier.clickable {
                        // בדיקת הרשאה לפני פתיחת מצלמה
                        val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                            val uri = context.createImageFile()
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )

                // אפשרות 2: בחירה מהגלריה
                ListItem(
                    headlineContent = { Text("Choose from Gallery") },
                    leadingContent = { Icon(Icons.Default.Image, null, tint = GreenPrimary) },
                    modifier = Modifier.clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
        }
    }

    Scaffold(
        containerColor = GreenBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New Plant", fontWeight = FontWeight.Bold, color = GreenPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = GreenPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = GreenBackground)
            )
        },
        bottomBar = { AdBanner() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // כפתור התמונה פותח עכשיו את ה-BottomSheet
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .dashedBorder(2.dp, GreenPrimary, 24.dp)
                    .clickable {
                        // במקום לפתוח גלריה ישר, נפתח את התפריט
                        showImageSourceOption = true
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Selected Plant",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Add Photo",
                            tint = GreenPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Add Photo", color = GreenPrimary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ... שאר השדות נשארים ללא שינוי ...

            // 2. שם הצמח
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant Name") },
                leadingIcon = { Icon(Icons.Default.Label, null, tint = GreenPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    focusedLabelColor = GreenPrimary,
                    cursorColor = GreenPrimary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. מיקום + Chips
            OutlinedTextField(
                value = type,
                onValueChange = { type = it },
                label = { Text("Place (e.g. Kitchen)") },
                leadingIcon = { Icon(Icons.Default.Home, null, tint = GreenPrimary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    focusedLabelColor = GreenPrimary,
                    cursorColor = GreenPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                commonPlaces.forEach { place ->
                    FilterChip(
                        selected = type == place,
                        onClick = { type = place },
                        label = { Text(place) },
                        leadingIcon = if (type == place) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GreenSoft,
                            selectedLabelColor = GreenPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. סליידר תדירות השקיה
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, null, tint = Color(0xFF2196F3))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Water every ${waterFreqFloat.toInt()} days",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = waterFreqFloat,
                        onValueChange = { waterFreqFloat = it },
                        valueRange = 1f..30f,
                        steps = 28,
                        colors = SliderDefaults.colors(
                            thumbColor = GreenPrimary,
                            activeTrackColor = GreenPrimary,
                            inactiveTrackColor = GreenSoft
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 Day", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("30 Days", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 5. כפתור שמירה
            Button(
                onClick = { onSave() },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Save Plant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// פונקציית העזר ליצירת URI זמני למצלמה
fun Context.createImageFile(): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val image = File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )
    return FileProvider.getUriForFile(
        this,
        "${packageName}.fileprovider", // וודא שזה תואם למה שכתבת ב-Manifest
        image
    )
}

// Helper function for dashed border
fun Modifier.dashedBorder(width: Dp, color: Color, radius: Dp) = drawBehind {
    drawRoundRect(
        color = color,
        style = Stroke(
            width = width.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        ),
        cornerRadius = CornerRadius(radius.toPx())
    )
}