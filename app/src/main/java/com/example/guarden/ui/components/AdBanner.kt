package com.example.guarden.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.guarden.ui.theme.AdBannerGray

@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    // זהו Placeholder. בעתיד נחליף את זה ב-AdMob אמיתי.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp) // גובה סטנדרטי של באנר
            .background(AdBannerGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Advertisement Area",
            color = Color.White,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall
        )
    }
}