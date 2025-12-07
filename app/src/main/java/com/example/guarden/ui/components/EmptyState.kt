package com.example.guarden.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // חשוב!
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.* // הייבוא של הלוטי
import com.example.guarden.R
import com.example.guarden.ui.theme.GreenPrimary

@Composable
fun EmptyState() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.houseplant)) // וודא שהשם תואם לקובץ שלך!
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever // לופ אינסופי
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // הצגת האנימציה
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(200.dp) // גודל האנימציה
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your garden is empty.\nAdd some life to it!",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = GreenPrimary.copy(alpha = 0.8f)
        )
    }
}