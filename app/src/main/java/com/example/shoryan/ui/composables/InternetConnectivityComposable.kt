package com.example.shoryan.ui.composables

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.shoryan.R

@Composable
fun InternetConnectionBanner(
    context: Context,
    backgroundColor: Color,
    connectionStatus: Boolean){
    if(!connectionStatus)
        Row(
            modifier = Modifier
                .background(backgroundColor)
                .fillMaxWidth()
                .padding(0.dp, 0.dp, 0.dp, 10.dp),
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = context.resources.getString(R.string.no_internet_connection),
                modifier = Modifier
                    .background(MaterialTheme.colors.secondaryVariant, MaterialTheme.shapes.small)
                    .fillMaxWidth(0.95f)
                    .padding(0.dp, 10.dp),
                color = MaterialTheme.colors.onSecondary,
                style = MaterialTheme.typography.body2,
                textAlign = TextAlign.Center
            )
        }
}