package com.rayven0421.ashnotes.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.rayven0421.ashnotes.R

/**
 * Typography scale for AshNotes per DESIGN.md Section 3.
 * Display / titles: Bricolage Grotesque (Google Fonts with SansSerif fallback)
 * Body / UI: Instrument Sans (Google Fonts with SansSerif fallback)
 * All financial numbers use tabular figures (`tnum`).
 */

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val BricolageGrotesqueFont = GoogleFont("Bricolage Grotesque")
val InstrumentSansFont = GoogleFont("Instrument Sans")

val DisplayFontFamily = FontFamily(
    Font(googleFont = BricolageGrotesqueFont, fontProvider = fontProvider),
    Font(googleFont = BricolageGrotesqueFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = BricolageGrotesqueFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

val BodyFontFamily = FontFamily(
    Font(googleFont = InstrumentSansFont, fontProvider = fontProvider),
    Font(googleFont = InstrumentSansFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = InstrumentSansFont, fontProvider = fontProvider, weight = FontWeight.Bold)
)

const val TabularFigures = "tnum"

val AshTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontFeatureSettings = TabularFigures
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontFeatureSettings = TabularFigures
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontFeatureSettings = TabularFigures
    )
)
