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

    // אנו משתמשים ב-Card של Compose כדי לקבל את המסגרת והצללית בדיוק כמו PlantItemCard
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
                // 1. Root - NativeAdView (חובה)
                val adView = NativeAdView(context)

                // 2. Layout ראשי - שורה אופקית (כמו PlantItemCard)
                val rowLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(32, 32, 32, 32) // המרה גסה מ-12dp (בערך)
                    gravity = Gravity.CENTER_VERTICAL
                }

                // --- חלק שמאלי: תמונה (Icon) ---
                // ב-PlantItemCard זה Surface בגודל 60dp
                val iconSizePx = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 60f, context.resources.displayMetrics
                ).toInt()

                val iconView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    // עיגול פינות לתמונה (כמו shape=RoundedCornerShape(12.dp))
                    background = GradientDrawable().apply {
                        cornerRadius = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics
                        )
                        setColor(AndroidColor.LTGRAY) // צבע זמני עד שהתמונה תיטען
                    }
                    clipToOutline = true
                }

                // --- חלק אמצעי: טקסטים (כותרת + תיוג) ---
                val textColumn = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f // Weight 1 - תופס את המקום הפנוי
                    ).apply {
                        marginStart = 48 // שוליים מהתמונה
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

                // שורה קטנה מתחת לכותרת (Ad Badge + Body)
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
                    setBackgroundColor(AndroidColor.parseColor("#FFCC00")) // צהוב
                    setPadding(8, 2, 8, 2)
                }

                val bodyView = TextView(context).apply {
                    textSize = 12f
                    setTextColor(AndroidColor.parseColor("#78909C")) // TextGray
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { marginStart = 16 }
                }

                // הרכבת עמודת הטקסט
                subRow.addView(adBadge)
                subRow.addView(bodyView)
                textColumn.addView(headlineView)
                textColumn.addView(subRow)

                // --- חלק ימני: כפתור פעולה (CTA) ---
                // נעצב אותו שיראה נקי, בצבע הירוק של האפליקציה
                val ctaButton = Button(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, // רוחב לפי הטקסט
                        100 // גובה מוקטן (בערך 36dp)
                    )
                    textSize = 12f
                    isAllCaps = false
                    background = GradientDrawable().apply {
                        setColor(GreenPrimary.toArgb()) // הצבע הירוק שלך
                        cornerRadius = 50f // עגול לגמרי
                    }
                    setTextColor(AndroidColor.WHITE)
                    setPadding(32, 0, 32, 0)
                }

                // --- הרכבת ה-Row הראשי ---
                rowLayout.addView(iconView)
                rowLayout.addView(textColumn)
                rowLayout.addView(ctaButton)

                // --- הוספה ל-Root ---
                adView.addView(rowLayout)

                // --- רישום ה-Views (קריטי ל-AdMob) ---
                adView.iconView = iconView
                adView.headlineView = headlineView
                adView.bodyView = bodyView
                adView.callToActionView = ctaButton

                // --- טעינת המודעה ---
                val adLoader = AdLoader.Builder(context, adUnitId)
                    .forNativeAd { nativeAd ->
                        // מילוי הנתונים
                        headlineView.text = nativeAd.headline
                        bodyView.text = nativeAd.body
                        ctaButton.text = nativeAd.callToAction

                        if (nativeAd.icon != null) {
                            iconView.setImageDrawable(nativeAd.icon?.drawable)
                            iconView.visibility = android.view.View.VISIBLE
                        } else {
                            // אם אין אייקון, נעלים את הריבוע האפור
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