package com.example.shoryan.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AppBar(title: String){
    Surface(
        Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.primary,
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    )
    {

        AppBarTitle(title)
    }
}

@Composable
fun AppBarTitle(title: String){
    Box(contentAlignment = Alignment.Center){
        Text(
            text = title,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 20.dp),
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.onPrimary
        )
    }
}