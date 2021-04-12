package com.example.shoryan.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


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
fun ShoryanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit){
    MaterialTheme(
        content = content,
        colors = if(darkTheme) DarkColors else LightColors,
        typography = ShoryanTypography,
        shapes = ShoryanShapes)
}