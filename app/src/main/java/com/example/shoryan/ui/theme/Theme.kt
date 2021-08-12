package com.example.shoryan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration


private val LightColors = lightColors(
    primary = RedDark,
    primaryVariant = Red,
    onPrimary = Color.White,
    secondary = RedLight,
    secondaryVariant = Color.Black,
    onSecondary = Color.White,
    error = Red
)

private val DarkColors = darkColors(
    primary = RedDark,
    primaryVariant = Red,
    onPrimary = Color.White,
    secondary = Red,
    onSecondary = Color.White,
    error = Red
)

@Composable
fun ProvideDimens(
    dimensions: Dimensions,
    content: @Composable () -> Unit
) {
    val dimensionSet = remember { dimensions }
    CompositionLocalProvider(LocalAppDimens provides dimensionSet, content = content)
}

private val LocalAppDimens = staticCompositionLocalOf {
    smallDimensions
}

@Composable
fun ShoryanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit){
    val configuration = LocalConfiguration.current
    val dimensions = if (configuration.screenHeightDp <= 700) smallDimensions else sh700Dimensions
    ProvideDimens(dimensions = dimensions) {
        MaterialTheme(
            content = content,
            colors = if (darkTheme) DarkColors else LightColors,
            typography = ShoryanTypography,
            shapes = ShoryanShapes
        )
    }
}

object ShoryanTheme {
    val dimens: Dimensions
        @Composable
        get() = LocalAppDimens.current
}

val Dimens: Dimensions
    @Composable
    get() = ShoryanTheme.dimens