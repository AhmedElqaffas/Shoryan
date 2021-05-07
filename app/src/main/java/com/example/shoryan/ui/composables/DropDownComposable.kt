package com.example.shoryan.ui.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DropDownComposable(
    isOpen: Boolean,
    itemsList: List<String>,
    setMenuVisibility: (Boolean) -> Unit,
    setSelectedString: (String) -> Unit
){
    DropdownMenu(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        expanded = isOpen,
        onDismissRequest = { setMenuVisibility(false) }
    ){

        itemsList.forEach {
            DropdownMenuItem(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    setMenuVisibility(false)
                    setSelectedString(it)
                }
            ) {
                Text(
                    text = it,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.wrapContentWidth(),
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}