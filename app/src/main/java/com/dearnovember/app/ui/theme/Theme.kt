package com.dearnovember.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object Palette {
    val Cream = Color(0xFFFFF6EC); val Blush = Color(0xFFFFE3E8); val Peach = Color(0xFFFFC9A8)
    val Rose = Color(0xFFD9708A); val Sunset = Color(0xFFF2894F); val Lavender = Color(0xFFC9B8E8)
    val Brown = Color(0xFF4A2F2A); val Navy = Color(0xFF14172E); val NavySoft = Color(0xFF232847)
    val romanticDay = Brush.verticalGradient(listOf(Lavender, Blush, Peach, Cream))
    val romanticNight = Brush.verticalGradient(listOf(Navy, NavySoft, Color(0xFF3A3560)))
    val cinematicDay = Brush.verticalGradient(listOf(Color(0xFF6C5B9E), Color(0xFFE58C6A), Color(0xFFFFC98A), Cream))
    val cinematicNight = Brush.verticalGradient(listOf(Color(0xFF0B0E22), Color(0xFF2A2350), Color(0xFF8A4F5E)))
}

val LocalSky = staticCompositionLocalOf { Palette.romanticDay }

private val Light = lightColorScheme(
    primary = Palette.Rose, onPrimary = Color.White, secondary = Palette.Sunset, tertiary = Palette.Lavender,
    background = Palette.Cream, onBackground = Palette.Brown, surface = Color(0xFFFFFBF7), onSurface = Palette.Brown,
    surfaceVariant = Palette.Blush, onSurfaceVariant = Color(0xFF7A5A54),
)
private val Dark = darkColorScheme(
    primary = Color(0xFFF0A0B4), onPrimary = Palette.Navy, secondary = Color(0xFFF6A56E), tertiary = Palette.Lavender,
    background = Palette.Navy, onBackground = Color(0xFFFCEBE4), surface = Palette.NavySoft, onSurface = Color(0xFFFCEBE4),
    surfaceVariant = Color(0xFF2E3358), onSurfaceVariant = Color(0xFFD8C6D0),
)

private val Serif = FontFamily.Serif
private val Typo = Typography(
    displaySmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp),
)

/** style: ROMANTIC or CINEMATIC. mode: SYSTEM, LIGHT or DARK. */
@Composable
fun DearNovemberTheme(style: String = "ROMANTIC", mode: String = "SYSTEM", content: @Composable () -> Unit) {
    val dark = when (mode) { "DARK" -> true; "LIGHT" -> false; else -> isSystemInDarkTheme() }
    val sky = when {
        style == "CINEMATIC" && dark -> Palette.cinematicNight
        style == "CINEMATIC" -> Palette.cinematicDay
        dark -> Palette.romanticNight
        else -> Palette.romanticDay
    }
    CompositionLocalProvider(LocalSky provides sky) {
        MaterialTheme(colorScheme = if (dark) Dark else Light, typography = Typo, content = content)
    }
}
