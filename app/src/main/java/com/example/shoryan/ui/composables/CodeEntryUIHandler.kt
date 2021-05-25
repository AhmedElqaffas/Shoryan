package com.example.shoryan.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// Represents the code entered in the PinEntryComposable
private var enteredCode: String = ""

/**
 * This file contains the composables needed to create a code entry layout, a code entry layout
 * consists of: [PinEntryComposable], [ResendCodeText], and [ConfirmButton].
 */
@Composable
fun CodeEntryUI(
    numberOfCells: Int,
    modifier: Modifier = Modifier,
    cellsMarginPercentage: Float = 0.5f,
    cellColor: Color = Color.White,
    activeBorderColor: Color = MaterialTheme.colors.primary,
    inActiveBorderColor: Color = Color.Black,
    activeBorderWidth: Dp = 5.dp,
    inActiveBorderWidth: Dp = 3.dp,
    isPassword: Boolean = false,
    onCodeEntered: (String) -> Unit,
    layoutDirection: Int,
    screenWidth: Dp,
    screenHeight: Dp,
    buttonText: String,
    resendCodeText: String,
    onResendCodeClicked: () -> Unit,
    canResendSMS: StateFlow<Boolean>,
    remainingTime: Flow<String>
) {
    Column(
        modifier = modifier
    ) {

            PinEntryComposable(
                numberOfCells,
                Modifier,
                cellsMarginPercentage,
                cellColor,
                activeBorderColor,
                inActiveBorderColor,
                activeBorderWidth,
                inActiveBorderWidth,
                isPassword,
                ::updateCodeInstance,
                onCodeEntered,
                layoutDirection
            )
        ConfirmButton(screenWidth, screenHeight, buttonText, onCodeEntered)
        ResendCodeText(screenHeight, resendCodeText, onResendCodeClicked, canResendSMS, remainingTime)
    }
}
/*@Preview
@Composable
fun t(){
    BasicTextField(
        value = "8",
        onValueChange = {},
        modifier = Modifier.requiredWidth(50.dp).background(Color.Red),
        textStyle = MaterialTheme.typography.h1.copy(fontSize = 100.sp)
    )
}*/

@Composable
fun ConfirmButton(
    screenWidth: Dp,
    screenHeight: Dp,
    text: String,
    onClick: (String) -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, screenHeight * 0.09f, 0.dp, 0.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { onClick(enteredCode)},
            enabled = true,
            contentPadding = PaddingValues(20.dp),
            shape = RoundedCornerShape(29.dp),
            modifier = Modifier
                .width(screenWidth * 0.8f)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.body1,
                color = Color.White
            )
        }
    }
}

@Composable
fun ResendCodeText(
    screenHeight: Dp,
    resendCodeText: String,
    onClick: () -> Unit,
    canResendSMSFlow: StateFlow<Boolean>,
    remainingTimeFlow: Flow<String>
) {
    val canResendSMS = canResendSMSFlow.collectAsState(true)
    val remainingTime = remainingTimeFlow.collectAsState("" )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, screenHeight * 0.04f, 0.dp, 0.dp),
        horizontalArrangement = Arrangement.Center
    ){
        Text(
            text = if(canResendSMS.value) resendCodeText else remainingTime.value,
            color = MaterialTheme.colors.secondaryVariant,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.clickable(
                onClick = {
                    if(canResendSMS.value) onClick()
                }
            )
        )
    }
}

private fun updateCodeInstance(code: String){
    enteredCode = code
}