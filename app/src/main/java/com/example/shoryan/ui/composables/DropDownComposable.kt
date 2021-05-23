package com.example.shoryan.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DropDownComposable(
    isOpen: Boolean,
    itemsList: List<String>,
    setMenuVisibility: (Boolean) -> Unit,
    setSelectedString: (String) -> Unit,
    popupWidth: Dp
){
    DropdownMenu(
            modifier = Modifier
                .height(150.dp),
            expanded = isOpen,
            onDismissRequest = { setMenuVisibility(false) }
        ) {
            itemsList.forEach {
                DropdownMenuItem(
                    onClick = {
                        setMenuVisibility(false)
                        setSelectedString(it)
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()

                    ){
                        Text(
                            text = it,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(popupWidth),
                            style = MaterialTheme.typography.body1
                        )
                    }

                }
            }
    }
}