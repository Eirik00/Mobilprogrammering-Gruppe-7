package com.example.landingpage.ui.theme

import android.hardware.lights.Light
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.example.wanderly.ui.theme.Typography
import androidx.compose.runtime.Composable

//Light Theme
import com.example.wanderly.ui.theme.LT_MainColor
import com.example.wanderly.ui.theme.LT_MainColorLighter
import com.example.wanderly.ui.theme.LT_ButtonColor
import com.example.wanderly.ui.theme.LT_BackgroundColor
import com.example.wanderly.ui.theme.LT_TextColor

import com.example.wanderly.ui.theme.Pink40
import com.example.wanderly.ui.theme.Purple40
import com.example.wanderly.ui.theme.PurpleGrey40


private val LightColorPalette = lightColorScheme(
    primary = LT_MainColor,
    primaryContainer = LT_ButtonColor,
    secondary = LT_MainColorLighter,
    background = LT_BackgroundColor,
    onPrimary = LT_TextColor,
    onSecondary = LT_TextColor,
    onBackground = LT_TextColor,
    onSurface = LT_TextColor,
    onPrimaryContainer = LT_TextColor
)

private val DarkColorPalette = LightColorPalette

@Composable
fun LandingPageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}