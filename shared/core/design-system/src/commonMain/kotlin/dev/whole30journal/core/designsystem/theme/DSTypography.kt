package dev.whole30journal.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class DSTypography(
    val text2xs: TextStyle,
    val textXs: TextStyle,
    val textSm: TextStyle,
    val textBase: TextStyle,
    val textMd: TextStyle,
    val textLg: TextStyle,
    val textXl: TextStyle,
    val text2xl: TextStyle,
    val text3xl: TextStyle,
    val text4xl: TextStyle,
)

fun dsTypography(fontFamily: FontFamily = dsFontFamily()): DSTypography = DSTypography(
    text2xs = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, fontWeight = FontWeight.Normal),
    textXs = TextStyle(fontFamily = fontFamily, fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
    textSm = TextStyle(fontFamily = fontFamily, fontSize = 12.sp, fontWeight = FontWeight.Normal),
    textBase = TextStyle(fontFamily = fontFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal),
    textMd = TextStyle(fontFamily = fontFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    textLg = TextStyle(fontFamily = fontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold),
    textXl = TextStyle(fontFamily = fontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold),
    text2xl = TextStyle(fontFamily = fontFamily, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold),
    text3xl = TextStyle(fontFamily = fontFamily, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold),
    text4xl = TextStyle(fontFamily = fontFamily, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold),
)

val LocalDSTypography = staticCompositionLocalOf { dsTypography() }
