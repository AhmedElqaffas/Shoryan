package com.example.shoryan.ui.theme

import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color

val Red = Color(0xffD12A2A)
val RedDark = Color(0xff9C1111)
val RedLight = Color(0xffED5A5A)
val Shimmer = Color(0xffDDDDDD)
val Gray = Color(0xFFADACAC)
val DarkGreen = Color(0xFF008D00)



val Colors.shimmer: Color
    get() = if (isLight) Shimmer else Shimmer
