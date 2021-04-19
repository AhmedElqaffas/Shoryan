package com.example.shoryan.ui.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Top
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

/**
 * Contains the cells where the code is inserted
 * @param numberOfCells The number of character in the code
 * @param modifier: The modifier to apply to this composable
 * @param cellsMarginPercentage Space between cells divided by cell size, i.e., ratio between
 * cell size and white space between cells
 * @param cellColor The background color of the cell
 * @param activeBorderColor The color of the border around the cell, when the cell is in focus
 * @param inActiveBorderColor The color of the border around the cell, when the cell isn't in focus
 * @param activeBorderWidth The width of the border around the cell, when the cell is in focus
 * @param inActiveBorderWidth The width of the border around the cell, when the cell isn't in focus
 * @param isPassword A boolean that determines whether the characters should be treated as password
 * characters or be left as plain text
 * @param onChange A function invoked when a new code character is entered
 * @param onCodeEntered A function invoked when a new code character is entered
 */
@Composable
fun CodeEntryComposable(
    numberOfCells: Int,
    modifier: Modifier = Modifier,
    cellsMarginPercentage: Float = 0.5f,
    cellColor: Color = Color.White,
    activeBorderColor: Color = MaterialTheme.colors.primary,
    inActiveBorderColor: Color = Color.Black,
    activeBorderWidth: Dp = 3.dp,
    inActiveBorderWidth: Dp = 1.dp,
    isPassword: Boolean = false,
    onChange: (String) -> Unit,
    onCodeEntered: (String) -> Unit
){
    // Create a focusRequester for each cell to use it to give focus to a cell when needed
    val focusRequesters = List(numberOfCells) { FocusRequester() }
    /* Keep the text state of each cell in this component and pass it later to the cells so that
       each cell knows the states of the other cells
    */
    val cellsTexts = List(numberOfCells){mutableStateOf("")}
    Column{
        Row(
            modifier = modifier
        ) {
            for(i in 0 until numberOfCells){
                /* Each cell should should know the focusRequester of the cell directly after and
                   before it. Last cell doesn't have any cell after it, therefore its
                   "nextFocus" value = null. First cell doesn't have any cell before it, therefore its
                   "previousFocus" value = null
                */
                val nextFocus = if(i < numberOfCells - 1) focusRequesters[i + 1] else null
                val previousFocus = if(i > 0) focusRequesters[i - 1] else null
                Cell(this, i, cellsTexts, focusRequesters[i], nextFocus, previousFocus,
                    cellColor = cellColor,
                    activeBorderColor = activeBorderColor,
                    inActiveBorderColor = inActiveBorderColor,
                    activeBorderWidth = activeBorderWidth,
                    inActiveBorderWidth = inActiveBorderWidth,
                    isPassword = isPassword,
                    onChange = onChange,
                    onCodeEntered = onCodeEntered)
                // Add space between cells
                if(i != numberOfCells - 1)
                    Spacer(modifier = Modifier.weight(cellsMarginPercentage))
            }
        }
    }

}

/**
 * Each cell represents a character field in the CodeEntryComposable.
 * @param parentLayoutScope The parent row scope.
 * @param id The order of the cell.
 * @param cellsText A list containing the text state of each cell.
 * @param focusRequester Reference to the FocusRequester used to request focus to this cell.
 * @param nextFocusRequester nullable reference to the FocusRequester used to request focus to
 * the cell following this cell. It is null if this is the last cell
 * @param previousFocusRequester nullable reference to the FocusRequester used to request focus to
 * the cell preceding this cell. It is null if this is the first cell
 * @param cellColor The background color of the cell
 * @param activeBorderColor The color of the border around the cell, when the cell is in focus
 * @param inActiveBorderColor The color of the border around the cell, when the cell isn't in focus
 * @param activeBorderWidth The width of the border around the cell, when the cell is in focus
 * @param inActiveBorderWidth The width of the border around the cell, when the cell isn't in focus
 * @param isPassword A boolean that determines whether the characters should be treated as password
 * characters or be left as plain text
 * @param onChange A function invoked when a new code character is entered
 * @param onCodeEntered A function invoked when all cells are filled with characters
 */

