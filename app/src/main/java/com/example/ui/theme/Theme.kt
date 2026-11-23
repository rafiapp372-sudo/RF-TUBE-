package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.localization.AppLanguage
import com.example.localization.LocalStrings
import com.example.localization.StringsBn
import com.example.localization.StringsEn

private val RfTubeDarkColorScheme = darkColorScheme(
    primary = CrimsonCoral,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3E1220),
    onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = MintPulse,
    onSecondary = Color(0xFF00201A),
    secondaryContainer = Color(0xFF0A382E),
    onSecondaryContainer = Color(0xFF9DF7E0),
    tertiary = SunsetEmber,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = LiveRed,
    onError = Color.White
)

private val RfTubeLightColorScheme = lightColorScheme(
    primary = CrimsonCoralDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE5EA),
    onPrimaryContainer = Color(0xFF5A0016),
    secondary = Color(0xFF008F72),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5F7EE),
    onSecondaryContainer = Color(0xFF00382B),
    tertiary = SunsetEmber,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = LiveRed,
    onError = Color.White
)

@Composable
fun RfTubeTheme(
    darkTheme: Boolean = true,
    language: AppLanguage = AppLanguage.EN,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) RfTubeDarkColorScheme else RfTubeLightColorScheme
    val strings = if (language == AppLanguage.BN) StringsBn else StringsEn
    val typography = buildRfTubeTypography(isBengali = language == AppLanguage.BN)

    CompositionLocalProvider(LocalStrings provides strings) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
