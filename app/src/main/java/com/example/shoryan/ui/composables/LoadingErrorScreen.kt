package com.example.shoryan.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource

@Composable
fun LoadingErrorScreen(
    iconId: Int,
    text: String,
    whatToRetry: () -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        LoadingErrorIcon(iconId, whatToRetry)
        ErrorText(text, whatToRetry)
    }
}

@Composable
private fun LoadingErrorIcon(iconId: Int, whatToRetry: () -> Unit){
    Image(
        painterResource(iconId),
        contentDescription = "Error Icon",
        modifier = Modifier.clickable(
            interactionSource = MutableInteractionSource(),
            indication = null,
        ){
            whatToRetry()
        }
    )
}

@Composable
fun ErrorText(text: String, whatToRetry: () -> Unit){
    Text(
        text = text,
        style = MaterialTheme.typography.h6,
        color = MaterialTheme.colors.secondaryVariant,
        modifier = Modifier.clickable(
            interactionSource = MutableInteractionSource(),
            indication = null,
        ){
            whatToRetry()
        }
    )
}