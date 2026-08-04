package dev.whole30journal.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class Whole30Colors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val divider: Color,
    val text: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentOn: Color,
    val accentTint: Color,
    val gridLine: Color,
    val avgLine: Color,
    val track: Color,
    val scoreLow: Color,
    val scoreMid: Color,
    val scoreHigh: Color,
    val iconEnergy: Color,
    val iconMood: Color,
    val iconSleep: Color,
    val iconCravings: Color,
)

fun whole30DarkColors(): Whole30Colors = Whole30Colors(
    bg = Color(0xFF101217),
    surface = Color(0xFF1C1F26),
    surface2 = Color(0xFF262A32),
    divider = Color(0xFF262A32),
    text = Color(0xFFF2F3F5),
    textSecondary = Color(0xFF9AA0AA),
    textTertiary = Color(0xFF6B7078),
    accent = Color(0xFF2DD4BF),
    accentOn = Color(0xFF062621),
    accentTint = Color(0x1A2DD4BF),
    gridLine = Color(0xFF262A32),
    avgLine = Color(0xFF3A3F49),
    track = Color(0xFF262A32),
    scoreLow = Color(0xFFE2705A),
    scoreMid = Color(0xFFF0B429),
    scoreHigh = Color(0xFF2DD4BF),
    iconEnergy = Color(0xFFE2705A),
    iconMood = Color(0xFFC084FC),
    iconSleep = Color(0xFF5B8DEF),
    iconCravings = Color(0xFF2DD4BF),
)

fun whole30LightColors(): Whole30Colors = Whole30Colors(
    bg = Color(0xFFE4E6E5),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFEEF0EF),
    divider = Color(0xFFD8DBDA),
    text = Color(0xFF14171A),
    textSecondary = Color(0xFF5B6169),
    textTertiary = Color(0xFF8B9198),
    accent = Color(0xFF0E9488),
    accentOn = Color(0xFFFFFFFF),
    accentTint = Color(0x1A0E9488),
    gridLine = Color(0xFFE9EBEA),
    avgLine = Color(0xFFC7CCCB),
    track = Color(0xFFD3D7D6),
    scoreLow = Color(0xFFC2503A),
    scoreMid = Color(0xFFB8860B),
    scoreHigh = Color(0xFF0E9488),
    iconEnergy = Color(0xFFC2503A),
    iconMood = Color(0xFF9B5DE0),
    iconSleep = Color(0xFF3D6FCE),
    iconCravings = Color(0xFF0E9488),
)

val LocalWhole30Colors = staticCompositionLocalOf { whole30DarkColors() }
