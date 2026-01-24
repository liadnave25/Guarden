package com.example.guarden.ui.components

import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.guarden.ui.theme.GreenPrimary
import com.example.guarden.ui.theme.WhiteCard
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun NativeAdComponent() {
    val adUnitId = "ca-app-pub-3940256099942544/2247696110" // Test ID

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhiteCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        AndroidView(
            factory = { context ->
                val adView = NativeAdView(context)

                val rowLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(32, 32, 32, 32) // המרה גסה מ-12dp (בערך)
                    gravity = Gravity.CENTER_VERTICAL
                }


                val iconSizePx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 60f, context.resources.displayMetrics
                ).toInt()

                val iconView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    background = GradientDrawable().apply {
                        cornerRadius = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics
                        )
                        setColor(AndroidColor.LTGRAY)
                    }
                    clipToOutline = true
                }

                val textColumn = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        marginStart = 48
                        marginEnd = 16
                    }
                }

                val headlineView = TextView(context).apply {
                    textSize = 16f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(AndroidColor.parseColor("#263238")) // TextDark
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                }

                val subRow = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 8 }
                }

                val adBadge = TextView(context).apply {
                    text = "Ad"
                    textSize = 10f
                    setTextColor(AndroidColor.BLACK)
                    setBackgroundColor(AndroidColor.parseColor("#FFCC00"))
                    setPadding(8, 2, 8, 2)
                }

                val bodyView = TextView(context).apply {
                    textSize = 12f
                    setTextColor(AndroidColor.parseColor("#78909C"))
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { marginStart = 16 }
                }

                subRow.addView(adBadge)
                subRow.addView(bodyView)
                textColumn.addView(headlineView)
                textColumn.addView(subRow)


                val ctaButton = Button(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        100
                    )
                    textSize = 12f
                    isAllCaps = false
                    background = GradientDrawable().apply {
                        setColor(GreenPrimary.toArgb())
                        cornerRadius = 50f
                    }
                    setTextColor(AndroidColor.WHITE)
                    setPadding(32, 0, 32, 0)
                }

                rowLayout.addView(iconView)
                rowLayout.addView(textColumn)
                rowLayout.addView(ctaButton)

                adView.addView(rowLayout)

                adView.iconView = iconView
                adView.headlineView = headlineView
                adView.bodyView = bodyView
                adView.callToActionView = ctaButton

                val adLoader = AdLoader.Builder(context, adUnitId)
                    .forNativeAd { nativeAd ->
                        headlineView.text = nativeAd.headline
                        bodyView.text = nativeAd.body
                        ctaButton.text = nativeAd.callToAction

                        if (nativeAd.icon != null) {
                            iconView.setImageDrawable(nativeAd.icon?.drawable)
                            iconView.visibility = android.view.View.VISIBLE
                        } else {
                            iconView.visibility = android.view.View.GONE
                        }

                        adView.setNativeAd(nativeAd)
                    }
                    .build()

                adLoader.loadAd(AdRequest.Builder().build())

                adView
            }
        )
    }
}