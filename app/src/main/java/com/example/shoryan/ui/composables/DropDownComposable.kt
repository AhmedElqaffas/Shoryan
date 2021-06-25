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
import com.example.shoryan.data.Branch

@Composable
fun DropDownComposable(
    isOpen: Boolean,
    storeBranches: List<Branch>,
    setMenuVisibility: (Boolean) -> Unit,
    onBranchSelected: (String, String) -> Unit,
    popupWidth: Dp
){
    DropdownMenu(
            modifier = Modifier
                .height(150.dp),
            expanded = isOpen,
            onDismissRequest = { setMenuVisibility(false) }
        ) {
            storeBranches.forEach {
                DropdownMenuItem(
                    onClick = {
                        setMenuVisibility(false)
                        onBranchSelected(it.id, it.getStringAddress())
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()

                    ){
                        Text(
                            text = it.getStringAddress(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(popupWidth),
                            style = MaterialTheme.typography.body1
                        )
                    }

                }
            }
    }
}