@Composable
fun Cell(
    parentLayoutScope: RowScope,
    id: Int,
    cellsText: List<MutableState<String>>,
    focusRequester: FocusRequester,
    nextFocusRequester: FocusRequester?,
    previousFocusRequester: FocusRequester?,
    cellColor: Color,
    activeBorderColor: Color,
    inActiveBorderColor: Color,
    activeBorderWidth: Dp,
    inActiveBorderWidth: Dp,
    isPassword: Boolean,
    onChange: (String) -> Unit,
    onCodeEntered: (String) -> Unit
) {
    /* "textBeforeChange" is used to determine the change in text. For example if textBeforeChange
       = "5" and the cellsText[id] = "85", we can conclude that the new text entered = 8.
       That is because , although the cursor is invisible, the cursor position may be at the start
       of the TextField instead of the end, so entering a new character (8 in this example) will
       be appended to the start of cellsText[id] string instead of the end.
       So we need "textBeforeChange" state to determine the new entered character whether the cursor
       was at the start or the end of the TextField.
    */
    val textBeforeChange = remember{mutableStateOf("")}
    val borderColor = remember { mutableStateOf(inActiveBorderColor) }
    val borderWidth = remember { mutableStateOf(1.dp) }
    parentLayoutScope.apply{
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val fontSize = min(maxHeight*0.4f*0.78f, maxWidth * 0.4f * 0.78f)
            TextField(
                value = cellsText[id].value,
                onValueChange =
                {
                    if(it.isEmpty()) {
                        cellsText[id].value = it
                        previousFocusRequester?.requestFocus()
                    }
                    else{
                        if(it[0].toString() == textBeforeChange.value && textBeforeChange.value.isNotEmpty()) {
                            cellsText[id].value = it.last().toString()
                        }
                        else {
                            cellsText[id].value = it[0].toString()
                        }
                        if(nextFocusRequester != null)
                            nextFocusRequester.requestFocus()
                        else if(cellsText.all{state -> state.value.isNotEmpty()}) {
                            onCodeEntered(extractCodeString(cellsText))
                        }
                    }
                    onChange(extractCodeString(cellsText))
                    textBeforeChange.value = cellsText[id].value
                },
                modifier = Modifier
                    .onFocusChanged {
                        borderColor.value = if (it.isFocused) activeBorderColor else inActiveBorderColor
                        borderWidth.value = if (it.isFocused) activeBorderWidth else inActiveBorderWidth
                    }
                    .focusOrder(focusRequester) {
                        /* When the user clicks "Next" on the keyboard:
                           - If this isn't the last cell, the focus is passed to the next cell
                           - If this is the last cell, and all cells contain values, invoke "onCodeEntered"
                        */
                        if (nextFocusRequester != null)
                            nextFocusRequester.requestFocus()
                        else if (cellsText.all { state -> state.value.isNotEmpty() }) {
                            focusRequester.freeFocus()
                            onCodeEntered(extractCodeString(cellsText))
                        }
                        else{
                            onChange(extractCodeString(cellsText))
                        }
                    }
                    .border(borderWidth.value, borderColor.value, LineBorder),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = if(isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                textStyle = TextStyle(fontSize= LocalDensity.current.run { fontSize.toSp() }, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = cellColor,
                    cursorColor = Color.Transparent, // To hide the cursor
                    focusedIndicatorColor = activeBorderColor) // To hide the underline below the textField by blending it with border color
            )
        }

    }
}

fun extractCodeString(cellsText: List<MutableState<String>>): String {
    var codeString = ""
    cellsText.forEach{
        codeString += it.value
    }
    return codeString
}

private val LineBorder = GenericShape { size, _ ->
    moveTo(0f, size.height)
    lineTo(size.width, size.height)
}